package com.iviet.ivshs.entities;

import java.util.Objects;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private T owner;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseTranslation)) return false;
        BaseTranslation<?> that = (BaseTranslation<?>) o;
        boolean sameClass = getClass().equals(that.getClass());
        boolean sameId = getId() != null && that.getId() != null && Objects.equals(getId(), that.getId());
        boolean sameOwner = getOwner() != null && that.getOwner() != null && Objects.equals(getOwner().getId(), that.getOwner().getId());
        boolean sameLang = getLangCode() != null && that.getLangCode() != null && Objects.equals(getLangCode(), that.getLangCode());
        return sameOwner && sameLang && sameClass && sameId;
    }

    @Override
    public int hashCode() {
        String lang = getLangCode() != null ? getLangCode() : "";
        return Objects.hash(getClass(), getOwner().getClass(), getId(), lang);
    }
}