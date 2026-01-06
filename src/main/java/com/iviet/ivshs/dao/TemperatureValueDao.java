package com.iviet.ivshs.dao;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.AverageTemperatureValueDtoV1;
import com.iviet.ivshs.entities.TemperatureValue;
import com.iviet.ivshs.exception.domain.BadRequestException;

@Repository
public class TemperatureValueDao extends BaseTelemetryDao<TemperatureValue> {
	private static final String INSERT_SQL = "INSERT INTO temperature_value_v1 (sensor_id, timestamp, temp_c) VALUES (?, ?, ?)";
	private static final String DATE_FORMAT = "%Y-%m-%d %H:%i";
	private static final String DATE_FUNC = "CAST(FUNCTION('DATE_FORMAT', v.timestamp, '" + DATE_FORMAT + "') AS string)";

	public TemperatureValueDao() {
		super(TemperatureValue.class);
	}

	@Override
	public void saveAndForget(Long sensorId, TemperatureValue entity) {			
		this.saveAndForget(sensorId, Collections.singletonList(entity));
	}

	@Override
	public void saveAndForget(Long sensorId, List<TemperatureValue> entities) {
		if (entities == null || entities.isEmpty()) return;

		jdbcTemplate.batchUpdate(INSERT_SQL, entities, BATCH_SIZE, (ps, entity) -> {
			if (sensorId != null) {
				ps.setLong(1, sensorId);
			} else throw new BadRequestException("Sensor ID cannot be null");

			if (entity.getTimestamp() != null) {
				ps.setTimestamp(2, java.sql.Timestamp.from(entity.getTimestamp()));
			} else throw new BadRequestException("Timestamp cannot be null");

			if (entity.getTempC() != null) ps.setDouble(3, entity.getTempC());
			else ps.setNull(3, java.sql.Types.DOUBLE);
		});
	}

	public List<AverageTemperatureValueDtoV1> getAverageHistoryByRoom(Long roomId, Instant startedAt, Instant endedAt) {
		String dtoPath = AverageTemperatureValueDtoV1.class.getName();
		String jpql = """
						SELECT new %s(%s, AVG(v.tempC))
						FROM TemperatureValue v
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
						FROM TemperatureValue v
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
						DELETE FROM TemperatureValue v
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
						DELETE FROM TemperatureValue v
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

