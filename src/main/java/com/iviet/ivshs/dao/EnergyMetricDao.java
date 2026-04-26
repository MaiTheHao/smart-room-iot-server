package com.iviet.ivshs.dao;

import com.iviet.ivshs.dto.EnergyMetricDto;
import com.iviet.ivshs.entities.EnergyMetric;
import com.iviet.ivshs.enumeration.EnergyMetricCategory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    public List<EnergyMetricDto> findHistory(EnergyMetricCategory category, Long targetId, Instant from, Instant to, int divisor) {
        String sql = """
            SELECT
                (unix_minute DIV :divisor) * :divisor * 60 as unix_seconds,
                AVG(voltage) as avg_voltage,
                AVG(current) as avg_current,
                AVG(power) as avg_power,
                MAX(energy) as max_energy,
                AVG(frequency) as avg_frequency,
                AVG(power_factor) as avg_power_factor
            FROM energy_metrics
            WHERE target_category = :category
              AND target_id = :targetId
              AND timestamp BETWEEN :from AND :to
            GROUP BY unix_seconds
            ORDER BY unix_seconds ASC
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createNativeQuery(sql)
            .setParameter("category", category.name())
            .setParameter("targetId", targetId)
            .setParameter("from", java.sql.Timestamp.from(from))
            .setParameter("to", java.sql.Timestamp.from(to))
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
            .setParameter("category", category)
            .setParameter("targetId", targetId)
            .setMaxResults(1)
            .getResultList()
            .stream()
            .findFirst();
    }
}
