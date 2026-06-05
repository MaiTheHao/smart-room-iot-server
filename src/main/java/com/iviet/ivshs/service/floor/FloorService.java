package com.iviet.ivshs.service.floor;

import java.util.List;

import com.iviet.ivshs.dto.floor.CreateFloorDto;
import com.iviet.ivshs.dto.floor.FloorDto;
import com.iviet.ivshs.dto.system.PaginatedResponse;
import com.iviet.ivshs.dto.floor.UpdateFloorDto;
import com.iviet.ivshs.entities.Floor;

public interface FloorService {
    PaginatedResponse<FloorDto> getList(int page, int size);
    List<FloorDto> getAll();
    FloorDto getById(Long id);
    Long getVersionById(Long id);
    Floor getEntityById(Long id);
    FloorDto create(CreateFloorDto dto);
    FloorDto update(Long id, UpdateFloorDto dto);
    FloorDto patchUpdate(Long id, UpdateFloorDto dto);
    void delete(Long id);
}
