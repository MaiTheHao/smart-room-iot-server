package com.iviet.ivshs.entities;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseAuditEntityV1 extends BaseEntityV1 {

    private static final long serialVersionUID = 1L;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by", length = 256, updatable = false)
    private String createdBy;

    @Column(name = "updated_by", length = 256)
    private String updatedBy;

    @Version
    @Column(name = "v", nullable = false)
    private Long version = 0L;
}