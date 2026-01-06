package com.iviet.ivshs.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.SysClientFunctionCache;

@Repository
public class SysClientFunctionCacheDao extends BaseDao<SysClientFunctionCache> {
    
    public SysClientFunctionCacheDao() {
        super(SysClientFunctionCache.class);
    }

    public boolean hasPermission(Long clientId, String functionCode) {
        return exists(root -> {
            var cb = entityManager.getCriteriaBuilder();
            return cb.and(
                cb.equal(root.get("clientId"), clientId),
                cb.equal(root.get("functionCode"), functionCode)
            );
        });
    }

    public List<String> getFunctionCodesByClient(Long clientId) {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(String.class);
        var root = query.from(SysClientFunctionCache.class);
        
        query.select(root.get("functionCode"))
             .distinct(true)
             .where(cb.equal(root.get("clientId"), clientId));
        
        return entityManager.createQuery(query).getResultList();
    }

    public List<SysClientFunctionCache> findByClient(Long clientId) {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(SysClientFunctionCache.class);
        var root = query.from(SysClientFunctionCache.class);
        
        query.where(cb.equal(root.get("clientId"), clientId));
        
        return entityManager.createQuery(query).getResultList();
    }

    public List<SysClientFunctionCache> findByGroup(Long groupId) {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(SysClientFunctionCache.class);
        var root = query.from(SysClientFunctionCache.class);
        
        query.where(cb.equal(root.get("groupId"), groupId));
        
        return entityManager.createQuery(query).getResultList();
    }

    public int deleteByClientAndGroup(Long clientId, Long groupId) {
        var cb = entityManager.getCriteriaBuilder();
        var delete = cb.createCriteriaDelete(SysClientFunctionCache.class);
        var root = delete.from(SysClientFunctionCache.class);
        
        delete.where(cb.and(
            cb.equal(root.get("clientId"), clientId),
            cb.equal(root.get("groupId"), groupId)
        ));
        
        return entityManager.createQuery(delete).executeUpdate();
    }

    public int deleteByClient(Long clientId) {
        var cb = entityManager.getCriteriaBuilder();
        var delete = cb.createCriteriaDelete(SysClientFunctionCache.class);
        var root = delete.from(SysClientFunctionCache.class);
        
        delete.where(cb.equal(root.get("clientId"), clientId));
        
        return entityManager.createQuery(delete).executeUpdate();
    }

    public int deleteByGroup(Long groupId) {
        var cb = entityManager.getCriteriaBuilder();
        var delete = cb.createCriteriaDelete(SysClientFunctionCache.class);
        var root = delete.from(SysClientFunctionCache.class);
        
        delete.where(cb.equal(root.get("groupId"), groupId));
        
        return entityManager.createQuery(delete).executeUpdate();
    }

    public int deleteByFunctionCode(String functionCode) {
        var cb = entityManager.getCriteriaBuilder();
        var delete = cb.createCriteriaDelete(SysClientFunctionCache.class);
        var root = delete.from(SysClientFunctionCache.class);
        
        delete.where(cb.equal(root.get("functionCode"), functionCode));
        
        return entityManager.createQuery(delete).executeUpdate();
    }

    public boolean exists(Long clientId, String functionCode, Long groupId) {
        return exists(root -> {
            var cb = entityManager.getCriteriaBuilder();
            return cb.and(
                cb.equal(root.get("clientId"), clientId),
                cb.equal(root.get("functionCode"), functionCode),
                cb.equal(root.get("groupId"), groupId)
            );
        });
    }

    public long countDistinctFunctionsByClient(Long clientId) {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(Long.class);
        var root = query.from(SysClientFunctionCache.class);
        
        query.select(cb.countDistinct(root.get("functionCode")))
             .where(cb.equal(root.get("clientId"), clientId));
        
        return entityManager.createQuery(query).getSingleResult();
    }
}
