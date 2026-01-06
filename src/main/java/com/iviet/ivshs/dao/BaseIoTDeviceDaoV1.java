package com.iviet.ivshs.dao;

import com.iviet.ivshs.entities.BaseIoTDevice;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import java.util.Optional;

public abstract class BaseIoTDeviceDaoV1<T extends BaseIoTDevice<?>> extends BaseTranslatableEntityDaoV1<T> {

    protected BaseIoTDeviceDaoV1(Class<T> clazz) {
        super(clazz);
	}

    @Override
    public Optional<T> findById(Long id) {
        return findOne(
            root -> entityManager.getCriteriaBuilder().equal(root.get("id"), id),
            (root, cq) -> {
                root.fetch("room", JoinType.LEFT);
                root.fetch("deviceControl", JoinType.LEFT).fetch("client", JoinType.LEFT);
            }
        );
    }

    public Optional<T> findByNaturalId(String naturalId) {
        return findOne(root -> entityManager.getCriteriaBuilder().equal(root.get("naturalId"), naturalId), (root, cq) -> {
            root.fetch("room", JoinType.LEFT);
            root.fetch("deviceControl", JoinType.LEFT).fetch("client", JoinType.LEFT);
        });
    }

    public abstract Optional<?> findByNaturalId(String naturalId, String langCode);

    public List<T> findAllByNaturalIds(List<String> naturalIds) {
        if (naturalIds == null || naturalIds.isEmpty()) {
            return List.of();
        }
        return findAll(root -> root.get("naturalId").in(naturalIds), (root, cq) -> {
            root.fetch("room", JoinType.LEFT);
            root.fetch("deviceControl", JoinType.LEFT).fetch("client", JoinType.LEFT);
            cq.orderBy(entityManager.getCriteriaBuilder().desc(root.get("createdAt")));
        });
    }

    public List<T> findAllByRoomId(Long roomId, int page, int size) {
        return findAll(
            root -> entityManager.getCriteriaBuilder().equal(root.get("room").get("id"), roomId),
            (root, cq) -> {
                root.fetch("room", JoinType.LEFT);
                root.fetch("deviceControl", JoinType.LEFT).fetch("client", JoinType.LEFT);
                cq.orderBy(entityManager.getCriteriaBuilder().desc(root.get("createdAt")));
            },
            page,
            size
        );
    }

    public Optional<T> findByDeviceControlId(Long controlId) {
        return findOne(root -> entityManager.getCriteriaBuilder().equal(root.get("deviceControl").get("id"), controlId), (root, cq) -> {
            root.fetch("room", JoinType.LEFT);
            root.fetch("deviceControl", JoinType.LEFT).fetch("client", JoinType.LEFT);
        });
    }

    public long countByRoomId(Long roomId) {
        return count(root -> entityManager.getCriteriaBuilder().equal(root.get("room").get("id"), roomId));
    }
}