package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.LightControlRequestBody;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.service.strategy.DeviceControlServiceStrategy;

public interface LightControlService extends DeviceControlServiceStrategy<LightControlRequestBody> {
    
    void handlePowerControl(String naturalId, ActuatorPower power);

    void handleTogglePowerControl(String naturalId);

    void handleLevelControl(String naturalId, int level);
}
