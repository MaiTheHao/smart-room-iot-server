package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.SysFunctionDtoV1;
import com.iviet.ivshs.dto.SysFunctionWithGroupStatusDtoV1;
import com.iviet.ivshs.entities.SysFunction;

@Repository
public class SysFunctionDao extends BaseTranslatableEntityDao<SysFunction> {
    
    public SysFunctionDao() {
        super(SysFunction.class);
    }

    // ======= Find by Function Code =======
    
    /**
     * Tìm SysFunction entity theo functionCode
     */
    public Optional<SysFunction> findByCode(String functionCode) {
        return findOne(root -> entityManager.getCriteriaBuilder()
            .equal(root.get("functionCode"), functionCode));
    }

    /**
     * Tìm SysFunction DTO theo functionCode với translation
     */
    public Optional<SysFunctionDtoV1> findByCode(String functionCode, String langCode) {
        String dtoClassPath = SysFunctionDtoV1.class.getName();

        String jpql = """
                SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
                FROM SysFunction f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                WHERE f.functionCode = :functionCode
                """.formatted(dtoClassPath);

        List<SysFunctionDtoV1> results = entityManager.createQuery(jpql, SysFunctionDtoV1.class)
                .setParameter("functionCode", functionCode)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Kiểm tra function code đã tồn tại chưa
     */
    public boolean existsByCode(String functionCode) {
        return exists(root -> entityManager.getCriteriaBuilder()
            .equal(root.get("functionCode"), functionCode));
    }

    // ======= Find by ID =======
    
    /**
     * Tìm SysFunction DTO theo ID với translation
     */
    public Optional<SysFunctionDtoV1> findById(Long functionId, String langCode) {
        String dtoClassPath = SysFunctionDtoV1.class.getName();

        String jpql = """
                SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
                FROM SysFunction f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                WHERE f.id = :functionId
                """.formatted(dtoClassPath);
        
        List<SysFunctionDtoV1> results = entityManager.createQuery(jpql, SysFunctionDtoV1.class)
                .setParameter("functionId", functionId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // ======= Find All =======
    
    /**
     * Lấy danh sách tất cả Functions với phân trang và translation
     */
    public List<SysFunctionDtoV1> findAll(int page, int size, String langCode) {
        String dtoClassPath = SysFunctionDtoV1.class.getName();

        String jpql = """
                SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
                FROM SysFunction f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                ORDER BY f.functionCode ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, SysFunctionDtoV1.class)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * Lấy tất cả Functions (không phân trang) với translation
     */
    public List<SysFunctionDtoV1> findAll(String langCode) {
        String dtoClassPath = SysFunctionDtoV1.class.getName();

        String jpql = """
                SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
                FROM SysFunction f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                ORDER BY f.functionCode ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, SysFunctionDtoV1.class)
                .setParameter("langCode", langCode)
                .getResultList();
    }

    // ======= Find All Functions with Group Status =======
    
    /**
     * Lấy tất cả Functions với trạng thái đã được assign vào Group hay chưa
     * Dùng cho UI khi chọn Functions để add/remove khỏi Group
     * 
     * @param groupId ID của Group cần kiểm tra
     * @param langCode Mã ngôn ngữ
     * @return Danh sách Functions với trạng thái isAssignedToGroup
     */
    public List<SysFunctionWithGroupStatusDtoV1> findAllWithGroupStatus(Long groupId, String langCode) {
        String dtoClassPath = SysFunctionWithGroupStatusDtoV1.class.getName();

        String jpql = """
                SELECT new %s(
                    f.id, 
                    f.functionCode, 
                    flan.name, 
                    flan.description,
                    CASE WHEN r.id IS NOT NULL THEN true ELSE false END,
                    r.id
                )
                FROM SysFunction f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                LEFT JOIN f.roles r ON r.group.id = :groupId AND r.isActive = true
                ORDER BY f.functionCode ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, SysFunctionWithGroupStatusDtoV1.class)
                .setParameter("groupId", groupId)
                .setParameter("langCode", langCode)
                .getResultList();
    }

    /**
     * Lấy tất cả Functions với trạng thái và phân trang
     */
    public List<SysFunctionWithGroupStatusDtoV1> findAllWithGroupStatus(
            Long groupId, String langCode, int page, int size) {
        String dtoClassPath = SysFunctionWithGroupStatusDtoV1.class.getName();

        String jpql = """
                SELECT new %s(
                    f.id, 
                    f.functionCode, 
                    flan.name, 
                    flan.description,
                    CASE WHEN r.id IS NOT NULL THEN true ELSE false END,
                    r.id
                )
                FROM SysFunction f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                LEFT JOIN f.roles r ON r.group.id = :groupId AND r.isActive = true
                ORDER BY f.functionCode ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, SysFunctionWithGroupStatusDtoV1.class)
                .setParameter("groupId", groupId)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    // ======= Count =======
    
    /**
     * Đếm tổng số Functions
     */
    public long countAll() {
        String jpql = "SELECT COUNT(f) FROM SysFunction f";
        return entityManager.createQuery(jpql, Long.class)
                .getSingleResult();
    }
}
