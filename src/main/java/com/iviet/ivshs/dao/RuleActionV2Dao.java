package com.iviet.ivshs.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.RuleActionV2;

@Repository
public class RuleActionV2Dao extends BaseAuditEntityDao<RuleActionV2> {

    public RuleActionV2Dao() {
        super(RuleActionV2.class);
    }

    public List<RuleActionV2> findByRuleId(Long ruleId) {
        String jpql = "SELECT a FROM RuleActionV2 a WHERE a.rule.id = :ruleId ORDER BY a.executionOrder ASC";
        return entityManager.createQuery(jpql, RuleActionV2.class)
                .setParameter("ruleId", ruleId)
                .getResultList();
    }

    public List<RuleActionV2> findByTargetDeviceId(Long deviceId) {
        String jpql = "SELECT a FROM RuleActionV2 a WHERE a.targetDeviceId = :deviceId";
        return entityManager.createQuery(jpql, RuleActionV2.class)
                .setParameter("deviceId", deviceId)
                .getResultList();
    }
}
