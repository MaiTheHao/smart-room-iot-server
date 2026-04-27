package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dao.HardwareConfigDao;
import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.AirConditionDto;
import com.iviet.ivshs.dto.CreateAirConditionDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateAirConditionDto;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.AirConditionLan;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.AirConditionService;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirConditionServiceImpl implements AirConditionService {

  private final AirConditionDao airConditionDao;
  private final RoomDao roomDao;
  private final HardwareConfigDao deviceControlDao;
  private final LanguageDao languageDao;
  @Override
  public PaginatedResponse<AirConditionDto> getList(int page, int size) {
    var langCode = LocalContextUtil.getCurrentLangCode();
    var data = airConditionDao.findAll(page, size, langCode);
    var totalElements = airConditionDao.count();
    return new PaginatedResponse<>(data, page, size, totalElements);
  }

  @Override
  public List<AirConditionDto> getAll() {
    var langCode = LocalContextUtil.getCurrentLangCode();
    return airConditionDao.findAll(langCode);
  }

  @Override
  public PaginatedResponse<AirConditionDto> getListByRoomId(Long roomId, int page, int size) {
    if (roomId == null) {
      throw new BadRequestException("Room ID is required");
    }
    var langCode = LocalContextUtil.getCurrentLangCode();
    var data = airConditionDao.findAllByRoomId(roomId, page, size, langCode);
    var totalElements = airConditionDao.countByRoomId(roomId);
    return new PaginatedResponse<>(data, page, size, totalElements);
  }

  @Override
  public List<AirConditionDto> getAllByRoomId(Long roomId) {
    if (roomId == null) {
      throw new BadRequestException("Room ID is required");
    }
    var langCode = LocalContextUtil.getCurrentLangCode();
    return airConditionDao.findAllByRoomId(roomId, langCode);
  }

  @Override
  public AirConditionDto getById(Long id) {
    var langCode = LocalContextUtil.getCurrentLangCode();
    return airConditionDao.findById(id, langCode)
      .orElseThrow(() -> new NotFoundException("Air Condition not found with ID: " + id));
  }

  @Override
  @Transactional
  public AirConditionDto create(CreateAirConditionDto dto) {
    if (dto == null) {
      throw new BadRequestException("Air Condition data is required");
    }
    if (dto.roomId() == null) {
      throw new BadRequestException("Room ID is required");
    }

    var naturalId = dto.naturalId().trim();
    if (airConditionDao.existsByNaturalId(naturalId)) {
      throw new BadRequestException("Natural ID already exists: " + naturalId);
    }

    var room = roomDao.findById(dto.roomId())
      .orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));

    HardwareConfig hardwareConfig = null;
    if (dto.deviceControlId() != null) {
      hardwareConfig = deviceControlDao.findById(dto.deviceControlId())
        .orElseThrow(() -> new NotFoundException("Device Control not found with ID: " + dto.deviceControlId()));
    }

    var langCode = LocalContextUtil.resolveLangCode(dto.langCode());
    if (!languageDao.existsByCode(langCode)) {
      throw new NotFoundException("Language not found: " + langCode);
    }

    validateControlValues(dto.temperature(), dto.fanSpeed());

    var ac = new AirCondition();
    ac.setNaturalId(naturalId);
    ac.setIsActive(dto.isActive() != null ? dto.isActive() : false);
    ac.setRoom(room);
    ac.setHardwareConfig(hardwareConfig);
    ac.setPower(dto.power() != null ? dto.power() : ActuatorPower.OFF);
    ac.setTemperature(dto.temperature() != null ? dto.temperature() : 25);
    ac.setMode(dto.mode() != null ? dto.mode() : ActuatorMode.COOL);
    ac.setFanSpeed(dto.fanSpeed() != null ? dto.fanSpeed() : 3);
    ac.setSwing(dto.swing() != null ? dto.swing() : ActuatorSwing.OFF);

    var lan = new AirConditionLan();
    lan.setLangCode(langCode);
    lan.setName(dto.name() != null ? dto.name().trim() : "");
    lan.setDescription(dto.description());
    lan.setOwner(ac);

    ac.getTranslations().add(lan);
    ac.touch();
    airConditionDao.save(ac);
    airConditionDao.flush();

    return airConditionDao.findById(ac.getId(), langCode)
      .orElseThrow(() -> new InternalServerErrorException("Failed to retrieve created Air Condition"));
  }

  @Override
  @Transactional
  public AirConditionDto update(Long id, UpdateAirConditionDto dto) {
    var ac = getAirConditionOrThrow(id);
    var langCode = LocalContextUtil.resolveLangCode(dto.langCode());
    if (!languageDao.existsByCode(langCode)) {
      throw new NotFoundException("Language not found: " + langCode);
    }

    if (dto.roomId() != null && !dto.roomId().equals(ac.getRoom().getId())) {
      var room = roomDao.findById(dto.roomId())
        .orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));
      ac.setRoom(room);
    }

    if (dto.deviceControlId() != null) {
      var dc = deviceControlDao.findById(dto.deviceControlId())
        .orElseThrow(() -> new NotFoundException("Device Control not found with ID: " + dto.deviceControlId()));
      ac.setHardwareConfig(dc);
    }

    validateControlValues(dto.temperature(), dto.fanSpeed());

    if (dto.isActive() != null) ac.setIsActive(dto.isActive());
    if (dto.power() != null) ac.setPower(dto.power());
    if (dto.temperature() != null) ac.setTemperature(dto.temperature());
    if (dto.mode() != null) ac.setMode(dto.mode());
    if (dto.fanSpeed() != null) ac.setFanSpeed(dto.fanSpeed());
    if (dto.swing() != null) ac.setSwing(dto.swing());

    var lan = ac.getTranslations().stream()
      .filter(l -> langCode.equals(l.getLangCode()))
      .findFirst()
      .orElseGet(() -> {
        var newLan = new AirConditionLan();
        newLan.setLangCode(langCode);
        newLan.setOwner(ac);
        ac.getTranslations().add(newLan);
        return newLan;
      });

    if (StringUtils.hasText(dto.name())) lan.setName(dto.name().trim());
    if (dto.description() != null) lan.setDescription(dto.description());

    ac.touch();
    airConditionDao.save(ac);
    airConditionDao.flush();

    return airConditionDao.findById(id, langCode)
      .orElseThrow(() -> new InternalServerErrorException("Failed to retrieve updated Air Condition"));
  }

  @Override
  @Transactional
  public void delete(Long id) {
    var ac = getAirConditionOrThrow(id);
    var hardwareConfig = ac.getHardwareConfig();
    deviceControlDao.delete(hardwareConfig);
  }

  @Override
  public Long countByRoomId(Long roomId) {
    if (roomId == null) throw new BadRequestException("Room ID is required");
    return airConditionDao.countByRoomId(roomId);
  }

  private AirCondition getAirConditionOrThrow(Long id) {
    return airConditionDao.findById(id)
      .orElseThrow(() -> new NotFoundException("Air Condition not found with ID: " + id));
  }

  private void validateControlValues(Integer temperature, Integer fanSpeed) {
    if (temperature != null && (temperature < AirCondition.MIN_TEMP || temperature > AirCondition.MAX_TEMP)) {
      throw new BadRequestException("Temperature must be between " + AirCondition.MIN_TEMP + " and " + AirCondition.MAX_TEMP);
    }

    if (fanSpeed != null && (fanSpeed < AirCondition.MIN_FAN_SPEED || fanSpeed > AirCondition.MAX_FAN_SPEED)) {
      throw new BadRequestException("Fan speed must be between " + AirCondition.MIN_FAN_SPEED + " and " + AirCondition.MAX_FAN_SPEED);
    }
  }
}
