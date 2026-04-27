package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.HardwareConfigDao;
import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.CreateLightDto;
import com.iviet.ivshs.dto.LightDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateLightDto;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.entities.LightLan;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.LightService;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LightServiceImpl implements LightService {

  private final LightDao lightDao;
  private final RoomDao roomDao;
  private final LanguageDao languageDao;
  private final HardwareConfigDao deviceControlDao;
  @Override
  public PaginatedResponse<LightDto> getList(int page, int size) {
    var langCode = LocalContextUtil.getCurrentLangCode();
    return new PaginatedResponse<>(
      lightDao.findAll(page, size, langCode),
      page, size, lightDao.count()
    );
  }

  @Override
  public List<LightDto> getAll() {
    var langCode = LocalContextUtil.getCurrentLangCode();
    return lightDao.findAll(langCode);
  }

  @Override
  public PaginatedResponse<LightDto> getListByRoomId(Long roomId, int page, int size) {
    if (roomId == null) throw new BadRequestException("Room ID is required");
    var langCode = LocalContextUtil.getCurrentLangCode();
    return new PaginatedResponse<>(
      lightDao.findAllByRoomId(roomId, page, size, langCode),
      page, size, lightDao.countByRoomId(roomId)
    );
  }

  @Override
  public List<LightDto> getAllByRoomId(Long roomId) {
    if (roomId == null) throw new BadRequestException("Room ID is required");
    var langCode = LocalContextUtil.getCurrentLangCode();
    return lightDao.findAllByRoomId(roomId, langCode);
  }

  @Override
  public LightDto getById(Long lightId) {
    return lightDao.findById(lightId, LocalContextUtil.getCurrentLangCode())
      .orElseThrow(() -> new NotFoundException("Light not found with ID: " + lightId));
  }

  @Override
  @Transactional
  public LightDto create(CreateLightDto dto) {
    if (dto == null) {
      throw new BadRequestException("Light data is required");
    }
    if (dto.roomId() == null) {
      throw new BadRequestException("Room ID is required");
    }

    var naturalId = dto.naturalId().trim();
    checkDuplicate(naturalId, null);

    var room = roomDao.findById(dto.roomId())
      .orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));

    var hardwareConfig = dto.deviceControlId() != null
      ? deviceControlDao.findById(dto.deviceControlId())
        .orElseThrow(() -> new NotFoundException("Device Control not found with ID: " + dto.deviceControlId()))
      : null;

    var langCode = LocalContextUtil.resolveLangCode(dto.langCode());
    if (!languageDao.existsByCode(langCode)) {
      throw new NotFoundException("Language not found: " + langCode);
    }

    if (dto.level() != null && (dto.level() < Light.MIN_LEVEL || dto.level() > Light.MAX_LEVEL)) {
      throw new BadRequestException("Light level must be between " + Light.MIN_LEVEL + " and " + Light.MAX_LEVEL);
    }

    var light = new Light();
    light.setNaturalId(naturalId);
    light.setIsActive(dto.isActive() != null ? dto.isActive() : false);
    light.setRoom(room);
    light.setHardwareConfig(hardwareConfig);
    light.setPower(dto.power() != null ? dto.power() : ActuatorPower.OFF);
    light.setLevel(dto.level() != null ? dto.level() : 0);

    var lightLan = new LightLan();
    lightLan.setLangCode(langCode);
    lightLan.setName(dto.name() != null ? dto.name().trim() : "");
    lightLan.setDescription(dto.description());
    lightLan.setOwner(light);

    light.getTranslations().add(lightLan);
    light.touch();
    lightDao.save(light);
    lightDao.flush();

    return lightDao.findById(light.getId(), langCode)
      .orElseThrow(() -> new InternalServerErrorException("Failed to retrieve created Light"));
  }

  @Override
  @Transactional
  public LightDto update(Long lightId, UpdateLightDto dto) {
    var light = getLightOrThrow(lightId);

    var langCode = LocalContextUtil.resolveLangCode(dto.langCode());
    if (!languageDao.existsByCode(langCode)) {
      throw new NotFoundException("Language not found: " + langCode);
    }

    if (StringUtils.hasText(dto.naturalId()) && !dto.naturalId().trim().equals(light.getNaturalId())) {
      checkDuplicate(dto.naturalId().trim(), lightId);
      light.setNaturalId(dto.naturalId().trim());
    }

    if (dto.roomId() != null && !dto.roomId().equals(light.getRoom().getId())) {
      var room = roomDao.findById(dto.roomId())
        .orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));
      light.setRoom(room);
    }

    if (dto.deviceControlId() != null && (light.getHardwareConfig() == null || !dto.deviceControlId().equals(light.getHardwareConfig().getId()))) {
      var dc = deviceControlDao.findById(dto.deviceControlId())
        .orElseThrow(() -> new NotFoundException("Device Control not found with ID: " + dto.deviceControlId()));
      light.setHardwareConfig(dc);
    }

    if (dto.level() != null && (dto.level() < Light.MIN_LEVEL || dto.level() > Light.MAX_LEVEL)) {
      throw new BadRequestException("Light level must be between " + Light.MIN_LEVEL + " and " + Light.MAX_LEVEL);
    }

    if (dto.isActive() != null) light.setIsActive(dto.isActive());
    if (dto.power() != null) light.setPower(dto.power());
    if (dto.level() != null) light.setLevel(dto.level());

    var lan = light.getTranslations().stream()
      .filter(ll -> langCode.equals(ll.getLangCode()))
      .findFirst()
      .orElseGet(() -> {
        var newLan = new LightLan();
        newLan.setLangCode(langCode);
        newLan.setOwner(light);
        light.getTranslations().add(newLan);
        return newLan;
      });

    if (dto.name() != null) lan.setName(dto.name().trim());
    if (dto.description() != null) lan.setDescription(dto.description());

    light.touch();
    lightDao.save(light);
    lightDao.flush();

    return lightDao.findById(lightId, langCode)
      .orElseThrow(() -> new InternalServerErrorException("Failed to retrieve updated Light"));
  }

  @Override
  @Transactional
  public void delete(Long lightId) {
    var light = getLightOrThrow(lightId);
    var hardwareConfig = light.getHardwareConfig();
    deviceControlDao.delete(hardwareConfig);
  }

  @Override
  public Long countByRoomId(Long roomId) {
    if (roomId == null) throw new BadRequestException("Room ID is required");
    return lightDao.countByRoomId(roomId);
  }

  private Light getLightOrThrow(Long lightId) {
    return lightDao.findById(lightId).orElseThrow(() -> new NotFoundException("Light not found with ID: " + lightId));
  }

  private void checkDuplicate(String naturalId, Long lightId) {
    lightDao.findByNaturalId(naturalId).ifPresent(e -> {
      if (lightId == null || !e.getId().equals(lightId)) {
        throw new BadRequestException("Natural ID already exists: " + naturalId);
      }
    });
  }
}
