package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dto.LightControlRequestBody;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.LightControlService;
import com.iviet.ivshs.service.client.gateway.GatewayControlClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

import com.iviet.ivshs.dto.ControlDeviceResult;
import org.springframework.http.ResponseEntity;
import com.iviet.ivshs.dto.ApiResponse;
import java.util.function.Supplier;

@Slf4j(topic = "CONTROL-LIGHT")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LightControlServiceImpl implements LightControlService {

  private final LightDao lightDao;
  private final GatewayControlClient gatewayControlClient;

  @Override
  public DeviceCategory getSupportedCategory() {
    return DeviceCategory.LIGHT;
  }

  @Override
  public Class<LightControlRequestBody> getControlDtoClass() {
    return LightControlRequestBody.class;
  }

  @Override
  @Transactional
  public ControlDeviceResult handlePowerControl(String naturalId, ActuatorPower power) {
    Light light = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(light);
    light.setPower(power);
    lightDao.save(light);
    return handlePowerControlCall(gatewayIp, light.getNaturalId(), power);
  }

  @Override
  @Transactional
  public ControlDeviceResult handleTogglePowerControl(String naturalId) {
    Light light = getOrThrow(naturalId);
    ActuatorPower newPowerState = (light.getPower() == ActuatorPower.ON) ? ActuatorPower.OFF : ActuatorPower.ON;
    String gatewayIp = extractClientIpAddress(light);
    light.setPower(newPowerState);
    lightDao.save(light);
    return handlePowerControlCall(gatewayIp, light.getNaturalId(), newPowerState);
  }

  @Override
  @Transactional
  public ControlDeviceResult handleLevelControl(String naturalId, int level) {
    Light light = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(light);
    light.setLevel(level);
    lightDao.save(light);
    return handleLevelControlCall(gatewayIp, light.getNaturalId(), level);
  }

  @Override
  @Transactional
  public ControlDeviceResult control(String naturalId, LightControlRequestBody body) {
    Light light = getOrThrow(naturalId);
    return applyControlParams(light, body);
  }

  @Override
  @Transactional
  public ControlDeviceResult control(Long id, LightControlRequestBody body) {
    Light light = lightDao.findById(id)
      .orElseThrow(() -> new BadRequestException("Light not found with id: " + id));
    return applyControlParams(light, body);
  }

  private ControlDeviceResult applyControlParams(Light light, LightControlRequestBody body) {
    String gatewayIp = extractClientIpAddress(light);
    ControlDeviceResult result = new ControlDeviceResult();
    if (body.power() != null) {
      light.setPower(body.power());
      executeControl(result, "power", () -> gatewayControlClient.controlLightPowerV2(gatewayIp, light.getNaturalId(), body.power()));
    }
    if (body.level() != null) {
      light.setLevel(body.level());
      executeControl(result, "level", () -> gatewayControlClient.controlLightLevelV2(gatewayIp, light.getNaturalId(), body.level()));
    }
    lightDao.save(light);
    return result;
  }

  private void executeControl(ControlDeviceResult result, String parameter, Supplier<ResponseEntity<ApiResponse<String>>> call) {
    try {
      ResponseEntity<ApiResponse<String>> response = call.get();
      if (response.getStatusCode().is2xxSuccessful()) {
        result.addDetail(parameter, true, "Success");
      } else {
        result.addDetail(parameter, false, "Gateway error: " + response.getStatusCode());
      }
    } catch (Exception e) {
      result.addDetail(parameter, false, e.getMessage());
    }
  }

  private ControlDeviceResult handlePowerControlCall(String gatewayIp, String naturalId, ActuatorPower power) {
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "power", () -> gatewayControlClient.controlLightPowerV2(gatewayIp, naturalId, power));
    return result;
  }

  private ControlDeviceResult handleLevelControlCall(String gatewayIp, String naturalId, int level) {
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "level", () -> gatewayControlClient.controlLightLevelV2(gatewayIp, naturalId, level));
    return result;
  }

  private Light getOrThrow(String naturalId) {
    return lightDao.findByNaturalId(naturalId)
      .orElseThrow(() -> new BadRequestException("Light not found with naturalId: " + naturalId));
  }

  private String extractClientIpAddress(Light light) {
    HardwareConfig control = light.getHardwareConfig();
    if (control == null) {
      throw new BadRequestException("DeviceControl not found for Light: " + light.getId());
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