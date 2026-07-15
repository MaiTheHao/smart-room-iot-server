package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseEntityDao;
import com.iviet.ivshs.dto.LuxMetricDto;
import com.iviet.ivshs.entities.LuxMetric;
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
public class LuxMetricDao extends BaseEntityDao<LuxMetric> {

    public LuxMetricDao() {
        super(LuxMetric.class);
    }

    @Override
    @Transactional
    public List<LuxMetric> save(List<LuxMetric> entities) {
        String sql = """
                INSERT INTO lux_metrics
                (target_category, target_id, timestamp, unix_minute, lux)
                VALUES (?, ?, ?, ?, ?)
                """;
        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                    LuxMetric e = entities.get(i);
                    ps.setString(1, e.getTargetCategory());
                    ps.setLong(2, e.getTargetId());
                    ps.setObject(3, e.getTimestamp());
                    ps.setObject(4, e.getUnixMinute());
                    ps.setDouble(5, e.getLux());
                }

                @Override
                public int getBatchSize() {
                    return entities.size();
                }
            });
            return entities;
        } catch (Exception e) {
            throw new RuntimeException("Failed to batch insert LuxMetric entities", e);
        }
    }

    public List<LuxMetric> findHistory(Long targetId, Instant from, Instant to) {
        String jpql = """
                SELECT lm
                FROM LuxMetric lm
                WHERE lm.targetId = :targetId
                  AND lm.timestamp BETWEEN :from AND :to
                ORDER BY lm.timestamp ASC
                """;
        return entityManager.createQuery(jpql, LuxMetric.class)
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    public List<LuxMetricDto> findHistory(Long targetId, Instant from, Instant to, int divisor) {
        String jpql = """
                SELECT
                    (lm.unixMinute - MOD(lm.unixMinute, :divisor)) * 60L,
                    AVG(lm.lux)
                FROM LuxMetric lm
                WHERE lm.targetId = :targetId
                    AND lm.timestamp BETWEEN :from AND :to
                GROUP BY (lm.unixMinute - MOD(lm.unixMinute, :divisor)) * 60L
                ORDER BY (lm.unixMinute - MOD(lm.unixMinute, :divisor)) * 60L ASC
                """;

        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("divisor", divisor)
                .getResultList();

        return results.stream()
                .map(row -> LuxMetricDto.builder()
                        .timestamp(Instant.ofEpochSecond(((Number) row[0]).longValue()))
                        .lux(row[1] != null ? ((Number) row[1]).doubleValue() : null)
                        .build())
                .toList();
    }

    public Optional<LuxMetric> findLatest(Long targetId) {
        String jpql = """
                SELECT lm
                FROM LuxMetric lm
                WHERE lm.targetId = :targetId
                ORDER BY lm.timestamp DESC
                """;
        return entityManager.createQuery(jpql, LuxMetric.class)
                .setParameter("targetId", targetId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
