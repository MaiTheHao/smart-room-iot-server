package com.iviet.ivshs.dao;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class SensorMetadataDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Long countByRoomId(Long roomId) {
        String jpql = """
            SELECT (SELECT COUNT(t) FROM Temperature t WHERE t.room.id = :roomId) +
                   (SELECT COUNT(pc) FROM PowerConsumption pc WHERE pc.room.id = :roomId) +
                   (SELECT COUNT(hs) FROM HumiditySensor hs WHERE hs.room.id = :roomId) +
                   (SELECT COUNT(cs) FROM Co2Sensor cs WHERE cs.room.id = :roomId) +
                   (SELECT COUNT(ls) FROM LuxSensor ls WHERE ls.room.id = :roomId)
            FROM Room r WHERE r.id = :roomId
            """;
        return entityManager.createQuery(jpql, Long.class)
            .setParameter("roomId", roomId)
            .getSingleResult();
    }
}
