package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.Rule;

@Repository
public class RuleDao extends BaseAuditEntityDao<Rule> {

    public RuleDao() {
        super(Rule.class);
    }

    public List<Rule> findAllActive() {
        String jpql = "SELECT r FROM Rule r WHERE r.isActive = true ORDER BY r.priority DESC, r.updatedAt DESC";
        return entityManager.createQuery(jpql, Rule.class)
                .getResultList();
    }

    /**
     * Tìm Rule theo ID và nạp đầy đủ Conditions + Actions (Sử dụng Double Fetch)
     */
    public Optional<Rule> findByIdWithConditionsAndActions(Long ruleId) {
        String jpqlConditions = "SELECT r FROM Rule r LEFT JOIN FETCH r.conditions WHERE r.id = :id";
        List<Rule> results = entityManager.createQuery(jpqlConditions, Rule.class)
                .setParameter("id", ruleId)
                .getResultList();

        if (results.isEmpty()) return Optional.empty();
        
        Rule rule = results.get(0);

        String jpqlActions = "SELECT r FROM Rule r LEFT JOIN FETCH r.actions WHERE r.id = :id";
        entityManager.createQuery(jpqlActions, Rule.class)
                .setParameter("id", ruleId)
                .getResultList();

        return Optional.of(rule);
    }

    /**
     * Lấy tất cả Rule đang Active kèm đầy đủ dữ liệu để Engine xử lý (Sử dụng Double Fetch)
     */
    public List<Rule> findAllActiveWithConditionsAndActions() {
        String jpqlConditions = """
                SELECT DISTINCT r FROM Rule r 
                LEFT JOIN FETCH r.conditions 
                WHERE r.isActive = true 
                ORDER BY r.priority DESC, r.updatedAt DESC
                """;
        List<Rule> rules = entityManager.createQuery(jpqlConditions, Rule.class)
                .getResultList();

        if (rules.isEmpty()) return rules;

        String jpqlActions = "SELECT DISTINCT r FROM Rule r LEFT JOIN FETCH r.actions WHERE r IN :rules";
        entityManager.createQuery(jpqlActions, Rule.class)
                .setParameter("rules", rules)
                .getResultList();

        return rules;
    }

    public void updateActiveStatus(Long id, boolean isActive) {
        String jpql = "UPDATE Rule r SET r.isActive = :isActive WHERE r.id = :id";
        entityManager.createQuery(jpql)
                .setParameter("isActive", isActive)
                .setParameter("id", id)
                .executeUpdate();
    }

    public boolean existsByName(String name) {
        String jpql = "SELECT COUNT(r) FROM Rule r WHERE r.name = :name";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("name", name)
                .getSingleResult() > 0;
    }

    public boolean existsByNameAndIdNot(String name, Long id) {
        String jpql = "SELECT COUNT(r) FROM Rule r WHERE r.name = :name AND r.id != :id";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("name", name)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }

    public List<Rule> findAllPaginated(int page, int size) {
        String jpql = "SELECT r FROM Rule r ORDER BY r.priority DESC, r.updatedAt DESC";
        return entityManager.createQuery(jpql, Rule.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

}
