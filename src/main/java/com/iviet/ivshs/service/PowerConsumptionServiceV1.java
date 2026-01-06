package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.CreatePowerConsumptionDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.PowerConsumptionDto;
import com.iviet.ivshs.dto.UpdatePowerConsumptionDto;
import com.iviet.ivshs.entities.PowerConsumption;

public interface PowerConsumptionServiceV1 {

    PaginatedResponse<PowerConsumptionDto> getListByRoom(Long roomId, int page, int size);

    PaginatedResponse<PowerConsumption> getListEntityByRoom(Long roomId, int page, int size);

    PowerConsumptionDto getById(Long powerSensorId);

    PowerConsumption getEntityById(Long powerSensorId);

    PowerConsumptionDto getByNaturalId(String naturalId);

    PowerConsumption getEntityByNaturalId(String naturalId);

    PowerConsumptionDto create(CreatePowerConsumptionDto dto);

    PowerConsumptionDto update(Long powerSensorId, UpdatePowerConsumptionDto dto);

    void delete(Long powerSensorId);
}
