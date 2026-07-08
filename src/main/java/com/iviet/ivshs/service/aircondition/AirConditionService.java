package com.iviet.ivshs.service.aircondition;

import java.util.List;

import com.iviet.ivshs.dto.aircondition.AirConditionDto;
import com.iviet.ivshs.dto.aircondition.CreateAirConditionDto;
import com.iviet.ivshs.dto.aircondition.UpdateAirConditionDto;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.service.control.DeviceMetadataServiceStrategy;

public interface AirConditionService extends DeviceMetadataServiceStrategy {

    PaginatedResponse<AirConditionDto> getList(int page, int size);

    List<AirConditionDto> getAll();

    PaginatedResponse<AirConditionDto> getListByRoomId(Long roomId, int page, int size);

    List<AirConditionDto> getAllByRoomId(Long roomId);

    AirConditionDto getById(Long id);

    AirConditionDto create(CreateAirConditionDto dto);

    AirConditionDto update(Long id, UpdateAirConditionDto dto);

    void delete(Long id);

    Long countByRoomId(Long roomId);
}
