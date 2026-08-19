package com.iviet.ivshs.entities;

import java.util.ArrayList;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobDataMap;

import com.iviet.ivshs.entities.base.BaseSchedulableEntity;
import com.iviet.ivshs.scheduler.dynamic.base.GenericSchedulableJob;
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
@Table(name = "rule", indexes = { @Index(name = "idx_rule_status", columnList = "is_active") })
public class Rule extends BaseSchedulableEntity {

    public static final String JOB_GROUP = "RULE_ENGINE_SYSTEM";

    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;





    @Override
    public String getJobName() {
        return "QUARZTJOB_RULE_" + this.getId();
    }

    @Override
    public String getJobGroup() {
        return JOB_GROUP;
    }

    @Override
    public Class<? extends Job> getJobClass() {
        return GenericSchedulableJob.class;
    }

    @Override
    public JobDataMap getJobDataMap() {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("id", this.getId());
        dataMap.put("type", "RULE");
        return dataMap;
    }
}
