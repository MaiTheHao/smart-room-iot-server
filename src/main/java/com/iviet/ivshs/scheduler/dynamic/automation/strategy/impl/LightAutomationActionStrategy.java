package com.iviet.ivshs.scheduler.dynamic.automation.strategy.impl;

import org.springframework.stereotype.Component;

import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.scheduler.dynamic.automation.strategy.AutomationActionStrategy;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.JobActionType;
import com.iviet.ivshs.shared.enumeration.JobTargetType;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.service.control.LightControlService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LightAutomationActionStrategy implements AutomationActionStrategy {

    private final LightControlService controlService;
    private final LightDao lightDao;

    @Override
    public JobTargetType getTargetType() {
        return JobTargetType.LIGHT;
    }

    @Override
    public void handle(AutomationAction action) throws Exception {
        ActuatorPower newState = (action.getActionType() == JobActionType.ON) ? ActuatorPower.ON : ActuatorPower.OFF;

        try {
            Light light = lightDao.findById(action.getTargetId()).orElseThrow(() -> new NotFoundException("Light not found: " + action.getTargetId()));

            controlService.handlePowerControl(light.getNaturalId(), newState);
            log.info("Success: TargetId={}, State={}", action.getTargetId(), newState);
        } catch (Exception e) {
            log.warn("Failed: TargetId={}, Reason={}", action.getTargetId(), e.getMessage());
            throw e;
        }
    }
}
