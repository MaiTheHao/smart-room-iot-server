package com.iviet.ivshs.service.temperature;

import java.util.List;

import com.iviet.ivshs.dto.temperature.CreateTemperatureDto;
import com.iviet.ivshs.dto.system.PaginatedResponse;
import com.iviet.ivshs.dto.temperature.TemperatureDto;
import com.iviet.ivshs.dto.temperature.UpdateTemperatureDto;
import com.iviet.ivshs.entities.Temperature;

public interface TemperatureService {

    PaginatedResponse<TemperatureDto> getList(int page, int size);
    
    List<TemperatureDto> getAll();

    PaginatedResponse<TemperatureDto> getListByRoomId(Long roomId, int page, int size);

    List<TemperatureDto> getAllByRoomId(Long roomId);

    PaginatedResponse<Temperature> getListEntityByRoomId(Long roomId, int page, int size);

    TemperatureDto getById(Long id);

    Temperature getEntityById(Long id);

    TemperatureDto getByNaturalId(String naturalId);

    Temperature getEntityByNaturalId(String naturalId);

    TemperatureDto create(CreateTemperatureDto dto);

    TemperatureDto update(Long id, UpdateTemperatureDto dto);

    void delete(Long id);

    Long countByRoomId(Long roomId);
}
