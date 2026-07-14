package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseEntityDao;
import com.iviet.ivshs.dto.TemperatureMetricDto;
import com.iviet.ivshs.entities.TemperatureMetric;
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
public class TemperatureMetricDao extends BaseEntityDao<TemperatureMetric> {

    public TemperatureMetricDao() {
        super(TemperatureMetric.class);
    }

    @Override
    @Transactional
    public List<TemperatureMetric> save(List<TemperatureMetric> entities) {
        String sql = """
                INSERT INTO temperature_metrics
                (target_category, target_id, timestamp, unix_minute, temperature)
                VALUES (?, ?, ?, ?, ?)
                """;
        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                    TemperatureMetric e = entities.get(i);
                    ps.setString(1, e.getTargetCategory());
                    ps.setLong(2, e.getTargetId());
                    ps.setObject(3, e.getTimestamp());
                    ps.setObject(4, e.getUnixMinute());
                    ps.setDouble(5, e.getTemperature());
                }

                @Override
                public int getBatchSize() {
                    return entities.size();
                }
            });
            return entities;
        } catch (Exception e) {
            throw new RuntimeException("Failed to batch insert TemperatureMetric entities", e);
        }
    }

    public List<TemperatureMetric> findHistory(Long targetId, Instant from, Instant to) {
        String jpql = """
                SELECT tm
                FROM TemperatureMetric tm
                WHERE tm.targetId = :targetId
                  AND tm.timestamp BETWEEN :from AND :to
                ORDER BY tm.timestamp ASC
                """;
        return entityManager.createQuery(jpql, TemperatureMetric.class)
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    public List<TemperatureMetricDto> findHistory(Long targetId, Instant from, Instant to, int divisor) {
        String jpql = """
                SELECT
                    (tm.unixMinute - MOD(tm.unixMinute, :divisor)) * 60L,
                    AVG(tm.temperature)
                FROM TemperatureMetric tm
                WHERE tm.targetId = :targetId
                    AND tm.timestamp BETWEEN :from AND :to
                GROUP BY (tm.unixMinute - MOD(tm.unixMinute, :divisor)) * 60L
                ORDER BY (tm.unixMinute - MOD(tm.unixMinute, :divisor)) * 60L ASC
                """;

        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("divisor", divisor)
                .getResultList();

        return results.stream()
                .map(row -> TemperatureMetricDto.builder()
                        .timestamp(Instant.ofEpochSecond(((Number) row[0]).longValue()))
                        .temperature(row[1] != null ? ((Number) row[1]).doubleValue() : null)
                        .build())
                .toList();
    }

    public Optional<TemperatureMetric> findLatest(Long targetId) {
        String jpql = """
                SELECT tm
                FROM TemperatureMetric tm
                WHERE tm.targetId = :targetId
                ORDER BY tm.timestamp DESC
                """;
        return entityManager.createQuery(jpql, TemperatureMetric.class)
                .setParameter("targetId", targetId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
