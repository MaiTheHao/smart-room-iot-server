package com.iviet.ivshs.dao;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.RuleCondition;

@Repository
public class RuleConditionDao extends BaseAuditEntityDao<RuleCondition> {

    public RuleConditionDao() {
        super(RuleCondition.class);
    }
}
