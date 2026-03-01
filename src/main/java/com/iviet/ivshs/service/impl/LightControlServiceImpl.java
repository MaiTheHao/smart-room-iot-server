package com.iviet.ivshs.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dto.LightControlRequestBody;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.LightControlService;
import com.iviet.ivshs.util.HttpClientUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LightControlServiceImpl implements LightControlService {

  private final LightDao lightDao;

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
  public void handlePowerControl(String naturalId, ActuatorPower power) {
    Light light = lightDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("Light not found with naturalId: " + naturalId));
    String gatewayIp = extractClientIpAddress(light);

    light.setPower(power);
    lightDao.save(light);

    handlePowerControlCall(gatewayIp, light.getNaturalId(), power);
  }

  private void handlePowerControlCall(String gatewayIp, String naturalId, ActuatorPower power) {
    String url = UrlConstant.getControlLightPowerUrlV2(gatewayIp, naturalId);
    Map<String, Object> payload = Map.of("data", power);
    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  public void handleTogglePowerControl(String naturalId) {
    Light light = lightDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("Light not found with naturalId: " + naturalId));
    ActuatorPower newPowerState = (light.getPower() == ActuatorPower.ON) ? ActuatorPower.OFF : ActuatorPower.ON;
    String gatewayIp = extractClientIpAddress(light);

    light.setPower(newPowerState);
    lightDao.save(light);

    handlePowerControlCall(gatewayIp, light.getNaturalId(), newPowerState);
  }

  @Override
  @Transactional
  public void handleLevelControl(String naturalId, int level) {
    Light light = lightDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("Light not found with naturalId: " + naturalId));
    String gatewayIp = extractClientIpAddress(light);

    light.setLevel(level);
    lightDao.save(light);

    handleLevelControlCall(gatewayIp, light.getNaturalId(), level);
  }

  private void handleLevelControlCall(String gatewayIp, String naturalId, int level) {
    String url = UrlConstant.getControlLightLevelUrlV2(gatewayIp, naturalId);
    Map<String, Object> payload = Map.of("data", level);
    HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
  }

  @Override
  @Transactional
  public void control(String naturalId, LightControlRequestBody params) {
    Light light = lightDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("Light not found with naturalId: " + naturalId));
    applyControlParams(light, params);
  }

  @Override
  @Transactional
  public void control(Long id, LightControlRequestBody params) {
    Light light = lightDao.findById(id).orElseThrow(() -> new BadRequestException("Light not found with id: " + id));
    applyControlParams(light, params);
  }

  private void applyControlParams(Light light, LightControlRequestBody params) {
    String gatewayIp = extractClientIpAddress(light);
    
    if (params.power() != null) {
      light.setPower(params.power());
      handlePowerControlCall(gatewayIp, light.getNaturalId(), params.power());
    }
    if (params.level() != null) {
      light.setLevel(params.level());
      handleLevelControlCall(gatewayIp, light.getNaturalId(), params.level());
    }

    lightDao.save(light);
  }

  private String extractClientIpAddress(Light light) {
    DeviceControl control = light.getDeviceControl();
    if (control == null)
      throw new BadRequestException("DeviceControl not found for Light: " + light.getNaturalId());
    Client client = control.getClient();
    if (client == null)
      throw new BadRequestException("Client not found for DeviceControl: " + control.getId());
    String gatewayIp = client.getIpAddress();
    if (gatewayIp == null || gatewayIp.isBlank())
      throw new BadRequestException("IP Address not found for Client: " + client.getId());
    return gatewayIp;
  }
}
