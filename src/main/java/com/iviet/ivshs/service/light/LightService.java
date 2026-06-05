package com.iviet.ivshs.service.light;

import java.util.List;

import com.iviet.ivshs.dto.light.CreateLightDto;
import com.iviet.ivshs.dto.light.LightDto;
import com.iviet.ivshs.dto.system.PaginatedResponse;
import com.iviet.ivshs.dto.light.UpdateLightDto;

public interface LightService {

    PaginatedResponse<LightDto> getList(int page, int size);

    List<LightDto> getAll();

    PaginatedResponse<LightDto> getListByRoomId(Long roomId, int page, int size);

    List<LightDto> getAllByRoomId(Long roomId);
    
    LightDto getById(Long lightId);

    LightDto create(CreateLightDto createDto);
    
    LightDto update(Long lightId, UpdateLightDto updateDto);
    
    void delete(Long lightId);

    Long countByRoomId(Long roomId);
}
