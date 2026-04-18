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

	public List<AverageTemperatureValueDto> getAverageHistoryByRoom(Long roomId, Instant startedAt, Instant endedAt, int divisor) {
		String sql = """
						SELECT 
							(unix_minute DIV :divisor) * :divisor * 60 as unix_seconds,
							AVG(temp_c) as avg_temp
						FROM temperature_value
						WHERE sensor_id IN (SELECT id FROM temperature WHERE room_id = :roomId)
						AND timestamp BETWEEN :startedAt AND :endedAt
						GROUP BY unix_seconds
						ORDER BY unix_seconds ASC
						""";

		@SuppressWarnings("unchecked")
		List<Object[]> results = entityManager.createNativeQuery(sql)
						.setParameter("roomId", roomId)
						.setParameter("startedAt", java.sql.Timestamp.from(startedAt))
						.setParameter("endedAt", java.sql.Timestamp.from(endedAt))
						.setParameter("divisor", divisor)
						.getResultList();

		return results.stream()
						.map(row -> new AverageTemperatureValueDto(
								((Number) row[0]).longValue(),
								((Number) row[1]).doubleValue()))
						.toList();
	}

	public List<AverageTemperatureValueDto> getAverageHistoryByClient(Long clientId, Instant startedAt, Instant endedAt, int divisor) {
		String jpql = """
						SELECT new %s((tv.unixMinute / :divisor) * :divisor * 60L, AVG(tv.tempC))
						FROM TemperatureValue tv
						WHERE tv.sensor.deviceControl.client.id = :clientId
						AND tv.timestamp BETWEEN :startedAt AND :endedAt
						GROUP BY (tv.unixMinute / :divisor) * :divisor * 60L
						ORDER BY (tv.unixMinute / :divisor) * :divisor * 60L ASC
						""".formatted(AverageTemperatureValueDto.class.getName());

		return entityManager.createQuery(jpql, AverageTemperatureValueDto.class)
						.setParameter("clientId", clientId)
						.setParameter("startedAt", startedAt)
						.setParameter("endedAt", endedAt)
						.setParameter("divisor", divisor)
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

