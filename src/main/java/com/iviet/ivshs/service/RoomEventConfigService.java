package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.CreateRoomEventConfigDto;
import com.iviet.ivshs.dto.RoomEventConfigDto;
import com.iviet.ivshs.dto.UpdateRoomEventConfigDto;
import java.util.List;

public interface RoomEventConfigService {
  RoomEventConfigDto create(Long roomId, CreateRoomEventConfigDto dto);

  RoomEventConfigDto update(Long roomId, Long configId, UpdateRoomEventConfigDto dto);

  void delete(Long roomId, Long configId);

  RoomEventConfigDto getById(Long roomId, Long configId);

  List<RoomEventConfigDto> getAllByRoomId(Long roomId);
}
