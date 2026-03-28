package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import com.iviet.ivshs.entities.BaseTelemetryValue;

public abstract class BaseTelemetryDao<T extends BaseTelemetryValue<?>> extends BaseEntityDao<T> {
	
	protected BaseTelemetryDao(Class<T> entityClass) {
		super(entityClass);
	}

	public abstract void saveAndForget(Long sensorId, T entity);
	public abstract void saveAndForget(Long sensorId, List<T> entities);

	public Optional<T> findById(Long id) {
		return findOne(
			root -> entityManager.getCriteriaBuilder().equal(root.get("id"), id),
			(root, cq) -> {
				root.fetch("sensor");
			}
		);
	}
}
