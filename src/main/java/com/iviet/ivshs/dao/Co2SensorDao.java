package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseIoTSensorDao;
import com.iviet.ivshs.entities.Co2Sensor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class Co2SensorDao extends BaseIoTSensorDao<Co2Sensor> {

    public Co2SensorDao() {
        super(Co2Sensor.class);
    }

    @Override
    public Optional<Co2Sensor> findByNaturalId(String naturalId) {
        return findOne(root ->
            entityManager.getCriteriaBuilder().equal(root.get("naturalId"), naturalId)
        );
    }

    @Override
    public Optional<Co2Sensor> findByNaturalId(String naturalId, String langCode) {
        String jpql = """
                SELECT cs
                FROM Co2Sensor cs
                LEFT JOIN cs.translations tl ON tl.langCode = :langCode
                WHERE cs.naturalId = :naturalId
                """;
        return entityManager.createQuery(jpql, Co2Sensor.class)
                .setParameter("naturalId", naturalId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Co2Sensor> findByRoomAndNaturalId(Long roomId, String naturalId, String langCode) {
        String jpql = """
                SELECT cs
                FROM Co2Sensor cs
                LEFT JOIN cs.translations tl ON tl.langCode = :langCode
                WHERE cs.room.id = :roomId AND cs.naturalId = :naturalId
                """;
        return entityManager.createQuery(jpql, Co2Sensor.class)
                .setParameter("roomId", roomId)
                .setParameter("naturalId", naturalId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
