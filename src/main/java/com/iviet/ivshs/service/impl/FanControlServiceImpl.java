package com.iviet.ivshs.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dto.FanControlRequestBody;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanIr;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorState;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.FanControlService;
import com.iviet.ivshs.util.HttpClientUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "CONTROL-FAN")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FanControlServiceImpl implements FanControlService {

  private final FanDao fanDao;

  @Override
  public DeviceCategory getSupportedCategory() {
    return DeviceCategory.FAN;
  }

  @Override
  public Class<FanControlRequestBody> getControlDtoClass() {
    return FanControlRequestBody.class;
  }

  @Override
  @Transactional
  public void handlePowerControl(String naturalId, ActuatorPower power) {
    Fan fan = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(fan);
    fan.setPower(power);
    fanDao.save(fan);
    handlePowerControlCall(gatewayIp, fan.getNaturalId(), power);
  }

  @Override
  @Transactional
  public void handleTogglePowerControl(String naturalId) {
    Fan fan = getOrThrow(naturalId);
    ActuatorPower newPowerState = (fan.getPower() == ActuatorPower.ON) ? ActuatorPower.OFF : ActuatorPower.ON;
    String gatewayIp = extractClientIpAddress(fan);
    fan.setPower(newPowerState);
    fanDao.save(fan);
    handlePowerControlCall(gatewayIp, fan.getNaturalId(), newPowerState);
  }

  @Override
  @Transactional
  public void handleModeControl(String naturalId, ActuatorMode mode) {
    Fan fan = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(fan);
    if (fan instanceof FanIr fanIr) {
      fanIr.setMode(mode);
      fanDao.save(fanIr);
    }
    handleModeControlCall(gatewayIp, fan.getNaturalId(), mode);
  }

  @Override
  @Transactional
  public void handleSpeedControl(String naturalId, int speed) {
    Fan fan = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(fan);
    fan.setSpeed(speed);
    fanDao.save(fan);
    handleSpeedControlCall(gatewayIp, fan.getNaturalId(), speed);
  }

  @Override
  @Transactional
  public void handleSwingControl(String naturalId, ActuatorSwing swing) {
    Fan fan = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(fan);
    if (fan instanceof FanIr fanIr) {
      fanIr.setSwing(swing);
      fanDao.save(fanIr);
    }
    handleSwingControlCall(gatewayIp, fan.getNaturalId(), swing);
  }

  @Override
  @Transactional
  public void handleLightControl(String naturalId, ActuatorState light) {
    Fan fan = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(fan);
    if (fan instanceof FanIr fanIr) {
      fanIr.setLight(light);
      fanDao.save(fanIr);
    }
    handleLightControlCall(gatewayIp, fan.getNaturalId(), light);
  }

  @Override
  @Transactional
  public void control(String naturalId, FanControlRequestBody body) {
    Fan fan = getOrThrow(naturalId);
    applyControlParams(fan, body);
  }

  @Override
  @Transactional
  public void control(Long id, FanControlRequestBody body) {
    Fan fan = fanDao.findById(id).orElseThrow(() -> new BadRequestException("Fan not found with id: " + id));
    applyControlParams(fan, body);
  }

  private void applyControlParams(Fan fan, FanControlRequestBody body) {
    String gatewayIp = extractClientIpAddress(fan);
    if (body.power() != null) {
      fan.setPower(body.power());
      handlePowerControlCall(gatewayIp, fan.getNaturalId(), body.power());
    }
    if (body.speed() != null) {
      fan.setSpeed(body.speed());
      handleSpeedControlCall(gatewayIp, fan.getNaturalId(), body.speed());
    }
    if (body.mode() != null && fan instanceof FanIr fanIr) {
      fanIr.setMode(body.mode());
      handleModeControlCall(gatewayIp, fan.getNaturalId(), body.mode());
    }
    if (body.swing() != null && fan instanceof FanIr fanIr) {
      fanIr.setSwing(body.swing());
      handleSwingControlCall(gatewayIp, fan.getNaturalId(), body.swing());
    }
    if (body.light() != null && fan instanceof FanIr fanIr) {
      fanIr.setLight(body.light());
      handleLightControlCall(gatewayIp, fan.getNaturalId(), body.light());
    }
    fanDao.save(fan);
  }

  private void handlePowerControlCall(String gatewayIp, String naturalId, ActuatorPower power) {
    String url = UrlConstant.getControlFanPowerUrlV2(gatewayIp, naturalId);
    HttpClientUtil.putAsync(url, Map.of("data", power)).exceptionally(ex -> null);
  }

  private void handleModeControlCall(String gatewayIp, String naturalId, ActuatorMode mode) {
    String url = UrlConstant.getControlFanModeUrlV2(gatewayIp, naturalId);
    HttpClientUtil.putAsync(url, Map.of("data", mode)).exceptionally(ex -> null);
  }

  private void handleSpeedControlCall(String gatewayIp, String naturalId, int speed) {
    String url = UrlConstant.getControlFanSpeedUrlV2(gatewayIp, naturalId);
    HttpClientUtil.putAsync(url, Map.of("data", speed)).exceptionally(ex -> null);
  }

  private void handleSwingControlCall(String gatewayIp, String naturalId, ActuatorSwing swing) {
    String url = UrlConstant.getControlFanSwingUrlV2(gatewayIp, naturalId);
    HttpClientUtil.putAsync(url, Map.of("data", swing)).exceptionally(ex -> null);
  }

  private void handleLightControlCall(String gatewayIp, String naturalId, ActuatorState light) {
    String url = UrlConstant.getControlFanLightUrlV2(gatewayIp, naturalId);
    HttpClientUtil.putAsync(url, Map.of("data", light)).exceptionally(ex -> null);
  }

  private Fan getOrThrow(String naturalId) {
    return fanDao.findByNaturalId(naturalId)
      .orElseThrow(() -> new BadRequestException("Fan not found with naturalId: " + naturalId));
  }

  private String extractClientIpAddress(Fan fan) {
    HardwareConfig control = fan.getHardwareConfig();
    if (control == null) {
      throw new BadRequestException("DeviceControl not found for Fan: " + fan.getId());
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