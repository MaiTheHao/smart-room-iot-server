package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseEntityDao;
import com.iviet.ivshs.entities.MotionMetric;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MotionMetricDao extends BaseEntityDao<MotionMetric> {

  public MotionMetricDao() {
    super(MotionMetric.class);
  }

  @Override
  @Transactional
  public List<MotionMetric> save(List<MotionMetric> entities) {
    String sql = """
        INSERT INTO motion_metrics
        (target_category, target_id, timestamp, unix_minute, motion_detected)
        VALUES (?, ?, ?, ?, ?)
        """;
    try {
      jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
        @Override
        public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
          MotionMetric e = entities.get(i);
          ps.setString(1, e.getTargetCategory());
          ps.setLong(2, e.getTargetId());
          ps.setObject(3, e.getTimestamp());
          ps.setObject(4, e.getUnixMinute());
          ps.setBoolean(5, e.getMotionDetected());
        }

        @Override
        public int getBatchSize() {
          return entities.size();
        }
      });
      return entities;
    } catch (Exception e) {
      throw new RuntimeException("Failed to batch insert MotionMetric entities", e);
    }
  }

  public List<MotionMetric> findHistory(Long targetId, Instant from, Instant to) {
    String jpql = """
        SELECT mm
        FROM MotionMetric mm
        WHERE mm.targetId = :targetId
          AND mm.timestamp BETWEEN :from AND :to
        ORDER BY mm.timestamp ASC
        """;
    return entityManager
        .createQuery(jpql, MotionMetric.class)
        .setParameter("targetId", targetId)
        .setParameter("from", from)
        .setParameter("to", to)
        .getResultList();
  }

  public Optional<MotionMetric> findLatest(Long targetId) {
    String jpql = """
        SELECT mm
        FROM MotionMetric mm
        WHERE mm.targetId = :targetId
        ORDER BY mm.timestamp DESC
        """;
    return entityManager
        .createQuery(jpql, MotionMetric.class)
        .setParameter("targetId", targetId)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }

  public Long countTriggeredByRoomId(Long roomId, Instant from, Instant to) {
    String jpql = """
        SELECT COUNT(mm)
        FROM MotionMetric mm
        JOIN MotionDetector md ON md.id = mm.targetId
        WHERE md.room.id = :roomId
          AND mm.motionDetected = true
          AND mm.timestamp BETWEEN :from AND :to
        """;
    return entityManager
        .createQuery(jpql, Long.class)
        .setParameter("roomId", roomId)
        .setParameter("from", from)
        .setParameter("to", to)
        .getSingleResult();
  }
}
