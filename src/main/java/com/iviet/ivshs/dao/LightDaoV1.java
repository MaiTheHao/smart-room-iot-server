package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.LightDtoV1;
import com.iviet.ivshs.entities.LightV1;

@Repository
public class LightDaoV1 extends BaseIoTDeviceDaoV1<LightV1> {
    
    public LightDaoV1() {
        super(LightV1.class);
    }

    public Optional<LightDtoV1> findById(Long lightId, String langCode) {
        String dtoPath = LightDtoV1.class.getName();

        String jpql = """
                SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.level, l.room.id)
                FROM LightV1 l 
                LEFT JOIN l.lightLans ll ON ll.langCode = :langCode 
                WHERE l.id = :lightId
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, LightDtoV1.class)
                .setParameter("lightId", lightId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public List<LightDtoV1> findAll(int page, int size, String langCode) {
        String dtoPath = LightDtoV1.class.getName();

        String jpql = """
                SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.level, l.room.id)
                FROM LightV1 l 
                LEFT JOIN l.lightLans ll ON ll.langCode = :langCode 
                ORDER BY l.id ASC
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, LightDtoV1.class)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public List<LightDtoV1> findAllByRoomId(Long roomId, int page, int size, String langCode) {
        String dtoPath = LightDtoV1.class.getName();

        String jpql = """
                SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.level, l.room.id)
                FROM LightV1 l 
                LEFT JOIN l.lightLans ll ON ll.langCode = :langCode 
                WHERE l.room.id = :roomId 
                ORDER BY l.id ASC
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, LightDtoV1.class)
                .setParameter("roomId", roomId)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }
}
