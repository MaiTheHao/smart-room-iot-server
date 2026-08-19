package com.iviet.ivshs.entities;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.base.BaseAuditEntity;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "`action`", indexes = {
    @Index(name = "idx_action_owner", columnList = "owner_category, owner_id"),
    @Index(name = "idx_action_target", columnList = "target_category, target_id")
})
public class Action extends BaseAuditEntity {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "owner_category", nullable = false, length = 50)
    private ActionOwnerCategory ownerCategory;

    @Column(name = "owner_id", nullable = false, length = 256)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "target_category", nullable = false, length = 50)
    private DeviceCategory targetCategory;

    @Column(name = "target_id", nullable = false, length = 256)
    private String targetId;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "params", columnDefinition = "TEXT")
    private JsonNode params;

    @Column(name = "execution_order", nullable = false)
    @Builder.Default
    private Integer executionOrder = 0;
}
