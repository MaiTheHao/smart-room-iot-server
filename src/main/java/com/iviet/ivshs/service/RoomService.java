package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.CreateRoomDto;
import com.iviet.ivshs.dto.RoomDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateRoomDto;
import com.iviet.ivshs.entities.Room;

public interface RoomService {
    PaginatedResponse<RoomDto> getListByFloor(Long floorId, int page, int size);
    PaginatedResponse<RoomDto> getList(int page, int size);
    List<RoomDto> getAllByFloor(Long floorId);
    List<RoomDto> getAll();
    RoomDto getById(Long roomId);
    Room getEntityById(Long roomId);
    RoomDto getByCode(String roomCode);
    Room getEntityByCode(String roomCode);
    RoomDto create(Long floorId, CreateRoomDto dto);
    RoomDto update(Long roomId, UpdateRoomDto dto);
    void delete(Long roomId);
}
