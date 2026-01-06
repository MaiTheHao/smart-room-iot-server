package com.iviet.ivshs.dao;

import com.iviet.ivshs.entities.BaseEntity;

import java.util.Optional;

public abstract class BaseEntityDaoV1<T extends BaseEntity> extends BaseDaoV1<T> {

    protected BaseEntityDaoV1(Class<T> clazz) {
        super(clazz);
    }

    public Optional<T> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entityManager.find(clazz, id));
    }

    public T getReferenceById(Long id) {
        return entityManager.getReference(clazz, id);
    }

    public boolean existsById(Long id) {
        if (id == null) return false;
		return exists(
			root -> {
				var cb = entityManager.getCriteriaBuilder();
				return cb.equal(root.get("id"), id);
			} 
		);
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    public long count() {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(clazz)));
        return entityManager.createQuery(cq).getSingleResult();
    }
}