package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseIoTSensorDao;
import com.iviet.ivshs.entities.HumiditySensor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class HumiditySensorDao extends BaseIoTSensorDao<HumiditySensor> {

    public HumiditySensorDao() {
        super(HumiditySensor.class);
    }

    @Override
    public Optional<HumiditySensor> findByNaturalId(String naturalId) {
        return findOne(root ->
            entityManager.getCriteriaBuilder().equal(root.get("naturalId"), naturalId)
        );
    }

    @Override
    public Optional<HumiditySensor> findByNaturalId(String naturalId, String langCode) {
        String jpql = """
                SELECT hs
                FROM HumiditySensor hs
                LEFT JOIN hs.translations tl ON tl.langCode = :langCode
                WHERE hs.naturalId = :naturalId
                """;
        return entityManager.createQuery(jpql, HumiditySensor.class)
                .setParameter("naturalId", naturalId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<HumiditySensor> findByRoomAndNaturalId(Long roomId, String naturalId, String langCode) {
        String jpql = """
                SELECT hs
                FROM HumiditySensor hs
                LEFT JOIN hs.translations tl ON tl.langCode = :langCode
                WHERE hs.room.id = :roomId AND hs.naturalId = :naturalId
                """;
        return entityManager.createQuery(jpql, HumiditySensor.class)
                .setParameter("roomId", roomId)
                .setParameter("naturalId", naturalId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
