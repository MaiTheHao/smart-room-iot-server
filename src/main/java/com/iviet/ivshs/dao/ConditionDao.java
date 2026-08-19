package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ConditionDao extends BaseAuditEntityDao<Condition> {

  public ConditionDao() {
    super(Condition.class);
  }

  public List<Condition> findByOwner(ConditionOwnerCategory ownerCategory, String ownerId) {
    String jpql = """
        SELECT c FROM Condition c
        WHERE c.ownerCategory = :ownerCategory AND c.ownerId = :ownerId
        ORDER BY c.sortOrder ASC
        """;
    return entityManager
        .createQuery(jpql, Condition.class)
        .setParameter("ownerCategory", ownerCategory)
        .setParameter("ownerId", ownerId)
        .getResultList();
  }

  public List<Condition> findByOwner(ConditionOwnerCategory ownerCategory, Long ownerId) {
    return findByOwner(ownerCategory, String.valueOf(ownerId));
  }

  public long countByOwner(ConditionOwnerCategory ownerCategory, String ownerId) {
    String jpql =
        "SELECT COUNT(c) FROM Condition c WHERE c.ownerCategory = :ownerCategory AND c.ownerId ="
            + " :ownerId";
    return entityManager
        .createQuery(jpql, Long.class)
        .setParameter("ownerCategory", ownerCategory)
        .setParameter("ownerId", ownerId)
        .getSingleResult();
  }

  public long countByOwner(ConditionOwnerCategory ownerCategory, Long ownerId) {
    return countByOwner(ownerCategory, String.valueOf(ownerId));
  }

  public boolean existsByOwner(ConditionOwnerCategory ownerCategory, String ownerId) {
    return countByOwner(ownerCategory, ownerId) > 0;
  }

  public List<Condition> findBySource(ConditionDataSource sourceCategory, String sourceTargetId) {
    String jpql = """
        SELECT c FROM Condition c
        WHERE c.sourceCategory = :sourceCategory AND c.sourceTargetId = :sourceTargetId
        ORDER BY c.sortOrder ASC
        """;
    return entityManager
        .createQuery(jpql, Condition.class)
        .setParameter("sourceCategory", sourceCategory)
        .setParameter("sourceTargetId", sourceTargetId)
        .getResultList();
  }

  public List<Condition> findBySource(ConditionDataSource sourceCategory, Long sourceTargetId) {
    return findBySource(sourceCategory, String.valueOf(sourceTargetId));
  }

  public List<Condition> findBySourceAndType(
      ConditionDataSource sourceCategory, String sourceTargetId, DeviceCategory sourceTargetType) {
    String jpql = """
        SELECT c FROM Condition c
        WHERE c.sourceCategory = :sourceCategory
          AND c.sourceTargetId = :sourceTargetId
          AND c.sourceTargetType = :sourceTargetType
        ORDER BY c.sortOrder ASC
        """;
    return entityManager
        .createQuery(jpql, Condition.class)
        .setParameter("sourceCategory", sourceCategory)
        .setParameter("sourceTargetId", sourceTargetId)
        .setParameter("sourceTargetType", sourceTargetType)
        .getResultList();
  }

  public List<Condition> findBySourceAndType(
      ConditionDataSource sourceCategory, Long sourceTargetId, DeviceCategory sourceTargetType) {
    return findBySourceAndType(sourceCategory, String.valueOf(sourceTargetId), sourceTargetType);
  }

  public long countBySource(ConditionDataSource sourceCategory, String sourceTargetId) {
    String jpql = "SELECT COUNT(c) FROM Condition c WHERE c.sourceCategory = :sourceCategory AND"
        + " c.sourceTargetId = :sourceTargetId";
    return entityManager
        .createQuery(jpql, Long.class)
        .setParameter("sourceCategory", sourceCategory)
        .setParameter("sourceTargetId", sourceTargetId)
        .getSingleResult();
  }

  public long countBySource(ConditionDataSource sourceCategory, Long sourceTargetId) {
    return countBySource(sourceCategory, String.valueOf(sourceTargetId));
  }

  public int deleteByOwner(ConditionOwnerCategory ownerCategory, String ownerId) {
    String jpql =
        "DELETE FROM Condition c WHERE c.ownerCategory = :ownerCategory AND c.ownerId = :ownerId";
    return entityManager
        .createQuery(jpql)
        .setParameter("ownerCategory", ownerCategory)
        .setParameter("ownerId", ownerId)
        .executeUpdate();
  }

  public int deleteByOwner(ConditionOwnerCategory ownerCategory, Long ownerId) {
    return deleteByOwner(ownerCategory, String.valueOf(ownerId));
  }

  public int deleteBySourceTarget(ConditionDataSource sourceCategory, String sourceTargetId) {
    String jpql =
        "DELETE FROM Condition c WHERE c.sourceCategory = :sourceCategory AND c.sourceTargetId ="
            + " :sourceTargetId";
    return entityManager
        .createQuery(jpql)
        .setParameter("sourceCategory", sourceCategory)
        .setParameter("sourceTargetId", sourceTargetId)
        .executeUpdate();
  }

  public int deleteBySourceTarget(ConditionDataSource sourceCategory, Long sourceTargetId) {
    return deleteBySourceTarget(sourceCategory, String.valueOf(sourceTargetId));
  }

  public int deleteBySourceTargetAndType(
      ConditionDataSource sourceCategory, String sourceTargetId, DeviceCategory sourceTargetType) {
    String jpql = """
        DELETE FROM Condition c
        WHERE c.sourceCategory = :sourceCategory
          AND c.sourceTargetId = :sourceTargetId
          AND c.sourceTargetType = :sourceTargetType
        """;
    return entityManager
        .createQuery(jpql)
        .setParameter("sourceCategory", sourceCategory)
        .setParameter("sourceTargetId", sourceTargetId)
        .setParameter("sourceTargetType", sourceTargetType)
        .executeUpdate();
  }

  public int deleteBySourceTargetAndType(
      ConditionDataSource sourceCategory, Long sourceTargetId, DeviceCategory sourceTargetType) {
    return deleteBySourceTargetAndType(
        sourceCategory, String.valueOf(sourceTargetId), sourceTargetType);
  }
}
