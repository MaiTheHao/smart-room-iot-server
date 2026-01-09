package com.iviet.ivshs.dao;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.entities.TemperatureValue;
import com.iviet.ivshs.exception.domain.BadRequestException;

@Repository
public class TemperatureValueDao extends BaseTelemetryDao<TemperatureValue> {
	private static final String DATE_FORMAT = "%Y-%m-%d %H:%i";
	private static final String DATE_FUNC = "CAST(FUNCTION('DATE_FORMAT', tv.timestamp, '" + DATE_FORMAT + "') AS string)";

	public TemperatureValueDao() {
		super(TemperatureValue.class);
	}

	private String getInsertSql() {
		return "INSERT INTO %s (sensor_id, timestamp, temp_c) VALUES (?, ?, ?)".formatted(getTableName());
	}

	@Override
	public void saveAndForget(Long sensorId, TemperatureValue entity) {			
		this.saveAndForget(sensorId, Collections.singletonList(entity));
	}

	@Override
	public void saveAndForget(Long sensorId, List<TemperatureValue> entities) {
		if (entities == null || entities.isEmpty()) return;

		String insertSql = getInsertSql();
		jdbcTemplate.batchUpdate(insertSql, entities, BATCH_SIZE, (ps, entity) -> {
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

	public List<AverageTemperatureValueDto> getAverageHistoryByRoom(Long roomId, Instant startedAt, Instant endedAt) {
		String dtoPath = AverageTemperatureValueDto.class.getName();
		String jpql = """
						SELECT new %s(%s, AVG(tv.tempC))
						FROM TemperatureValue tv
						WHERE tv.sensor.room.id = :roomId 
						AND tv.timestamp BETWEEN :startedAt AND :endedAt
						GROUP BY %s
						ORDER BY %s ASC
						""".formatted(dtoPath, DATE_FUNC, DATE_FUNC, DATE_FUNC);

		return entityManager.createQuery(jpql, AverageTemperatureValueDto.class)
						.setParameter("roomId", roomId)
						.setParameter("startedAt", startedAt)
						.setParameter("endedAt", endedAt)
						.getResultList();
	}

	public List<AverageTemperatureValueDto> getAverageHistoryByClient(Long clientId, Instant startedAt, Instant endedAt) {
		String dtoPath = AverageTemperatureValueDto.class.getName();
		String jpql = """
						SELECT new %s(%s, AVG(tv.tempC))
						FROM TemperatureValue tv
						WHERE tv.sensor.deviceControl.client.id = :clientId
						AND tv.timestamp BETWEEN :startedAt AND :endedAt
						GROUP BY %s
						ORDER BY %s ASC
						""".formatted(dtoPath, DATE_FUNC, DATE_FUNC, DATE_FUNC);

		return entityManager.createQuery(jpql, AverageTemperatureValueDto.class)
						.setParameter("clientId", clientId)
						.setParameter("startedAt", startedAt)
						.setParameter("endedAt", endedAt)
						.getResultList();
	}

	public void deleteBySensorIdAndTimestampBetween(Long sensorId, Instant startedAt, Instant endedAt) {
		String jpql = """
						DELETE FROM TemperatureValue tv
						WHERE tv.sensor.id = :sensorId
						AND tv.timestamp BETWEEN :startedAt AND :endedAt
						""";

		entityManager.createQuery(jpql)
						.setParameter("sensorId", sensorId)
						.setParameter("startedAt", startedAt)
						.setParameter("endedAt", endedAt)
						.executeUpdate();
	}

	public void deleteBySensorNaturalIdAndTimestampBetween(String sensorNaturalId, Instant startedAt, Instant endedAt) {
		String jpql = """
						DELETE FROM TemperatureValue tv
						WHERE tv.sensor.naturalId = :sensorNaturalId
						AND tv.timestamp BETWEEN :startedAt AND :endedAt
						""";

		entityManager.createQuery(jpql)
						.setParameter("sensorNaturalId", sensorNaturalId)
						.setParameter("startedAt", startedAt)
						.setParameter("endedAt", endedAt)
						.executeUpdate();
	}
}

