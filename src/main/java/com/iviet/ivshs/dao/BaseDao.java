package com.iviet.ivshs.dao;

import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseDao<T> {

    @PersistenceContext
    protected EntityManager entityManager;

	@Autowired
	protected JdbcTemplate jdbcTemplate;

    protected final Class<T> clazz;
	protected final int BATCH_SIZE = 50;

    protected BaseDao(Class<T> clazz) {
        this.clazz = Objects.requireNonNull(clazz, "Entity class must not be null");
    }

    public T save(T entity) {
        entityManager.persist(entity);
        return entity;
    }

	public List<T> save(List<T> entities) {
		if (entities == null || entities.isEmpty()) return List.of();
		
		int count = 0;
		for (T entity : entities) {
			entityManager.persist(entity);
			count++;
			if (count % 50 == 0) {
				entityManager.flush();
				entityManager.clear();
			}
		}
		return entities;
	}

    public T update(T entity) {
        return entityManager.merge(entity);
    }

    public void delete(T entity) {
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

	public List<T> findAll() {
		var cb = this.getCB();
		var cq = cb.createQuery(clazz);
		var root = cq.from(clazz);
		cq.select(root);
		TypedQuery<T> query = entityManager.createQuery(cq);
		return query.getResultList();
	}

	public List<T> findAll(
		Function<Root<T>, Predicate> specification,
		BiConsumer<Root<T>, CriteriaQuery<T>> queryCustomizer,
		int page,
		int size
	) {
		var cb = this.getCB();
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

	public List<T> findAll(
		Function<Root<T>, Predicate> specification,
		BiConsumer<Root<T>, CriteriaQuery<T>> queryCustomizer
	) {
		var cb = this.getCB();
		var cq = cb.createQuery(clazz);
		var root = cq.from(clazz);

		if (specification != null) {
			cq.where(specification.apply(root));
		}

		if (queryCustomizer != null) {
			queryCustomizer.accept(root, cq);
		}

		TypedQuery<T> query = entityManager.createQuery(cq);
		return query.getResultList();
	}
	
	public Optional<T> findOne(Function<Root<T>, Predicate> specification) {
        CriteriaBuilder cb = this.getCB();
        CriteriaQuery<T> cq = cb.createQuery(clazz);
        Root<T> root = cq.from(clazz);

        if(specification != null) cq.where(specification.apply(root));
		
		List<T> results = entityManager.createQuery(cq)
				.setMaxResults(1)
				.getResultList();
		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}

	public Optional<T> findOne(Function<Root<T>, Predicate> specification, BiConsumer<Root<T>, CriteriaQuery<T>> queryCustomizer) {
		CriteriaBuilder cb = this.getCB();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> root = cq.from(clazz);

		if(specification != null) cq.where(specification.apply(root));

		if (queryCustomizer != null) {
			queryCustomizer.accept(root, cq);
		}

		List<T> results = entityManager.createQuery(cq)
				.setMaxResults(1)
				.getResultList();

		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}

	public boolean exists(Function<Root<T>, Predicate> specification) {
		return findOne(specification).isPresent();
	}

	public long count(Function<Root<T>, Predicate> specification) {
		var cb = this.getCB();
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

	protected String getTableName() {
		Table table = clazz.getAnnotation(Table.class);
		if (table != null && !table.name().isEmpty()) {
			return table.name();
		} else {
			return clazz.getSimpleName();
		}
	}

	protected List<String> getColumnNames() {
		return Arrays.stream(clazz.getDeclaredFields())
			.filter(f -> (f.isAnnotationPresent(Column.class) || f.isAnnotationPresent(JoinColumn.class) || f.isAnnotationPresent(Id.class)))
			.map(f -> {
				if (f.isAnnotationPresent(Column.class)) {
					Column column = f.getAnnotation(Column.class);
					return column.name().isEmpty() ? f.getName() : column.name();
				} else if (f.isAnnotationPresent(JoinColumn.class)) {
					JoinColumn joinColumn = f.getAnnotation(JoinColumn.class);
					return joinColumn.name().isEmpty() ? f.getName() : joinColumn.name();
				} else if (f.isAnnotationPresent(Id.class)) {
					return f.getName();
				} else {
					return f.getName();
				}
			})
			.collect(Collectors.toList());

	}

	protected EntityManager getEntityManager() {
		return entityManager;
	}

	protected jakarta.persistence.criteria.CriteriaBuilder getCB() {
        return entityManager.getCriteriaBuilder();
    }
}