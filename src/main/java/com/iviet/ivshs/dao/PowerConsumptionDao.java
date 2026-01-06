package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.PowerConsumptionDto;
import com.iviet.ivshs.entities.PowerConsumption;

@Repository
public class PowerConsumptionDao extends BaseIoTDeviceDao<PowerConsumption> {

	public PowerConsumptionDao() {
		super(PowerConsumption.class);
	}

	@Override
	public Optional<PowerConsumptionDto> findByNaturalId(String naturalId, String langCode) {
		String dtoPath = PowerConsumptionDto.class.getName();
		String jpql = """
				SELECT new %s(t.id, tl.name, tl.description, t.isActive, t.currentWatt, t.currentWattHour, t.naturalId, t.room.id)
				FROM PowerConsumption t
				LEFT JOIN t.translations tl ON tl.langCode = :langCode
				WHERE t.naturalId = :naturalId
				""".formatted(dtoPath);
		return entityManager.createQuery(jpql, PowerConsumptionDto.class)
				.setParameter("naturalId", naturalId)
				.setParameter("langCode", langCode)
				.setMaxResults(1)
				.getResultStream()
				.findFirst();
	}

	public Optional<PowerConsumptionDto> findById(Long sensorId, String langCode) {
		String dtoPath = PowerConsumptionDto.class.getName();
		String jpql = """
				SELECT new %s(s.id, sl.name, sl.description, s.isActive, s.currentWatt, s.currentWattHour, s.naturalId, s.room.id)
				FROM PowerConsumption s 
				LEFT JOIN s.translations sl ON sl.langCode = :langCode 
				WHERE s.id = :sensorId
				""".formatted(dtoPath);

		return entityManager.createQuery(jpql, PowerConsumptionDto.class)
				.setParameter("sensorId", sensorId)
				.setParameter("langCode", langCode)
				.setMaxResults(1)
				.getResultStream()
				.findFirst();
	}

	public List<PowerConsumptionDto> findAllByRoomId(Long roomId, int page, int size, String langCode) {
		String dtoPath = PowerConsumptionDto.class.getName();
		String jpql = """
				SELECT new %s(s.id, sl.name, sl.description, s.isActive, s.currentWatt, s.currentWattHour, s.naturalId, s.room.id)
				FROM PowerConsumption s 
				LEFT JOIN s.translations sl ON sl.langCode = :langCode 
				WHERE s.room.id = :roomId 
				ORDER BY s.createdAt DESC
				""".formatted(dtoPath);

		return entityManager.createQuery(jpql, PowerConsumptionDto.class)
				.setParameter("roomId", roomId)
				.setParameter("langCode", langCode)
				.setFirstResult(page * size)
				.setMaxResults(size)
				.getResultList();
	}
}
