package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.PowerConsumptionDtoV1;
import com.iviet.ivshs.entities.PowerConsumptionV1;

@Repository
public class PowerConsumptionDaoV1 extends BaseEntityDaoV1<PowerConsumptionV1> {

	public PowerConsumptionDaoV1() {
		super(PowerConsumptionV1.class);
	}

	public Optional<PowerConsumptionDtoV1> findById(Long sensorId, String langCode) {
		String dtoPath = PowerConsumptionDtoV1.class.getName();
		String jpql = """
				SELECT new %s(s.id, sl.name, sl.description, s.isActive, s.currentWatt, s.currentWattHour, s.naturalId, s.room.id)
				FROM PowerConsumptionV1 s 
				LEFT JOIN s.sensorLans sl ON sl.langCode = :langCode 
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
				FROM PowerConsumptionV1 s 
				LEFT JOIN s.sensorLans sl ON sl.langCode = :langCode 
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

	public Long countByRoomId(Long roomId) {
		return count(root -> entityManager.getCriteriaBuilder().equal(root.get("room").get("id"), roomId));
	}
}
