package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseEntityDao;
import com.iviet.ivshs.dto.HumidityMetricDto;
import com.iviet.ivshs.entities.HumidityMetric;
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
public class HumidityMetricDao extends BaseEntityDao<HumidityMetric> {

    public HumidityMetricDao() {
        super(HumidityMetric.class);
    }

    @Override
    @Transactional
    public List<HumidityMetric> save(List<HumidityMetric> entities) {
        String sql = """
                INSERT INTO humidity_metrics
                (target_category, target_id, timestamp, unix_minute, humidity)
                VALUES (?, ?, ?, ?, ?)
                """;
        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                    HumidityMetric e = entities.get(i);
                    ps.setString(1, e.getTargetCategory());
                    ps.setLong(2, e.getTargetId());
                    ps.setObject(3, e.getTimestamp());
                    ps.setObject(4, e.getUnixMinute());
                    ps.setDouble(5, e.getHumidity());
                }

                @Override
                public int getBatchSize() {
                    return entities.size();
                }
            });
            return entities;
        } catch (Exception e) {
            throw new RuntimeException("Failed to batch insert HumidityMetric entities", e);
        }
    }

    public List<HumidityMetric> findHistory(Long targetId, Instant from, Instant to) {
        String jpql = """
                SELECT hm
                FROM HumidityMetric hm
                WHERE hm.targetId = :targetId
                  AND hm.timestamp BETWEEN :from AND :to
                ORDER BY hm.timestamp ASC
                """;
        return entityManager.createQuery(jpql, HumidityMetric.class)
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    public List<HumidityMetricDto> findHistory(Long targetId, Instant from, Instant to, int divisor) {
        String jpql = """
                SELECT
                    (hm.unixMinute - MOD(hm.unixMinute, :divisor)) * 60L,
                    AVG(hm.humidity)
                FROM HumidityMetric hm
                WHERE hm.targetId = :targetId
                    AND hm.timestamp BETWEEN :from AND :to
                GROUP BY (hm.unixMinute - MOD(hm.unixMinute, :divisor)) * 60L
                ORDER BY (hm.unixMinute - MOD(hm.unixMinute, :divisor)) * 60L ASC
                """;

        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("divisor", divisor)
                .getResultList();

        return results.stream()
                .map(row -> HumidityMetricDto.builder()
                        .timestamp(Instant.ofEpochSecond(((Number) row[0]).longValue()))
                        .humidity(row[1] != null ? ((Number) row[1]).doubleValue() : null)
                        .build())
                .toList();
    }

    public Optional<HumidityMetric> findLatest(Long targetId) {
        String jpql = """
                SELECT hm
                FROM HumidityMetric hm
                WHERE hm.targetId = :targetId
                ORDER BY hm.timestamp DESC
                """;
        return entityManager.createQuery(jpql, HumidityMetric.class)
                .setParameter("targetId", targetId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    /**
     * [ROOM – getLatest] Lấy danh sách currentHumidity của tất cả HumiditySensor active trong phòng.
     * Median sẽ được tính ở tầng Service.
     */
    public List<Double> findCurrentValuesByRoomId(Long roomId) {
        String jpql = """
                SELECT hs.currentHumidity
                FROM HumiditySensor hs
                WHERE hs.room.id = :roomId
                  AND hs.isActive = true
                  AND hs.currentHumidity IS NOT NULL
                """;
        return entityManager.createQuery(jpql, Double.class)
                .setParameter("roomId", roomId)
                .getResultList();
    }

    /**
     * [ROOM – getHistory] Raw (bucket, humidity) per reading từ tất cả sensor trong phòng.
     * Median per bucket sẽ được tính ở tầng Service.
     * Trả về Object[] { Long bucket (unix seconds), Double humidity }
     */
    public List<Object[]> findRawHistoryByRoomId(Long roomId, Instant from, Instant to, int divisor) {
        String jpql = """
                SELECT
                    (hm.unixMinute - MOD(hm.unixMinute, :divisor)) * 60L,
                    hm.humidity
                FROM HumidityMetric hm
                JOIN HumiditySensor hs ON hs.id = hm.targetId
                WHERE hs.room.id = :roomId
                  AND hm.timestamp BETWEEN :from AND :to
                ORDER BY (hm.unixMinute - MOD(hm.unixMinute, :divisor)) * 60L ASC
                """;
        return entityManager.createQuery(jpql, Object[].class)
                .setParameter("roomId", roomId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("divisor", divisor)
                .getResultList();
    }
}
