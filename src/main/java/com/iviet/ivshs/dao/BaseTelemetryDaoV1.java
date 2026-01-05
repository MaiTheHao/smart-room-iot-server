package com.iviet.ivshs.dao;

import java.util.List;
import java.util.Optional;

import com.iviet.ivshs.entities.BaseTelemetryValueV1;

public abstract class BaseTelemetryDaoV1<T extends BaseTelemetryValueV1<?>> extends BaseEntityDaoV1<T> {
	
	protected BaseTelemetryDaoV1(Class<T> entityClass) {
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
