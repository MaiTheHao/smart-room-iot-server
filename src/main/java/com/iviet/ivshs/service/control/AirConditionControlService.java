package com.iviet.ivshs.service.control;

import com.iviet.ivshs.dto.aircondition.AirConditionControlRequestBody;
import com.iviet.ivshs.dto.control.ControlDeviceResult;
import com.iviet.ivshs.shared.enumeration.ActuatorMode;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.ActuatorSwing;

public interface AirConditionControlService extends DeviceControlServiceStrategy<AirConditionControlRequestBody> {

  ControlDeviceResult handlePowerControl(String naturalId, ActuatorPower power);

  ControlDeviceResult handleTogglePowerControl(String naturalId);

  ControlDeviceResult handleTemperatureControl(String naturalId, int temperature);

  ControlDeviceResult handleModeControl(String naturalId, ActuatorMode mode);

  ControlDeviceResult handleFanSpeedControl(String naturalId, int speed);

  ControlDeviceResult handleSwingControl(String naturalId, ActuatorSwing swing);
}
