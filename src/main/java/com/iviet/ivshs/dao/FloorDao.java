package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.FloorDto;
import com.iviet.ivshs.entities.Floor;

@Repository
public class FloorDao extends BaseAuditEntityDao<Floor> {
    
    public FloorDao() {
        super(Floor.class);
    }

    public Optional<Floor> findByCode(String code) {
        return findOne(root -> entityManager.getCriteriaBuilder().equal(root.get("code"), code));
    }

    public Optional<FloorDto> findByCode(String code, String langCode) {
        String dtoClassPath = FloorDto.class.getName();

        String jpql = """
                SELECT new %s(f.id, flan.name, f.code, flan.description, f.level)
                FROM Floor f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                WHERE f.code = :code
                """.formatted(dtoClassPath);

        List<FloorDto> results = entityManager.createQuery(jpql, FloorDto.class)
                .setParameter("code", code)
                .setParameter("langCode", langCode)
                .setMaxResults(1) 
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<FloorDto> findById(Long floorId, String langCode) {
        String dtoClassPath = FloorDto.class.getName();

        String jpql = """
                SELECT new %s(f.id, flan.name, f.code, flan.description, f.level)
                FROM Floor f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                WHERE f.id = :floorId
                """.formatted(dtoClassPath);
        
        List<FloorDto> results = entityManager.createQuery(jpql, FloorDto.class)
                .setParameter("floorId", floorId)
                .setParameter("langCode", langCode)
                .setMaxResults(1) 
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<FloorDto> findAll(int page, int size, String langCode) {
        String dtoClassPath = FloorDto.class.getName();

        String jpql = """
                SELECT new %s(f.id, flan.name, f.code, flan.description, f.level)
                FROM Floor f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                """.formatted(dtoClassPath);

        List<FloorDto> results = entityManager.createQuery(jpql, FloorDto.class)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();

        return results;
    }

    public List<FloorDto> findAll(String langCode) {
        String dtoClassPath = FloorDto.class.getName();

        String jpql = """
                SELECT new %s(f.id, flan.name, f.code, flan.description, f.level)
                FROM Floor f
                LEFT JOIN f.translations flan ON flan.langCode = :langCode
                """.formatted(dtoClassPath);

        List<FloorDto> results = entityManager.createQuery(jpql, FloorDto.class)
                .setParameter("langCode", langCode)
                .getResultList();

        return results;
    }
}
