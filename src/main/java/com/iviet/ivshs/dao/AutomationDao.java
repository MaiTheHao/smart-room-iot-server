package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.Automation;

import jakarta.persistence.TypedQuery;

@Repository
public class AutomationDao extends BaseAuditEntityDao<Automation> {

    public AutomationDao() {
        super(Automation.class);
    }

    public Optional<Automation> findByIdWithActions(Long automationId) {
        String jpql = """
                SELECT DISTINCT a FROM Automation a
                LEFT JOIN FETCH a.actions
                WHERE a.id = :id
                """;
        
        List<Automation> results = entityManager.createQuery(jpql, Automation.class)
                .setParameter("id", automationId)
                .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Automation> findAllActiveWithActions() {
        String jpql = """
                SELECT DISTINCT a FROM Automation a
                LEFT JOIN FETCH a.actions
                WHERE a.isActive = true
                ORDER BY a.id
                """;
        
        return entityManager.createQuery(jpql, Automation.class)
                .getResultList();
    }

    public boolean existsByName(String name) {
        String jpql = "SELECT COUNT(a) FROM Automation a WHERE a.name = :name";
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("name", name)
                .getSingleResult();
        return count > 0;
    }

    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        String jpql = "SELECT COUNT(a) FROM Automation a WHERE a.name = :name AND a.id != :excludeId";
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("name", name)
                .setParameter("excludeId", excludeId)
                .getSingleResult();
        return count > 0;
    }

    public void updateActiveStatus(Long automationId, boolean isActive) {
        String jpql = "UPDATE Automation a SET a.isActive = :isActive WHERE a.id = :id";
        entityManager.createQuery(jpql)
                .setParameter("isActive", isActive)
                .setParameter("id", automationId)
                .executeUpdate();
    }

    public List<Automation> findAllPaginated(int page, int size) {
        String jpql = "SELECT a FROM Automation a ORDER BY a.createdAt DESC";
        TypedQuery<Automation> query = entityManager.createQuery(jpql, Automation.class);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public long countAll() {
        String jpql = "SELECT COUNT(a) FROM Automation a";
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }
}
