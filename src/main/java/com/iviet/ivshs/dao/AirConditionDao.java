package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.AirConditionDto;
import com.iviet.ivshs.entities.AirCondition;

@Repository
public class AirConditionDao extends BaseIoTActuatorDao<AirCondition> {

	private static final String DTO_CLASS = AirConditionDto.class.getName();

	public AirConditionDao() {
		super(AirCondition.class);
	}

	@Override
	public Optional<AirConditionDto> findByNaturalId(String naturalId, String langCode) {
		String jpql = """
				SELECT new %s(
						ac.id, ac.naturalId, tl.name, tl.description, ac.isActive, ac.room.id,
						ac.power, ac.temperature, ac.mode, ac.fanSpeed, ac.swing, ac.deviceControl.id
				)
				FROM AirCondition ac
				LEFT JOIN ac.translations tl ON tl.langCode = :langCode
				WHERE ac.naturalId = :naturalId
				""".formatted(DTO_CLASS);

		return entityManager.createQuery(jpql, AirConditionDto.class)
				.setParameter("naturalId", naturalId)
				.setParameter("langCode", langCode)
				.setMaxResults(1)
				.getResultStream()
				.findFirst();
	}

	public Optional<AirConditionDto> findById(Long id, String langCode) {
		String jpql = """
				SELECT new %s(
						ac.id, ac.naturalId, tl.name, tl.description, ac.isActive, ac.room.id,
						ac.power, ac.temperature, ac.mode, ac.fanSpeed, ac.swing, ac.deviceControl.id
				)
				FROM AirCondition ac
				LEFT JOIN ac.translations tl ON tl.langCode = :langCode
				WHERE ac.id = :id
				""".formatted(DTO_CLASS);

		return entityManager.createQuery(jpql, AirConditionDto.class)
				.setParameter("id", id)
				.setParameter("langCode", langCode)
				.setMaxResults(1)
				.getResultStream()
				.findFirst();
	}

	public List<AirConditionDto> findAll(int page, int size, String langCode) {
		String jpql = """
				SELECT new %s(
						ac.id, ac.naturalId, tl.name, tl.description, ac.isActive, ac.room.id,
						ac.power, ac.temperature, ac.mode, ac.fanSpeed, ac.swing, ac.deviceControl.id
				)
				FROM AirCondition ac
				LEFT JOIN ac.translations tl ON tl.langCode = :langCode
				ORDER BY ac.id ASC
				""".formatted(DTO_CLASS);

		return entityManager.createQuery(jpql, AirConditionDto.class)
				.setParameter("langCode", langCode)
				.setFirstResult(page * size)
				.setMaxResults(size)
				.getResultList();
	}

	public List<AirConditionDto> findAll(String langCode) {
		String jpql = """
				SELECT new %s(
						ac.id, ac.naturalId, tl.name, tl.description, ac.isActive, ac.room.id,
						ac.power, ac.temperature, ac.mode, ac.fanSpeed, ac.swing, ac.deviceControl.id
				)
				FROM AirCondition ac
				LEFT JOIN ac.translations tl ON tl.langCode = :langCode
				ORDER BY ac.id ASC
				""".formatted(DTO_CLASS);

		return entityManager.createQuery(jpql, AirConditionDto.class)
				.setParameter("langCode", langCode)
				.getResultList();
	}

	public List<AirConditionDto> findAllByRoomId(Long roomId, int page, int size, String langCode) {
		String jpql = """
				SELECT new %s(
						ac.id, ac.naturalId, tl.name, tl.description, ac.isActive, ac.room.id,
						ac.power, ac.temperature, ac.mode, ac.fanSpeed, ac.swing, ac.deviceControl.id
				)
				FROM AirCondition ac
				LEFT JOIN ac.translations tl ON tl.langCode = :langCode
				WHERE ac.room.id = :roomId
				ORDER BY ac.id ASC
				""".formatted(DTO_CLASS);

		return entityManager.createQuery(jpql, AirConditionDto.class)
				.setParameter("roomId", roomId)
				.setParameter("langCode", langCode)
				.setFirstResult(page * size)
				.setMaxResults(size)
				.getResultList();
	}

	public List<AirConditionDto> findAllByRoomId(Long roomId, String langCode) {
		String jpql = """
				SELECT new %s(
						ac.id, ac.naturalId, tl.name, tl.description, ac.isActive, ac.room.id,
						ac.power, ac.temperature, ac.mode, ac.fanSpeed, ac.swing, ac.deviceControl.id
				)
				FROM AirCondition ac
				LEFT JOIN ac.translations tl ON tl.langCode = :langCode
				WHERE ac.room.id = :roomId
				ORDER BY ac.id ASC
				""".formatted(DTO_CLASS);

		return entityManager.createQuery(jpql, AirConditionDto.class)
				.setParameter("roomId", roomId)
				.setParameter("langCode", langCode)
				.getResultList();
	}

	@Override
	public Optional<AirConditionDto> findByRoomAndNaturalId(Long roomId, String naturalId, String langCode) {
		String jpql = """
				SELECT new %s(
						ac.id, ac.naturalId, tl.name, tl.description, ac.isActive, ac.room.id,
						ac.power, ac.temperature, ac.mode, ac.fanSpeed, ac.swing, ac.deviceControl.id
				)
				FROM AirCondition ac
				LEFT JOIN ac.translations tl ON tl.langCode = :langCode
				WHERE ac.room.id = :roomId AND ac.naturalId = :naturalId
				""".formatted(DTO_CLASS);

		return entityManager.createQuery(jpql, AirConditionDto.class)
				.setParameter("roomId", roomId)
				.setParameter("naturalId", naturalId)
				.setParameter("langCode", langCode)
				.setMaxResults(1)
				.getResultStream()
				.findFirst();
	}
}