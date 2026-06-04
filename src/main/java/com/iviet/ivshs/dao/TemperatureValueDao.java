package com.iviet.ivshs.dao;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dao.base.BaseTelemetryDao;
import com.iviet.ivshs.dto.AverageTemperatureValueDto;
import com.iviet.ivshs.entities.TemperatureValue;
import com.iviet.ivshs.shared.exception.domain.BadRequestException;

@Repository
public class TemperatureValueDao extends BaseTelemetryDao<TemperatureValue> {
	public TemperatureValueDao() {
		super(TemperatureValue.class);
	}

	@NonNull
	private String getInsertSql() {
		String sql = "INSERT INTO %s (sensor_id, timestamp, temp_c, unix_minute) VALUES (?, ?, ?, ?)"
				.formatted(getTableName());
		return sql;
	}

	@Override
	public void saveAndForget(Long sensorId, TemperatureValue entity) {
		this.saveAndForget(sensorId, Collections.singletonList(entity));
	}

	@Override
	public void saveAndForget(Long sensorId, List<TemperatureValue> entities) {
		if (entities == null || entities.isEmpty())
			return;

		String insertSql = getInsertSql();
		jdbcTemplate.batchUpdate(insertSql, entities, databaseProperties.getHibernateBatchSize(), (ps, entity) -> {
			if (sensorId != null) {
				ps.setLong(1, sensorId);
			} else
				throw new BadRequestException("Sensor ID cannot be null");

			if (entity.getTimestamp() != null) {
				ps.setObject(2, entity.getTimestamp());
			} else
				throw new BadRequestException("Timestamp cannot be null");

			if (entity.getTempC() != null)
				ps.setDouble(3, entity.getTempC());
			else
				ps.setNull(3, java.sql.Types.DOUBLE);

			ps.setObject(4, entity.getUnixMinute());
		});
	}

	public List<AverageTemperatureValueDto> getAverageHistoryByRoom(Long roomId, Instant startedAt, Instant endedAt,
			int divisor) {
		String jpql = """
				SELECT new %s((tv.unixMinute - MOD(tv.unixMinute, :divisor)) * 60L, AVG(tv.tempC))
				FROM TemperatureValue tv
				WHERE tv.sensor.room.id = :roomId
				AND tv.timestamp BETWEEN :startedAt AND :endedAt
				GROUP BY (tv.unixMinute - MOD(tv.unixMinute, :divisor)) * 60L
				ORDER BY (tv.unixMinute - MOD(tv.unixMinute, :divisor)) * 60L ASC
				""".formatted(AverageTemperatureValueDto.class.getName());

		return entityManager.createQuery(jpql, AverageTemperatureValueDto.class)
				.setParameter("roomId", roomId)
				.setParameter("startedAt", startedAt)
				.setParameter("endedAt", endedAt)
				.setParameter("divisor", divisor)
				.getResultList();
	}

	public List<AverageTemperatureValueDto> getAverageHistoryByClient(Long clientId, Instant startedAt, Instant endedAt,
			int divisor) {
		String jpql = """
				SELECT new %s((tv.unixMinute - MOD(tv.unixMinute, :divisor)) * 60L, AVG(tv.tempC))
				FROM TemperatureValue tv
				WHERE tv.sensor.hardwareConfig.client.id = :clientId
				AND tv.timestamp BETWEEN :startedAt AND :endedAt
				GROUP BY (tv.unixMinute - MOD(tv.unixMinute, :divisor)) * 60L
				ORDER BY (tv.unixMinute - MOD(tv.unixMinute, :divisor)) * 60L ASC
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
