package com.iviet.ivshs.scheduler.automation.strategy.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.scheduler.automation.strategy.AutomationActionStrategy;
import com.iviet.ivshs.service.control.FanControlService;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.JobActionType;
import com.iviet.ivshs.shared.enumeration.JobTargetType;
import com.iviet.ivshs.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FanAutomationActionStrategy implements AutomationActionStrategy {

    private final FanControlService controlService;
    private final FanDao fanDao;

    @Override
    public JobTargetType getTargetType() {
        return JobTargetType.FAN;
    }

    @Override
    public void handle(AutomationAction action) throws Exception {
        ActuatorPower newState = (action.getActionType() == JobActionType.ON) ? ActuatorPower.ON : ActuatorPower.OFF;

        try {
            Fan fan = fanDao.findById(action.getTargetId()).orElseThrow(() -> new NotFoundException("Fan not found: " + action.getTargetId()));

            controlService.handlePowerControl(fan.getNaturalId(), newState);
            log.info("Success: TargetId={}, State={}", action.getTargetId(), newState);
        } catch (Exception e) {
            log.warn("Failed: TargetId={}, Reason={}", action.getTargetId(), e.getMessage());
            throw e;
        }
    }
}
