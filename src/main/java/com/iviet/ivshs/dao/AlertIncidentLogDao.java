package com.iviet.ivshs.dao;

import com.iviet.ivshs.entities.AlertIncidentLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AlertIncidentLogDao {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(AlertIncidentLog log) {
        entityManager.persist(log);
    }

    /** Lấy toàn bộ log của một alert event, sắp xếp theo thời gian tăng dần (timeline). */
    public List<AlertIncidentLog> findAllByAlertId(Long alertId) {
        String jpql = """
            SELECT ail FROM AlertIncidentLog ail
            WHERE ail.alert.id = :alertId
            ORDER BY ail.createdAt ASC
            """;
        return entityManager.createQuery(jpql, AlertIncidentLog.class)
                .setParameter("alertId", alertId)
                .getResultList();
    }
}
