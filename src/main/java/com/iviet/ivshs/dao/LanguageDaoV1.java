package com.iviet.ivshs.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.iviet.ivshs.entities.LanguageV1;

@Repository
public class LanguageDaoV1 extends AuditableEntityDaoV1<LanguageV1> {
    
    public LanguageDaoV1() {
        super(LanguageV1.class);
    }

    public Optional<LanguageV1> findByCode(String code) {
        return findOne(root -> entityManager.getCriteriaBuilder().equal(root.get("code"), code));
    }
    
    public boolean existsByCode(String code) {
        return exists(root -> entityManager.getCriteriaBuilder().equal(root.get("code"), code));
    }
}
