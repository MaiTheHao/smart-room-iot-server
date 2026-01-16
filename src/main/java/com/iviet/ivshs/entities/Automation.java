package com.iviet.ivshs.entities;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "automation", indexes = {
    @Index(name = "idx_auto_status", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Automation extends BaseAuditEntity { 

    @Column(nullable = false)
    private String name;

    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "automation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AutomationAction> actions = new ArrayList<>();
	
    public void addAction(AutomationAction action) {
        actions.add(action);
        action.setAutomation(this);
    }
}