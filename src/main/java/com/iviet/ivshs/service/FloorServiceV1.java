package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.CreateFloorDtoV1;
import com.iviet.ivshs.dto.FloorDtoV1;
import com.iviet.ivshs.dto.PaginatedResponseV1;
import com.iviet.ivshs.dto.UpdateFloorDtoV1;

public interface FloorServiceV1 {
    PaginatedResponseV1<FloorDtoV1> getList(int page, int size);
    FloorDtoV1 getById(Long id);
    FloorDtoV1 create(CreateFloorDtoV1 dto);
    FloorDtoV1 update(Long id, UpdateFloorDtoV1 dto);
    void delete(Long id);
}
