package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.DeviceControlDao;
import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.CreateFanDto;
import com.iviet.ivshs.dto.FanDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateFanDto;
import com.iviet.ivshs.dto.UpdateFanIrDto;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanIr;
import com.iviet.ivshs.entities.FanLan;
import com.iviet.ivshs.enumeration.FanType;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.FanService;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FanServiceImpl implements FanService {

  private final FanDao fanDao;
  private final RoomDao roomDao;
  private final DeviceControlDao deviceControlDao;
  private final LanguageDao languageDao;

  @Override
  public PaginatedResponse<FanDto> getList(int page, int size) {
    var langCode = LocalContextUtil.getCurrentLangCode();
    var data = fanDao.findAll(page, size, langCode);
    var totalElements = fanDao.count();
    return new PaginatedResponse<>(data, page, size, totalElements);
  }

  @Override
  public List<FanDto> getAll() {
    return fanDao.findAll(LocalContextUtil.getCurrentLangCode());
  }

  @Override
  public PaginatedResponse<FanDto> getListByRoomId(Long roomId, int page, int size) {
    var langCode = LocalContextUtil.getCurrentLangCode();
    var data = fanDao.findAllByRoomId(roomId, page, size, langCode);
    var totalElements = fanDao.countByRoomId(roomId);
    return new PaginatedResponse<>(data, page, size, totalElements);
  }

  @Override
  public List<FanDto> getAllByRoomId(Long roomId) {
    return fanDao.findAllByRoomId(roomId, LocalContextUtil.getCurrentLangCode());
  }

  @Override
  public FanDto getByRoomAndNaturalId(Long roomId, String naturalId) {
    return fanDao.findByRoomAndNaturalId(roomId, naturalId, LocalContextUtil.getCurrentLangCode())
      .orElseThrow(() -> new NotFoundException("Fan not found with Room ID: " + roomId + " and Natural ID: " + naturalId));
  }

  @Override
  public FanDto getById(Long id) {
    return fanDao.findById(id, LocalContextUtil.getCurrentLangCode())
      .orElseThrow(() -> new NotFoundException("Fan not found with ID: " + id));
  }

  @Override
  @Transactional
  public FanDto create(CreateFanDto dto) {
    var naturalId = dto.naturalId().trim();
    if (fanDao.existsByNaturalId(naturalId)) {
      throw new BadRequestException("Natural ID already exists: " + naturalId);
    }

    var room = roomDao.findById(dto.roomId())
      .orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));

    var langCode = LocalContextUtil.resolveLangCode(dto.langCode());
    if (!languageDao.existsByCode(langCode)) {
      throw new NotFoundException("Language not found: " + langCode);
    }

    var fan = dto.toEntity();
    fan.setRoom(room);

    if (dto.deviceControlId() != null) {
      fan.setDeviceControl(deviceControlDao.findById(dto.deviceControlId())
        .orElseThrow(() -> new NotFoundException("Device Control not found with ID: " + dto.deviceControlId())));
    }

    fan.touch();
    fanDao.save(fan);
    fanDao.flush();

    return fanDao.findById(fan.getId(), langCode)
      .orElseThrow(() -> new InternalServerErrorException("Failed to retrieve created Fan"));
  }

  @Override
  @Transactional
  public FanDto update(Long id, UpdateFanDto dto) {
    var fan = getFanOrThrow(id);

    var langCode = LocalContextUtil.resolveLangCode(dto.langCode());
    if (!languageDao.existsByCode(langCode)) {
      throw new NotFoundException("Language not found: " + langCode);
    }

    if (dto.type() != null) {
      FanType existingType = (fan instanceof FanIr) ? FanType.IR : FanType.GPIO;
      if (dto.type() != existingType) {
        throw new BadRequestException("Cannot change Fan type after creation. Please delete and create a new device.");
      }
    }

    if (StringUtils.hasText(dto.naturalId()) && !dto.naturalId().trim().equals(fan.getNaturalId())) {
      if (fanDao.existsByNaturalId(dto.naturalId().trim())) {
        throw new BadRequestException("Natural ID already exists: " + dto.naturalId());
      }
      fan.setNaturalId(dto.naturalId().trim());
    }

    if (dto.roomId() != null && !dto.roomId().equals(fan.getRoom().getId())) {
      var room = roomDao.findById(dto.roomId())
        .orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));
      fan.setRoom(room);
    }

    if (dto.deviceControlId() != null) {
      fan.setDeviceControl(deviceControlDao.findById(dto.deviceControlId())
        .orElseThrow(() -> new NotFoundException("Device Control not found with ID: " + dto.deviceControlId())));
    }

    if (dto.isActive() != null) fan.setIsActive(dto.isActive());
    if (dto.power() != null) fan.setPower(dto.power());

    if (fan instanceof FanIr fanIr && dto instanceof UpdateFanIrDto irDto) {
      if (irDto.mode() != null) fanIr.setMode(irDto.mode());
      if (irDto.speed() != null) fanIr.setSpeed(irDto.speed());
      if (irDto.swing() != null) fanIr.setSwing(irDto.swing());
      if (irDto.light() != null) fanIr.setLight(irDto.light());
    }

    var lan = fan.getTranslations().stream()
      .filter(l -> langCode.equals(l.getLangCode()))
      .findFirst()
      .orElseGet(() -> {
        var newLan = new FanLan();
        newLan.setLangCode(langCode);
        newLan.setOwner(fan);
        fan.getTranslations().add(newLan);
        return newLan;
      });

    if (StringUtils.hasText(dto.name())) lan.setName(dto.name().trim());
    if (dto.description() != null) lan.setDescription(dto.description());

    fan.touch();
    fanDao.save(fan);
    fanDao.flush();

    return fanDao.findById(id, langCode)
      .orElseThrow(() -> new InternalServerErrorException("Failed to retrieve updated Fan"));
  }

  @Override
  @Transactional
  public void delete(Long id) {
    var fan = getFanOrThrow(id);
    var deviceControl = fan.getDeviceControl();
    deviceControlDao.delete(deviceControl);
  }

  @Override
  public Long countByRoomId(Long roomId) {
    if (roomId == null) throw new BadRequestException("Room ID is required");
    return fanDao.countByRoomId(roomId);
  }

  private Fan getFanOrThrow(Long id) {
    return fanDao.findById(id).orElseThrow(() -> new NotFoundException("Fan not found with ID: " + id));
  }
}
