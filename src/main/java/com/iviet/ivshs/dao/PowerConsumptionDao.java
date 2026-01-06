package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.PowerConsumptionDtoV1;
import com.iviet.ivshs.entities.PowerConsumption;

@Repository
public class PowerConsumptionDao extends BaseIoTDeviceDao<PowerConsumption> {

	public PowerConsumptionDao() {
		super(PowerConsumption.class);
	}

	@Override
	public Optional<PowerConsumptionDtoV1> findByNaturalId(String naturalId, String langCode) {
		String dtoPath = PowerConsumptionDtoV1.class.getName();
		String jpql = """
				SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentWatt, t.currentWattHour, t.naturalId, t.room.id)
				FROM PowerConsumption t
				LEFT JOIN t.translations tl ON tl.langCode = :langCode
				WHERE t.naturalId = :naturalId
				""".formatted(dtoPath);
		return entityManager.createQuery(jpql, PowerConsumptionDtoV1.class)
				.setParameter("naturalId", naturalId)
				.setParameter("langCode", langCode)
				.setMaxResults(1)
				.getResultStream()
				.findFirst();
	}

	public Optional<PowerConsumptionDtoV1> findById(Long sensorId, String langCode) {
		String dtoPath = PowerConsumptionDtoV1.class.getName();
		String jpql = """
				SELECT new %s(s.id, sl.name, sl.description, s.isActive, s.currentWatt, s.currentWattHour, s.naturalId, s.room.id)
				FROM PowerConsumption s 
				LEFT JOIN s.translations sl ON sl.langCode = :langCode 
				WHERE s.id = :sensorId
				""".formatted(dtoPath);

		return entityManager.createQuery(jpql, PowerConsumptionDtoV1.class)
				.setParameter("sensorId", sensorId)
				.setParameter("langCode", langCode)
				.setMaxResults(1)
				.getResultStream()
				.findFirst();
	}

	public List<PowerConsumptionDtoV1> findAllByRoomId(Long roomId, int page, int size, String langCode) {
		String dtoPath = PowerConsumptionDtoV1.class.getName();
		String jpql = """
				SELECT new %s(s.id, sl.name, sl.description, s.isActive, s.currentWatt, s.currentWattHour, s.naturalId, s.room.id)
				FROM PowerConsumption s 
				LEFT JOIN s.translations sl ON sl.langCode = :langCode 
				WHERE s.room.id = :roomId 
				ORDER BY s.createdAt DESC
				""".formatted(dtoPath);

		return entityManager.createQuery(jpql, PowerConsumptionDtoV1.class)
				.setParameter("roomId", roomId)
				.setParameter("langCode", langCode)
				.setFirstResult(page * size)
				.setMaxResults(size)
				.getResultList();
	}
}
