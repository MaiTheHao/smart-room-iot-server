package com.iviet.ivshs.dao;
import com.iviet.ivshs.entities.BaseTranslatableEntityV1;

public abstract class BaseTranslatableEntityDaoV1<T extends BaseTranslatableEntityV1<?>> extends BaseAuditEntityDaoV1<T> {
	
    protected BaseTranslatableEntityDaoV1(Class<T> clazz) {
        super(clazz);
    }
}
