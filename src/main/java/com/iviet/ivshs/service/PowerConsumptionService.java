package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.CreatePowerConsumptionDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.PowerConsumptionDto;
import com.iviet.ivshs.dto.UpdatePowerConsumptionDto;
import com.iviet.ivshs.entities.PowerConsumption;

public interface PowerConsumptionService {

    PaginatedResponse<PowerConsumptionDto> getList(int page, int size);

    List<PowerConsumptionDto> getAll();

    PaginatedResponse<PowerConsumptionDto> getListByRoomId(Long roomId, int page, int size);

    List<PowerConsumptionDto> getAllByRoomId(Long roomId);

    PaginatedResponse<PowerConsumption> getListEntityByRoomId(Long roomId, int page, int size);

    PowerConsumptionDto getById(Long id);

    PowerConsumption getEntityById(Long id);

    PowerConsumptionDto getByNaturalId(String naturalId);

    PowerConsumption getEntityByNaturalId(String naturalId);

    PowerConsumptionDto create(CreatePowerConsumptionDto dto);

    PowerConsumptionDto update(Long id, UpdatePowerConsumptionDto dto);

    void delete(Long id);

    Long countByRoomId(Long roomId);
}
