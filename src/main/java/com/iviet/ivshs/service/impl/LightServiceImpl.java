package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.DeviceControlDao;
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
import com.iviet.ivshs.enumeration.GatewayCommand;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.ControlService;
import com.iviet.ivshs.service.LightService;
import com.iviet.ivshs.util.LocalContextUtil;
import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.util.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LightServiceImpl implements LightService {

  private final LightDao lightDao;
  private final RoomDao roomDao;
  private final LanguageDao languageDao;
  private final DeviceControlDao deviceControlDao;
  private final ControlService controlService;

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

    var deviceControl = dto.deviceControlId() != null
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
    light.setDeviceControl(deviceControl);
    light.setPower(dto.power() != null ? dto.power() : ActuatorPower.OFF);
    light.setLevel(dto.level() != null ? dto.level() : 0);

    var lightLan = new LightLan();
    lightLan.setLangCode(langCode);
    lightLan.setName(dto.name() != null ? dto.name().trim() : "");
    lightLan.setDescription(dto.description());
    lightLan.setOwner(light);

    light.getTranslations().add(lightLan);
    lightDao.save(light);

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

    if (dto.deviceControlId() != null && (light.getDeviceControl() == null || !dto.deviceControlId().equals(light.getDeviceControl().getId()))) {
      var dc = deviceControlDao.findById(dto.deviceControlId())
        .orElseThrow(() -> new NotFoundException("Device Control not found with ID: " + dto.deviceControlId()));
      light.setDeviceControl(dc);
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

    lightDao.save(light);

    return lightDao.findById(lightId, langCode)
      .orElseThrow(() -> new InternalServerErrorException("Failed to retrieve updated Light"));
  }

  @Override
  @Transactional
  public void delete(Long lightId) {
    var light = getLightOrThrow(lightId);
    var deviceControl = light.getDeviceControl();
    deviceControlDao.delete(deviceControl);
  }

  @Override
  public Long countByRoomId(Long roomId) {
    if (roomId == null) throw new BadRequestException("Room ID is required");
    return lightDao.countByRoomId(roomId);
  }

  @Override
  @Transactional
  public void _v2api_handlePowerControl(Long lightId, ActuatorPower power) {
    var light = getLightOrThrow(lightId);
    var client = light.getDeviceControl().getClient();

    var actualPower = (power != null) ? power : ActuatorPower.OFF;
    light.setPower(actualPower);
    lightDao.save(light);

    var url = UrlConstant.getControlLightPowerUrlV2(client.getIpAddress(), light.getNaturalId());
    var payload = Map.of("data", actualPower);

    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  public void _v2api_handleTogglePowerControl(Long lightId) {
    var light = getLightOrThrow(lightId);
    var client = light.getDeviceControl().getClient();

    var currentPower = light.getPower() != null ? light.getPower() : ActuatorPower.OFF;
    var newPower = (currentPower == ActuatorPower.ON) ? ActuatorPower.OFF : ActuatorPower.ON;

    light.setPower(newPower);
    lightDao.save(light);

    var url = UrlConstant.getControlLightPowerUrlV2(client.getIpAddress(), light.getNaturalId());
    var payload = Map.of("data", newPower);

    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  public void _v2api_handleLevelControl(Long lightId, int level) {
    if (level < Light.MIN_LEVEL || level > Light.MAX_LEVEL) {
      throw new BadRequestException("Light level must be between " + Light.MIN_LEVEL + " and " + Light.MAX_LEVEL);
    }
    var light = getLightOrThrow(lightId);
    var client = light.getDeviceControl().getClient();

    light.setLevel(level);
    lightDao.save(light);

    var url = UrlConstant.getControlLightLevelUrlV2(client.getIpAddress(), light.getNaturalId());
    var payload = Map.of("data", level);

    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  @Deprecated
  public void controlPower(Long id, ActuatorPower state) {
    var light = getLightOrThrow(id);
    var client = light.getDeviceControl().getClient();

    controlService.sendCommand(
      client.getIpAddress(),
      light.getNaturalId(),
      ((state == ActuatorPower.ON) ? GatewayCommand.ON : GatewayCommand.OFF)
    );
    light.setPower(state);
    light.setIsActive(state == ActuatorPower.ON);
    lightDao.save(light);
  }

  @Override
  @Transactional
  @Deprecated
  public void togglePower(Long id) {
    var light = getLightOrThrow(id);
    var currentPower = light.getPower() != null ? light.getPower() : ActuatorPower.OFF;
    var newPower = (currentPower == ActuatorPower.ON) ? ActuatorPower.OFF : ActuatorPower.ON;
    controlPower(id, newPower);
  }

  @Override
  @Transactional
  @Deprecated
  public void controlLevel(Long id, int level) {
    if (level < Light.MIN_LEVEL || level > Light.MAX_LEVEL) {
      throw new BadRequestException("Light level must be between " + Light.MIN_LEVEL + " and " + Light.MAX_LEVEL);
    }
    throw new UnsupportedOperationException("Legacy level control is not supported. Please use the new API endpoint for level control.");
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
