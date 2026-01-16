package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.CreateLightDto;
import com.iviet.ivshs.dto.LightDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateLightDto;

public interface LightService {

    PaginatedResponse<LightDto> getList(int page, int size);

    PaginatedResponse<LightDto> getListByRoomId(Long roomId, int page, int size);

    LightDto getById(Long lightId);

    LightDto create(CreateLightDto createDto);

    LightDto update(Long lightId, UpdateLightDto updateDto);

    void delete(Long lightId);

    void handleStateControl(Long lightId, boolean newState);

    void handleToggleStateControl(Long lightId);

    void handleSetLevelControl(Long lightId, int newLevel);
}
