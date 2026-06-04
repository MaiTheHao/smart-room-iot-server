package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseEntityDao;
import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.entities.EnergyMetric;
import com.iviet.ivshs.shared.enumeration.EnergyMetricCategory;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class EnergyMetricDao extends BaseEntityDao<EnergyMetric> {

    public EnergyMetricDao() {
        super(EnergyMetric.class);
    }

    @Override
    @Transactional
    public EnergyMetric save(EnergyMetric entity) {
        return super.save(entity);
    }

    @Override
    public List<EnergyMetric> save(List<EnergyMetric> entities) {
        String sql = """
                INSERT INTO energy_metrics
                (target_category, target_id, timestamp, unix_minute, voltage, current, power, energy, frequency, power_factor)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NonNull PreparedStatement ps, int i) throws java.sql.SQLException {
                    EnergyMetric e = entities.get(i);
                    ps.setString(1, e.getTargetCategory());
                    ps.setLong(2, e.getTargetId());
                    ps.setObject(3, e.getTimestamp());
                    ps.setObject(4, e.getUnixMinute());
                    ps.setObject(5, e.getVoltage());
                    ps.setObject(6, e.getCurrent());
                    ps.setObject(7, e.getPower());
                    ps.setObject(8, e.getEnergy());
                    ps.setObject(9, e.getFrequency());
                    ps.setObject(10, e.getPowerFactor());
                }

                @Override
                public int getBatchSize() {
                    return entities.size();
                }
            });
            return entities;
        } catch (Exception e) {
            throw new RuntimeException("Failed to batch insert EnergyMetric entities", e);
        }
    }

    public List<EnergyMetricDto> findHistory(EnergyMetricCategory category, Long targetId, Instant from, Instant to,
            int divisor) {
        String jpql = """
                SELECT
                    (e.unixMinute - MOD(e.unixMinute, :divisor)) * 60L,
                    AVG(e.voltage),
                    AVG(e.current),
                    AVG(e.power),
                    MAX(e.energy),
                    AVG(e.frequency),
                    AVG(e.powerFactor)
                FROM EnergyMetric e
                WHERE e.targetCategory = :category
                    AND e.targetId = :targetId
                    AND e.timestamp BETWEEN :from AND :to
                GROUP BY (e.unixMinute - MOD(e.unixMinute, :divisor)) * 60L
                ORDER BY (e.unixMinute - MOD(e.unixMinute, :divisor)) * 60L ASC
                """;

        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                .setParameter("category", category.name())
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("divisor", divisor)
                .getResultList();

        return results.stream()
                .map(row -> EnergyMetricDto.builder()
                        .timestamp(Instant.ofEpochSecond(((Number) row[0]).longValue()))
                        .voltage(row[1] != null ? ((Number) row[1]).doubleValue() : null)
                        .current(row[2] != null ? ((Number) row[2]).doubleValue() : null)
                        .power(row[3] != null ? ((Number) row[3]).doubleValue() : null)
                        .energy(row[4] != null ? ((Number) row[4]).doubleValue() : null)
                        .frequency(row[5] != null ? ((Number) row[5]).doubleValue() : null)
                        .powerFactor(row[6] != null ? ((Number) row[6]).doubleValue() : null)
                        .build())
                .toList();
    }

    public Optional<EnergyMetricDto> findLatest(EnergyMetricCategory category, Long targetId) {
        String jpql = """
                SELECT new com.iviet.ivshs.dto.EnergyMetricDto(
                    em.timestamp, em.voltage, em.current, em.power,
                    em.energy, em.frequency, em.powerFactor
                )
                FROM EnergyMetric em
                WHERE em.targetCategory = :category
                    AND em.targetId = :targetId
                ORDER BY em.timestamp DESC
                """;

        return entityManager.createQuery(jpql, EnergyMetricDto.class)
                .setParameter("category", category.name())
                .setParameter("targetId", targetId)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }
}
