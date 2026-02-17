package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.CreateLightDto;
import com.iviet.ivshs.dto.LightDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateLightDto;
import com.iviet.ivshs.enumeration.ActuatorPower;

public interface LightService {

    PaginatedResponse<LightDto> getList(int page, int size);

    List<LightDto> getAll();

    PaginatedResponse<LightDto> getListByRoomId(Long roomId, int page, int size);

    List<LightDto> getAllByRoomId(Long roomId);

    LightDto getById(Long lightId);

    LightDto create(CreateLightDto createDto);

    LightDto update(Long lightId, UpdateLightDto updateDto);

    void delete(Long lightId);

    @Deprecated
    void controlPower(Long id, ActuatorPower state);

    @Deprecated
    void togglePower(Long id);

    @Deprecated
    void controlLevel(Long id, int level);

    // New
    void _v2api_handlePowerControl(Long lightId, ActuatorPower power);

    // New
    void _v2api_handleTogglePowerControl(Long lightId);

    // New
    void _v2api_handleLevelControl(Long lightId, int level);
}
