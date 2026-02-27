package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.LightControlRequestBody;
import com.iviet.ivshs.enumeration.ActuatorPower;

public interface LightControlService {
    
    void handlePowerControl(String naturalId, ActuatorPower power);

    void handleTogglePowerControl(String naturalId);
  
    void handleLevelControl(String naturalId, int level);

    void control(String naturalId, LightControlRequestBody params);
}
