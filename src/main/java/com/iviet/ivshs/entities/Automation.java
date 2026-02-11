package com.iviet.ivshs.entities;

import java.util.ArrayList;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobDataMap;

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
public class Automation extends BaseSchedulableEntity{ 

    public static final String JOB_GROUP = "AUTOMATION_GROUP";

    @Column(nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "automation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AutomationAction> actions = new ArrayList<>();
	
    public void addAction(AutomationAction action) {
        actions.add(action);
        action.setAutomation(this);
    }

    @Override
    public String getJobName() {
        return "AutomationJob-" + this.getId();
    }

    @Override
    public String getJobGroup() {
        return JOB_GROUP;
    }

    @Override
    public Class<? extends Job> getJobClass() {
        return com.iviet.ivshs.automation.job.AutomationJob.class;
    }

    @Override
    public JobDataMap getJobDataMap() {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("id", this.getId());
        return dataMap;
    }
}