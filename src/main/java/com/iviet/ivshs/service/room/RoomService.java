package com.iviet.ivshs.service.room;

import com.iviet.ivshs.dto.room.RoomDeviceCountDto;

import java.util.List;

import com.iviet.ivshs.dto.room.CreateRoomDto;
import com.iviet.ivshs.dto.room.RoomDto;
import com.iviet.ivshs.dto.room.RoomStatusDto;
import com.iviet.ivshs.dto.system.PaginatedResponse;
import com.iviet.ivshs.dto.room.UpdateRoomDto;
import com.iviet.ivshs.entities.Room;

public interface RoomService {
  PaginatedResponse<RoomDto> getListByFloor(Long floorId, int page, int size);

  PaginatedResponse<RoomDto> getList(int page, int size);

  List<RoomDto> getAllByFloor(Long floorId);

  List<RoomDto> getAll();

  List<com.iviet.ivshs.dto.room.RoomDeviceCountDto> getDeviceCountsByRoomIds(List<Long> roomIds);

  RoomDto getById(Long roomId);

  Long getVersionById(Long roomId);

  Room getEntityById(Long roomId);

  RoomDto getByCode(String roomCode);

  Room getEntityByCode(String roomCode);

  RoomDto create(Long floorId, CreateRoomDto dto);

  RoomDto update(Long roomId, UpdateRoomDto dto);

  RoomDto patchUpdate(Long roomId, UpdateRoomDto dto);

  void delete(Long roomId);

  RoomStatusDto getRoomStatus(Long roomId);
}
