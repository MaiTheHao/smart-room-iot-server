package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.LightDto;
import com.iviet.ivshs.entities.Light;

@Repository
public class LightDao extends BaseIoTDeviceDao<Light> {
    
    public LightDao() {
        super(Light.class);
    }

	@Override
	public Optional<LightDto> findByNaturalId(String naturalId, String langCode) {
		String dtoPath = LightDto.class.getName();
		String jpql = """
				SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.level, l.room.id)
				FROM Light l 
				LEFT JOIN l.translations ll ON ll.langCode = :langCode 
				WHERE l.naturalId = :naturalId
				""".formatted(dtoPath);
		return entityManager.createQuery(jpql, LightDto.class)
				.setParameter("naturalId", naturalId)
				.setParameter("langCode", langCode)
				.setMaxResults(1)
				.getResultStream()
				.findFirst();
	}

    public Optional<LightDto> findById(Long lightId, String langCode) {
        String dtoPath = LightDto.class.getName();

        String jpql = """
                SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.level, l.room.id)
                FROM Light l 
                LEFT JOIN l.translations ll ON ll.langCode = :langCode 
                WHERE l.id = :lightId
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, LightDto.class)
                .setParameter("lightId", lightId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public List<LightDto> findAll(int page, int size, String langCode) {
        String dtoPath = LightDto.class.getName();

        String jpql = """
                SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.level, l.room.id)
                FROM Light l 
                LEFT JOIN l.translations ll ON ll.langCode = :langCode 
                ORDER BY l.id ASC
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, LightDto.class)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public List<LightDto> findAllByRoomId(Long roomId, int page, int size, String langCode) {
        String dtoPath = LightDto.class.getName();

        String jpql = """
                SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.level, l.room.id)
                FROM Light l 
                LEFT JOIN l.translations ll ON ll.langCode = :langCode 
                WHERE l.room.id = :roomId 
                ORDER BY l.id ASC
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, LightDto.class)
                .setParameter("roomId", roomId)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }
}
