package com.iviet.ivshs.schedule.automation.strategy.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.JobActionType;
import com.iviet.ivshs.enumeration.JobTargetType;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.schedule.automation.strategy.AutomationActionStrategy;
import com.iviet.ivshs.service.AirConditionControlService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "AUTOMATION_ACTION_AIRCONDITION")
@Component
@RequiredArgsConstructor
public class AirConditionAutomationActionStrategy implements AutomationActionStrategy {

    private final AirConditionControlService controlService;
    private final AirConditionDao airConditionDao;

    @Override
    public JobTargetType getTargetType() {
        return JobTargetType.AIR_CONDITION;
    }

    @Override
    public void handle(AutomationAction action) throws Exception {
        ActuatorPower newState = (action.getActionType() == JobActionType.ON) ? ActuatorPower.ON : ActuatorPower.OFF;
        
        try {
            AirCondition ac = airConditionDao.findById(action.getTargetId())
                    .orElseThrow(() -> new NotFoundException("Air Conditioner not found: " + action.getTargetId()));
            
            controlService.handlePowerControl(ac.getNaturalId(), newState);
            log.info("Success: TargetId={}, State={}", action.getTargetId(), newState);
        } catch (Exception e) {
            log.warn("Failed: TargetId={}, Reason={}", action.getTargetId(), e.getMessage());
            throw e;
        }
    }
}