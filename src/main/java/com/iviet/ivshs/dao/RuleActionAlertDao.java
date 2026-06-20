package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
import com.iviet.ivshs.entities.RuleActionAlert;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RuleActionAlertDao extends BaseAuditEntityDao<RuleActionAlert> {

    public RuleActionAlertDao() {
        super(RuleActionAlert.class);
    }

    public List<RuleActionAlert> findAllByRuleId(Long ruleId) {
        String jpql = "SELECT raa FROM RuleActionAlert raa WHERE raa.rule.id = :ruleId";
        return entityManager.createQuery(jpql, RuleActionAlert.class)
                .setParameter("ruleId", ruleId)
                .getResultList();
    }

    /**
     * Tìm cấu hình alert cho một Rule ID cụ thể.
     * Trả về Optional.empty() nếu Rule đó chưa có alert config.
     */
    public Optional<RuleActionAlert> findByRuleId(Long ruleId) {
        String jpql = "SELECT raa FROM RuleActionAlert raa WHERE raa.rule.id = :ruleId";
        return entityManager.createQuery(jpql, RuleActionAlert.class)
                .setParameter("ruleId", ruleId)
                .getResultStream()
                .findFirst();
    }

    public boolean existsByRuleId(Long ruleId) {
        String jpql = "SELECT COUNT(raa) FROM RuleActionAlert raa WHERE raa.rule.id = :ruleId";
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("ruleId", ruleId)
                .getSingleResult();
        return count > 0;
    }
}
