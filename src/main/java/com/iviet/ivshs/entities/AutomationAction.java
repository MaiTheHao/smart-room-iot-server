package com.iviet.ivshs.entities;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.iviet.ivshs.enumeration.JobActionType;
import com.iviet.ivshs.enumeration.JobTargetType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "automation_action")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutomationAction extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "automation_id", nullable = false)
    private Automation automation;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "target_type", nullable = false, length = 256)
    private JobTargetType targetType; 

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "action_type", nullable = false, length = 256)
    private JobActionType actionType;

    @Column(name = "parameter_value")
    private String parameterValue;
    
    @Column(name = "execution_order")
    private Integer executionOrder = 0;
}