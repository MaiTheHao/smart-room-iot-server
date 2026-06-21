package com.iviet.ivshs.dao;

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
}
