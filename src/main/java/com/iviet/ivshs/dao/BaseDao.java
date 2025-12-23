package com.iviet.ivshs.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Path;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.entities.BaseEntityV1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class BaseDao<T extends BaseEntityV1, PK extends Serializable> {
    @PersistenceContext
    protected EntityManager entityManager;

    protected final Class<T> clazz;
    protected final String entityClassName;

    protected BaseDao(Class<T> clazz) {
        this.clazz = Objects.requireNonNull(clazz, "Entity class must not be null");
        this.entityClassName = clazz.getSimpleName();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public Optional<T> findById(PK id) {
        return Optional.ofNullable(entityManager.find(clazz, id));
    }

    public List<T> findAll() {
        return findAll(0, Integer.MAX_VALUE);
    }

    public List<T> findAll(int page, int size) {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(clazz);
        var root = cq.from(clazz);
        cq.select(root);
        
        return entityManager.createQuery(cq)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
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

    @Transactional
    public void deleteById(PK id) {
        String entityName = clazz.getSimpleName();
        String jpql = String.format("DELETE FROM %s e WHERE e.id = :id", entityName);

        entityManager.createQuery(jpql)
                .setParameter("id", id)
                .executeUpdate();
    }

    public long count() {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(clazz)));
        return entityManager.createQuery(cq).getSingleResult();
    }

    public boolean existsById(PK id) {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(Integer.class);
        var root = cq.from(clazz);
        cq.select(cb.literal(1)).where(cb.equal(root.get("id"), id));
        
        return !entityManager.createQuery(cq).setMaxResults(1).getResultList().isEmpty();
    }

    // Hiện tại batch hoạt động vẫn tuần tự :)), do các bảng dùng IDENTITY nên là JPA không hỗ trợ batch do cơ chế của nó :))
    // Chúc bạn hạnh phúc :)) From MaiTheHao (C4F) with bugs.
    @Transactional
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            entityManager.persist(entity);
            result.add(entity);
        }
        return result;
    }

    public boolean existsByField(String fieldName, Object fieldValue) {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(clazz);
        
        cq.select(cb.count(root)).where(cb.equal(root.get(fieldName), fieldValue));
        
        Long count = entityManager.createQuery(cq).getSingleResult();
        return count != null && count > 0;
    }

    public List<T> findAllByField(String fieldName, Object fieldValue, int page, int size) {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(clazz);
        var root = cq.from(clazz);
        
        cq.where(cb.equal(root.get(fieldName), fieldValue));
        
        return entityManager.createQuery(cq)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    protected long countByField(String fieldPath, Object fieldValue) {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(clazz);
        
        Path<?> path = root;
        for (String part : fieldPath.split("\\.")) {
            path = path.get(part);
        }
        
        cq.select(cb.count(root)).where(cb.equal(path, fieldValue));
        
        Long count = entityManager.createQuery(cq).getSingleResult();
        return count != null ? count : 0L;
    }
}