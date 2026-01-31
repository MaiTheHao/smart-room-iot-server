package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.AirConditionDto;
import com.iviet.ivshs.dto.CreateAirConditionDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateAirConditionDto;
import com.iviet.ivshs.enumeration.AcMode;
import com.iviet.ivshs.enumeration.AcPower;
import com.iviet.ivshs.enumeration.AcSwing;

public interface AirConditionService {
    
    PaginatedResponse<AirConditionDto> getList(int page, int size);

    List<AirConditionDto> getAll();
    
    PaginatedResponse<AirConditionDto> getListByRoomId(Long roomId, int page, int size);

    List<AirConditionDto> getAllByRoomId(Long roomId);
    
    AirConditionDto getById(Long id);
    
    AirConditionDto create(CreateAirConditionDto dto);
    
    AirConditionDto update(Long id, UpdateAirConditionDto dto);
    
    void delete(Long id);
    
    void controlPower(Long id, AcPower state);
    
    void controlTemperature(Long id, int temperature);
    
    void controlMode(Long id, AcMode mode);
    
    void controlFanSpeed(Long id, int speed);
    
    void controlSwing(Long id, AcSwing swing);
}