package com.iviet.ivshs.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class DeviceMetadataDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Long countByRoomId(Long roomId) {
        String jpql = """
                SELECT (SELECT COUNT(l) FROM Light l WHERE l.room.id = :roomId) +
                       (SELECT COUNT(f) FROM Fan f WHERE f.room.id = :roomId) +
                       (SELECT COUNT(ac) FROM AirCondition ac WHERE ac.room.id = :roomId)
                FROM Room r WHERE r.id = :roomId
                """;

        return entityManager.createQuery(jpql, Long.class)
                .setParameter("roomId", roomId)
                .getSingleResult();
    }
}
