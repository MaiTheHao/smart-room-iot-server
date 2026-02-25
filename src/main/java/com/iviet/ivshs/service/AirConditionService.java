package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.AirConditionDto;
import com.iviet.ivshs.dto.CreateAirConditionDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateAirConditionDto;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorSwing;

public interface AirConditionService {
    
    PaginatedResponse<AirConditionDto> getList(int page, int size);

    List<AirConditionDto> getAll();
    
    PaginatedResponse<AirConditionDto> getListByRoomId(Long roomId, int page, int size);

    List<AirConditionDto> getAllByRoomId(Long roomId);
    
    AirConditionDto getById(Long id);
    
    AirConditionDto create(CreateAirConditionDto dto);
    
    AirConditionDto update(Long id, UpdateAirConditionDto dto);
    
    void delete(Long id);
    
    @Deprecated
    void controlPower(Long id, ActuatorPower state);
    
    @Deprecated
    void controlTemperature(Long id, int temperature);
    
    @Deprecated
    void controlMode(Long id, ActuatorMode mode);
    
    @Deprecated
    void controlFanSpeed(Long id, int speed);
    
    @Deprecated
    void controlSwing(Long id, ActuatorSwing swing);

    // New
    void _v2api_handlePowerControl(Long id, ActuatorPower power);

    // New
    void _v2api_handleTogglePowerControl(Long id);

    // New
    void _v2api_handleTemperatureControl(Long id, int temperature);

    // New
    void _v2api_handleModeControl(Long id, ActuatorMode mode);

    // New
    void _v2api_handleFanSpeedControl(Long id, int speed);

    // New
    void _v2api_handleSwingControl(Long id, ActuatorSwing swing);
}