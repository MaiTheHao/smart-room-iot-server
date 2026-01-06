package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.SysFunctionDto;
import com.iviet.ivshs.dto.SysGroupDto;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.SysGroup;

@Repository
public class SysGroupDao extends BaseTranslatableEntityDao<SysGroup> {
    
    public SysGroupDao() {
        super(SysGroup.class);
    }

    // ======= Find by Group Code =======
    
    /**
     * Tìm SysGroup entity theo groupCode
     */
    public Optional<SysGroup> findByCode(String groupCode) {
        return findOne(root -> entityManager.getCriteriaBuilder()
            .equal(root.get("groupCode"), groupCode));
    }

    /**
     * Tìm SysGroup DTO theo groupCode với translation
     */
    public Optional<SysGroupDto> findByCode(String groupCode, String langCode) {
        String dtoClassPath = SysGroupDto.class.getName();

        String jpql = """
                SELECT new %s(g.id, g.groupCode, glan.name, glan.description)
                FROM SysGroup g
                LEFT JOIN g.translations glan ON glan.langCode = :langCode
                WHERE g.groupCode = :groupCode
                """.formatted(dtoClassPath);

        List<SysGroupDto> results = entityManager.createQuery(jpql, SysGroupDto.class)
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
    public Optional<SysGroupDto> findById(Long groupId, String langCode) {
        String dtoClassPath = SysGroupDto.class.getName();

        String jpql = """
                SELECT new %s(g.id, g.groupCode, glan.name, glan.description)
                FROM SysGroup g
                LEFT JOIN g.translations glan ON glan.langCode = :langCode
                WHERE g.id = :groupId
                """.formatted(dtoClassPath);
        
        List<SysGroupDto> results = entityManager.createQuery(jpql, SysGroupDto.class)
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
    public List<SysGroupDto> findAll(int page, int size, String langCode) {
        String dtoClassPath = SysGroupDto.class.getName();

        String jpql = """
                SELECT new %s(g.id, g.groupCode, glan.name, glan.description)
                FROM SysGroup g
                LEFT JOIN g.translations glan ON glan.langCode = :langCode
                ORDER BY g.groupCode ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, SysGroupDto.class)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * Lấy tất cả Groups (không phân trang) với translation
     */
    public List<SysGroupDto> findAll(String langCode) {
        String dtoClassPath = SysGroupDto.class.getName();

        String jpql = """
                SELECT new %s(g.id, g.groupCode, glan.name, glan.description)
                FROM SysGroup g
                LEFT JOIN g.translations glan ON glan.langCode = :langCode
                ORDER BY g.groupCode ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, SysGroupDto.class)
                .setParameter("langCode", langCode)
                .getResultList();
    }

    // ======= Find Functions of Group =======
    
    /**
     * Lấy danh sách Functions của một Group cụ thể
     * Chỉ lấy các functions có isActive = true
     */
    public List<SysFunctionDto> findFunctionsByGroupId(Long groupId, String langCode) {
        String dtoClassPath = SysFunctionDto.class.getName();

        String jpql = """
                SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
                FROM SysGroup g
                JOIN g.roles r ON r.isActive = true
                JOIN r.function f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                WHERE g.id = :groupId
                ORDER BY f.functionCode ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, SysFunctionDto.class)
                .setParameter("groupId", groupId)
                .setParameter("langCode", langCode)
                .getResultList();
    }

    /**
     * Lấy danh sách Functions của một Group với phân trang
     */
    public List<SysFunctionDto> findFunctionsByGroupId(
            Long groupId, String langCode, int page, int size) {
        String dtoClassPath = SysFunctionDto.class.getName();

        String jpql = """
                SELECT new %s(f.id, f.functionCode, flan.name, flan.description)
                FROM SysGroup g
                JOIN g.roles r ON r.isActive = true
                JOIN r.function f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                WHERE g.id = :groupId
                ORDER BY f.functionCode ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, SysFunctionDto.class)
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
    public List<ClientDto> findClientsByGroupId(Long groupId) {
        String dtoClassPath = ClientDto.class.getName();

        String jpql = """
                SELECT new %s(
                    c.id, c.username, c.clientType, 
                    c.ipAddress, c.macAddress, c.avatarUrl, c.lastLoginAt
                )
                FROM SysGroup g
                JOIN g.clients c
                WHERE g.id = :groupId
                ORDER BY c.username ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, ClientDto.class)
                .setParameter("groupId", groupId)
                .getResultList();
    }

    /**
     * Lấy danh sách Clients thuộc một Group với phân trang - trả về DTO
     */
    public List<ClientDto> findClientsByGroupId(Long groupId, int page, int size) {
        String dtoClassPath = ClientDto.class.getName();

        String jpql = """
                SELECT new %s(
                    c.id, c.username, c.clientType, 
                    c.ipAddress, c.macAddress, c.avatarUrl, c.lastLoginAt
                )
                FROM SysGroup g
                JOIN g.clients c
                WHERE g.id = :groupId
                ORDER BY c.username ASC
                """.formatted(dtoClassPath);

        return entityManager.createQuery(jpql, ClientDto.class)
                .setParameter("groupId", groupId)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    /**
     * Lấy danh sách Clients thuộc một Group - trả về Entity
     * Dùng khi cần thao tác với entity
     */
    public List<Client> findClientEntitiesByGroupId(Long groupId) {
        String jpql = """
                SELECT c
                FROM SysGroup g
                JOIN g.clients c
                WHERE g.id = :groupId
                ORDER BY c.username ASC
                """;

        return entityManager.createQuery(jpql, Client.class)
                .setParameter("groupId", groupId)
                .getResultList();
    }

    /**
     * Lấy danh sách Clients thuộc một Group với phân trang - trả về Entity
     */
    public List<Client> findClientEntitiesByGroupId(Long groupId, int page, int size) {
        String jpql = """
                SELECT c
                FROM SysGroup g
                JOIN g.clients c
                WHERE g.id = :groupId
                ORDER BY c.username ASC
                """;

        return entityManager.createQuery(jpql, Client.class)
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
        String jpql = "SELECT COUNT(g) FROM SysGroup g";
        return entityManager.createQuery(jpql, Long.class)
                .getSingleResult();
    }

    /**
     * Đếm số Functions của một Group
     */
    public long countFunctionsByGroupId(Long groupId) {
        String jpql = """
                SELECT COUNT(f)
                FROM SysGroup g
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
                FROM SysGroup g
                JOIN g.clients c
                WHERE g.id = :groupId
                """;
        
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("groupId", groupId)
                .getSingleResult();
    }
}
