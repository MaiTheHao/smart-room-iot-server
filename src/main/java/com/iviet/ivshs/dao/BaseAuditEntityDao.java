package com.iviet.ivshs.dao;

import com.iviet.ivshs.entities.BaseAuditEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public abstract class BaseAuditEntityDao<T extends BaseAuditEntity> extends BaseEntityDao<T> {

    protected BaseAuditEntityDao(Class<T> clazz) {
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

    public Optional<Long> findVersionById(Long id) {
        String jpql = "SELECT e.v FROM " + clazz.getSimpleName() + " e WHERE e.id = :id"; 
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
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