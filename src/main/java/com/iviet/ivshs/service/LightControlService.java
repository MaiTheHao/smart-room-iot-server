package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.LightControlRequestBody;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.service.strategy.DeviceControlServiceStrategy;

import com.iviet.ivshs.dto.ControlDeviceResult;

public interface LightControlService extends DeviceControlServiceStrategy<LightControlRequestBody> {
    
    ControlDeviceResult handlePowerControl(String naturalId, ActuatorPower power);

    ControlDeviceResult handleTogglePowerControl(String naturalId);

    ControlDeviceResult handleLevelControl(String naturalId, int level);
}
