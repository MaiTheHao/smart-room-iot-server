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

    public List<EnergyMetricDto> findHistory(EnergyMetricCategory category, Long targetId, Instant from, Instant to) {
        String jpql = """
            SELECT new com.iviet.ivshs.dto.EnergyMetricDto(
                em.timestamp, em.voltage, em.current, em.power,
                em.energy, em.frequency, em.powerFactor
            )
            FROM EnergyMetric em
            WHERE em.category = :category
              AND em.targetId = :targetId
              AND em.timestamp >= :from
              AND em.timestamp <= :to
            ORDER BY em.timestamp ASC
            """;

        return entityManager.createQuery(jpql, EnergyMetricDto.class)
            .setParameter("category", category)
            .setParameter("targetId", targetId)
            .setParameter("from", from)
            .setParameter("to", to)
            .getResultList();
    }

    public Optional<EnergyMetricDto> findNewest(EnergyMetricCategory category, Long targetId) {
        String jpql = """
            SELECT new com.iviet.ivshs.dto.EnergyMetricDto(
                em.timestamp, em.voltage, em.current, em.power,
                em.energy, em.frequency, em.powerFactor
            )
            FROM EnergyMetric em
            WHERE em.category = :category
              AND em.targetId = :targetId
            ORDER BY em.timestamp DESC
            """;

        return entityManager.createQuery(jpql, EnergyMetricDto.class)
            .setParameter("category", category)
            .setParameter("targetId", targetId)
            .setMaxResults(1)
            .getResultStream()
            .findFirst();
    }
}
