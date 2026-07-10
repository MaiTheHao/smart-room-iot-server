package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseEntityDao;
import com.iviet.ivshs.entities.DeviceStatusMetric;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class DeviceStatusMetricDao extends BaseEntityDao<DeviceStatusMetric> {

    public DeviceStatusMetricDao() {
        super(DeviceStatusMetric.class);
    }

    @Override
    @Transactional
    public List<DeviceStatusMetric> save(List<DeviceStatusMetric> entities) {
        String sql = """
                INSERT INTO device_status_metrics
                (target_category, target_id, timestamp, unix_minute, status_data)
                VALUES (?, ?, ?, ?, ?)
                """;
        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                    DeviceStatusMetric e = entities.get(i);
                    ps.setString(1, e.getTargetCategory());
                    ps.setLong(2, e.getTargetId());
                    ps.setObject(3, e.getTimestamp());
                    ps.setObject(4, e.getUnixMinute());
                    ps.setString(5, e.getStatusData() != null ? e.getStatusData().toString() : null);
                }

                @Override
                public int getBatchSize() {
                    return entities.size();
                }
            });
            return entities;
        } catch (Exception e) {
            throw new RuntimeException("Failed to batch insert DeviceStatusMetric entities", e);
        }
    }

    public List<DeviceStatusMetric> findHistory(String category, Long targetId, Instant from, Instant to) {
        String jpql = """
                SELECT dsm
                FROM DeviceStatusMetric dsm
                WHERE dsm.targetCategory = :category
                  AND dsm.targetId = :targetId
                  AND dsm.timestamp BETWEEN :from AND :to
                ORDER BY dsm.timestamp ASC
                """;
        return entityManager.createQuery(jpql, DeviceStatusMetric.class)
                .setParameter("category", category)
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    public Optional<DeviceStatusMetric> findLatest(String category, Long targetId) {
        String jpql = """
                SELECT dsm
                FROM DeviceStatusMetric dsm
                WHERE dsm.targetCategory = :category
                  AND dsm.targetId = :targetId
                ORDER BY dsm.timestamp DESC
                """;
        return entityManager.createQuery(jpql, DeviceStatusMetric.class)
                .setParameter("category", category)
                .setParameter("targetId", targetId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public List<DeviceStatusMetric> findAllLatestForEachDevice() {
        String jpql = """
                SELECT dsm
                FROM DeviceStatusMetric dsm
                WHERE dsm.id IN (
                    SELECT MAX(d.id)
                    FROM DeviceStatusMetric d
                    GROUP BY d.targetCategory, d.targetId
                )
                """;
        return entityManager.createQuery(jpql, DeviceStatusMetric.class).getResultList();
    }
}
