package com.iviet.ivshs.dao;

import com.iviet.ivshs.entities.BaseAuditEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import java.time.Instant;
import java.util.List;

public abstract class BaseAuditEntityDaoV1<T extends BaseAuditEntity> extends BaseEntityDaoV1<T> {

    protected BaseAuditEntityDaoV1(Class<T> clazz) {
        super(clazz);
    }

    public List<T> findAll(int page, int size) {
        return findAll(
            null,
            (root, cq) -> cq.orderBy(entityManager.getCriteriaBuilder().desc(root.get("createdAt"))),
            page,
            size
        );
    }

    public List<T> findAllByCreatedBy(String username, int page, int size) {
        return findAll(
            root -> entityManager.getCriteriaBuilder().equal(root.get("createdBy"), username),
            (root, cq) -> {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                cq.orderBy(cb.desc(root.get("createdAt")));
            },
            page,
            size
        );
    }

	public List<T> findByCreatedAtBetween(Instant start, Instant end, int page, int size) {
		return findAll(
			root -> entityManager.getCriteriaBuilder().between(root.get("createdAt"), start, end),
			(root, cq) -> {
				CriteriaBuilder cb = entityManager.getCriteriaBuilder();
				cq.orderBy(cb.desc(root.get("createdAt")));
			},
			page,
			size
		);
	}

    public List<T> findByUpdatedAtBetween(Instant start, Instant end, int page, int size) {
        return findAll(
            root -> entityManager.getCriteriaBuilder().between(root.get("updatedAt"), start, end),
            (root, cq) -> {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                cq.orderBy(cb.desc(root.get("updatedAt")));
            },
            page,
            size
        );
    }

    public long countByCreatedBy(String username) {
		return count(
			root -> entityManager.getCriteriaBuilder().equal(root.get("createdBy"), username)
		);
	}

	public long countByCreatedAtBetween(Instant start, Instant end) {
		return count(
			root -> entityManager.getCriteriaBuilder().between(root.get("createdAt"), start, end)
		);
        }

	public long countByUpdatedAtBetween(Instant start, Instant end) {
		return count(
			root -> entityManager.getCriteriaBuilder().between(root.get("updatedAt"), start, end)
		);
	}
}