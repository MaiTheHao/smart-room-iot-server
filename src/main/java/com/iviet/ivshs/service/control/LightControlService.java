package com.iviet.ivshs.service.control;

import com.iviet.ivshs.dto.control.ControlDeviceResult;
import com.iviet.ivshs.dto.light.LightControlRequestBody;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;

public interface LightControlService extends DeviceControlServiceStrategy<LightControlRequestBody> {

    ControlDeviceResult handlePowerControl(String naturalId, ActuatorPower power);

    ControlDeviceResult handleTogglePowerControl(String naturalId);

    ControlDeviceResult handleLevelControl(String naturalId, int level);
}
