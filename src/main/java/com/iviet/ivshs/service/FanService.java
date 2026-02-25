package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.CreateFanDto;
import com.iviet.ivshs.dto.FanDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateFanDto;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorState;
import com.iviet.ivshs.enumeration.ActuatorSwing;

public interface FanService {
    
    PaginatedResponse<FanDto> getList(int page, int size);

    List<FanDto> getAll();
    
    PaginatedResponse<FanDto> getListByRoomId(Long roomId, int page, int size);

    List<FanDto> getAllByRoomId(Long roomId);
    
    FanDto getById(Long id);
    
    FanDto create(CreateFanDto dto);
    
    FanDto update(Long id, UpdateFanDto dto);
    
    void delete(Long id);

    // New
    void _v2api_handlePowerControl(Long id, ActuatorPower power);

    // New
    void _v2api_handleTogglePowerControl(Long id);

    // New
    void _v2api_handleModeControl(Long id, ActuatorMode mode);

    // New
    void _v2api_handleSpeedControl(Long id, int speed);

    // New
    void _v2api_handleSwingControl(Long id, ActuatorSwing swing);

    // New
    void _v2api_handleLightControl(Long id, ActuatorState light);
}
