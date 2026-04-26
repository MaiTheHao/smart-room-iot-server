package com.iviet.ivshs.entities;

import java.util.Objects;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;


@MappedSuperclass
@Getter
@Setter
public abstract class BaseTranslation<T extends BaseTranslatableEntity<?>> extends BaseAuditEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "name", nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "description")
    private String description;

    @Column(name = "lang_code", length = 10, nullable = false) 
    private String langCode;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "owner_id", nullable = false)
    private T owner;

    @Override
    public void touch() {
        super.touch();
        if (this.owner != null) {
            this.owner.touch();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseTranslation<?> that = (BaseTranslation<?>) o;
        
        if (getId() != null && that.getId() != null) {
            return Objects.equals(getId(), that.getId());
        }
        
        boolean sameOwner = Objects.equals(getOwner(), that.getOwner());
        boolean sameLang = Objects.equals(getLangCode(), that.getLangCode());
        
        return sameOwner && sameLang;
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return Objects.hash(getClass(), getId());
        }
        String lang = getLangCode() != null ? getLangCode() : "";
        Object ownerKey = getOwner() != null ? getOwner().getClass() : null;
        return Objects.hash(getClass(), ownerKey, lang);
    }
}