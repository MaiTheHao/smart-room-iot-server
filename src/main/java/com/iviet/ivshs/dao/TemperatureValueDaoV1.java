package com.iviet.ivshs.dao;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.AverageTemperatureValueDtoV1;
import com.iviet.ivshs.entities.TemperatureValueV1;

@Repository
public class TemperatureValueDaoV1 extends BaseEntityDaoV1<TemperatureValueV1> {
        private static final String DATE_FORMAT = "%Y-%m-%d %H:%i";
        private static final String DATE_FUNC = "CAST(FUNCTION('DATE_FORMAT', v.timestamp, '" + DATE_FORMAT + "') AS string)";

        public TemperatureValueDaoV1() {
                super(TemperatureValueV1.class);
        }

        public List<AverageTemperatureValueDtoV1> getAverageHistoryByRoom(Long roomId, Instant startedAt, Instant endedAt) {
                String dtoPath = AverageTemperatureValueDtoV1.class.getName();
                String jpql = """
                                SELECT new %s(%s, AVG(v.tempC))
                                FROM TemperatureValueV1 v
                                WHERE v.sensor.room.id = :roomId 
                                AND v.timestamp BETWEEN :startedAt AND :endedAt
                                GROUP BY %s
                                ORDER BY %s ASC
                                """.formatted(dtoPath, DATE_FUNC, DATE_FUNC, DATE_FUNC);

                return entityManager.createQuery(jpql, AverageTemperatureValueDtoV1.class)
                                .setParameter("roomId", roomId)
                                .setParameter("startedAt", startedAt)
                                .setParameter("endedAt", endedAt)
                                .getResultList();
        }

        public List<AverageTemperatureValueDtoV1> getAverageHistoryByClient(Long clientId, Instant startedAt, Instant endedAt) {
                String dtoPath = AverageTemperatureValueDtoV1.class.getName();
                String jpql = """
                                SELECT new %s(%s, AVG(v.tempC))
                                FROM TemperatureValueV1 v
                                WHERE v.sensor.deviceControl.client.id = :clientId
                                AND v.timestamp BETWEEN :startedAt AND :endedAt
                                GROUP BY %s
                                ORDER BY %s ASC
                                """.formatted(dtoPath, DATE_FUNC, DATE_FUNC, DATE_FUNC);

                return entityManager.createQuery(jpql, AverageTemperatureValueDtoV1.class)
                                .setParameter("clientId", clientId)
                                .setParameter("startedAt", startedAt)
                                .setParameter("endedAt", endedAt)
                                .getResultList();
        }

        public void deleteBySensorIdAndTimestampBetween(Long sensorId, Instant startedAt, Instant endedAt) {
                String jpql = """
                                DELETE FROM TemperatureValueV1 v
                                WHERE v.sensor.id = :sensorId
                                AND v.timestamp BETWEEN :startedAt AND :endedAt
                                """;

                entityManager.createQuery(jpql)
                                .setParameter("sensorId", sensorId)
                                .setParameter("startedAt", startedAt)
                                .setParameter("endedAt", endedAt)
                                .executeUpdate();
        }

        public void deleteBySensorNaturalIdAndTimestampBetween(String sensorNaturalId, Instant startedAt, Instant endedAt) {
                String jpql = """
                                DELETE FROM TemperatureValueV1 v
                                WHERE v.sensor.naturalId = :sensorNaturalId
                                AND v.timestamp BETWEEN :startedAt AND :endedAt
                                """;

                entityManager.createQuery(jpql)
                                .setParameter("sensorNaturalId", sensorNaturalId)
                                .setParameter("startedAt", startedAt)
                                .setParameter("endedAt", endedAt)
                                .executeUpdate();
        }
}

