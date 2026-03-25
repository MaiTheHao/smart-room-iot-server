package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.RuleV2;

@Repository
public class RuleV2Dao extends BaseAuditEntityDao<RuleV2> {

    public RuleV2Dao() {
        super(RuleV2.class);
    }

    public List<RuleV2> findAllActive() {
        String jpql = "SELECT r FROM RuleV2 r WHERE r.isActive = true ORDER BY r.priority DESC, r.updatedAt DESC";
        return entityManager.createQuery(jpql, RuleV2.class)
                .getResultList();
    }

    public Optional<RuleV2> findByIdWithConditionsAndActions(Long ruleId) {
        String jpql = """
                SELECT DISTINCT r FROM RuleV2 r
                LEFT JOIN FETCH r.conditions
                LEFT JOIN FETCH r.actions
                WHERE r.id = :id
                """;
        
        List<RuleV2> results = entityManager.createQuery(jpql, RuleV2.class)
                .setParameter("id", ruleId)
                .getResultList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<RuleV2> findAllActiveWithConditionsAndActions() {
        String jpql = """
                SELECT DISTINCT r FROM RuleV2 r
                LEFT JOIN FETCH r.conditions
                LEFT JOIN FETCH r.actions
                WHERE r.isActive = true
                ORDER BY r.priority DESC, r.updatedAt DESC
                """;
        
        return entityManager.createQuery(jpql, RuleV2.class)
                .getResultList();
    }

    public List<RuleV2> findByRoomId(Long roomId, int page, int size) {
        return findAll(
            root -> entityManager.getCriteriaBuilder().equal(root.get("roomId"), roomId),
            (root, cq) -> {
                var cb = entityManager.getCriteriaBuilder();
                cq.orderBy(cb.desc(root.get("priority")), cb.desc(root.get("updatedAt")));
            },
            page,
            size
        );
    }
}
