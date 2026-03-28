package com.iviet.ivshs.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dto.AirConditionControlRequestBody;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.AirConditionControlService;
import com.iviet.ivshs.util.HttpClientUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirConditionControlServiceImpl implements AirConditionControlService {

  private final AirConditionDao airConditionDao;

  @Override
  public DeviceCategory getSupportedCategory() {
    return DeviceCategory.AIR_CONDITION;
  }

  @Override
  public Class<AirConditionControlRequestBody> getControlDtoClass() {
    return AirConditionControlRequestBody.class;
  }

  @Override
  @Transactional
  public void handlePowerControl(String naturalId, ActuatorPower power) {
    AirCondition ac = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(ac);
    ac.setPower(power);
    airConditionDao.save(ac);
    handlePowerControlCall(gatewayIp, ac.getNaturalId(), power);
  }

  @Override
  @Transactional
  public void handleTogglePowerControl(String naturalId) {
    AirCondition ac = getOrThrow(naturalId);
    ActuatorPower newPowerState = (ac.getPower() == ActuatorPower.ON) ? ActuatorPower.OFF : ActuatorPower.ON;
    String gatewayIp = extractClientIpAddress(ac);
    ac.setPower(newPowerState);
    airConditionDao.save(ac);
    handlePowerControlCall(gatewayIp, ac.getNaturalId(), newPowerState);
  }

  @Override
  @Transactional
  public void handleTemperatureControl(String naturalId, int temperature) {
    AirCondition ac = getOrThrow(naturalId);
    int currentTemp = ac.getTemperature() != null ? ac.getTemperature() : 25;
    String gatewayIp = extractClientIpAddress(ac);
    ac.setTemperature(temperature);
    airConditionDao.save(ac);
    handleTemperatureControlCall(gatewayIp, ac.getNaturalId(), temperature, currentTemp);
  }

  @Override
  @Transactional
  public void handleModeControl(String naturalId, ActuatorMode mode) {
    AirCondition ac = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(ac);
    ac.setMode(mode);
    airConditionDao.save(ac);
    handleModeControlCall(gatewayIp, ac.getNaturalId(), mode);
  }

  @Override
  @Transactional
  public void handleFanSpeedControl(String naturalId, int speed) {
    AirCondition ac = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(ac);
    ac.setFanSpeed(speed);
    airConditionDao.save(ac);
    handleFanSpeedControlCall(gatewayIp, ac.getNaturalId(), speed);
  }

  @Override
  @Transactional
  public void handleSwingControl(String naturalId, ActuatorSwing swing) {
    AirCondition ac = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(ac);
    ac.setSwing(swing);
    airConditionDao.save(ac);
    handleSwingControlCall(gatewayIp, ac.getNaturalId(), swing);
  }

  @Override
  @Transactional
  public void control(String naturalId, AirConditionControlRequestBody body) {
    AirCondition ac = getOrThrow(naturalId);
    applyControlParams(ac, body);
  }

  @Override
  @Transactional
  public void control(Long id, AirConditionControlRequestBody body) {
    AirCondition ac = airConditionDao.findById(id)
      .orElseThrow(() -> new BadRequestException("AirCondition not found with id: " + id));
    applyControlParams(ac, body);
  }

  private void applyControlParams(AirCondition ac, AirConditionControlRequestBody body) {
    String gatewayIp = extractClientIpAddress(ac);
    if (body.power() != null) {
      ac.setPower(body.power());
      handlePowerControlCall(gatewayIp, ac.getNaturalId(), body.power());
    }
    if (body.temperature() != null) {
      int currentTemp = ac.getTemperature() != null ? ac.getTemperature() : 25;
      ac.setTemperature(body.temperature());
      handleTemperatureControlCall(gatewayIp, ac.getNaturalId(), body.temperature(), currentTemp);
    }
    if (body.mode() != null) {
      ac.setMode(body.mode());
      handleModeControlCall(gatewayIp, ac.getNaturalId(), body.mode());
    }
    if (body.fanSpeed() != null) {
      ac.setFanSpeed(body.fanSpeed());
      handleFanSpeedControlCall(gatewayIp, ac.getNaturalId(), body.fanSpeed());
    }
    if (body.swing() != null) {
      ac.setSwing(body.swing());
      handleSwingControlCall(gatewayIp, ac.getNaturalId(), body.swing());
    }
    airConditionDao.save(ac);
  }

  private void handlePowerControlCall(String gatewayIp, String naturalId, ActuatorPower power) {
    String url = UrlConstant.getControlAcPowerUrlV2(gatewayIp, naturalId);
    HttpClientUtil.putAsync(url, Map.of("data", power)).exceptionally(ex -> null);
  }

  private void handleTemperatureControlCall(String gatewayIp, String naturalId, int temperature, int currentTemp) {
    String url = temperature > currentTemp ? UrlConstant.getControlAcTempUpUrlV2(gatewayIp, naturalId) : UrlConstant.getControlAcTempDownUrlV2(gatewayIp, naturalId);
    HttpClientUtil.putAsync(url, Map.of("data", temperature)).exceptionally(ex -> null);
  }

  private void handleModeControlCall(String gatewayIp, String naturalId, ActuatorMode mode) {
    String url = UrlConstant.getControlAcModeUrlV2(gatewayIp, naturalId);
    HttpClientUtil.putAsync(url, Map.of("data", mode)).exceptionally(ex -> null);
  }

  private void handleFanSpeedControlCall(String gatewayIp, String naturalId, int speed) {
    String url = UrlConstant.getControlAcFanUrlV2(gatewayIp, naturalId);
    HttpClientUtil.putAsync(url, Map.of("data", speed == 0 ? "AUTO" : speed)).exceptionally(ex -> null);
  }

  private void handleSwingControlCall(String gatewayIp, String naturalId, ActuatorSwing swing) {
    String url = UrlConstant.getControlAcSwingUrlV2(gatewayIp, naturalId);
    HttpClientUtil.putAsync(url, Map.of("data", swing)).exceptionally(ex -> null);
  }

  private AirCondition getOrThrow(String naturalId) {
    return airConditionDao.findByNaturalId(naturalId)
      .orElseThrow(() -> new BadRequestException("AirCondition not found with naturalId: " + naturalId));
  }

  private String extractClientIpAddress(AirCondition ac) {
    DeviceControl control = ac.getDeviceControl();
    if (control == null) {
      throw new BadRequestException("DeviceControl not found for AirCondition: " + ac.getId());
    }
    Client client = control.getClient();
    if (client == null) {
      throw new BadRequestException("Client not found for DeviceControl: " + control.getId());
    }
    String gatewayIp = client.getIpAddress();
    if (gatewayIp == null || gatewayIp.isBlank()) {
      throw new BadRequestException("IP Address not found for Client: " + client.getId());
    }
    return gatewayIp;
  }
}