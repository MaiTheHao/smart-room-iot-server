package com.iviet.ivshs.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.SysClientFunctionCache;

@Repository
public class SysClientFunctionCacheDao extends BaseDao<SysClientFunctionCache> {
    
    private final String ENTITY_CLIENT = Client.class.getSimpleName();
    private final String ENTITY_CLIENT_FUNCTION_CACHE = SysClientFunctionCache.class.getSimpleName();

    public SysClientFunctionCacheDao() {
        super(SysClientFunctionCache.class);
    }

    public int rebuildAllPermissions() {
        this.deleteAll();
        this.entityManager.flush();

        String jpql = """
            SELECT c.id, g.id, f.functionCode
            FROM %s c
                JOIN c.groups g
                JOIN g.roles r
                JOIN r.function f
                GROUP BY c.id, g.id, f.functionCode
            """.formatted(ENTITY_CLIENT);

        List<Object[]> results = entityManager.createQuery(jpql, Object[].class).getResultList();

        int count = 0;
        for (Object[] row : results) {
            SysClientFunctionCache cache = new SysClientFunctionCache();
            cache.setClientId((Long) row[0]);
            cache.setGroupId((Long) row[1]);
            cache.setFunctionCode((String) row[2]);
            
            entityManager.persist(cache);
            count++;
            
            if (count % 50 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        
        return count;
    }

    public int rebuildPermissionsByClientId(Long clientId) {
        this.deleteByClient(clientId);

        String jpql = """
            SELECT g.id, f.functionCode
            FROM %s c
                JOIN c.groups g
                JOIN g.roles r
                JOIN r.function f
                WHERE c.id = :clientId
                GROUP BY g.id, f.functionCode
            """.formatted(ENTITY_CLIENT);

        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                .setParameter("clientId", clientId)
                .getResultList();

        int count = 0;
        for (Object[] row : results) {
            SysClientFunctionCache cache = new SysClientFunctionCache();
            cache.setClientId(clientId);
            cache.setGroupId((Long) row[0]);
            cache.setFunctionCode((String) row[1]);
            
            entityManager.persist(cache);
            count++;
        }
        
        return count;
    }

    public boolean hasPermission(Long clientId, String functionCode) {
        String jpql = """
            SELECT COUNT(x) FROM %s x
            WHERE x.clientId = :clientId AND x.functionCode = :fCode
                """.formatted(ENTITY_CLIENT_FUNCTION_CACHE);
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("clientId", clientId)
                .setParameter("fCode", functionCode)
                .getSingleResult();
        return count > 0;
    }

    public List<String> getFunctionCodesByClient(Long clientId) {
        String jpql = """
            SELECT DISTINCT x.functionCode FROM %s x
            WHERE x.clientId = :clientId
                """.formatted(ENTITY_CLIENT_FUNCTION_CACHE);
        return entityManager.createQuery(jpql, String.class)
                .setParameter("clientId", clientId)
                .getResultList();
    }

    public List<SysClientFunctionCache> findByClient(Long clientId) {
        String jpql = """
            SELECT x FROM %s x
            WHERE x.clientId = :clientId
                """.formatted(ENTITY_CLIENT_FUNCTION_CACHE);
        return entityManager.createQuery(jpql, SysClientFunctionCache.class)
                .setParameter("clientId", clientId)
                .getResultList();
    }

    public List<SysClientFunctionCache> findByGroup(Long groupId) {
        String jpql = """
            SELECT x FROM %s x
            WHERE x.groupId = :groupId
                """.formatted(ENTITY_CLIENT_FUNCTION_CACHE);
        return entityManager.createQuery(jpql, SysClientFunctionCache.class)
                .setParameter("groupId", groupId)
                .getResultList();
    }

    public int deleteByClientAndGroup(Long clientId, Long groupId) {
        String jpql = """
            DELETE FROM %s x
            WHERE x.clientId = :clientId AND x.groupId = :groupId
                """.formatted(ENTITY_CLIENT_FUNCTION_CACHE);
        return entityManager.createQuery(jpql)
                .setParameter("clientId", clientId)
                .setParameter("groupId", groupId)
                .executeUpdate();
    }

    public int deleteByClient(Long clientId) {
        String jpql = """
            DELETE FROM %s x
            WHERE x.clientId = :clientId
                """.formatted(ENTITY_CLIENT_FUNCTION_CACHE);
        return entityManager.createQuery(jpql)
                .setParameter("clientId", clientId)
                .executeUpdate();
    }

    public int deleteByGroup(Long groupId) {
        String jpql = """
            DELETE FROM %s x
            WHERE x.groupId = :groupId
                """.formatted(ENTITY_CLIENT_FUNCTION_CACHE);
        return entityManager.createQuery(jpql)
                .setParameter("groupId", groupId)
                .executeUpdate();
    }

    public int deleteByFunctionCode(String functionCode) {
        String jpql = """
            DELETE FROM %s x
            WHERE x.functionCode = :fCode
                """.formatted(ENTITY_CLIENT_FUNCTION_CACHE);
        return entityManager.createQuery(jpql)
                .setParameter("fCode", functionCode)
                .executeUpdate();
    }

    public boolean exists(Long clientId, String functionCode, Long groupId) {
        String jpql = """
            SELECT COUNT(x) FROM %s x
            WHERE x.clientId = :cid AND x.functionCode = :fcode AND x.groupId = :gid
                """.formatted(ENTITY_CLIENT_FUNCTION_CACHE);
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("cid", clientId)
                .setParameter("fcode", functionCode)
                .setParameter("gid", groupId)
                .getSingleResult();
        return count > 0;
    }


    public long countDistinctFunctionsByClient(Long clientId) {
        String jpql = """
            SELECT COUNT(DISTINCT x.functionCode) FROM %s x
            WHERE x.clientId = :clientId
                """.formatted(ENTITY_CLIENT_FUNCTION_CACHE);
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("clientId", clientId)
                .getSingleResult();
    }

    public void deleteAll() {
        entityManager.createQuery("DELETE FROM %s".formatted(ENTITY_CLIENT_FUNCTION_CACHE)).executeUpdate();
    }
}
