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
import com.iviet.ivshs.enumeration.ConditionLogic;
import com.iviet.ivshs.enumeration.ConditionOperator;
import com.iviet.ivshs.enumeration.RuleDataSource;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rule_condition_v2", indexes = {
        @Index(name = "idx_rule_condition_v2_rule_id", columnList = "rule_v2_id")
})
public class RuleConditionV2 extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_v2_id", nullable = false, updatable = false)
    private RuleV2 ruleV2;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "data_source", nullable = false, length = 256)
    private RuleDataSource dataSource;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "resource_param")
    private JsonNode resourceParam; // JSON: { "deviceId": 1, "category": "FAN", "property": "level" }

    @Column(name = "operator", nullable = false, length = 5)
    private ConditionOperator operator;

    @Column(name = "value_param", nullable = false, length = 256)
    private String value;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "next_logic", length = 3)
    private ConditionLogic nextLogic;
}
