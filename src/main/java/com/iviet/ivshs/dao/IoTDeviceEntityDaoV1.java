package com.iviet.ivshs.dao;

import com.iviet.ivshs.entities.BaseIoTDeviceV1;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import java.util.Optional;

public abstract class IoTDeviceEntityDaoV1<T extends BaseIoTDeviceV1> extends AuditableEntityDaoV1<T> {

    protected IoTDeviceEntityDaoV1(Class<T> clazz) {
        super(clazz);
	}

    public Optional<T> findByNaturalId(String naturalId) {
        return findOne(root -> entityManager.getCriteriaBuilder().equal(root.get("naturalId"), naturalId));
    }

    public List<T> findAllByRoomId(Long roomId, int page, int size) {
        return findAll(
            root -> entityManager.getCriteriaBuilder().equal(root.get("room").get("id"), roomId),
            (root, cq) -> {
                root.fetch("room", JoinType.LEFT);
                root.fetch("deviceControl", JoinType.LEFT);
                cq.orderBy(entityManager.getCriteriaBuilder().desc(root.get("createdAt")));
            },
            page,
            size
        );
    }

    public Optional<T> findByDeviceControlId(Long controlId) {
        return findOne(root -> entityManager.getCriteriaBuilder().equal(root.get("deviceControl").get("id"), controlId));
    }

    public long countByRoomId(Long roomId) {
        return count(root -> entityManager.getCriteriaBuilder().equal(root.get("room").get("id"), roomId));
    }
}