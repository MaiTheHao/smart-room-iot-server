package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.ClientDtoV1;
import com.iviet.ivshs.dto.SysFunctionDtoV1;
import com.iviet.ivshs.dto.SysGroupDtoV1;
import com.iviet.ivshs.entities.ClientV1;
import com.iviet.ivshs.entities.SysGroupV1;

@Repository
public class SysGroupDaoV1 extends BaseTranslatableEntityDaoV1<SysGroupV1> {
    
    public SysGroupDaoV1() {
        super(SysGroupV1.class);
    }

    // ======= Find by Group Code =======
    
    /**
     * Tìm SysGroup entity theo groupCode
     */
    public Optional<SysGroupV1> findByCode(String groupCode) {
        return findOne(root -> entityManager.getCriteriaBuilder()
            .equal(root.get("groupCode"), groupCode));
    }

    /**
     * Tìm SysGroup DTO theo groupCode với translation
     */
    public Optional<SysGroupDtoV1> findByCode(String groupCode, String langCode) {
        String dtoClassPath = SysGroupDtoV1.class.getName();

        String jpql = """
                SELECT new %s(g.id, g.groupCode, glan.name, glan.description)
                FROM SysGroupV1 g
                LEFT JOIN g.translations glan ON glan.langCode = :langCode
                WHERE g.groupCode = :groupCode
                """.formatted(dtoClassPath);

        List<SysGroupDtoV1> results = entityManager.createQuery(jpql, SysGroupDtoV1.class)
                .setParameter("groupCode", groupCode)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Kiểm tra group code đã tồn tại chưa
     */
    public boolean existsByCode(String groupCode) {
        return exists(root -> entityManager.getCriteriaBuilder()
            .equal(root.get("groupCode"), groupCode));
    }

    // ======= Find by ID =======
    
    /**
     * Tìm SysGroup DTO theo ID với translation
     */
    public Optional<SysGroupDtoV1> findById(Long groupId, String langCode) {
        String dtoClassPath = SysGroupDtoV1.class.getName();

        String jpql = """
                SELECT new %s(g.id, g.groupCode, glan.name, glan.description)
                FROM SysGroupV1 g
                LEFT JOIN g.translations glan ON glan.langCode = :langCode
                WHERE g.id = :groupId
                """.formatted(dtoClassPath);
        
        List<SysGroupDtoV1> results = entityManager.createQuery(jpql, SysGroupDtoV1.class)
                .setParameter("groupId", groupId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // ======= Find All Groups =======
    
    /**
     * Lấy danh sách tất cả Groups với phân trang và translation
     */
    public List<SysGroupDtoV1> findAll(int page, int size, String langCode) {
        String dtoClassPath = SysGroupDtoV1.class.getName();

        String jpql = """
                SELECT new %s(g.id, g.groupCode, glan.name, glan.description)
                FROM SysGroupV1 g
                LEFT JOIN g.translations glan ON glan.langCode = :langCode
                ORDER BY g.groupCode ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, SysGroupDtoV1.class)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * Lấy tất cả Groups (không phân trang) với translation
     */
    public List<SysGroupDtoV1> findAll(String langCode) {
        String dtoClassPath = SysGroupDtoV1.class.getName();

        String jpql = """
                SELECT new %s(g.id, g.groupCode, glan.name, glan.description)
                FROM SysGroupV1 g
                LEFT JOIN g.translations glan ON glan.langCode = :langCode
                ORDER BY g.groupCode ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, SysGroupDtoV1.class)
                .setParameter("langCode", langCode)
                .getResultList();
    }

    // ======= Find Functions of Group =======
    
    /**
     * Lấy danh sách Functions của một Group cụ thể
     * Chỉ lấy các functions có isActive = true
     */
    public List<SysFunctionDtoV1> findFunctionsByGroupId(Long groupId, String langCode) {
        String dtoClassPath = SysFunctionDtoV1.class.getName();

        String jpql = """
                SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
                FROM SysGroupV1 g
                JOIN g.roles r ON r.isActive = true
                JOIN r.function f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                WHERE g.id = :groupId
                ORDER BY f.functionCode ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, SysFunctionDtoV1.class)
                .setParameter("groupId", groupId)
                .setParameter("langCode", langCode)
                .getResultList();
    }

    /**
     * Lấy danh sách Functions của một Group với phân trang
     */
    public List<SysFunctionDtoV1> findFunctionsByGroupId(
            Long groupId, String langCode, int page, int size) {
        String dtoClassPath = SysFunctionDtoV1.class.getName();

        String jpql = """
                SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
                FROM SysGroupV1 g
                JOIN g.roles r ON r.isActive = true
                JOIN r.function f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                WHERE g.id = :groupId
                ORDER BY f.functionCode ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, SysFunctionDtoV1.class)
                .setParameter("groupId", groupId)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    // ======= Find Clients of Group =======
    
    /**
     * Lấy danh sách Clients thuộc một Group cụ thể - trả về DTO
     */
    public List<ClientDtoV1> findClientsByGroupId(Long groupId) {
        String dtoClassPath = ClientDtoV1.class.getName();

        String jpql = """
                SELECT new %s(
                    c.id, c.username, c.clientType, 
                    c.ipAddress, c.macAddress, c.avatarUrl, c.lastLoginAt
                )
                FROM SysGroupV1 g
                JOIN g.clients c
                WHERE g.id = :groupId
                ORDER BY c.username ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, ClientDtoV1.class)
                .setParameter("groupId", groupId)
                .getResultList();
    }

    /**
     * Lấy danh sách Clients thuộc một Group với phân trang - trả về DTO
     */
    public List<ClientDtoV1> findClientsByGroupId(Long groupId, int page, int size) {
        String dtoClassPath = ClientDtoV1.class.getName();

        String jpql = """
                SELECT new %s(
                    c.id, c.username, c.clientType, 
                    c.ipAddress, c.macAddress, c.avatarUrl, c.lastLoginAt
                )
                FROM SysGroupV1 g
                JOIN g.clients c
                WHERE g.id = :groupId
                ORDER BY c.username ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, ClientDtoV1.class)
                .setParameter("groupId", groupId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * Lấy danh sách Clients thuộc một Group - trả về Entity
     * Dùng khi cần thao tác với entity
     */
    public List<ClientV1> findClientEntitiesByGroupId(Long groupId) {
        String jpql = """
                SELECT c
                FROM SysGroupV1 g
                JOIN g.clients c
                WHERE g.id = :groupId
                ORDER BY c.username ASC
                """;

        return entityManager.createQuery(jpql, ClientV1.class)
                .setParameter("groupId", groupId)
                .getResultList();
    }

    /**
     * Lấy danh sách Clients thuộc một Group với phân trang - trả về Entity
     */
    public List<ClientV1> findClientEntitiesByGroupId(Long groupId, int page, int size) {
        String jpql = """
                SELECT c
                FROM SysGroupV1 g
                JOIN g.clients c
                WHERE g.id = :groupId
                ORDER BY c.username ASC
                """;

        return entityManager.createQuery(jpql, ClientV1.class)
                .setParameter("groupId", groupId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    // ======= Count =======
    
    /**
     * Đếm tổng số Groups
     */
    public long countAll() {
        String jpql = "SELECT COUNT(g) FROM SysGroupV1 g";
        return entityManager.createQuery(jpql, Long.class)
                .getSingleResult();
    }

    /**
     * Đếm số Functions của một Group
     */
    public long countFunctionsByGroupId(Long groupId) {
        String jpql = """
                SELECT COUNT(f)
                FROM SysGroupV1 g
                JOIN g.roles r ON r.isActive = true
                JOIN r.function f
                WHERE g.id = :groupId
                """;
        
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("groupId", groupId)
                .getSingleResult();
    }

    /**
     * Đếm số Clients của một Group
     */
    public long countClientsByGroupId(Long groupId) {
        String jpql = """
                SELECT COUNT(c)
                FROM SysGroupV1 g
                JOIN g.clients c
                WHERE g.id = :groupId
                """;
        
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("groupId", groupId)
                .getSingleResult();
    }
}
