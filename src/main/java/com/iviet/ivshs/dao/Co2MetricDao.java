package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseEntityDao;
import com.iviet.ivshs.dto.Co2MetricDto;
import com.iviet.ivshs.dto.RoomCo2MetricDto;
import com.iviet.ivshs.entities.Co2Metric;
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
public class Co2MetricDao extends BaseEntityDao<Co2Metric> {

    public Co2MetricDao() {
        super(Co2Metric.class);
    }

    @Override
    @Transactional
    public List<Co2Metric> save(List<Co2Metric> entities) {
        String sql = """
                INSERT INTO co2_metrics
                (target_category, target_id, timestamp, unix_minute, co2)
                VALUES (?, ?, ?, ?, ?)
                """;
        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                    Co2Metric e = entities.get(i);
                    ps.setString(1, e.getTargetCategory());
                    ps.setLong(2, e.getTargetId());
                    ps.setObject(3, e.getTimestamp());
                    ps.setObject(4, e.getUnixMinute());
                    ps.setDouble(5, e.getCo2());
                }

                @Override
                public int getBatchSize() {
                    return entities.size();
                }
            });
            return entities;
        } catch (Exception e) {
            throw new RuntimeException("Failed to batch insert Co2Metric entities", e);
        }
    }

    public List<Co2Metric> findHistory(Long targetId, Instant from, Instant to) {
        String jpql = """
                SELECT cm
                FROM Co2Metric cm
                WHERE cm.targetId = :targetId
                  AND cm.timestamp BETWEEN :from AND :to
                ORDER BY cm.timestamp ASC
                """;
        return entityManager.createQuery(jpql, Co2Metric.class)
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    public List<Co2MetricDto> findHistory(Long targetId, Instant from, Instant to, int divisor) {
        String jpql = """
                SELECT
                    (cm.unixMinute - MOD(cm.unixMinute, :divisor)) * 60L,
                    AVG(cm.co2)
                FROM Co2Metric cm
                WHERE cm.targetId = :targetId
                    AND cm.timestamp BETWEEN :from AND :to
                GROUP BY (cm.unixMinute - MOD(cm.unixMinute, :divisor)) * 60L
                ORDER BY (cm.unixMinute - MOD(cm.unixMinute, :divisor)) * 60L ASC
                """;

        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("divisor", divisor)
                .getResultList();

        return results.stream()
                .map(row -> Co2MetricDto.builder()
                        .timestamp(Instant.ofEpochSecond(((Number) row[0]).longValue()))
                        .co2(row[1] != null ? ((Number) row[1]).doubleValue() : null)
                        .build())
                .toList();
    }

    public Optional<Co2Metric> findLatest(Long targetId) {
        String jpql = """
                SELECT cm
                FROM Co2Metric cm
                WHERE cm.targetId = :targetId
                ORDER BY cm.timestamp DESC
                """;
        return entityManager.createQuery(jpql, Co2Metric.class)
                .setParameter("targetId", targetId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public Optional<RoomCo2MetricDto> findLatestByRoomId(Long roomId) {
        String jpql = """
                SELECT AVG(cs.currentCO2), MAX(cs.currentCO2)
                FROM Co2Sensor cs
                WHERE cs.room.id = :roomId
                  AND cs.isActive = true
                """;
        Object[] result = entityManager.createQuery(jpql, Object[].class)
                .setParameter("roomId", roomId)
                .getSingleResult();
        if (result[0] == null) return Optional.empty();
        return Optional.of(RoomCo2MetricDto.builder()
                .timestamp(Instant.now())
                .avgCo2(((Number) result[0]).doubleValue())
                .maxCo2(((Number) result[1]).doubleValue())
                .build());
    }

    public List<RoomCo2MetricDto> findHistoryByRoomId(Long roomId, Instant from, Instant to, int divisor) {
        String jpql = """
                SELECT
                    (cm.unixMinute - MOD(cm.unixMinute, :divisor)) * 60L,
                    AVG(cm.co2),
                    MAX(cm.co2)
                FROM Co2Metric cm
                JOIN Co2Sensor cs ON cs.id = cm.targetId
                WHERE cs.room.id = :roomId
                  AND cm.timestamp BETWEEN :from AND :to
                GROUP BY (cm.unixMinute - MOD(cm.unixMinute, :divisor)) * 60L
                ORDER BY (cm.unixMinute - MOD(cm.unixMinute, :divisor)) * 60L ASC
                """;
        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                .setParameter("roomId", roomId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("divisor", divisor)
                .getResultList();
        return results.stream()
                .map(row -> RoomCo2MetricDto.builder()
                        .timestamp(Instant.ofEpochSecond(((Number) row[0]).longValue()))
                        .avgCo2(row[1] != null ? ((Number) row[1]).doubleValue() : null)
                        .maxCo2(row[2] != null ? ((Number) row[2]).doubleValue() : null)
                        .build())
                .toList();
    }
}
