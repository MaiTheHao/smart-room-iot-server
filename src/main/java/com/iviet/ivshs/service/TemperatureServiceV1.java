package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.CreateTemperatureDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.TemperatureDto;
import com.iviet.ivshs.dto.UpdateTemperatureDto;
import com.iviet.ivshs.entities.Temperature;

public interface TemperatureServiceV1 {

    PaginatedResponse<TemperatureDto> getListByRoom(Long roomId, int page, int size);

    PaginatedResponse<Temperature> getListEntityByRoom(Long roomId, int page, int size);

    TemperatureDto getById(Long tempSensorId);

    Temperature getEntityById(Long tempSensorId);

    TemperatureDto getByNaturalId(String naturalId);

    Temperature getEntityByNaturalId(String naturalId);

    TemperatureDto create(CreateTemperatureDto dto);

    TemperatureDto update(Long tempSensorId, UpdateTemperatureDto dto);

    void delete(Long tempSensorId);
}
