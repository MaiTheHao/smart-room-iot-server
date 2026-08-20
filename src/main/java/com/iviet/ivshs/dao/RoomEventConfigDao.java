package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
import com.iviet.ivshs.entities.RoomEventConfig;
import com.iviet.ivshs.shared.enumeration.RoomEventCode;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RoomEventConfigDao extends BaseAuditEntityDao<RoomEventConfig> {

  public RoomEventConfigDao() {
    super(RoomEventConfig.class);
  }

  public Optional<RoomEventConfig> findByRoomIdAndEventCode(Long roomId, RoomEventCode eventCode) {
    String jpql = """
        SELECT rec
        FROM RoomEventConfig rec
        JOIN FETCH rec.room r
        JOIN FETCH rec.roomEvent re
        WHERE r.id = :roomId AND re.code = :eventCode
        """;
    return entityManager
        .createQuery(jpql, RoomEventConfig.class)
        .setParameter("roomId", roomId)
        .setParameter("eventCode", eventCode)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }

  public List<RoomEventConfig> findAllByRoomId(Long roomId) {
    String jpql = """
        SELECT rec
        FROM RoomEventConfig rec
        JOIN FETCH rec.room r
        JOIN FETCH rec.roomEvent re
        WHERE r.id = :roomId
        """;
    return entityManager
        .createQuery(jpql, RoomEventConfig.class)
        .setParameter("roomId", roomId)
        .getResultList();
  }
}
