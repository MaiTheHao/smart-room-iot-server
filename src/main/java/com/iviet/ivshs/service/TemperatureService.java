package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.CreateTemperatureDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.TemperatureDto;
import com.iviet.ivshs.dto.UpdateTemperatureDto;
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
