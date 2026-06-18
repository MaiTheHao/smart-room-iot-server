package com.iviet.ivshs.scheduler.dynamic.automation.strategy.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dto.aircondition.AirConditionControlRequestBody;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.scheduler.dynamic.automation.strategy.AutomationActionStrategy;
import com.iviet.ivshs.service.control.AirConditionControlService;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.JobActionType;
import com.iviet.ivshs.shared.enumeration.JobTargetType;
import com.iviet.ivshs.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
            AirCondition ac = airConditionDao.findById(action.getTargetId()).orElseThrow(() -> new NotFoundException("Air Conditioner not found: " + action.getTargetId()));

            controlService.control(ac.getNaturalId(), new AirConditionControlRequestBody(newState, null, null, null, null));
            log.info("Success: TargetId={}, State={}", action.getTargetId(), newState);
        } catch (Exception e) {
            log.warn("Failed: TargetId={}, Reason={}", action.getTargetId(), e.getMessage());
            throw e;
        }
    }
}
