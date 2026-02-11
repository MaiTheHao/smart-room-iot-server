package com.iviet.ivshs.automation.handler.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.automation.handler.AutomationActionHandler;
import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.enumeration.AcPower;
import com.iviet.ivshs.enumeration.JobActionType;
import com.iviet.ivshs.enumeration.JobTargetType;
import com.iviet.ivshs.service.AirConditionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "ACTION-AIRCONDITION")
@Component
@RequiredArgsConstructor
public class AirConditionAutomationActionHandler implements AutomationActionHandler {

    private final AirConditionService service;

    @Override
    public JobTargetType getTargetType() {
        return JobTargetType.AIR_CONDITION;
    }

    @Override
    public void handle(AutomationAction action) throws Exception {
		AcPower newState = (action.getActionType() == JobActionType.ON) ? AcPower.ON : AcPower.OFF;
        
        try {
			service.controlPower(action.getTargetId(), newState);
            log.info("Success: TargetId={}, State={}", action.getTargetId(), newState);
        } catch (Exception e) {
            log.warn("Failed: TargetId={}, Reason={}", action.getTargetId(), e.getMessage());
            throw e;
        }
    }
}