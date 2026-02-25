package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.FanDto;
import com.iviet.ivshs.entities.Fan;

@Repository
public class FanDao extends BaseIoTActuatorDao<Fan> {

  public FanDao() {
    super(Fan.class);
  }

  @Override
  public Optional<FanDto> findByNaturalId(String naturalId, String langCode) {
    String jpql = """
        SELECT f FROM Fan f
        LEFT JOIN FETCH f.translations tl
        LEFT JOIN FETCH f.room
        WHERE f.naturalId = :naturalId AND (tl.langCode = :langCode OR tl.langCode IS NULL)
        """;

    return entityManager.createQuery(jpql, Fan.class)
        .setParameter("naturalId", naturalId)
        .setParameter("langCode", langCode)
        .setMaxResults(1)
        .getResultStream()
        .map(this::mapToDtoWithTranslation)
        .findFirst();
  }

  @Override
  public Optional<FanDto> findByRoomAndNaturalId(Long roomId, String naturalId, String langCode) {
    String jpql = """
        SELECT f FROM Fan f
        LEFT JOIN FETCH f.translations tl
        LEFT JOIN FETCH f.room r
        WHERE r.id = :roomId AND f.naturalId = :naturalId AND (tl.langCode = :langCode OR tl.langCode IS NULL)
        """;

    return entityManager.createQuery(jpql, Fan.class)
        .setParameter("roomId", roomId)
        .setParameter("naturalId", naturalId)
        .setParameter("langCode", langCode)
        .setMaxResults(1)
        .getResultStream()
        .map(this::mapToDtoWithTranslation)
        .findFirst();
  }

  public Optional<FanDto> findById(Long id, String langCode) {
    String jpql = """
        SELECT f FROM Fan f
        LEFT JOIN FETCH f.translations tl
        LEFT JOIN FETCH f.room
        WHERE f.id = :id AND (tl.langCode = :langCode OR tl.langCode IS NULL)
        """;

    return entityManager.createQuery(jpql, Fan.class)
        .setParameter("id", id)
        .setParameter("langCode", langCode)
        .setMaxResults(1)
        .getResultStream()
        .map(this::mapToDtoWithTranslation)
        .findFirst();
  }

  public List<FanDto> findAll(int page, int size, String langCode) {
    String jpql = """
        SELECT f FROM Fan f
        LEFT JOIN FETCH f.translations tl
        LEFT JOIN FETCH f.room
        WHERE (tl.langCode = :langCode OR tl.langCode IS NULL)
        ORDER BY f.id ASC
        """;

    return entityManager.createQuery(jpql, Fan.class)
        .setParameter("langCode", langCode)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultStream()
        .map(this::mapToDtoWithTranslation)
        .toList();
  }

  public List<FanDto> findAll(String langCode) {
    String jpql = """
        SELECT f FROM Fan f
        LEFT JOIN FETCH f.translations tl
        LEFT JOIN FETCH f.room
        WHERE (tl.langCode = :langCode OR tl.langCode IS NULL)
        ORDER BY f.id ASC
        """;

    return entityManager.createQuery(jpql, Fan.class)
        .setParameter("langCode", langCode)
        .getResultStream()
        .map(this::mapToDtoWithTranslation)
        .toList();
  }

  public List<FanDto> findAllByRoomId(Long roomId, int page, int size, String langCode) {
    String jpql = """
        SELECT f FROM Fan f
        LEFT JOIN FETCH f.translations tl
        LEFT JOIN FETCH f.room
        WHERE f.room.id = :roomId AND (tl.langCode = :langCode OR tl.langCode IS NULL)
        ORDER BY f.id ASC
        """;

    return entityManager.createQuery(jpql, Fan.class)
        .setParameter("roomId", roomId)
        .setParameter("langCode", langCode)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultStream()
        .map(this::mapToDtoWithTranslation)
        .toList();
  }

  public List<FanDto> findAllByRoomId(Long roomId, String langCode) {
    String jpql = """
        SELECT f FROM Fan f
        LEFT JOIN FETCH f.translations tl
        LEFT JOIN FETCH f.room
        WHERE f.room.id = :roomId AND (tl.langCode = :langCode OR tl.langCode IS NULL)
        ORDER BY f.id ASC
        """;

    return entityManager.createQuery(jpql, Fan.class)
        .setParameter("roomId", roomId)
        .setParameter("langCode", langCode)
        .getResultStream()
        .map(this::mapToDtoWithTranslation)
        .toList();
  }

  private FanDto mapToDtoWithTranslation(Fan fan) {
    FanDto dto = FanDto.from(fan);
    if (fan.getTranslations() != null && !fan.getTranslations().isEmpty()) {
      var translation = fan.getTranslations().iterator().next();
      return new FanDto(
          dto.id(), dto.naturalId(), translation.getName(), translation.getDescription(),
          dto.isActive(), dto.roomId(), dto.power(), dto.type(), dto.speed(), dto.mode(), dto.light(), dto.swing()
      );
    }
    return dto;
  }
}
