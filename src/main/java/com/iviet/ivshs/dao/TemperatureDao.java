package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.TemperatureDto;
import com.iviet.ivshs.entities.Temperature;

@Repository
public class TemperatureDao extends BaseIoTDeviceDao<Temperature> {

    private final String DTO_CLASS = TemperatureDto.class.getName();

    public TemperatureDao() {
        super(Temperature.class);
    }

    public Optional<TemperatureDto> findById(Long temperatureId, String langCode) {
        String jpql = """
                SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentValue, t.naturalId, t.room.id)
                FROM Temperature t 
                LEFT JOIN t.translations tl ON tl.langCode = :langCode 
                WHERE t.id = :temperatureId
                """.formatted(DTO_CLASS);

        return entityManager.createQuery(jpql, TemperatureDto.class)
                .setParameter("temperatureId", temperatureId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public List<TemperatureDto> findAllByRoomId(Long roomId, int page, int size, String langCode) {
        String jpql = """
                SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentValue, t.naturalId, t.room.id)
                FROM Temperature t 
                LEFT JOIN t.translations tl ON tl.langCode = :langCode 
                WHERE t.room.id = :roomId 
                ORDER BY t.id ASC
                """.formatted(DTO_CLASS);

        return entityManager.createQuery(jpql, TemperatureDto.class)
                .setParameter("roomId", roomId)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public Optional<TemperatureDto> findByNaturalId(String naturalId, String langCode) {
        String jpql = """
                SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentValue, t.naturalId, t.room.id)
                FROM Temperature t 
                LEFT JOIN t.translations tl ON tl.langCode = :langCode 
                WHERE t.naturalId = :naturalId
                """.formatted(DTO_CLASS);
        return entityManager.createQuery(jpql, TemperatureDto.class)
                .setParameter("naturalId", naturalId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
