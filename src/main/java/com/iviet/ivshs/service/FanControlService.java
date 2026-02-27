package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.FanControlRequestBody;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorState;
import com.iviet.ivshs.enumeration.ActuatorSwing;

public interface FanControlService {

    void handlePowerControl(String naturalId, ActuatorPower power);

    void handleTogglePowerControl(String naturalId);

    void handleModeControl(String naturalId, ActuatorMode mode);

    void handleSpeedControl(String naturalId, int speed);

    void handleSwingControl(String naturalId, ActuatorSwing swing);

    void handleLightControl(String naturalId, ActuatorState light);

    void control(String naturalId, FanControlRequestBody body);
}
