package com.iviet.ivshs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseTranslationV1 extends BaseAuditEntityV1 {

    private static final long serialVersionUID = 1L;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "lang_code", length = 10, nullable = false)
    private String langCode;
}
