package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.JoinType;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.FanDto;
import com.iviet.ivshs.entities.Fan;

@Repository
public class FanDao extends BaseIoTActuatorDao<Fan> {

  private static final String DTO_CLASS = FanDto.class.getName();

  public FanDao() {
    super(Fan.class);
  }

  @Override
  public Optional<FanDto> findByNaturalId(String naturalId, String langCode) {
    String jpql = """
        SELECT new %s(
            f.id, f.naturalId, tl.name, tl.description, f.isActive, f.room.id,
            f.power, f.type, f.speed, f.mode, f.light, f.swing, f.deviceControl.id
        )
        FROM Fan f
        LEFT JOIN f.translations tl ON tl.langCode = :langCode
        WHERE f.naturalId = :naturalId
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, FanDto.class)
        .setParameter("naturalId", naturalId)
        .setParameter("langCode", langCode)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }

  @Override
  public Optional<FanDto> findByRoomAndNaturalId(Long roomId, String naturalId, String langCode) {
    String jpql = """
        SELECT new %s(
            f.id, f.naturalId, tl.name, tl.description, f.isActive, f.room.id,
            f.power, f.type, f.speed, f.mode, f.light, f.swing, f.deviceControl.id
        )
        FROM Fan f
        LEFT JOIN f.translations tl ON tl.langCode = :langCode
        WHERE f.room.id = :roomId AND f.naturalId = :naturalId
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, FanDto.class)
        .setParameter("roomId", roomId)
        .setParameter("naturalId", naturalId)
        .setParameter("langCode", langCode)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }

  public Optional<FanDto> findById(Long id, String langCode) {
    String jpql = """
        SELECT new %s(
            f.id, f.naturalId, tl.name, tl.description, f.isActive, f.room.id,
            f.power, f.type, f.speed, f.mode, f.light, f.swing, f.deviceControl.id
        )
        FROM Fan f
        LEFT JOIN f.translations tl ON tl.langCode = :langCode
        WHERE f.id = :id
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, FanDto.class)
        .setParameter("id", id)
        .setParameter("langCode", langCode)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }

  public List<FanDto> findAll(int page, int size, String langCode) {
    String jpql = """
        SELECT new %s(
            f.id, f.naturalId, tl.name, tl.description, f.isActive, f.room.id,
            f.power, f.type, f.speed, f.mode, f.light, f.swing, f.deviceControl.id
        )
        FROM Fan f
        LEFT JOIN f.translations tl ON tl.langCode = :langCode
        ORDER BY f.id ASC
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, FanDto.class)
        .setParameter("langCode", langCode)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
  }

  public List<FanDto> findAll(String langCode) {
    String jpql = """
        SELECT new %s(
            f.id, f.naturalId, tl.name, tl.description, f.isActive, f.room.id,
            f.power, f.type, f.speed, f.mode, f.light, f.swing, f.deviceControl.id
        )
        FROM Fan f
        LEFT JOIN f.translations tl ON tl.langCode = :langCode
        ORDER BY f.id ASC
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, FanDto.class)
        .setParameter("langCode", langCode)
        .getResultList();
  }

  public List<FanDto> findAllByRoomId(Long roomId, int page, int size, String langCode) {
    String jpql = """
        SELECT new %s(
            f.id, f.naturalId, tl.name, tl.description, f.isActive, f.room.id,
            f.power, f.type, f.speed, f.mode, f.light, f.swing, f.deviceControl.id
        )
        FROM Fan f
        LEFT JOIN f.translations tl ON tl.langCode = :langCode
        WHERE f.room.id = :roomId
        ORDER BY f.id ASC
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, FanDto.class)
        .setParameter("roomId", roomId)
        .setParameter("langCode", langCode)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
  }

  public List<FanDto> findAllByRoomId(Long roomId, String langCode) {
    String jpql = """
        SELECT new %s(
            f.id, f.naturalId, tl.name, tl.description, f.isActive, f.room.id,
            f.power, f.type, f.speed, f.mode, f.light, f.swing, f.deviceControl.id
        )
        FROM Fan f
        LEFT JOIN f.translations tl ON tl.langCode = :langCode
        WHERE f.room.id = :roomId
        ORDER BY f.id ASC
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, FanDto.class)
        .setParameter("roomId", roomId)
        .setParameter("langCode", langCode)
        .getResultList();
  }

  /**
   * Fetch all active Fan entities for a given gateway (client).
   * Used by energy metric collection job to iterate per-gateway devices.
   */
  public List<Fan> findAllActiveByClientId(Long clientId) {
    return findAll(
      root -> entityManager.getCriteriaBuilder().and(
        entityManager.getCriteriaBuilder().equal(root.get("deviceControl").get("client").get("id"), clientId),
        entityManager.getCriteriaBuilder().isTrue(root.get("isActive"))
      ),
      (root, cq) -> {
        root.fetch("deviceControl", JoinType.LEFT).fetch("client", JoinType.LEFT);
        root.fetch("room", JoinType.LEFT);
      }
    );
  }
}
