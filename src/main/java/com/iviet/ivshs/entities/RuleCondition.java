package com.iviet.ivshs.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "rule_condition")
public class RuleCondition extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

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

    @Column(name = "value_param", nullable = false)
    private String value;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "next_logic", length = 3)
    private ConditionLogic nextLogic;
}
