package com.iviet.ivshs.dao;

import com.iviet.ivshs.entities.BaseIoTEntity;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import java.util.Optional;

public abstract class BaseIoTEntityDao<T extends BaseIoTEntity<?>> extends BaseTranslatableEntityDao<T> {

  protected BaseIoTEntityDao(Class<T> clazz) {
    super(clazz);
  }

  @Override
  public Optional<T> findById(Long id) {
    return findOne(
      root -> entityManager.getCriteriaBuilder().equal(root.get("id"), id),
      (root, cq) -> {
        root.fetch("room", JoinType.LEFT);
        root.fetch("deviceControl", JoinType.LEFT).fetch("client", JoinType.LEFT);
      }
    );
  }

  public boolean existsByNaturalId(String naturalId) {
    String jpql = "SELECT COUNT(e) FROM " + clazz.getSimpleName() + " e WHERE e.naturalId = :naturalId";
    Long count = entityManager.createQuery(jpql, Long.class)
            .setParameter("naturalId", naturalId)
            .getSingleResult();
    return count != null && count > 0;
  }

  public boolean existsByNaturalIdAndIdNot(String naturalId, Long id) {
    String jpql = "SELECT COUNT(e) FROM " + clazz.getSimpleName() + " e WHERE e.naturalId = :naturalId AND e.id != :id";
    Long count = entityManager.createQuery(jpql, Long.class)
            .setParameter("naturalId", naturalId)
            .setParameter("id", id)
            .getSingleResult();
    return count != null && count > 0;
  }

  public Optional<T> findByRoomAndNaturalId(Long roomId, String naturalId) {
    return findOne(
      root -> entityManager.getCriteriaBuilder().and(
        entityManager.getCriteriaBuilder().equal(root.get("room").get("id"), roomId),
        entityManager.getCriteriaBuilder().equal(root.get("naturalId"), naturalId)
      ),
      (root, cq) -> {
        root.fetch("room", JoinType.LEFT);
        root.fetch("deviceControl", JoinType.LEFT).fetch("client", JoinType.LEFT);
      }
    );
  }

  public abstract Optional<?> findByRoomAndNaturalId(Long roomId, String naturalId, String langCode);

  public Optional<T> findByNaturalId(String naturalId) {
    return findOne(
      root -> entityManager.getCriteriaBuilder().equal(root.get("naturalId"), naturalId),
      (root, cq) -> {
        root.fetch("room", JoinType.LEFT);
        root.fetch("deviceControl", JoinType.LEFT).fetch("client", JoinType.LEFT);
      }
    );
  }

  public abstract Optional<?> findByNaturalId(String naturalId, String langCode);

  public List<T> findAllByNaturalIds(List<String> naturalIds) {
    if (naturalIds == null || naturalIds.isEmpty()) {
      return List.of();
    }
    return findAll(
      root -> root.get("naturalId").in(naturalIds),
      (root, cq) -> {
        root.fetch("room", JoinType.LEFT);
        root.fetch("deviceControl", JoinType.LEFT).fetch("client", JoinType.LEFT);
        cq.orderBy(entityManager.getCriteriaBuilder().desc(root.get("createdAt")));
      }
    );
  }

  public List<T> findAllByRoomId(Long roomId, int page, int size) {
    return findAll(
      root -> entityManager.getCriteriaBuilder().equal(root.get("room").get("id"), roomId),
      (root, cq) -> {
        root.fetch("room", JoinType.LEFT);
        root.fetch("deviceControl", JoinType.LEFT).fetch("client", JoinType.LEFT);
        cq.orderBy(entityManager.getCriteriaBuilder().desc(root.get("createdAt")));
      },
      page,
      size
    );
  }

  public Optional<T> findByDeviceControlId(Long controlId) {
    return findOne(
      root -> entityManager.getCriteriaBuilder().equal(root.get("deviceControl").get("id"), controlId),
      (root, cq) -> {
        root.fetch("room", JoinType.LEFT);
        root.fetch("deviceControl", JoinType.LEFT).fetch("client", JoinType.LEFT);
      }
    );
  }

  public long countByRoomId(Long roomId) {
    return count(root -> entityManager.getCriteriaBuilder().equal(root.get("room").get("id"), roomId));
  }
}