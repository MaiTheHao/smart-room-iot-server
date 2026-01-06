package com.iviet.ivshs.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.SysRole;

import jakarta.persistence.NoResultException;

@Repository
public class SysRoleDao extends BaseTranslatableEntityDao<SysRole> {
    
    public SysRoleDao() {
        super(SysRole.class);
    }

    // ======= Find by Group and Function =======
    
    /**
     * Tìm Role theo Group ID và Function ID
     */
    public Optional<SysRole> findByGroupAndFunction(Long groupId, Long functionId) {
        String jpql = """
                SELECT r
                FROM SysRoler
                WHERE r.group.id = :groupId AND r.function.id = :functionId
                """;

        try {
            SysRole result = entityManager.createQuery(jpql, SysRole.class)
                    .setParameter("groupId", groupId)
                    .setParameter("functionId", functionId)
                    .getSingleResult();
            return Optional.of(result);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Tìm Active Role theo Group ID và Function ID
     */
    public Optional<SysRole> findActiveByGroupAndFunction(Long groupId, Long functionId) {
        String jpql = """
                SELECT r
                FROM SysRoler
                WHERE r.group.id = :groupId 
                  AND r.function.id = :functionId 
                  AND r.isActive = true
                """;

        try {
            SysRole result = entityManager.createQuery(jpql, SysRole.class)
                    .setParameter("groupId", groupId)
                    .setParameter("functionId", functionId)
                    .getSingleResult();
            return Optional.of(result);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Kiểm tra Role có tồn tại hay không
     */
    public boolean existsByGroupAndFunction(Long groupId, Long functionId) {
        String jpql = """
                SELECT COUNT(r) > 0
                FROM SysRoler
                WHERE r.group.id = :groupId AND r.function.id = :functionId
                """;

        return entityManager.createQuery(jpql, Boolean.class)
                .setParameter("groupId", groupId)
                .setParameter("functionId", functionId)
                .getSingleResult();
    }

    /**
     * Kiểm tra Active Role có tồn tại hay không
     */
    public boolean existsActiveByGroupAndFunction(Long groupId, Long functionId) {
        String jpql = """
                SELECT COUNT(r) > 0
                FROM SysRoler
                WHERE r.group.id = :groupId 
                  AND r.function.id = :functionId 
                  AND r.isActive = true
                """;

        return entityManager.createQuery(jpql, Boolean.class)
                .setParameter("groupId", groupId)
                .setParameter("functionId", functionId)
                .getSingleResult();
    }

    // ======= Delete Operations =======
    
    /**
     * Xóa Role theo Group ID và Function ID
     * Trả về số lượng records đã xóa
     */
    public int deleteByGroupAndFunction(Long groupId, Long functionId) {
        String jpql = """
                DELETE FROM SysRoler
                WHERE r.group.id = :groupId AND r.function.id = :functionId
                """;

        return entityManager.createQuery(jpql)
                .setParameter("groupId", groupId)
                .setParameter("functionId", functionId)
                .executeUpdate();
    }

    /**
     * Xóa tất cả Roles của một Group
     * Trả về số lượng records đã xóa
     */
    public int deleteByGroupId(Long groupId) {
        String jpql = """
                DELETE FROM SysRoler
                WHERE r.group.id = :groupId
                """;

        return entityManager.createQuery(jpql)
                .setParameter("groupId", groupId)
                .executeUpdate();
    }

    /**
     * Xóa tất cả Roles của một Function
     * Trả về số lượng records đã xóa
     */
    public int deleteByFunctionId(Long functionId) {
        String jpql = """
                DELETE FROM SysRoler
                WHERE r.function.id = :functionId
                """;

        return entityManager.createQuery(jpql)
                .setParameter("functionId", functionId)
                .executeUpdate();
    }

    // ======= Count =======
    
    /**
     * Đếm số Roles của một Group
     */
    public long countByGroupId(Long groupId) {
        String jpql = """
                SELECT COUNT(r)
                FROM SysRoler
                WHERE r.group.id = :groupId
                """;
        
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("groupId", groupId)
                .getSingleResult();
    }

    /**
     * Đếm số Active Roles của một Group
     */
    public long countActiveByGroupId(Long groupId) {
        String jpql = """
                SELECT COUNT(r)
                FROM SysRoler
                WHERE r.group.id = :groupId AND r.isActive = true
                """;
        
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("groupId", groupId)
                .getSingleResult();
    }

    /**
     * Đếm số Roles của một Function
     */
    public long countByFunctionId(Long functionId) {
        String jpql = """
                SELECT COUNT(r)
                FROM SysRoler
                WHERE r.function.id = :functionId
                """;
        
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("functionId", functionId)
                .getSingleResult();
    }

    /**
     * Đếm số Active Roles của một Function
     */
    public long countActiveByFunctionId(Long functionId) {
        String jpql = """
                SELECT COUNT(r)
                FROM SysRoler
                WHERE r.function.id = :functionId AND r.isActive = true
                """;
        
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("functionId", functionId)
                .getSingleResult();
    }
}
