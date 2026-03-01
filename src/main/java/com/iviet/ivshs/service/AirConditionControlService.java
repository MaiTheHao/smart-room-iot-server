package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.AirConditionControlRequestBody;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorSwing;

public interface AirConditionControlService extends DeviceControlStrategy<AirConditionControlRequestBody> {

  void handlePowerControl(String naturalId, ActuatorPower power);

  void handleTogglePowerControl(String naturalId);

  void handleTemperatureControl(String naturalId, int temperature);

  void handleModeControl(String naturalId, ActuatorMode mode);

  void handleFanSpeedControl(String naturalId, int speed);

  void handleSwingControl(String naturalId, ActuatorSwing swing);
}
