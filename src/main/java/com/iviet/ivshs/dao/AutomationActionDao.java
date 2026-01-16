package com.iviet.ivshs.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.AutomationAction;

@Repository
public class AutomationActionDao extends BaseAuditEntityDao<AutomationAction> {

    public AutomationActionDao() {
        super(AutomationAction.class);
    }

    public List<AutomationAction> findByAutomationId(Long automationId) {
        String jpql = """
                SELECT a FROM AutomationAction a
                WHERE a.automation.id = :automationId
                ORDER BY a.executionOrder ASC, a.id ASC
                """;
        
        return entityManager.createQuery(jpql, AutomationAction.class)
                .setParameter("automationId", automationId)
                .getResultList();
    }

    public void deleteByAutomationId(Long automationId) {
        String jpql = "DELETE FROM AutomationAction a WHERE a.automation.id = :automationId";
        entityManager.createQuery(jpql)
                .setParameter("automationId", automationId)
                .executeUpdate();
    }

    public long countByAutomationId(Long automationId) {
        String jpql = "SELECT COUNT(a) FROM AutomationAction a WHERE a.automation.id = :automationId";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("automationId", automationId)
                .getSingleResult();
    }
}
