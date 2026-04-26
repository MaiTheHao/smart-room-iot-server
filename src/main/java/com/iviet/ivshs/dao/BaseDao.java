package com.iviet.ivshs.dao;

import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Lớp cơ sở DAO (Data Access Object) trừu tượng cung cấp các phương thức CRUD cơ bản
 * cho các entity JPA. Sử dụng JPA Criteria API để xây dựng các truy vấn động.
 *
 * @param <T> Kiểu dữ liệu của entity
 */
public abstract class BaseDao<T> {

    /** EntityManager được inject bởi Spring, dùng để quản lý lifecycle của entity */
    @PersistenceContext
    protected EntityManager entityManager;

    /** JdbcTemplate được inject bởi Spring, dùng cho các truy vấn SQL raw nếu cần */
    protected JdbcTemplate jdbcTemplate;

    /** Lớp của entity T, được truyền vào constructor */
    protected final Class<T> clazz;

    /** Kích thước batch cho việc lưu danh sách entity (flush/clear mỗi BATCH_SIZE bản ghi) */
    @Value("${hibernate.jdbc.batch_size:50}")
    protected int BATCH_SIZE;

    /**
     * Constructor khởi tạo BaseDao với lớp entity.
     *
     * @param clazz Lớp của entity (không được null)
     * @throws NullPointerException nếu clazz là null
     */
    protected BaseDao(Class<T> clazz) {
        this.clazz = Objects.requireNonNull(clazz, "Entity class must not be null");
    }

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Lưu một entity duy nhất vào cơ sở dữ liệu.
     * Entity sẽ được persist, sau đó có thể được flush để lưu vào DB.
     *
     * @param entity Entity cần lưu
     * @return Entity đã được lưu
     */
    public T save(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    /**
     * Lưu danh sách các entity vào cơ sở dữ liệu với batch processing.
     * Mỗi 50 bản ghi sẽ flush và clear EntityManager để tối ưu bộ nhớ.
     * Nếu danh sách trống, trả về danh sách rỗng.
     *
     * @param entities Danh sách các entity cần lưu
     * @return Danh sách các entity đã được lưu
     */
    public List<T> save(List<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        int count = 0;
        for (T entity : entities) {
            entityManager.persist(entity);
            count++;
            // Flush và clear mỗi BATCH_SIZE bản ghi để quản lý bộ nhớ hiệu quả
            if (count % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return entities;
    }

    /**
     * Cập nhật một entity có sẵn trong cơ sở dữ liệu.
     * Sử dụng merge() để gộp các thay đổi vào persistent context.
     *
     * @param entity Entity cần cập nhật
     * @return Entity đã được cập nhật
     */
    public T update(T entity) {
        return entityManager.merge(entity);
    }

    /**
     * Xóa một entity khỏi cơ sở dữ liệu.
     * Nếu entity không được quản lý bởi EntityManager, sẽ merge trước khi xóa.
     *
     * @param entity Entity cần xóa
     */
    public void delete(T entity) {
        // Kiểm tra xem entity có được quản lý hay không, nếu không thì merge trước
        entityManager.remove(
            entityManager.contains(entity) ? entity : entityManager.merge(entity)
        );
    }

    /**
     * Lấy tất cả các entity của loại T từ cơ sở dữ liệu mà không có điều kiện lọc.
     * Sử dụng JPA Criteria API để xây dựng truy vấn động.
     *
     * @return Danh sách tất cả các entity, trả về danh sách rỗng nếu không có dữ liệu
     */
    public List<T> findAll() {
        var cb = this.getCB();
        var cq = cb.createQuery(clazz);
        var root = cq.from(clazz);
        cq.select(root);
        TypedQuery<T> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    /**
     * Lấy danh sách các entity với điều kiện lọc, custom query, và phân trang.
     * Cho phép xác định specification (WHERE clause) và customizer (ORDER BY, GROUP BY, etc).
     *
     * @param specification Hàm dùng để tạo WHERE clause (có thể null để bỏ qua)
     * @param queryCustomizer Hàm dùng để customize query như thêm ORDER BY (có thể null)
     * @param page Trang (0-indexed), được nhân với size để tính offset
     * @param size Số lượng bản ghi trên mỗi trang
     * @return Danh sách các entity theo trang được chỉ định
     */
    public List<T> findAll(
        Function<Root<T>, Predicate> specification,
        BiConsumer<Root<T>, CriteriaQuery<T>> queryCustomizer,
        int page,
        int size
    ) {
        var cb = this.getCB();
        var cq = cb.createQuery(clazz);
        var root = cq.from(clazz);

        // Áp dụng WHERE clause nếu specification không null
        if (specification != null) {
            cq.where(specification.apply(root));
        }

        // Áp dụng custom query (ORDER BY, GROUP BY, etc) nếu queryCustomizer không null
        if (queryCustomizer != null) {
            queryCustomizer.accept(root, cq);
        }

        TypedQuery<T> query = entityManager.createQuery(cq);

        // Thực hiện phân trang
        return query
            .setFirstResult(page * size)
            .setMaxResults(size)
            .getResultList();
    }

    /**
     * Lấy danh sách các entity với điều kiện lọc và custom query (không có phân trang).
     * Dùng khi cần lấy tất cả kết quả thỏa mãn điều kiện mà không cần chia trang.
     *
     * @param specification Hàm dùng để tạo WHERE clause (có thể null)
     * @param queryCustomizer Hàm dùng để customize query (có thể null)
     * @return Danh sách tất cả các entity thỏa mãn điều kiện
     */
    public List<T> findAll(
        Function<Root<T>, Predicate> specification,
        BiConsumer<Root<T>, CriteriaQuery<T>> queryCustomizer
    ) {
        var cb = this.getCB();
        var cq = cb.createQuery(clazz);
        var root = cq.from(clazz);

        // Áp dụng WHERE clause nếu specification không null
        if (specification != null) {
            cq.where(specification.apply(root));
        }

        // Áp dụng custom query nếu queryCustomizer không null
        if (queryCustomizer != null) {
            queryCustomizer.accept(root, cq);
        }

        TypedQuery<T> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    /**
     * Tìm một entity duy nhất thỏa mãn điều kiện.
     * Trả về Optional để xử lý trường hợp không tìm thấy.
     * Chỉ lấy tối đa 1 bản ghi để tối ưu hiệu suất.
     *
     * @param specification Hàm dùng để tạo WHERE clause (có thể null)
     * @return Optional chứa entity nếu tìm thấy, Optional.empty() nếu không
     */
    public Optional<T> findOne(Function<Root<T>, Predicate> specification) {
        CriteriaBuilder cb = this.getCB();
        CriteriaQuery<T> cq = cb.createQuery(clazz);
        Root<T> root = cq.from(clazz);

        // Áp dụng WHERE clause nếu specification không null
        if (specification != null) {
            cq.where(specification.apply(root));
        }

        // Chỉ lấy 1 kết quả
        List<T> results = entityManager.createQuery(cq)
            .setMaxResults(1)
            .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Tìm một entity duy nhất với điều kiện lọc và custom query.
     * Cho phép thêm các tùy chỉnh như ORDER BY để quyết định entity nào được trả về.
     *
     * @param specification Hàm dùng để tạo WHERE clause (có thể null)
     * @param queryCustomizer Hàm dùng để customize query như ORDER BY (có thể null)
     * @return Optional chứa entity nếu tìm thấy, Optional.empty() nếu không
     */
    public Optional<T> findOne(
        Function<Root<T>, Predicate> specification,
        BiConsumer<Root<T>, CriteriaQuery<T>> queryCustomizer
    ) {
        CriteriaBuilder cb = this.getCB();
        CriteriaQuery<T> cq = cb.createQuery(clazz);
        Root<T> root = cq.from(clazz);

        // Áp dụng WHERE clause nếu specification không null
        if (specification != null) {
            cq.where(specification.apply(root));
        }

        // Áp dụng custom query (ORDER BY, etc) nếu queryCustomizer không null
        if (queryCustomizer != null) {
            queryCustomizer.accept(root, cq);
        }

        // Chỉ lấy 1 kết quả
        List<T> results = entityManager.createQuery(cq)
            .setMaxResults(1)
            .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Kiểm tra xem có tồn tại entity nào thỏa mãn điều kiện hay không.
     * Sử dụng findOne() để lấy kết quả và kiểm tra isPresent().
     *
     * @param specification Hàm dùng để tạo WHERE clause (có thể null)
     * @return true nếu tồn tại entity thỏa mãn điều kiện, false nếu không
     */
    public boolean exists(Function<Root<T>, Predicate> specification) {
        return findOne(specification).isPresent();
    }

    /**
     * Đếm số lượng entity thỏa mãn điều kiện lọc.
     * Sử dụng COUNT(*) để lấy số lượng thay vì lấy toàn bộ dữ liệu.
     *
     * @param specification Hàm dùng để tạo WHERE clause (có thể null để đếm tất cả)
     * @return Số lượng entity thỏa mãn điều kiện
     */
    public long count(Function<Root<T>, Predicate> specification) {
        var cb = this.getCB();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(clazz);
        cq.select(cb.count(root));

        // Áp dụng WHERE clause nếu specification không null
        if (specification != null) {
            cq.where(specification.apply(root));
        }

        return entityManager.createQuery(cq).getSingleResult();
    }

    /**
     * Đếm tất cả các entity của loại T mà không có điều kiện lọc.
     * Đơn giản là gọi count(null).
     *
     * @return Tổng số lượng entity
     */
    public long count() {
        return count(null);
    }

    /**
     * Flush các thay đổi chưa được lưu từ EntityManager vào cơ sở dữ liệu.
     * Thường dùng khi cần đồng bộ ngay mà không muốn commit transaction.
     */
    public void flush() {
        entityManager.flush();
    }

    /**
     * Clear EntityManager, xóa tất cả entity được quản lý khỏi persistent context.
     * Dùng để giải phóng bộ nhớ, đặc biệt là sau khi xử lý danh sách lớn.
     */
    public void clear() {
        entityManager.clear();
    }

    /**
     * Lấy tên bảng của entity từ annotation @Table.
     * Nếu @Table có tên tùy chỉnh, dùng tên đó; nếu không, dùng tên lớp.
     *
     * @return Tên bảng của entity trong cơ sở dữ liệu
     */
    protected String getTableName() {
        Table table = clazz.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        } else {
            return clazz.getSimpleName();
        }
    }

    /**
     * Lấy danh sách tên các cột của entity từ các annotation @Column, @JoinColumn, @Id.
     * Nếu annotation có tên tùy chỉnh, dùng tên đó; nếu không, dùng tên field.
     * Chỉ lấy các field có annotation JPA, bỏ qua các field khác.
     *
     * @return Danh sách tên các cột của entity
     */
    protected List<String> getColumnNames() {
        return Arrays.stream(clazz.getDeclaredFields())
            // Lọc chỉ những field có annotation @Column, @JoinColumn, hoặc @Id
            .filter(
                f -> (f.isAnnotationPresent(Column.class)
                    || f.isAnnotationPresent(JoinColumn.class)
                    || f.isAnnotationPresent(Id.class))
            )
            // Ánh xạ mỗi field tới tên cột của nó
            .map(f -> {
                if (f.isAnnotationPresent(Column.class)) {
                    Column column = f.getAnnotation(Column.class);
                    return column.name().isEmpty() ? f.getName() : column.name();
                } else if (f.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinColumn = f.getAnnotation(JoinColumn.class);
                    return joinColumn.name().isEmpty() ? f.getName() : joinColumn.name();
                } else if (f.isAnnotationPresent(Id.class)) {
                    return f.getName();
                } else {
                    return f.getName();
                }
            })
            .collect(Collectors.toList());
    }

    /**
     * Lấy EntityManager được quản lý bởi Spring.
     * Dùng cho các tác vụ nâng cao cần truy cập trực tiếp EntityManager.
     *
     * @return EntityManager hiện tại
     */
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Lấy CriteriaBuilder từ EntityManager.
     * CriteriaBuilder dùng để xây dựng các truy vấn dynamic sử dụng JPA Criteria API.
     *
     * @return CriteriaBuilder để tạo query
     */
    protected jakarta.persistence.criteria.CriteriaBuilder getCB() {
        return entityManager.getCriteriaBuilder();
    }
}