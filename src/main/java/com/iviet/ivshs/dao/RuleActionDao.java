package com.iviet.ivshs.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.RuleAction;

@Repository
public class RuleActionDao extends BaseAuditEntityDao<RuleAction> {

    public RuleActionDao() {
        super(RuleAction.class);
    }

    public List<RuleAction> findByRuleId(Long ruleId) {
        String jpql = "SELECT a FROM RuleAction a WHERE a.rule.id = :ruleId ORDER BY a.executionOrder ASC";
        return entityManager.createQuery(jpql, RuleAction.class)
                .setParameter("ruleId", ruleId)
                .getResultList();
    }

    public List<RuleAction> findByTargetDeviceId(Long deviceId) {
        String jpql = "SELECT a FROM RuleAction a WHERE a.targetDeviceId = :deviceId";
        return entityManager.createQuery(jpql, RuleAction.class)
                .setParameter("deviceId", deviceId)
                .getResultList();
    }
}
