package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseIoTSensorDao;
import com.iviet.ivshs.entities.MotionDetector;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MotionDetectorDao extends BaseIoTSensorDao<MotionDetector> {

  public MotionDetectorDao() {
    super(MotionDetector.class);
  }

  @Override
  public Optional<MotionDetector> findByNaturalId(String naturalId) {
    return findOne(
        root -> entityManager.getCriteriaBuilder().equal(root.get("naturalId"), naturalId));
  }

  @Override
  public Optional<MotionDetector> findByNaturalId(String naturalId, String langCode) {
    String jpql = """
        SELECT md
        FROM MotionDetector md
        LEFT JOIN md.translations tl ON tl.langCode = :langCode
        WHERE md.naturalId = :naturalId
        """;
    return entityManager
        .createQuery(jpql, MotionDetector.class)
        .setParameter("naturalId", naturalId)
        .setParameter("langCode", langCode)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }

  @Override
  public Optional<MotionDetector> findByRoomAndNaturalId(
      Long roomId, String naturalId, String langCode) {
    String jpql = """
        SELECT md
        FROM MotionDetector md
        LEFT JOIN md.translations tl ON tl.langCode = :langCode
        WHERE md.room.id = :roomId AND md.naturalId = :naturalId
        """;
    return entityManager
        .createQuery(jpql, MotionDetector.class)
        .setParameter("roomId", roomId)
        .setParameter("naturalId", naturalId)
        .setParameter("langCode", langCode)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }
}
