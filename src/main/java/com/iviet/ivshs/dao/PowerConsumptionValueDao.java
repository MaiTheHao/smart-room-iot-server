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

	private static final String DATE_FORMAT = "%Y-%m-%d %H:%i";
	private static final String DATE_FUNC = "CAST(FUNCTION('DATE_FORMAT', pcv.timestamp, '" + DATE_FORMAT + "') AS string)";
	private final String AVG_DTO_CLASS = AveragePowerConsumptionValueDto.class.getName();
	private final String SUM_DTO_CLASS = SumPowerConsumptionValueDto.class.getName();

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
		String jpql = """
				SELECT new %s(%s, AVG(pcv.watt))
				FROM PowerConsumptionValue pcv
				WHERE pcv.sensor.room.id = :roomId AND pcv.timestamp BETWEEN :startedAt AND :endedAt
				GROUP BY %s
				ORDER BY %s ASC
				""".formatted(AVG_DTO_CLASS, DATE_FUNC, DATE_FUNC, DATE_FUNC);
		
		return entityManager.createQuery(jpql, AveragePowerConsumptionValueDto.class)
				.setParameter("roomId", roomId)
				.setParameter("startedAt", startedAt)
				.setParameter("endedAt", endedAt)
				.getResultList();
	}

	public List<AveragePowerConsumptionValueDto> getAverageHistoryByClient(Long clientId, Instant startedAt, Instant endedAt) {
		String jpql = """
				SELECT new %s(%s, AVG(pcv.watt))
				FROM PowerConsumptionValue pcv
				WHERE pcv.sensor.deviceControl.client.id = :clientId
				AND pcv.timestamp BETWEEN :startedAt AND :endedAt
				GROUP BY %s
				ORDER BY %s ASC
				""".formatted(AVG_DTO_CLASS, DATE_FUNC, DATE_FUNC, DATE_FUNC);
		
		return entityManager.createQuery(jpql, AveragePowerConsumptionValueDto.class)
				.setParameter("clientId", clientId)
				.setParameter("startedAt", startedAt)
				.setParameter("endedAt", endedAt)
				.getResultList();
	}

	public List<SumPowerConsumptionValueDto> getSumHistoryByClient(Long clientId, Instant startedAt, Instant endedAt) {
		String jpql = """
				SELECT new %s(%s, SUM(pcv.watt))
				FROM PowerConsumptionValue pcv
				WHERE pcv.sensor.deviceControl.client.id = :clientId
				AND pcv.timestamp BETWEEN :startedAt AND :endedAt
				GROUP BY %s
				ORDER BY %s ASC
				""".formatted(SUM_DTO_CLASS, DATE_FUNC, DATE_FUNC, DATE_FUNC);
		
		return entityManager.createQuery(jpql, SumPowerConsumptionValueDto.class)
				.setParameter("clientId", clientId)
				.setParameter("startedAt", startedAt)
				.setParameter("endedAt", endedAt)
				.getResultList();
	}

	public List<SumPowerConsumptionValueDto> getSumHistoryByRoom(Long roomId, Instant startedAt, Instant endedAt) {
		String jpql = """
				SELECT new %s(%s, SUM(pcv.watt))
				FROM PowerConsumptionValue pcv
				WHERE pcv.sensor.room.id = :roomId AND pcv.timestamp BETWEEN :startedAt AND :endedAt
				GROUP BY %s
				ORDER BY %s ASC
				""".formatted(SUM_DTO_CLASS, DATE_FUNC, DATE_FUNC, DATE_FUNC);
		
		return entityManager.createQuery(jpql, SumPowerConsumptionValueDto.class)
				.setParameter("roomId", roomId)
				.setParameter("startedAt", startedAt)
				.setParameter("endedAt", endedAt)
				.getResultList();
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
