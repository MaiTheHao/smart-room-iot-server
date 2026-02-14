package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.Rule;

import jakarta.persistence.TypedQuery;

@Repository
public class RuleDao extends BaseAuditEntityDao<Rule> {

    public RuleDao() {
        super(Rule.class);
    }

    public List<Rule> findAllActive() {
        String jpql = "SELECT r FROM Rule r WHERE r.isActive = true ORDER BY r.priority DESC, r.updatedAt DESC";
        return entityManager.createQuery(jpql, Rule.class)
                .getResultList();
    }

    public Optional<Rule> findByIdWithConditions(Long ruleId) {
        String jpql = """
                SELECT DISTINCT r FROM Rule r
                LEFT JOIN FETCH r.conditions
                WHERE r.id = :id
                """;
        
        List<Rule> results = entityManager.createQuery(jpql, Rule.class)
                .setParameter("id", ruleId)
                .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Rule> findAllActiveWithConditions() {
        String jpql = """
                SELECT DISTINCT r FROM Rule r
                LEFT JOIN FETCH r.conditions
                WHERE r.isActive = true
                ORDER BY r.priority DESC, r.updatedAt DESC
                """;
        
        return entityManager.createQuery(jpql, Rule.class)
                .getResultList();
    }
    
    public List<Rule> findAllByDeviceIdAndActive(Long deviceId) {
        String jpql = """
                SELECT DISTINCT r FROM Rule r
                LEFT JOIN FETCH r.conditions
                WHERE r.deviceId = :deviceId AND r.isActive = true
                ORDER BY r.priority DESC, r.updatedAt DESC
                """;
        return entityManager.createQuery(jpql, Rule.class)
                .setParameter("deviceId", deviceId)
                .getResultList();
    }

    public boolean existsByName(String name) {
        String jpql = "SELECT COUNT(r) FROM Rule r WHERE r.name = :name";
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("name", name)
                .getSingleResult();
        return count > 0;
    }

    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        String jpql = "SELECT COUNT(r) FROM Rule r WHERE r.name = :name AND r.id != :excludeId";
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("name", name)
                .setParameter("excludeId", excludeId)
                .getSingleResult();
        return count > 0;
    }

    public List<Rule> findAllPaginated(int page, int size) {
        String jpql = "SELECT r FROM Rule r ORDER BY r.updatedAt DESC";
        TypedQuery<Rule> query = entityManager.createQuery(jpql, Rule.class);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public long countAll() {
        String jpql = "SELECT COUNT(r) FROM Rule r";
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }
}
