package com.iviet.ivshs.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.Language;

@Repository
public class LanguageDao extends BaseAuditEntityDao<Language> {
    
    public LanguageDao() {
        super(Language.class);
    }

    public Optional<Language> findByCode(String code) {
        return findOne(root -> entityManager.getCriteriaBuilder().equal(root.get("code"), code));
    }
    
    public boolean existsByCode(String code) {
        return exists(root -> entityManager.getCriteriaBuilder().equal(root.get("code"), code));
    }
}
