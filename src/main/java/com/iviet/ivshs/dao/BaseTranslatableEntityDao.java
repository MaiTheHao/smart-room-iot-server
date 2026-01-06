package com.iviet.ivshs.dao;
import com.iviet.ivshs.entities.BaseTranslatableEntity;

public abstract class BaseTranslatableEntityDao<T extends BaseTranslatableEntity<?>> extends BaseAuditEntityDao<T> {
	
    protected BaseTranslatableEntityDao(Class<T> clazz) {
        super(clazz);
    }
}
