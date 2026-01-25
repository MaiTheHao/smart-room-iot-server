package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.RoomDto;
import com.iviet.ivshs.entities.Room;

@Repository
public class RoomDao extends BaseAuditEntityDao<Room> {
    
    public RoomDao() {
        super(Room.class);
    }

    public Optional<Room> findByCode(String code) {
        return findOne(root -> entityManager.getCriteriaBuilder().equal(root.get("code"), code));
    }

    public Optional<RoomDto> findByCode(String code, String langCode) {
        String dtoPath = RoomDto.class.getName();

        String jpql = """
                SELECT new %s(r.id, r.code, rlan.name, rlan.description, r.floor.id)
                FROM Room r
                LEFT JOIN r.translations rlan ON rlan.langCode = :langCode
                WHERE r.code = :code
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, RoomDto.class)
                .setParameter("code", code)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public Optional<RoomDto> findById(Long roomId, String langCode) {
        String dtoPath = RoomDto.class.getName();

        String jpql = """
                SELECT new %s(r.id, r.code, rlan.name, rlan.description, r.floor.id)
                FROM Room r
                LEFT JOIN r.translations rlan ON rlan.langCode = :langCode
                WHERE r.id = :roomId
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, RoomDto.class)
                .setParameter("roomId", roomId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public List<RoomDto> findAllByFloorId(Long floorId, int page, int size, String langCode) {
        String dtoPath = RoomDto.class.getName();

        String jpql = """
                SELECT new %s(r.id, r.code, rlan.name, rlan.description, r.floor.id)
                FROM Room r
                LEFT JOIN r.translations rlan ON rlan.langCode = :langCode
                WHERE r.floor.id = :floorId
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, RoomDto.class)
                .setParameter("floorId", floorId)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public List<RoomDto> findAllByFloorId(Long floorId, String langCode) {
        String dtoPath = RoomDto.class.getName();

        String jpql = """
                SELECT new %s(r.id, r.code, rlan.name, rlan.description, r.floor.id)
                FROM Room r
                LEFT JOIN r.translations rlan ON rlan.langCode = :langCode
                WHERE r.floor.id = :floorId
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, RoomDto.class)
                .setParameter("floorId", floorId)
                .setParameter("langCode", langCode)
                .getResultList();
    }

    public List<RoomDto> findAll(String langCode) {
        String dtoPath = RoomDto.class.getName();

        String jpql = """
                SELECT new %s(r.id, r.code, rlan.name, rlan.description, r.floor.id)
                FROM Room r
                LEFT JOIN r.translations rlan ON rlan.langCode = :langCode
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, RoomDto.class)
                .setParameter("langCode", langCode)
                .getResultList();
    }

    public List<RoomDto> findAll(int page, int size, String langCode) {
        String dtoPath = RoomDto.class.getName();

        String jpql = """
                SELECT new %s(r.id, r.code, rlan.name, rlan.description, r.floor.id)
                FROM Room r
                LEFT JOIN r.translations rlan ON rlan.langCode = :langCode
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, RoomDto.class)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long countByFloorId(Long floorId) {
        return count(root -> entityManager.getCriteriaBuilder().equal(root.get("floor").get("id"), floorId));
    }
}
