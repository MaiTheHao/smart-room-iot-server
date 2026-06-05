package com.iviet.ivshs.dao.base;

import com.iviet.ivshs.entities.base.BaseTranslatableEntity;

public abstract class BaseTranslatableEntityDao<T extends BaseTranslatableEntity<?>> extends BaseAuditEntityDao<T> {

    protected BaseTranslatableEntityDao(Class<T> clazz) {
        super(clazz);
    }
}
