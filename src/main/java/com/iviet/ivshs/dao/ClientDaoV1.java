package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.ClientV1;
import com.iviet.ivshs.enumeration.ClientTypeV1;

import jakarta.persistence.criteria.JoinType;

@Repository
public class ClientDaoV1 extends AuditableEntityDaoV1<ClientV1> {
    
    public ClientDaoV1() {
        super(ClientV1.class);
    }

    public Optional<ClientV1> findByUsername(String username) {
        return findOne(root -> entityManager.getCriteriaBuilder().equal(root.get("username"), username));
    }

    public boolean existsByUsername(String username) {
        return exists(root -> entityManager.getCriteriaBuilder().equal(root.get("username"), username));
    }

    public List<ClientV1> findGatewaysByRoomId(Long roomId, int page, int size) {
        return findAll(
            root -> entityManager.getCriteriaBuilder().and(
                entityManager.getCriteriaBuilder().equal(root.get("room").get("id"), roomId),
                entityManager.getCriteriaBuilder().equal(root.get("clientType"), ClientTypeV1.HARDWARE_GATEWAY)
            ),
            (root, cq) -> {
                root.fetch("deviceControls", JoinType.LEFT);
                cq.orderBy(entityManager.getCriteriaBuilder().desc(root.get("createdAt")));
            },
            page,
            size
        );
    }

    public long countGatewaysByRoomId(Long roomId) {
        return count(root -> entityManager.getCriteriaBuilder().and(
            entityManager.getCriteriaBuilder().equal(root.get("room").get("id"), roomId),
            entityManager.getCriteriaBuilder().equal(root.get("clientType"), ClientTypeV1.HARDWARE_GATEWAY)
        ));
    }
}
