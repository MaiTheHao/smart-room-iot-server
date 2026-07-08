package com.iviet.ivshs.service.light;

import java.util.List;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.dto.light.CreateLightDto;
import com.iviet.ivshs.dto.light.LightDto;
import com.iviet.ivshs.dto.light.UpdateLightDto;
import com.iviet.ivshs.service.control.DeviceMetadataServiceStrategy;

public interface LightService extends DeviceMetadataServiceStrategy {

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
