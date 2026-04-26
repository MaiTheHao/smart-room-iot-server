package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.PowerConsumptionDto;
import com.iviet.ivshs.entities.PowerConsumption;

import jakarta.persistence.criteria.JoinType;

@Repository
public class PowerConsumptionDao extends BaseIoTSensorDao<PowerConsumption> {

  private static final String DTO_CLASS = PowerConsumptionDto.class.getName();

  public PowerConsumptionDao() {
    super(PowerConsumption.class);
  }

  @Override
  public Optional<PowerConsumptionDto> findByNaturalId(String naturalId, String langCode) {
    String jpql = """
        SELECT new %s(pc.id, pcl.name, pcl.description, pc.isActive, pc.currentWatt, pc.naturalId, pc.room.id, pc.hardwareConfig.id)
        FROM PowerConsumption pc
        LEFT JOIN pc.translations pcl ON pcl.langCode = :langCode
        WHERE pc.naturalId = :naturalId
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, PowerConsumptionDto.class)
        .setParameter("naturalId", naturalId)
        .setParameter("langCode", langCode)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }

  public Optional<PowerConsumptionDto> findById(Long id, String langCode) {
    String jpql = """
        SELECT new %s(pc.id, pcl.name, pcl.description, pc.isActive, pc.currentWatt, pc.naturalId, pc.room.id, pc.hardwareConfig.id)
        FROM PowerConsumption pc
        LEFT JOIN pc.translations pcl ON pcl.langCode = :langCode
        WHERE pc.id = :id
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, PowerConsumptionDto.class)
        .setParameter("id", id)
        .setParameter("langCode", langCode)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }

  public List<PowerConsumptionDto> findAll(int page, int size, String langCode) {
    String jpql = """
        SELECT new %s(pc.id, pcl.name, pcl.description, pc.isActive, pc.currentWatt, pc.naturalId, pc.room.id, pc.hardwareConfig.id)
        FROM PowerConsumption pc
        LEFT JOIN pc.translations pcl ON pcl.langCode = :langCode
        ORDER BY pc.createdAt DESC
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, PowerConsumptionDto.class)
        .setParameter("langCode", langCode)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
  }

  public List<PowerConsumptionDto> findAll(String langCode) {
    String jpql = """
        SELECT new %s(pc.id, pcl.name, pcl.description, pc.isActive, pc.currentWatt, pc.naturalId, pc.room.id, pc.hardwareConfig.id)
        FROM PowerConsumption pc
        LEFT JOIN pc.translations pcl ON pcl.langCode = :langCode
        ORDER BY pc.createdAt DESC
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, PowerConsumptionDto.class)
        .setParameter("langCode", langCode)
        .getResultList();
  }

  public List<PowerConsumptionDto> findAllByRoomId(Long roomId, int page, int size, String langCode) {
    String jpql = """
        SELECT new %s(pc.id, pcl.name, pcl.description, pc.isActive, pc.currentWatt, pc.naturalId, pc.room.id, pc.hardwareConfig.id)
        FROM PowerConsumption pc
        LEFT JOIN pc.translations pcl ON pcl.langCode = :langCode
        WHERE pc.room.id = :roomId
        ORDER BY pc.createdAt DESC
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, PowerConsumptionDto.class)
        .setParameter("roomId", roomId)
        .setParameter("langCode", langCode)
        .setFirstResult(page * size)
        .setMaxResults(size)
        .getResultList();
  }

  public List<PowerConsumptionDto> findAllByRoomId(Long roomId, String langCode) {
    String jpql = """
        SELECT new %s(pc.id, pcl.name, pcl.description, pc.isActive, pc.currentWatt, pc.naturalId, pc.room.id, pc.hardwareConfig.id)
        FROM PowerConsumption pc
        LEFT JOIN pc.translations pcl ON pcl.langCode = :langCode
        WHERE pc.room.id = :roomId
        ORDER BY pc.createdAt DESC
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, PowerConsumptionDto.class)
        .setParameter("roomId", roomId)
        .setParameter("langCode", langCode)
        .getResultList();
  }

  @Override
  public Optional<PowerConsumptionDto> findByRoomAndNaturalId(Long roomId, String naturalId, String langCode) {
    String jpql = """
        SELECT new %s(pc.id, pcl.name, pcl.description, pc.isActive, pc.currentWatt, pc.naturalId, pc.room.id, pc.hardwareConfig.id)
        FROM PowerConsumption pc
        LEFT JOIN pc.translations pcl ON pcl.langCode = :langCode
        WHERE pc.room.id = :roomId AND pc.naturalId = :naturalId
        """.formatted(DTO_CLASS);

    return entityManager.createQuery(jpql, PowerConsumptionDto.class)
        .setParameter("roomId", roomId)
        .setParameter("naturalId", naturalId)
        .setParameter("langCode", langCode)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }

  /**
   * Fetch all active PowerConsumption entities for a given gateway (client).
   * Used by energy metric collection job to iterate per-gateway devices.
   */
  public List<PowerConsumption> findAllActiveByClientId(Long clientId) {
    return findAll(
      root -> entityManager.getCriteriaBuilder().and(
        entityManager.getCriteriaBuilder().equal(root.get("hardwareConfig").get("client").get("id"), clientId),
        entityManager.getCriteriaBuilder().isTrue(root.get("isActive"))
      ),
      (root, cq) -> {
        root.fetch("hardwareConfig", JoinType.LEFT).fetch("client", JoinType.LEFT);
        root.fetch("room", JoinType.LEFT);
      }
    );
  }
}
