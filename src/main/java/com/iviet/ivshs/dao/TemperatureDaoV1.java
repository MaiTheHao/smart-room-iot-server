package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.TemperatureDtoV1;
import com.iviet.ivshs.entities.TemperatureV1;

@Repository
public class TemperatureDaoV1 extends IoTDeviceEntityDaoV1<TemperatureV1> {

    public TemperatureDaoV1() {
        super(TemperatureV1.class);
    }

    public Optional<TemperatureDtoV1> findById(Long temperatureId, String langCode) {
        String dtoPath = TemperatureDtoV1.class.getName();
        String jpql = """
                SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentValue, t.naturalId, t.room.id)
                FROM TemperatureV1 t 
                LEFT JOIN t.sensorLans tl ON tl.langCode = :langCode 
                WHERE t.id = :temperatureId
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, TemperatureDtoV1.class)
                .setParameter("temperatureId", temperatureId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public List<TemperatureDtoV1> findAllByRoomId(Long roomId, int page, int size, String langCode) {
        String dtoPath = TemperatureDtoV1.class.getName();
        String jpql = """
                SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentValue, t.naturalId, t.room.id)
                FROM TemperatureV1 t 
                LEFT JOIN t.sensorLans tl ON tl.langCode = :langCode 
                WHERE t.room.id = :roomId 
                ORDER BY t.id ASC
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, TemperatureDtoV1.class)
                .setParameter("roomId", roomId)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }
}
