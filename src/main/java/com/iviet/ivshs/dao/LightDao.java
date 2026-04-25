package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.JoinType;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.LightDto;
import com.iviet.ivshs.entities.Light;

@Repository
public class LightDao extends BaseIoTActuatorDao<Light> {

  private static final String DTO_CLASS = LightDto.class.getName();

  public LightDao() {
	super(Light.class);
  }

  @Override
  public Optional<LightDto> findByNaturalId(String naturalId, String langCode) {
	String jpql = """
		SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.power, l.level, l.room.id, l.deviceControl.id)
		FROM Light l
		LEFT JOIN l.translations ll ON ll.langCode = :langCode
		WHERE l.naturalId = :naturalId
		""".formatted(DTO_CLASS);
 
 	return entityManager.createQuery(jpql, LightDto.class)
 		.setParameter("naturalId", naturalId)
 		.setParameter("langCode", langCode)
 		.setMaxResults(1)
 		.getResultStream()
 		.findFirst();
  }

  public Optional<LightDto> findById(Long id, String langCode) {
	String jpql = """
		SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.power, l.level, l.room.id, l.deviceControl.id)
		FROM Light l
		LEFT JOIN l.translations ll ON ll.langCode = :langCode
		WHERE l.id = :id
		""".formatted(DTO_CLASS);
 
 	return entityManager.createQuery(jpql, LightDto.class)
 		.setParameter("id", id)
 		.setParameter("langCode", langCode)
 		.setMaxResults(1)
 		.getResultStream()
 		.findFirst();
  }

  public List<LightDto> findAll(int page, int size, String langCode) {
	String jpql = """
		SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.power, l.level, l.room.id, l.deviceControl.id)
		FROM Light l
		LEFT JOIN l.translations ll ON ll.langCode = :langCode
		ORDER BY l.id ASC
		""".formatted(DTO_CLASS);
 
 	return entityManager.createQuery(jpql, LightDto.class)
 		.setParameter("langCode", langCode)
 		.setFirstResult(page * size)
 		.setMaxResults(size)
 		.getResultList();
  }

  public List<LightDto> findAll(String langCode) {
	String jpql = """
		SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.power, l.level, l.room.id, l.deviceControl.id)
		FROM Light l
		LEFT JOIN l.translations ll ON ll.langCode = :langCode
		ORDER BY l.id ASC
		""".formatted(DTO_CLASS);
 
 	return entityManager.createQuery(jpql, LightDto.class)
 		.setParameter("langCode", langCode)
 		.getResultList();
  }

  public List<LightDto> findAllByRoomId(Long roomId, int page, int size, String langCode) {
	String jpql = """
		SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.power, l.level, l.room.id, l.deviceControl.id)
		FROM Light l
		LEFT JOIN l.translations ll ON ll.langCode = :langCode
		WHERE l.room.id = :roomId
		ORDER BY l.id ASC
		""".formatted(DTO_CLASS);
 
 	return entityManager.createQuery(jpql, LightDto.class)
 		.setParameter("roomId", roomId)
 		.setParameter("langCode", langCode)
 		.setFirstResult(page * size)
 		.setMaxResults(size)
 		.getResultList();
  }

  public List<LightDto> findAllByRoomId(Long roomId, String langCode) {
	String jpql = """
		SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.power, l.level, l.room.id, l.deviceControl.id)
		FROM Light l
		LEFT JOIN l.translations ll ON ll.langCode = :langCode
		WHERE l.room.id = :roomId
		ORDER BY l.id ASC
		""".formatted(DTO_CLASS);
 
 	return entityManager.createQuery(jpql, LightDto.class)
 		.setParameter("roomId", roomId)
 		.setParameter("langCode", langCode)
 		.getResultList();
  }

  @Override
  public Optional<LightDto> findByRoomAndNaturalId(Long roomId, String naturalId, String langCode) {
	String jpql = """
		SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.power, l.level, l.room.id, l.deviceControl.id)
		FROM Light l
		LEFT JOIN l.translations ll ON ll.langCode = :langCode
		WHERE l.room.id = :roomId AND l.naturalId = :naturalId
		""".formatted(DTO_CLASS);
 
 	return entityManager.createQuery(jpql, LightDto.class)
 		.setParameter("roomId", roomId)
 		.setParameter("naturalId", naturalId)
 		.setParameter("langCode", langCode)
 		.setMaxResults(1)
 		.getResultStream()
 		.findFirst();
  }
  /**
   * Fetch all active Light entities for a given gateway (client).
   * Used by energy metric collection job to iterate per-gateway devices.
   */
  public List<Light> findAllActiveByClientId(Long clientId) {
    return findAll(
      root -> entityManager.getCriteriaBuilder().and(
        entityManager.getCriteriaBuilder().equal(root.get("deviceControl").get("client").get("id"), clientId),
        entityManager.getCriteriaBuilder().isTrue(root.get("isActive"))
      ),
      (root, cq) -> {
        root.fetch("deviceControl", JoinType.LEFT).fetch("client", JoinType.LEFT);
        root.fetch("room", JoinType.LEFT);
      }
    );
  }
}
