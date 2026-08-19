package com.iviet.ivshs.entities;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.base.BaseAuditEntity;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
import com.iviet.ivshs.shared.enumeration.ConditionLogic;
import com.iviet.ivshs.shared.enumeration.ConditionOperator;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
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
@Table(name = "`condition`", indexes = {
    @Index(name = "idx_condition_owner", columnList = "owner_category, owner_id"),
    @Index(name = "idx_condition_source", columnList = "source_category, source_target_id")
})
public class Condition extends BaseAuditEntity {

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "owner_category", nullable = false, length = 50)
    private ConditionOwnerCategory ownerCategory;

    @Column(name = "owner_id", nullable = false, length = 256)
    private String ownerId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "source_category", nullable = false, length = 50)
    private ConditionDataSource sourceCategory;

    @Column(name = "source_target_id", nullable = false, length = 256)
    private String sourceTargetId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "source_target_type", nullable = false, length = 50)
    private DeviceCategory sourceTargetType;

    @Column(name = "property", nullable = false, length = 100)
    private String property;

    @Column(name = "operator", nullable = false, length = 10)
    private ConditionOperator operator;

    @Column(name = "value", nullable = false, length = 256)
    private String value;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "extra_params", columnDefinition = "TEXT")
    private JsonNode extraParams;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "next_logic", length = 10)
    private ConditionLogic nextLogic;
}
