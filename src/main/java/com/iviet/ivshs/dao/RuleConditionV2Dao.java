package com.iviet.ivshs.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.RuleConditionV2;

@Repository
public class RuleConditionV2Dao extends BaseAuditEntityDao<RuleConditionV2> {

    public RuleConditionV2Dao() {
        super(RuleConditionV2.class);
    }

    public List<RuleConditionV2> findByRuleId(Long ruleId) {
        String jpql = "SELECT c FROM RuleConditionV2 c WHERE c.ruleV2.id = :ruleId ORDER BY c.sortOrder ASC";
        return entityManager.createQuery(jpql, RuleConditionV2.class)
                .setParameter("ruleId", ruleId)
                .getResultList();
    }
}
