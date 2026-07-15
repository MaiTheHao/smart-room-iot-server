package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseIoTSensorDao;
import com.iviet.ivshs.entities.LuxSensor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class LuxSensorDao extends BaseIoTSensorDao<LuxSensor> {

    public LuxSensorDao() {
        super(LuxSensor.class);
    }

    @Override
    public Optional<LuxSensor> findByNaturalId(String naturalId) {
        return findOne(root ->
            entityManager.getCriteriaBuilder().equal(root.get("naturalId"), naturalId)
        );
    }

    @Override
    public Optional<LuxSensor> findByNaturalId(String naturalId, String langCode) {
        String jpql = """
                SELECT ls
                FROM LuxSensor ls
                LEFT JOIN ls.translations tl ON tl.langCode = :langCode
                WHERE ls.naturalId = :naturalId
                """;
        return entityManager.createQuery(jpql, LuxSensor.class)
                .setParameter("naturalId", naturalId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<LuxSensor> findByRoomAndNaturalId(Long roomId, String naturalId, String langCode) {
        String jpql = """
                SELECT ls
                FROM LuxSensor ls
                LEFT JOIN ls.translations tl ON tl.langCode = :langCode
                WHERE ls.room.id = :roomId AND ls.naturalId = :naturalId
                """;
        return entityManager.createQuery(jpql, LuxSensor.class)
                .setParameter("roomId", roomId)
                .setParameter("naturalId", naturalId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
