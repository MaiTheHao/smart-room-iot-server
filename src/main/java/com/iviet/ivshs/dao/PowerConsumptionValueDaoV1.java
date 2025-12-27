package com.iviet.ivshs.dao;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;
import com.iviet.ivshs.dto.AveragePowerConsumptionValueDtoV1;
import com.iviet.ivshs.dto.SumPowerConsumptionValueDtoV1;
import com.iviet.ivshs.entities.PowerConsumptionValueV1;
import jakarta.persistence.TypedQuery;

@Repository
public class PowerConsumptionValueDaoV1 extends BaseEntityDaoV1<PowerConsumptionValueV1> {

	private static final String DATE_FORMAT = "%Y-%m-%d %H:%i";
	private static final String DATE_FORMAT_FUNCTION = "CAST(FUNCTION('DATE_FORMAT', p.timestamp, '" + DATE_FORMAT + "') AS string)";

	public PowerConsumptionValueDaoV1() {
		super(PowerConsumptionValueV1.class);
	}

	public List<AveragePowerConsumptionValueDtoV1> getAverageHistoryByRoom(Long roomId, Instant startedAt, Instant endedAt) {
		String jpql = "SELECT new com.iviet.ivshs.dto.AveragePowerConsumptionValueDtoV1(" +
				DATE_FORMAT_FUNCTION + ", AVG(p.watt), AVG(p.wattHour)) " +
				"FROM PowerConsumptionValueV1 p " +
				"WHERE p.sensor.room.id = :roomId AND p.timestamp BETWEEN :startedAt AND :endedAt " +
				"GROUP BY " + DATE_FORMAT_FUNCTION + " " +
				"ORDER BY " + DATE_FORMAT_FUNCTION + " ASC";
		TypedQuery<AveragePowerConsumptionValueDtoV1> query = entityManager.createQuery(jpql, AveragePowerConsumptionValueDtoV1.class);
		query.setParameter("roomId", roomId);
		query.setParameter("startedAt", startedAt);
		query.setParameter("endedAt", endedAt);
		return query.getResultList();
	}

	public List<AveragePowerConsumptionValueDtoV1> getAverageHistoryByClient(Long clientId, Instant startedAt, Instant endedAt) {
		String jpql = "SELECT new com.iviet.ivshs.dto.AveragePowerConsumptionValueDtoV1(" +
				DATE_FORMAT_FUNCTION + ", AVG(p.watt), AVG(p.wattHour)) " +
				"FROM PowerConsumptionValueV1 p " +
				"WHERE p.sensor.deviceControl.client.id = :clientId " +
				"AND p.timestamp BETWEEN :startedAt AND :endedAt " +
				"GROUP BY " + DATE_FORMAT_FUNCTION + " " +
				"ORDER BY " + DATE_FORMAT_FUNCTION + " ASC";
		TypedQuery<AveragePowerConsumptionValueDtoV1> query = entityManager.createQuery(jpql, AveragePowerConsumptionValueDtoV1.class);
		query.setParameter("clientId", clientId);
		query.setParameter("startedAt", startedAt);
		query.setParameter("endedAt", endedAt);
		return query.getResultList();
	}

	public List<SumPowerConsumptionValueDtoV1> getSumHistoryByClient(Long clientId, Instant startedAt, Instant endedAt) {
		String jpql = "SELECT new com.iviet.ivshs.dto.SumPowerConsumptionValueDtoV1(" +
				DATE_FORMAT_FUNCTION + ", SUM(p.watt), SUM(p.wattHour)) " +
				"FROM PowerConsumptionValueV1 p " +
				"WHERE p.sensor.deviceControl.client.id = :clientId " +
				"AND p.timestamp BETWEEN :startedAt AND :endedAt " +
				"GROUP BY " + DATE_FORMAT_FUNCTION + " " +
				"ORDER BY " + DATE_FORMAT_FUNCTION + " ASC";
		TypedQuery<SumPowerConsumptionValueDtoV1> query = entityManager.createQuery(jpql, SumPowerConsumptionValueDtoV1.class);
		query.setParameter("clientId", clientId);
		query.setParameter("startedAt", startedAt);
		query.setParameter("endedAt", endedAt);
		return query.getResultList();
	}

	public List<SumPowerConsumptionValueDtoV1> getSumHistoryByRoom(Long roomId, Instant startedAt, Instant endedAt) {
		String jpql = "SELECT new com.iviet.ivshs.dto.SumPowerConsumptionValueDtoV1(" +
				DATE_FORMAT_FUNCTION + ", SUM(p.watt), SUM(p.wattHour)) " +
				"FROM PowerConsumptionValueV1 p " +
				"WHERE p.sensor.room.id = :roomId AND p.timestamp BETWEEN :startedAt AND :endedAt " +
				"GROUP BY " + DATE_FORMAT_FUNCTION + " " +
				"ORDER BY " + DATE_FORMAT_FUNCTION + " ASC";
		TypedQuery<SumPowerConsumptionValueDtoV1> query = entityManager.createQuery(jpql, SumPowerConsumptionValueDtoV1.class);
		query.setParameter("roomId", roomId);
		query.setParameter("startedAt", startedAt);
		query.setParameter("endedAt", endedAt);
		return query.getResultList();
	}

	public void deleteBySensorIdAndTimestampBetween(Long sensorId, Instant startedAt, Instant endedAt) {
			String jpql = """
							DELETE FROM PowerConsumptionValueV1 v
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
							DELETE FROM PowerConsumptionValueV1 v
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
