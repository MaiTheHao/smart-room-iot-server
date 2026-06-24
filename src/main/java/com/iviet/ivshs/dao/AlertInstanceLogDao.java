package com.iviet.ivshs.dao;

import com.iviet.ivshs.dto.alert.AlertInstanceLogFilterDto;
import com.iviet.ivshs.entities.AlertInstanceLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AlertInstanceLogDao {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(AlertInstanceLog log) {
        entityManager.persist(log);
    }

    /** Lấy toàn bộ log của một alert event, sắp xếp theo thời gian tăng dần (timeline). */
    public List<AlertInstanceLog> findAllByAlertId(Long alertId) {
        String jpql = """
                SELECT ail FROM AlertInstanceLog ail
                WHERE ail.alert.id = :alertId
                ORDER BY ail.createdAt ASC
                """;
        return entityManager.createQuery(jpql, AlertInstanceLog.class).setParameter("alertId", alertId).getResultList();
    }

    public List<AlertInstanceLog> findAllByAlertId(Long alertId, AlertInstanceLogFilterDto filter) {
        StringBuilder jpql = new StringBuilder("SELECT ail FROM AlertInstanceLog ail WHERE ail.alert.id = :alertId");
        if (filter.actionType() != null) {
            jpql.append(" AND ail.actionType = :actionType");
        }
        if (filter.actorType() != null) {
            jpql.append(" AND ail.actorType = :actorType");
        }
        jpql.append(" ORDER BY ail.createdAt ASC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstanceLog.class)
                .setParameter("alertId", alertId);
        if (filter.actionType() != null) {
            q.setParameter("actionType", filter.actionType());
        }
        if (filter.actorType() != null) {
            q.setParameter("actorType", filter.actorType());
        }
        return q.setFirstResult(filter.page() * filter.size())
                .setMaxResults(filter.size())
                .getResultList();
    }

    public long countByAlertId(Long alertId, AlertInstanceLogFilterDto filter) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(ail) FROM AlertInstanceLog ail WHERE ail.alert.id = :alertId");
        if (filter.actionType() != null) {
            jpql.append(" AND ail.actionType = :actionType");
        }
        if (filter.actorType() != null) {
            jpql.append(" AND ail.actorType = :actorType");
        }

        var q = entityManager.createQuery(jpql.toString(), Long.class)
                .setParameter("alertId", alertId);
        if (filter.actionType() != null) {
            q.setParameter("actionType", filter.actionType());
        }
        if (filter.actorType() != null) {
            q.setParameter("actorType", filter.actorType());
        }
        return q.getSingleResult();
    }
}
