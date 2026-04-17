package com.iviet.ivshs.dao;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;
import com.iviet.ivshs.dto.AveragePowerConsumptionValueDto;
import com.iviet.ivshs.dto.SumPowerConsumptionValueDto;
import com.iviet.ivshs.entities.PowerConsumptionValue;
import com.iviet.ivshs.exception.domain.BadRequestException;

@Repository
public class PowerConsumptionValueDao extends BaseTelemetryDao<PowerConsumptionValue> {

	public PowerConsumptionValueDao() {
		super(PowerConsumptionValue.class);
	}

	private String getInsertSql() {
		return "INSERT INTO %s (sensor_id, timestamp, watt) VALUES (?, ?, ?)".formatted(getTableName());
	}

	@Override
	public void saveAndForget(Long sensorId, PowerConsumptionValue entity) {
		this.saveAndForget(sensorId, Collections.singletonList(entity));
	}

	@Override
	public void saveAndForget(Long sensorId, List<PowerConsumptionValue> entities) {
		if (entities == null || entities.isEmpty()) return;

		String insertSql = getInsertSql();
		jdbcTemplate.batchUpdate(insertSql, entities, BATCH_SIZE, (ps, entity) -> {
			if (sensorId != null) {
				ps.setLong(1,  sensorId);
			} else throw new BadRequestException("Sensor ID cannot be null");

			if (entity.getTimestamp() != null) {
				ps.setTimestamp(2, java.sql.Timestamp.from(entity.getTimestamp()));
			} else throw new BadRequestException("Timestamp cannot be null");

			if (entity.getWatt() != null) ps.setDouble(3, entity.getWatt());
			else ps.setNull(3, java.sql.Types.DOUBLE);
		});
	}

	public List<AveragePowerConsumptionValueDto> getAverageHistoryByRoom(Long roomId, Instant startedAt, Instant endedAt) {
		String sql = """
				SELECT 
					(unix_minute * 60) as unix_seconds,
					AVG(watt) as avg_watt
				FROM power_consumption_value
				WHERE sensor_id IN (SELECT id FROM power_consumption WHERE room_id = :roomId)
				AND timestamp BETWEEN :startedAt AND :endedAt
				GROUP BY unix_minute
				ORDER BY unix_minute ASC
				""";
		
		@SuppressWarnings("unchecked")
		List<Object[]> results = entityManager.createNativeQuery(sql)
				.setParameter("roomId", roomId)
				.setParameter("startedAt", java.sql.Timestamp.from(startedAt))
				.setParameter("endedAt", java.sql.Timestamp.from(endedAt))
				.getResultList();

		return results.stream()
				.map(row -> new AveragePowerConsumptionValueDto(
						Instant.ofEpochSecond(((Number) row[0]).longValue()),
						((Number) row[1]).doubleValue()))
				.toList();
	}

	public List<AveragePowerConsumptionValueDto> getAverageHistoryByClient(Long clientId, Instant startedAt, Instant endedAt) {
		String jpql = """
				SELECT new %s(pcv.unixMinute * 60L, AVG(pcv.watt))
				FROM PowerConsumptionValue pcv
				WHERE pcv.sensor.deviceControl.client.id = :clientId
				AND pcv.timestamp BETWEEN :startedAt AND :endedAt
				GROUP BY pcv.unixMinute
				ORDER BY pcv.unixMinute ASC
				""".formatted(AveragePowerConsumptionValueDto.class.getName());
		
		return entityManager.createQuery(jpql, AveragePowerConsumptionValueDto.class)
				.setParameter("clientId", clientId)
				.setParameter("startedAt", startedAt)
				.setParameter("endedAt", endedAt)
				.getResultList();
	}

	public List<SumPowerConsumptionValueDto> getSumHistoryByClient(Long clientId, Instant startedAt, Instant endedAt) {
		String jpql = """
				SELECT new %s(pcv.unixMinute * 60L, SUM(pcv.watt))
				FROM PowerConsumptionValue pcv
				WHERE pcv.sensor.deviceControl.client.id = :clientId
				AND pcv.timestamp BETWEEN :startedAt AND :endedAt
				GROUP BY pcv.unixMinute
				ORDER BY pcv.unixMinute ASC
				""".formatted(SumPowerConsumptionValueDto.class.getName());
		
		return entityManager.createQuery(jpql, SumPowerConsumptionValueDto.class)
				.setParameter("clientId", clientId)
				.setParameter("startedAt", startedAt)
				.setParameter("endedAt", endedAt)
				.getResultList();
	}

	public List<SumPowerConsumptionValueDto> getSumHistoryByRoom(Long roomId, Instant startedAt, Instant endedAt) {
		String sql = """
				SELECT 
					(unix_minute * 60) as unix_seconds,
					SUM(watt) as sum_watt
				FROM power_consumption_value
				WHERE sensor_id IN (SELECT id FROM power_consumption WHERE room_id = :roomId)
				AND timestamp BETWEEN :startedAt AND :endedAt
				GROUP BY unix_minute
				ORDER BY unix_minute ASC
				""";
		
		@SuppressWarnings("unchecked")
		List<Object[]> results = entityManager.createNativeQuery(sql)
				.setParameter("roomId", roomId)
				.setParameter("startedAt", java.sql.Timestamp.from(startedAt))
				.setParameter("endedAt", java.sql.Timestamp.from(endedAt))
				.getResultList();

		return results.stream()
				.map(row -> new SumPowerConsumptionValueDto(
						Instant.ofEpochSecond(((Number) row[0]).longValue()),
						((Number) row[1]).doubleValue()))
				.toList();
	}

	public void deleteBySensorIdAndTimestampBetween(Long sensorId, Instant startedAt, Instant endedAt) {
		String jpql = """
				DELETE FROM PowerConsumptionValue pcv
				WHERE pcv.sensor.id = :sensorId
				AND pcv.timestamp BETWEEN :startedAt AND :endedAt
				""";

		entityManager.createQuery(jpql)
				.setParameter("sensorId", sensorId)
				.setParameter("startedAt", startedAt)
				.setParameter("endedAt", endedAt)
				.executeUpdate();
	}

	public void deleteBySensorNaturalIdAndTimestampBetween(String sensorNaturalId, Instant startedAt, Instant endedAt) {
		String jpql = """
				DELETE FROM PowerConsumptionValue pcv
				WHERE pcv.sensor.naturalId = :sensorNaturalId
				AND pcv.timestamp BETWEEN :startedAt AND :endedAt
				""";

		entityManager.createQuery(jpql)
				.setParameter("sensorNaturalId", sensorNaturalId)
				.setParameter("startedAt", startedAt)
				.setParameter("endedAt", endedAt)
				.executeUpdate();
	}
}
