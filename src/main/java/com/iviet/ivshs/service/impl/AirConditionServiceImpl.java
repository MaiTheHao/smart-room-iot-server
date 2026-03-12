package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dao.DeviceControlDao;
import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.AirConditionDto;
import com.iviet.ivshs.dto.CreateAirConditionDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateAirConditionDto;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.AirConditionLan;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.AirConditionService;
import com.iviet.ivshs.util.HttpClientUtil;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirConditionServiceImpl implements AirConditionService {

  private final AirConditionDao airConditionDao;
  private final RoomDao roomDao;
  private final DeviceControlDao deviceControlDao;
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

    DeviceControl deviceControl = null;
    if (dto.deviceControlId() != null) {
      deviceControl = deviceControlDao.findById(dto.deviceControlId())
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
    ac.setDeviceControl(deviceControl);
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
    airConditionDao.save(ac);

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

    if (StringUtils.hasText(dto.naturalId()) && !dto.naturalId().trim().equals(ac.getNaturalId())) {
      if (airConditionDao.existsByNaturalId(dto.naturalId().trim())) {
        throw new BadRequestException("Natural ID already exists: " + dto.naturalId());
      }
      ac.setNaturalId(dto.naturalId().trim());
    }

    if (dto.roomId() != null && !dto.roomId().equals(ac.getRoom().getId())) {
      var room = roomDao.findById(dto.roomId())
        .orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));
      ac.setRoom(room);
    }

    if (dto.deviceControlId() != null) {
      var dc = deviceControlDao.findById(dto.deviceControlId())
        .orElseThrow(() -> new NotFoundException("Device Control not found with ID: " + dto.deviceControlId()));
      ac.setDeviceControl(dc);
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

    airConditionDao.save(ac);

    return airConditionDao.findById(id, langCode)
      .orElseThrow(() -> new InternalServerErrorException("Failed to retrieve updated Air Condition"));
  }

  @Override
  @Transactional
  public void delete(Long id) {
    var ac = getAirConditionOrThrow(id);
    var deviceControl = ac.getDeviceControl();
    deviceControlDao.delete(deviceControl);
  }

  @Override
  public Long countByRoomId(Long roomId) {
    if (roomId == null) throw new BadRequestException("Room ID is required");
    return airConditionDao.countByRoomId(roomId);
  }

  @Override
  @Transactional
  @Deprecated
  public void controlPower(Long id, ActuatorPower state) {
    var ac = getAirConditionOrThrow(id);
    ac.setPower(state);
    airConditionDao.save(ac);

    var gatewayIp = getGatewayIp(ac);
    var url = UrlConstant.getAcPowerUrlV1(gatewayIp, ac.getNaturalId());
    var payload = Map.of("power", state);

    sendControlCommand(url, payload);
  }

  @Override
  @Transactional
  public void _v2api_handlePowerControl(Long id, ActuatorPower power) {
    var ac = getAirConditionOrThrow(id);
    ac.setPower(power);
    airConditionDao.save(ac);

    var gatewayIp = getGatewayIp(ac);
    var url = UrlConstant.getControlAcPowerUrlV2(gatewayIp, ac.getNaturalId());
    var payload = Map.of("data", power);

    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  public void _v2api_handleTogglePowerControl(Long id) {
    var ac = getAirConditionOrThrow(id);
    var currentPower = ac.getPower() != null ? ac.getPower() : ActuatorPower.OFF;
    var newPower = (currentPower == ActuatorPower.ON) ? ActuatorPower.OFF : ActuatorPower.ON;

    ac.setPower(newPower);
    airConditionDao.save(ac);

    var gatewayIp = getGatewayIp(ac);
    var url = UrlConstant.getControlAcPowerUrlV2(gatewayIp, ac.getNaturalId());
    var payload = Map.of("data", newPower);

    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  @Deprecated
  public void controlTemperature(Long id, int temperature) {
    if (temperature < AirCondition.MIN_TEMP || temperature > AirCondition.MAX_TEMP) {
      throw new BadRequestException("Temperature must be between " + AirCondition.MIN_TEMP + " and " + AirCondition.MAX_TEMP);
    }

    var ac = getAirConditionOrThrow(id);
    var currentTemp = ac.getTemperature() != null ? ac.getTemperature() : 25;

    if (temperature == currentTemp) {
      return;
    }

    ac.setTemperature(temperature);
    airConditionDao.save(ac);

    var gatewayIp = getGatewayIp(ac);
    var url = temperature > currentTemp
      ? UrlConstant.getAcTempUpUrlV1(gatewayIp, ac.getNaturalId())
      : UrlConstant.getAcTempDownUrlV1(gatewayIp, ac.getNaturalId());
    var payload = Map.of("temp", temperature);

    sendControlCommand(url, payload);
  }

  @Override
  @Transactional
  public void _v2api_handleTemperatureControl(Long id, int temperature) {
    if (temperature < AirCondition.MIN_TEMP || temperature > AirCondition.MAX_TEMP) {
      throw new BadRequestException("Temperature must be between " + AirCondition.MIN_TEMP + " and " + AirCondition.MAX_TEMP);
    }

    var ac = getAirConditionOrThrow(id);
    var currentTemp = ac.getTemperature() != null ? ac.getTemperature() : 25;

    if (temperature == currentTemp) {
      return;
    }

    ac.setTemperature(temperature);
    airConditionDao.save(ac);

    var gatewayIp = getGatewayIp(ac);
    var url = temperature > currentTemp
      ? UrlConstant.getControlAcTempUpUrlV2(gatewayIp, ac.getNaturalId())
      : UrlConstant.getControlAcTempDownUrlV2(gatewayIp, ac.getNaturalId());
    var payload = Map.of("data", temperature);

    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  @Deprecated
  public void controlMode(Long id, ActuatorMode mode) {
    var ac = getAirConditionOrThrow(id);
    ac.setMode(mode);
    airConditionDao.save(ac);

    var gatewayIp = getGatewayIp(ac);
    var url = UrlConstant.getAcModeUrlV1(gatewayIp, ac.getNaturalId());
    var payload = Map.of("mode", mode);

    sendControlCommand(url, payload);
  }

  @Override
  @Transactional
  public void _v2api_handleModeControl(Long id, ActuatorMode mode) {
    var ac = getAirConditionOrThrow(id);
    ac.setMode(mode);
    airConditionDao.save(ac);

    var gatewayIp = getGatewayIp(ac);
    var url = UrlConstant.getControlAcModeUrlV2(gatewayIp, ac.getNaturalId());
    var payload = Map.of("data", mode);

    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  @Deprecated
  public void controlFanSpeed(Long id, int speed) {
    if (speed < AirCondition.MIN_FAN_SPEED || speed > AirCondition.MAX_FAN_SPEED) {
      throw new BadRequestException("Fan speed must be between " + AirCondition.MIN_FAN_SPEED + " and " + AirCondition.MAX_FAN_SPEED);
    }

    var ac = getAirConditionOrThrow(id);
    ac.setFanSpeed(speed);
    airConditionDao.save(ac);

    var gatewayIp = getGatewayIp(ac);
    var url = UrlConstant.getAcFanUrlV1(gatewayIp, ac.getNaturalId());
    var payload = Map.of("fan", speed);

    sendControlCommand(url, payload);
  }

  @Override
  @Transactional
  public void _v2api_handleFanSpeedControl(Long id, int speed) {
    if (speed < AirCondition.MIN_FAN_SPEED || speed > AirCondition.MAX_FAN_SPEED) {
      throw new BadRequestException("Fan speed must be between " + AirCondition.MIN_FAN_SPEED + " and " + AirCondition.MAX_FAN_SPEED);
    }

    var ac = getAirConditionOrThrow(id);
    ac.setFanSpeed(speed);
    airConditionDao.save(ac);

    var gatewayIp = getGatewayIp(ac);
    var url = UrlConstant.getControlAcFanUrlV2(gatewayIp, ac.getNaturalId());
    var payload = Map.of("data", speed);

    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  @Deprecated
  public void controlSwing(Long id, ActuatorSwing swing) {
    var ac = getAirConditionOrThrow(id);
    ac.setSwing(swing);
    airConditionDao.save(ac);

    var gatewayIp = getGatewayIp(ac);
    var url = UrlConstant.getAcSwingUrlV1(gatewayIp, ac.getNaturalId());
    var payload = Map.of("swing", swing);

    sendControlCommand(url, payload);
  }

  @Override
  @Transactional
  public void _v2api_handleSwingControl(Long id, ActuatorSwing swing) {
    var ac = getAirConditionOrThrow(id);
    ac.setSwing(swing);
    airConditionDao.save(ac);

    var gatewayIp = getGatewayIp(ac);
    var url = UrlConstant.getControlAcSwingUrlV2(gatewayIp, ac.getNaturalId());
    var payload = Map.of("data", swing);

    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  private AirCondition getAirConditionOrThrow(Long id) {
    return airConditionDao.findById(id)
      .orElseThrow(() -> new NotFoundException("Air Condition not found with ID: " + id));
  }

  private String getGatewayIp(AirCondition ac) {
    var dc = ac.getDeviceControl();
    if (dc == null) {
      throw new BadRequestException("No Device Control associated with Air Condition: " + ac.getNaturalId());
    }

    var gateway = dc.getClient();
    if (gateway == null || gateway.getIpAddress() == null || gateway.getIpAddress().isEmpty()) {
      throw new BadRequestException("Gateway IP not configured for Air Condition: " + ac.getNaturalId());
    }

    return gateway.getIpAddress();
  }

  private void sendControlCommand(String url, Map<String, ? extends Object> payload) {
    HttpClientUtil.postAsync(url, payload).exceptionally(ex -> null);
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
