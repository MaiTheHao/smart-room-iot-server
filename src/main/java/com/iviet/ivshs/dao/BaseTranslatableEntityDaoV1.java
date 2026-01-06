package com.iviet.ivshs.dao;
import com.iviet.ivshs.entities.BaseTranslatableEntity;

public abstract class BaseTranslatableEntityDaoV1<T extends BaseTranslatableEntity<?>> extends BaseAuditEntityDaoV1<T> {
	
    protected BaseTranslatableEntityDaoV1(Class<T> clazz) {
        super(clazz);
    }
}
