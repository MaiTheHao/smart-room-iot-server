package com.iviet.ivshs.entities;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private JobTargetType targetType; 

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private JobActionType actionType;

    @Column(name = "parameter_value")
    private String parameterValue;
    
    @Column(name = "execution_order")
    private Integer executionOrder = 0;
}