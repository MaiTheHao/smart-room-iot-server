package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.FanControlRequestBody;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorState;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.service.strategy.DeviceControlServiceStrategy;

import com.iviet.ivshs.dto.ControlDeviceResult;

public interface FanControlService extends DeviceControlServiceStrategy<FanControlRequestBody> {

    ControlDeviceResult handlePowerControl(String naturalId, ActuatorPower power);

    ControlDeviceResult handleTogglePowerControl(String naturalId);

    ControlDeviceResult handleModeControl(String naturalId, ActuatorMode mode);

    ControlDeviceResult handleSpeedControl(String naturalId, int speed);

    ControlDeviceResult handleSwingControl(String naturalId, ActuatorSwing swing);

    ControlDeviceResult handleLightControl(String naturalId, ActuatorState light);

    ControlDeviceResult control(String naturalId, FanControlRequestBody body);
}
