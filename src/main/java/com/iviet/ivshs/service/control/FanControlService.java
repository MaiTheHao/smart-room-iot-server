package com.iviet.ivshs.service.control;

import com.iviet.ivshs.dto.control.ControlDeviceResult;
import com.iviet.ivshs.dto.fan.FanControlRequestBody;
import com.iviet.ivshs.shared.enumeration.ActuatorMode;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.ActuatorSwing;

public interface FanControlService extends DeviceControlServiceStrategy<FanControlRequestBody> {

    ControlDeviceResult handlePowerControl(String naturalId, ActuatorPower power);

    ControlDeviceResult handleTogglePowerControl(String naturalId);

    ControlDeviceResult handleModeControl(String naturalId, ActuatorMode mode);

    ControlDeviceResult handleSpeedControl(String naturalId, int speed);

    ControlDeviceResult handleSwingControl(String naturalId, ActuatorSwing swing);

    ControlDeviceResult handleLightControl(String naturalId, ActuatorPower light);

    ControlDeviceResult control(String naturalId, FanControlRequestBody body);
}
