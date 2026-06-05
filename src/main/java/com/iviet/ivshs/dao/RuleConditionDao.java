package com.iviet.ivshs.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
import com.iviet.ivshs.entities.RuleCondition;

@Repository
public class RuleConditionDao extends BaseAuditEntityDao<RuleCondition> {

    public RuleConditionDao() {
        super(RuleCondition.class);
    }

    public List<RuleCondition> findByRuleId(Long ruleId) {
        String jpql = "SELECT c FROM RuleCondition c WHERE c.rule.id = :ruleId ORDER BY c.sortOrder ASC";
        return entityManager.createQuery(jpql, RuleCondition.class)
                .setParameter("ruleId", ruleId)
                .getResultList();
    }
}
