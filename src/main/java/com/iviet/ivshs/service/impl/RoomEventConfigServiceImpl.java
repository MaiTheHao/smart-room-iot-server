package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.RoomEventConfigDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dao.RoomEventDao;
import com.iviet.ivshs.dto.CreateRoomEventConfigDto;
import com.iviet.ivshs.dto.RoomEventConfigDto;
import com.iviet.ivshs.dto.UpdateRoomEventConfigDto;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.entities.RoomEvent;
import com.iviet.ivshs.entities.RoomEventConfig;
import com.iviet.ivshs.service.ActionService;
import com.iviet.ivshs.service.ConditionService;
import com.iviet.ivshs.service.RoomEventConfigService;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoomEventConfigServiceImpl implements RoomEventConfigService {

  private final RoomEventConfigDao roomEventConfigDao;
  private final RoomDao roomDao;
  private final RoomEventDao roomEventDao;
  private final ConditionService conditionService;
  private final ActionService actionService;

  @Override
  public RoomEventConfigDto create(Long roomId, CreateRoomEventConfigDto dto) {
    Room room = roomDao.findById(roomId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

    RoomEvent roomEvent = roomEventDao.findByCode(dto.eventCode())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room event not found"));

    if (roomEventConfigDao.findByRoomIdAndEventCode(roomId, dto.eventCode()).isPresent()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Room event config already exists for this room and event code");
    }

    RoomEventConfig config = RoomEventConfig.builder()
                  .room(room)
                  .cooldownSeconds(dto.cooldownSeconds() != null ? dto.cooldownSeconds() : 0)
                  .isActive(dto.isActive() != null ? dto.isActive() : true)
                  .roomEvent(roomEvent)
                  .build();
    
    RoomEventConfig savedConfig = roomEventConfigDao.save(config);

    return RoomEventConfigDto.fromEntity(savedConfig);
  }

  @Override
  public RoomEventConfigDto update(Long roomId, Long configId, UpdateRoomEventConfigDto dto) {
    RoomEventConfig config = getConfigByIdAndRoomId(roomId, configId);

    if (dto.isActive() != null) {
      config.setIsActive(dto.isActive());
    }
    if (dto.cooldownSeconds() != null) {
      config.setCooldownSeconds(dto.cooldownSeconds());
    }

    RoomEventConfig updatedConfig = roomEventConfigDao.save(config);

    return RoomEventConfigDto.fromEntity(updatedConfig);
  }

  @Override
  public void delete(Long roomId, Long configId) {
    RoomEventConfig config = getConfigByIdAndRoomId(roomId, configId);

    conditionService.deleteByOwner(ConditionOwnerCategory.ROOM_EVENT, String.valueOf(configId));
    actionService.deleteByOwner(ActionOwnerCategory.ROOM_EVENT, String.valueOf(configId));

    roomEventConfigDao.deleteById(configId);
  }

  @Override
  @Transactional(readOnly = true)
  public RoomEventConfigDto getById(Long roomId, Long configId) {
    RoomEventConfig config = getConfigByIdAndRoomId(roomId, configId);
    return RoomEventConfigDto.fromEntity(config);
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoomEventConfigDto> getAllByRoomId(Long roomId) {
    if (!roomDao.existsById(roomId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
    }

    List<RoomEventConfig> configs = roomEventConfigDao.findAllByRoomId(roomId);
    return configs.stream().map(RoomEventConfigDto::fromEntity).toList();
  }

  private RoomEventConfig getConfigByIdAndRoomId(Long roomId, Long configId) {
    RoomEventConfig config = roomEventConfigDao.findById(configId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room event config not found"));

    if (!config.getRoom().getId().equals(roomId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room event config not found in this room");
    }

    return config;
  }
}
