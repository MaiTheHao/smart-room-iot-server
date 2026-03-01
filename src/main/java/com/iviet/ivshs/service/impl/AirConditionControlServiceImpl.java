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
    AirCondition ac = airConditionDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("AirCondition not found with naturalId: " + naturalId));
    String gatewayIp = extractClientIpAddress(ac);

    ac.setPower(power);
    airConditionDao.save(ac);
  
    handlePowerControlCall(gatewayIp, ac.getNaturalId(), power);
  }

  private void handlePowerControlCall(String gatewayIp, String naturalId, ActuatorPower power) {
    String url = UrlConstant.getControlAcPowerUrlV2(gatewayIp, naturalId);
    Map<String, Object> payload = Map.of("data", power);
    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  public void handleTogglePowerControl(String naturalId) {
    AirCondition ac = airConditionDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("AirCondition not found with naturalId: " + naturalId));
    ActuatorPower newPowerState = (ac.getPower() == ActuatorPower.ON) ? ActuatorPower.OFF : ActuatorPower.ON;
    String gatewayIp = extractClientIpAddress(ac);
    
    ac.setPower(newPowerState);
    airConditionDao.save(ac);
    
    handleTogglePowerControlCall(gatewayIp, ac.getNaturalId(), newPowerState);
  }

  private void handleTogglePowerControlCall(String gatewayIp, String naturalId, ActuatorPower newPowerState) {
    String url = UrlConstant.getControlAcPowerUrlV2(gatewayIp, naturalId);
    Map<String, Object> payload = Map.of("data", newPowerState);
    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  public void handleTemperatureControl(String naturalId, int temperature) {
    AirCondition ac = airConditionDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("AirCondition not found with naturalId: " + naturalId));
    int currentTemp = ac.getTemperature() != null ? ac.getTemperature() : 25;
    String gatewayIp = extractClientIpAddress(ac);

    ac.setTemperature(temperature);
    airConditionDao.save(ac);

    handleTemperatureControlCall(gatewayIp, ac.getNaturalId(), temperature, currentTemp);
  }

  private void handleTemperatureControlCall(String gatewayIp, String naturalId, int temperature, int currentTemp) {
    String url = temperature > currentTemp ? UrlConstant.getControlAcTempUpUrlV2(gatewayIp, naturalId) : UrlConstant.getControlAcTempDownUrlV2(gatewayIp, naturalId);
    Map<String, Object> payload = Map.of("data", temperature);
    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  public void handleModeControl(String naturalId, ActuatorMode mode) {
    AirCondition ac = airConditionDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("AirCondition not found with naturalId: " + naturalId));
    String gatewayIp = extractClientIpAddress(ac);

    ac.setMode(mode);
    airConditionDao.save(ac);

    handleModeControlCall(gatewayIp, ac.getNaturalId(), mode);
  }

  private void handleModeControlCall(String gatewayIp, String naturalId, ActuatorMode mode) {
    String url = UrlConstant.getControlAcModeUrlV2(gatewayIp, naturalId);
    Map<String, Object> payload = Map.of("data", mode);
    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  public void handleFanSpeedControl(String naturalId, int speed) {
    AirCondition ac = airConditionDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("AirCondition not found with naturalId: " + naturalId));
    String gatewayIp = extractClientIpAddress(ac);

    ac.setFanSpeed(speed);
    airConditionDao.save(ac);

    handleFanSpeedControlCall(gatewayIp, ac.getNaturalId(), speed);
  }

  private void handleFanSpeedControlCall(String gatewayIp, String naturalId, int speed) {
    String url = UrlConstant.getControlAcFanUrlV2(gatewayIp, naturalId);
    Map<String, Object> payload = Map.of("data", speed == 0 ? "AUTO" : speed);
    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  public void handleSwingControl(String naturalId, ActuatorSwing swing) {
    AirCondition ac = airConditionDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("AirCondition not found with naturalId: " + naturalId));
    String gatewayIp = extractClientIpAddress(ac);

    ac.setSwing(swing);
    airConditionDao.save(ac);

    handleSwingControlCall(gatewayIp, ac.getNaturalId(), swing);
  }

  private void handleSwingControlCall(String gatewayIp, String naturalId, ActuatorSwing swing) {
    String url = UrlConstant.getControlAcSwingUrlV2(gatewayIp, naturalId);
    Map<String, Object> payload = Map.of("data", swing);
    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  public void control(String naturalId, AirConditionControlRequestBody body) {
    AirCondition ac = airConditionDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("AirCondition not found with naturalId: " + naturalId));
    applyControlParams(ac, body);
  }

  @Override
  @Transactional
  public void control(Long id, AirConditionControlRequestBody body) {
    AirCondition ac = airConditionDao.findById(id).orElseThrow(() -> new BadRequestException("AirCondition not found with id: " + id));
    applyControlParams(ac, body);
  }

  private void applyControlParams(AirCondition ac, AirConditionControlRequestBody body) {
    String gatewayIp = extractClientIpAddress(ac);

    if (body.power() != null) {
      ac.setPower(body.power());
      handlePowerControlCall(gatewayIp, ac.getNaturalId(), body.power());
    }
    if (body.temperature() != null) {
      ac.setTemperature(body.temperature());
      handleTemperatureControlCall(gatewayIp, ac.getNaturalId(), body.temperature(), ac.getTemperature() != null ? ac.getTemperature() : 25);
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

  private String extractClientIpAddress(AirCondition ac) {
    DeviceControl control = ac.getDeviceControl();
    if (control == null) throw new BadRequestException("Device control not found for air condition with id: " + ac.getId());
    Client client = control.getClient();
    if (client == null) throw new BadRequestException("Client not found for device control with id: " + control.getId());
    String gatewayIp = client.getIpAddress();
    if (gatewayIp == null || gatewayIp.isBlank()) throw new BadRequestException("Gateway IP not found for client with id: " + client.getId());
    return gatewayIp;
  }
}
