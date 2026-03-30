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

    /**
     * Tìm Rule theo ID và nạp đầy đủ Conditions + Actions (Sử dụng Double Fetch)
     */
    public Optional<RuleV2> findByIdWithConditionsAndActions(Long ruleId) {
        String jpqlConditions = "SELECT r FROM RuleV2 r LEFT JOIN FETCH r.conditions WHERE r.id = :id";
        List<RuleV2> results = entityManager.createQuery(jpqlConditions, RuleV2.class)
                .setParameter("id", ruleId)
                .getResultList();

        if (results.isEmpty()) return Optional.empty();
        
        RuleV2 rule = results.get(0);

        String jpqlActions = "SELECT r FROM RuleV2 r LEFT JOIN FETCH r.actions WHERE r.id = :id";
        entityManager.createQuery(jpqlActions, RuleV2.class)
                .setParameter("id", ruleId)
                .getResultList();

        return Optional.of(rule);
    }

    /**
     * Lấy tất cả Rule đang Active kèm đầy đủ dữ liệu để Engine xử lý (Sử dụng Double Fetch)
     */
    public List<RuleV2> findAllActiveWithConditionsAndActions() {
        String jpqlConditions = """
                SELECT DISTINCT r FROM RuleV2 r 
                LEFT JOIN FETCH r.conditions 
                WHERE r.isActive = true 
                ORDER BY r.priority DESC, r.updatedAt DESC
                """;
        List<RuleV2> rules = entityManager.createQuery(jpqlConditions, RuleV2.class)
                .getResultList();

        if (rules.isEmpty()) return rules;

        String jpqlActions = "SELECT DISTINCT r FROM RuleV2 r LEFT JOIN FETCH r.actions WHERE r IN :rules";
        entityManager.createQuery(jpqlActions, RuleV2.class)
                .setParameter("rules", rules)
                .getResultList();

        return rules;
    }

    public void updateActiveStatus(Long id, boolean isActive) {
        String jpql = "UPDATE RuleV2 r SET r.isActive = :isActive WHERE r.id = :id";
        entityManager.createQuery(jpql)
                .setParameter("isActive", isActive)
                .setParameter("id", id)
                .executeUpdate();
    }

    public boolean existsByName(String name) {
        String jpql = "SELECT COUNT(r) FROM RuleV2 r WHERE r.name = :name";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("name", name)
                .getSingleResult() > 0;
    }

    public boolean existsByNameAndIdNot(String name, Long id) {
        String jpql = "SELECT COUNT(r) FROM RuleV2 r WHERE r.name = :name AND r.id != :id";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("name", name)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }

    public List<RuleV2> findAllPaginated(int page, int size) {
        String jpql = "SELECT r FROM RuleV2 r ORDER BY r.priority DESC, r.updatedAt DESC";
        return entityManager.createQuery(jpql, RuleV2.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

}
