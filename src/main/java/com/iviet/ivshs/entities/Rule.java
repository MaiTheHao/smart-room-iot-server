package com.iviet.ivshs.entities;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.enumeration.DeviceCategory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rule", indexes = {
        @Index(name = "idx_rule_room", columnList = "room_id"),
        @Index(name = "idx_rule_status", columnList = "is_active")
})
public class Rule extends BaseAuditEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "target_device_id", nullable = false)
    private Long targetDeviceId;
    
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "target_device_category", nullable = false, length = 256)
    private DeviceCategory targetDeviceCategory;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "action_params")
    private JsonNode actionParams;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RuleCondition> conditions = new ArrayList<>();

    public void addCondition(RuleCondition condition) {
        conditions.add(condition);
        condition.setRule(this);
    }
}
