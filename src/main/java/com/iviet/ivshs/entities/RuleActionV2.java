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
import com.iviet.ivshs.enumeration.DeviceCategory;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rule_action_v2", indexes = {
        @Index(name = "idx_rule_action_v2_rule_id", columnList = "rule_v2_id"),
        @Index(name = "idx_rule_action_v2_target_device", columnList = "target_device_id")
})
public class RuleActionV2 extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_v2_id", nullable = false, updatable = false)
    private RuleV2 ruleV2;

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
