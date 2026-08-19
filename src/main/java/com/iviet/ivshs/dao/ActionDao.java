package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ActionDao extends BaseAuditEntityDao<Action> {

  public ActionDao() {
    super(Action.class);
  }

  public List<Action> findByOwner(ActionOwnerCategory ownerCategory, String ownerId) {
    String jpql = """
        SELECT a FROM Action a
        WHERE a.ownerCategory = :ownerCategory AND a.ownerId = :ownerId
        ORDER BY a.executionOrder ASC
        """;
    return entityManager
        .createQuery(jpql, Action.class)
        .setParameter("ownerCategory", ownerCategory)
        .setParameter("ownerId", ownerId)
        .getResultList();
  }

  public List<Action> findByOwner(ActionOwnerCategory ownerCategory, Long ownerId) {
    return findByOwner(ownerCategory, String.valueOf(ownerId));
  }

  public long countByOwner(ActionOwnerCategory ownerCategory, String ownerId) {
    String jpql =
        "SELECT COUNT(a) FROM Action a WHERE a.ownerCategory = :ownerCategory AND a.ownerId ="
            + " :ownerId";
    return entityManager
        .createQuery(jpql, Long.class)
        .setParameter("ownerCategory", ownerCategory)
        .setParameter("ownerId", ownerId)
        .getSingleResult();
  }

  public long countByOwner(ActionOwnerCategory ownerCategory, Long ownerId) {
    return countByOwner(ownerCategory, String.valueOf(ownerId));
  }

  public boolean existsByOwner(ActionOwnerCategory ownerCategory, String ownerId) {
    return countByOwner(ownerCategory, ownerId) > 0;
  }

  public List<Action> findByTarget(DeviceCategory targetCategory, String targetId) {
    String jpql = """
        SELECT a FROM Action a
        WHERE a.targetCategory = :targetCategory AND a.targetId = :targetId
        ORDER BY a.executionOrder ASC
        """;
    return entityManager
        .createQuery(jpql, Action.class)
        .setParameter("targetCategory", targetCategory)
        .setParameter("targetId", targetId)
        .getResultList();
  }

  public List<Action> findByTarget(DeviceCategory targetCategory, Long targetId) {
    return findByTarget(targetCategory, String.valueOf(targetId));
  }

  public long countByTarget(DeviceCategory targetCategory, String targetId) {
    String jpql =
        "SELECT COUNT(a) FROM Action a WHERE a.targetCategory = :targetCategory AND a.targetId ="
            + " :targetId";
    return entityManager
        .createQuery(jpql, Long.class)
        .setParameter("targetCategory", targetCategory)
        .setParameter("targetId", targetId)
        .getSingleResult();
  }

  public long countByTarget(DeviceCategory targetCategory, Long targetId) {
    return countByTarget(targetCategory, String.valueOf(targetId));
  }

  public int deleteByOwner(ActionOwnerCategory ownerCategory, String ownerId) {
    String jpql =
        "DELETE FROM Action a WHERE a.ownerCategory = :ownerCategory AND a.ownerId = :ownerId";
    return entityManager
        .createQuery(jpql)
        .setParameter("ownerCategory", ownerCategory)
        .setParameter("ownerId", ownerId)
        .executeUpdate();
  }

  public int deleteByOwner(ActionOwnerCategory ownerCategory, Long ownerId) {
    return deleteByOwner(ownerCategory, String.valueOf(ownerId));
  }

  public int deleteByTarget(DeviceCategory targetCategory, String targetId) {
    String jpql =
        "DELETE FROM Action a WHERE a.targetCategory = :targetCategory AND a.targetId = :targetId";
    return entityManager
        .createQuery(jpql)
        .setParameter("targetCategory", targetCategory)
        .setParameter("targetId", targetId)
        .executeUpdate();
  }

  public int deleteByTarget(DeviceCategory targetCategory, Long targetId) {
    return deleteByTarget(targetCategory, String.valueOf(targetId));
  }
}
