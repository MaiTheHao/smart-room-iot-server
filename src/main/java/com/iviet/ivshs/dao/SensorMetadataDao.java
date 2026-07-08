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
                   (SELECT COUNT(pc) FROM PowerConsumption pc WHERE pc.room.id = :roomId)
            FROM Room r WHERE r.id = :roomId
            """;
        return entityManager.createQuery(jpql, Long.class)
            .setParameter("roomId", roomId)
            .getSingleResult();
    }
}
