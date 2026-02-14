package com.iviet.ivshs.schedule.handler;

import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.enumeration.JobTargetType;

public interface AutomationActionHandler {

    JobTargetType getTargetType();

    void handle(AutomationAction action) throws Exception;
}