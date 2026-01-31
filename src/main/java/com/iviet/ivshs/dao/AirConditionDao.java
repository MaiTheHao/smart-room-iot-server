package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.AirConditionDto;
import com.iviet.ivshs.entities.AirCondition;

@Repository
public class AirConditionDao extends BaseIoTDeviceDao<AirCondition> {

    public AirConditionDao() {
        super(AirCondition.class);
    }

    public boolean existsByNaturalId(String naturalId) {
        String jpql = "SELECT COUNT(ac) FROM AirCondition ac WHERE ac.naturalId = :naturalId";
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("naturalId", naturalId)
                .getSingleResult();
        return count > 0;
    }

    public boolean existsByNaturalIdAndIdNot(String naturalId, Long id) {
        String jpql = "SELECT COUNT(ac) FROM AirCondition ac WHERE ac.naturalId = :naturalId AND ac.id != :id";
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("naturalId", naturalId)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public Optional<AirConditionDto> findByNaturalId(String naturalId, String langCode) {
        String dtoPath = AirConditionDto.class.getName();
        String jpql = """
                SELECT new %s(
                    ac.id, ac.naturalId, tl.name, tl.description, ac.isActive, ac.room.id,
                    ac.power, ac.temperature, ac.mode, ac.fanSpeed, ac.swing
                )
                FROM AirCondition ac 
                LEFT JOIN ac.translations tl ON tl.langCode = :langCode 
                WHERE ac.naturalId = :naturalId
                """.formatted(dtoPath);
        
        return entityManager.createQuery(jpql, AirConditionDto.class)
                .setParameter("naturalId", naturalId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public Optional<AirConditionDto> findById(Long id, String langCode) {
        String dtoPath = AirConditionDto.class.getName();
        String jpql = """
                SELECT new %s(
                    ac.id, ac.naturalId, tl.name, tl.description, ac.isActive, ac.room.id,
                    ac.power, ac.temperature, ac.mode, ac.fanSpeed, ac.swing
                )
                FROM AirCondition ac 
                LEFT JOIN ac.translations tl ON tl.langCode = :langCode 
                WHERE ac.id = :id
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, AirConditionDto.class)
                .setParameter("id", id)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public List<AirConditionDto> findAll(int page, int size, String langCode) {
        String dtoPath = AirConditionDto.class.getName();
        String jpql = """
                SELECT new %s(
                    ac.id, ac.naturalId, tl.name, tl.description, ac.isActive, ac.room.id,
                    ac.power, ac.temperature, ac.mode, ac.fanSpeed, ac.swing
                )
                FROM AirCondition ac 
                LEFT JOIN ac.translations tl ON tl.langCode = :langCode 
                ORDER BY ac.id ASC
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, AirConditionDto.class)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public List<AirConditionDto> findAll(String langCode) {
        String dtoPath = AirConditionDto.class.getName();
        String jpql = """
                SELECT new %s(
                    ac.id, ac.naturalId, tl.name, tl.description, ac.isActive, ac.room.id,
                    ac.power, ac.temperature, ac.mode, ac.fanSpeed, ac.swing
                )
                FROM AirCondition ac 
                LEFT JOIN ac.translations tl ON tl.langCode = :langCode 
                ORDER BY ac.id ASC
                """.formatted(dtoPath);
        
        return entityManager.createQuery(jpql, AirConditionDto.class)
                .setParameter("langCode", langCode)
                .getResultList();
    }

    public List<AirConditionDto> findAllByRoomId(Long roomId, int page, int size, String langCode) {
        String dtoPath = AirConditionDto.class.getName();
        String jpql = """
                SELECT new %s(
                    ac.id, ac.naturalId, tl.name, tl.description, ac.isActive, ac.room.id,
                    ac.power, ac.temperature, ac.mode, ac.fanSpeed, ac.swing
                )
                FROM AirCondition ac 
                LEFT JOIN ac.translations tl ON tl.langCode = :langCode 
                WHERE ac.room.id = :roomId 
                ORDER BY ac.id ASC
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, AirConditionDto.class)
                .setParameter("roomId", roomId)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public List<AirConditionDto> findAllByRoomId(Long roomId, String langCode) {
        String dtoPath = AirConditionDto.class.getName();    
        String jpql = """
                SELECT new %s(
                    ac.id, ac.naturalId, tl.name, tl.description, ac.isActive, ac.room.id,
                    ac.power, ac.temperature, ac.mode, ac.fanSpeed, ac.swing
                )
                FROM AirCondition ac 
                LEFT JOIN ac.translations tl ON tl.langCode = :langCode 
                WHERE ac.room.id = :roomId 
                ORDER BY ac.id ASC
                """.formatted(dtoPath);
                
        return entityManager.createQuery(jpql, AirConditionDto.class)
                .setParameter("roomId", roomId)
                .setParameter("langCode", langCode)
                .getResultList();
    }
}