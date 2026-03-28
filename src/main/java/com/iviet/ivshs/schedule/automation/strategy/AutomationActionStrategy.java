package com.iviet.ivshs.schedule.automation.strategy;

import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.enumeration.JobTargetType;

public interface AutomationActionStrategy {

    JobTargetType getTargetType();

    void handle(AutomationAction action) throws Exception;
}