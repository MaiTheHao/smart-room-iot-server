package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.CreateRoomDtoV1;
import com.iviet.ivshs.dto.RoomDtoV1;
import com.iviet.ivshs.dto.PaginatedResponseV1;
import com.iviet.ivshs.dto.UpdateRoomDtoV1;

public interface RoomServiceV1 {
    PaginatedResponseV1<RoomDtoV1> getListByFloor(Long floorId, int page, int size);
    RoomDtoV1 getById(Long roomId);
    RoomDtoV1 create(Long floorId, CreateRoomDtoV1 dto);
    RoomDtoV1 update(Long roomId, UpdateRoomDtoV1 dto);
    void delete(Long roomId);
}
