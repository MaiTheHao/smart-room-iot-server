package com.iviet.ivshs.dao;

import java.util.List;
import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
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
