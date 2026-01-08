package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.CreateFloorDto;
import com.iviet.ivshs.dto.FloorDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateFloorDto;
import com.iviet.ivshs.entities.Floor;

public interface FloorService {
    PaginatedResponse<FloorDto> getList(int page, int size);
    FloorDto getById(Long id);
    Floor getEntityById(Long id);
    FloorDto create(CreateFloorDto dto);
    FloorDto update(Long id, UpdateFloorDto dto);
    void delete(Long id);
}
