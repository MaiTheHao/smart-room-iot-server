package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseEntityDao;
import com.iviet.ivshs.entities.RoomEvent;
import com.iviet.ivshs.shared.enumeration.RoomEventCode;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RoomEventDao extends BaseEntityDao<RoomEvent> {

  public RoomEventDao() {
    super(RoomEvent.class);
  }

  public Optional<RoomEvent> findByCode(RoomEventCode code) {
    String jpql = """
        SELECT re
        FROM RoomEvent re
        WHERE re.code = :code
        """;
    return entityManager
        .createQuery(jpql, RoomEvent.class)
        .setParameter("code", code)
        .setMaxResults(1)
        .getResultStream()
        .findFirst();
  }
}
