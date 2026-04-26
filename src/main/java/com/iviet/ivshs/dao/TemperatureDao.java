package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.TemperatureDto;
import com.iviet.ivshs.entities.Temperature;

@Repository
public class TemperatureDao extends BaseIoTSensorDao<Temperature> {

  private static final String DTO_CLASS = TemperatureDto.class.getName();

  public TemperatureDao() {
    super(Temperature.class);
  }

  public Optional<TemperatureDto> findById(Long id, String langCode) {
    String jpql = """
        SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentValue, t.naturalId, t.room.id, t.hardwareConfig.id)
        FROM Temperature t
        LEFT JOIN t.translations tl ON tl.langCode = :langCode
        WHERE t.id = :id
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, TemperatureDto.class)
        .setParameter("id", id)
        .setParameter("langCode", langCode)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }

  public List<TemperatureDto> findAll(int page, int size, String langCode) {
    String jpql = """
        SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentValue, t.naturalId, t.room.id, t.hardwareConfig.id)
        FROM Temperature t
        LEFT JOIN t.translations tl ON tl.langCode = :langCode
        ORDER BY t.id ASC
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, TemperatureDto.class)
        .setParameter("langCode", langCode)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
  }

  public List<TemperatureDto> findAll(String langCode) {
    String jpql = """
        SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentValue, t.naturalId, t.room.id, t.hardwareConfig.id)
        FROM Temperature t
        LEFT JOIN t.translations tl ON tl.langCode = :langCode
        ORDER BY t.id ASC
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, TemperatureDto.class)
        .setParameter("langCode", langCode)
        .getResultList();
  }

  public List<TemperatureDto> findAllByRoomId(Long roomId, int page, int size, String langCode) {
    String jpql = """
        SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentValue, t.naturalId, t.room.id, t.hardwareConfig.id)
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

  public List<TemperatureDto> findAllByRoomId(Long roomId, String langCode) {
    String jpql = """
        SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentValue, t.naturalId, t.room.id, t.hardwareConfig.id)
        FROM Temperature t
        LEFT JOIN t.translations tl ON tl.langCode = :langCode
        WHERE t.room.id = :roomId
        ORDER BY t.id ASC
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, TemperatureDto.class)
        .setParameter("roomId", roomId)
        .setParameter("langCode", langCode)
        .getResultList();
  }

  @Override
  public Optional<TemperatureDto> findByNaturalId(String naturalId, String langCode) {
    String jpql = """
        SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentValue, t.naturalId, t.room.id, t.hardwareConfig.id)
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

  @Override
  public Optional<TemperatureDto> findByRoomAndNaturalId(Long roomId, String naturalId, String langCode) {
    String jpql = """
        SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentValue, t.naturalId, t.room.id, t.hardwareConfig.id)
        FROM Temperature t
        LEFT JOIN t.translations tl ON tl.langCode = :langCode
        WHERE t.room.id = :roomId AND t.naturalId = :naturalId
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, TemperatureDto.class)
        .setParameter("roomId", roomId)
        .setParameter("naturalId", naturalId)
        .setParameter("langCode", langCode)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }
}
