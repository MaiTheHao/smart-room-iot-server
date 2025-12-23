package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.dto.RoomDtoV1;
import com.iviet.ivshs.entities.RoomV1;

@Repository
public class RoomDaoV1 extends AuditableEntityDaoV1<RoomV1> {
    
    public RoomDaoV1() {
        super(RoomV1.class);
    }

    public Optional<RoomV1> findByCode(String code) {
        return findOne(root -> entityManager.getCriteriaBuilder().equal(root.get("code"), code));
    }

    public Optional<RoomDtoV1> findByCode(String code, String langCode) {
        String dtoPath = RoomDtoV1.class.getName();

        String jpql = """
                SELECT new %s(r.id, r.code, rlan.name, rlan.description, r.floor.id)
                FROM RoomV1 r
                LEFT JOIN r.roomLans rlan ON rlan.langCode = :langCode
                WHERE r.code = :code
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, RoomDtoV1.class)
                .setParameter("code", code)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public Optional<RoomDtoV1> findById(Long roomId, String langCode) {
        String dtoPath = RoomDtoV1.class.getName();

        String jpql = """
                SELECT new %s(r.id, r.code, rlan.name, rlan.description, r.floor.id)
                FROM RoomV1 r
                LEFT JOIN r.roomLans rlan ON rlan.langCode = :langCode
                WHERE r.id = :roomId
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, RoomDtoV1.class)
                .setParameter("roomId", roomId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    public List<RoomDtoV1> findAllByFloorId(Long floorId, int page, int size, String langCode) {
        String dtoPath = RoomDtoV1.class.getName();

        String jpql = """
                SELECT new %s(r.id, r.code, rlan.name, rlan.description, r.floor.id)
                FROM RoomV1 r
                LEFT JOIN r.roomLans rlan ON rlan.langCode = :langCode
                WHERE r.floor.id = :floorId
                """.formatted(dtoPath);

        return entityManager.createQuery(jpql, RoomDtoV1.class)
                .setParameter("floorId", floorId)
                .setParameter("langCode", langCode)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long countByFloorId(Long floorId) {
        return count(root -> entityManager.getCriteriaBuilder().equal(root.get("floor").get("id"), floorId));
    }
}
