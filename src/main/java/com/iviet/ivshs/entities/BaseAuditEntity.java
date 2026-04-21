package com.iviet.ivshs.entities;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity extends BaseEntity implements Versionable {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    protected Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    protected Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 256, updatable = false)
    protected String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 256)
    protected String updatedBy;

    @Version
    @Column(name = "v", nullable = false, columnDefinition = "bigint default 0")
    protected Long version = 0L;

    public Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }
}