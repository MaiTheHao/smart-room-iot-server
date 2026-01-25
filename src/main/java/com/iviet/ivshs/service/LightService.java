package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.CreateLightDto;
import com.iviet.ivshs.dto.LightDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateLightDto;

public interface LightService {

    PaginatedResponse<LightDto> getList(int page, int size);

    List<LightDto> getAll();

    PaginatedResponse<LightDto> getListByRoomId(Long roomId, int page, int size);

    List<LightDto> getAllByRoomId(Long roomId);

    LightDto getById(Long lightId);

    LightDto create(CreateLightDto createDto);

    LightDto update(Long lightId, UpdateLightDto updateDto);

    void delete(Long lightId);

    void handleStateControl(Long lightId, boolean newState);

    void handleToggleStateControl(Long lightId);

    void handleSetLevelControl(Long lightId, int newLevel);
}
