package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.AirConditionControlRequestBody;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.service.strategy.DeviceControlServiceStrategy;

import com.iviet.ivshs.dto.ControlDeviceResult;

public interface AirConditionControlService extends DeviceControlServiceStrategy<AirConditionControlRequestBody> {

  ControlDeviceResult handlePowerControl(String naturalId, ActuatorPower power);

  ControlDeviceResult handleTogglePowerControl(String naturalId);

  ControlDeviceResult handleTemperatureControl(String naturalId, int temperature);

  ControlDeviceResult handleModeControl(String naturalId, ActuatorMode mode);

  ControlDeviceResult handleFanSpeedControl(String naturalId, int speed);

  ControlDeviceResult handleSwingControl(String naturalId, ActuatorSwing swing);
}
