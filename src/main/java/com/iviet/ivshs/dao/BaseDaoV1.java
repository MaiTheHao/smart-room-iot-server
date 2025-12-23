package com.iviet.ivshs.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class BaseDaoV1<T> {

    @PersistenceContext
    protected EntityManager entityManager;

    protected final Class<T> clazz;

    protected BaseDaoV1(Class<T> clazz) {
        this.clazz = Objects.requireNonNull(clazz, "Entity class must not be null");
    }

    @Transactional
    public T save(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Transactional
    public T update(T entity) {
        return entityManager.merge(entity);
    }

    @Transactional
    public void delete(T entity) {
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

	public List<T> findAll(
		Function<Root<T>, Predicate> specification,
		BiConsumer<Root<T>, CriteriaQuery<T>> queryCustomizer,
		int page,
		int size
	) {
		var cb = entityManager.getCriteriaBuilder();
		var cq = cb.createQuery(clazz);
		var root = cq.from(clazz);

		if (specification != null) {
			cq.where(specification.apply(root));
		}

		if (queryCustomizer != null) {
			queryCustomizer.accept(root, cq);
		}

		TypedQuery<T> query = entityManager.createQuery(cq);

		return query
			.setFirstResult(page * size)
			.setMaxResults(size)
			.getResultList();
	}
	
	public Optional<T> findOne(Function<Root<T>, Predicate> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(clazz);
        Root<T> root = cq.from(clazz);

        if(specification != null) cq.where(specification.apply(root));

        return entityManager.createQuery(cq)
                .getResultStream()
                .findFirst();
    }

	public boolean exists(Function<Root<T>, Predicate> specification) {
		return findOne(specification).isPresent();
	}

	public long count(Function<Root<T>, Predicate> specification) {
		var cb = entityManager.getCriteriaBuilder();
		var cq = cb.createQuery(Long.class);
		var root = cq.from(clazz);
		cq.select(cb.count(root));

		if (specification != null) cq.where(specification.apply(root));

		return entityManager.createQuery(cq).getSingleResult();
	}

	public long count() {
		return count(null);
	}

    public void flush() {
        entityManager.flush();
    }

    public void clear() {
        entityManager.clear();
    }
}