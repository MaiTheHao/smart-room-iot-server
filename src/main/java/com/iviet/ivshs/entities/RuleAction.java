package com.iviet.ivshs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.base.BaseAuditEntity;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rule_action", indexes = {
        @Index(name = "idx_rule_action_rule_id", columnList = "rule_id"),
        @Index(name = "idx_rule_action_target_device", columnList = "target_device_id")
})
public class RuleAction extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false, updatable = false)
    private Rule rule;

    @Column(name = "execution_order")
    private Integer executionOrder;

    @Column(name = "target_device_id", nullable = false)
    private Long targetDeviceId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "target_device_category", nullable = false, length = 256)
    private DeviceCategory targetDeviceCategory;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "action_params")
    private JsonNode actionParams;
}
