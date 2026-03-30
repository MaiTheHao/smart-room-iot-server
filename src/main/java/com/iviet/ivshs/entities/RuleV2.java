package com.iviet.ivshs.entities;

import java.util.ArrayList;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobDataMap;

import com.iviet.ivshs.schedule.rule.RuleV2Job;

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
@Table(name = "rule_v2", indexes = {
        @Index(name = "idx_rule_v2_status", columnList = "is_active")
})
public class RuleV2 extends BaseSchedulableEntity {

    public static final String JOB_GROUP = "RULEV2_ENGINE_SYSTEM";

    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @OneToMany(mappedBy = "ruleV2", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RuleConditionV2> conditions = new ArrayList<>();

    @OneToMany(mappedBy = "ruleV2", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RuleActionV2> actions = new ArrayList<>();

    public void addCondition(RuleConditionV2 condition) {
        conditions.add(condition);
        condition.setRuleV2(this);
    }

    public void addAction(RuleActionV2 action) {
        actions.add(action);
        action.setRuleV2(this);
    }

    @Override
    public String getJobName() {
        return "RuleV2Job-" + this.getId();
    }

    @Override
    public String getJobGroup() {
        return JOB_GROUP;
    }

    @Override
    public Class<? extends Job> getJobClass() {
        return RuleV2Job.class;
    }

    @Override
    public JobDataMap getJobDataMap() {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("id", this.getId());
        return dataMap;
    }
}
