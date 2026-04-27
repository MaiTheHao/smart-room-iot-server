package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import lombok.RequiredArgsConstructor;

import com.iviet.ivshs.dto.ControlDeviceResult;
import org.springframework.http.ResponseEntity;
import com.iviet.ivshs.dto.ApiResponse;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FanControlServiceImpl implements FanControlService {

  private final FanDao fanDao;
  private final com.iviet.ivshs.service.client.gateway.GatewayControlClient gatewayControlClient;

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
  public ControlDeviceResult handlePowerControl(String naturalId, ActuatorPower power) {
    Fan fan = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(fan);
    fan.setPower(power);
    fanDao.save(fan);
    return handlePowerControlCall(gatewayIp, fan.getNaturalId(), power);
  }

  @Override
  @Transactional
  public ControlDeviceResult handleTogglePowerControl(String naturalId) {
    Fan fan = getOrThrow(naturalId);
    ActuatorPower newPowerState = (fan.getPower() == ActuatorPower.ON) ? ActuatorPower.OFF : ActuatorPower.ON;
    String gatewayIp = extractClientIpAddress(fan);
    fan.setPower(newPowerState);
    fanDao.save(fan);
    return handlePowerControlCall(gatewayIp, fan.getNaturalId(), newPowerState);
  }

  @Override
  @Transactional
  public ControlDeviceResult handleModeControl(String naturalId, ActuatorMode mode) {
    Fan fan = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(fan);
    if (fan instanceof FanIr fanIr) {
      fanIr.setMode(mode);
      fanDao.save(fanIr);
    }
    return handleModeControlCall(gatewayIp, fan.getNaturalId(), mode);
  }

  @Override
  @Transactional
  public ControlDeviceResult handleSpeedControl(String naturalId, int speed) {
    Fan fan = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(fan);
    fan.setSpeed(speed);
    fanDao.save(fan);
    return handleSpeedControlCall(gatewayIp, fan.getNaturalId(), speed);
  }

  @Override
  @Transactional
  public ControlDeviceResult handleSwingControl(String naturalId, ActuatorSwing swing) {
    Fan fan = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(fan);
    if (fan instanceof FanIr fanIr) {
      fanIr.setSwing(swing);
      fanDao.save(fanIr);
    }
    return handleSwingControlCall(gatewayIp, fan.getNaturalId(), swing);
  }

  @Override
  @Transactional
  public ControlDeviceResult handleLightControl(String naturalId, ActuatorState light) {
    Fan fan = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(fan);
    if (fan instanceof FanIr fanIr) {
      fanIr.setLight(light);
      fanDao.save(fanIr);
    }
    return handleLightControlCall(gatewayIp, fan.getNaturalId(), light);
  }

  @Override
  @Transactional
  public ControlDeviceResult control(String naturalId, FanControlRequestBody body) {
    Fan fan = getOrThrow(naturalId);
    return applyControlParams(fan, body);
  }

  @Override
  @Transactional
  public ControlDeviceResult control(Long id, FanControlRequestBody body) {
    Fan fan = fanDao.findById(id).orElseThrow(() -> new BadRequestException("Fan not found with id: " + id));
    return applyControlParams(fan, body);
  }

  private ControlDeviceResult applyControlParams(Fan fan, FanControlRequestBody body) {
    String gatewayIp = extractClientIpAddress(fan);
    ControlDeviceResult result = new ControlDeviceResult();
    if (body.power() != null) {
      fan.setPower(body.power());
      executeControl(result, "power", () -> gatewayControlClient.controlFanPowerV2(gatewayIp, fan.getNaturalId(), body.power()));
    }
    if (body.speed() != null) {
      fan.setSpeed(body.speed());
      executeControl(result, "speed", () -> gatewayControlClient.controlFanSpeedV2(gatewayIp, fan.getNaturalId(), body.speed()));
    }
    if (body.mode() != null && fan instanceof FanIr fanIr) {
      fanIr.setMode(body.mode());
      executeControl(result, "mode", () -> gatewayControlClient.controlFanModeV2(gatewayIp, fan.getNaturalId(), body.mode()));
    }
    if (body.swing() != null && fan instanceof FanIr fanIr) {
      fanIr.setSwing(body.swing());
      executeControl(result, "swing", () -> gatewayControlClient.controlFanSwingV2(gatewayIp, fan.getNaturalId(), body.swing()));
    }
    if (body.light() != null && fan instanceof FanIr fanIr) {
      fanIr.setLight(body.light());
      executeControl(result, "light", () -> gatewayControlClient.controlFanLightV2(gatewayIp, fan.getNaturalId(), body.light()));
    }
    fanDao.save(fan);
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
    executeControl(result, "power", () -> gatewayControlClient.controlFanPowerV2(gatewayIp, naturalId, power));
    return result;
  }

  private ControlDeviceResult handleModeControlCall(String gatewayIp, String naturalId, ActuatorMode mode) {
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "mode", () -> gatewayControlClient.controlFanModeV2(gatewayIp, naturalId, mode));
    return result;
  }

  private ControlDeviceResult handleSpeedControlCall(String gatewayIp, String naturalId, int speed) {
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "speed", () -> gatewayControlClient.controlFanSpeedV2(gatewayIp, naturalId, speed));
    return result;
  }

  private ControlDeviceResult handleSwingControlCall(String gatewayIp, String naturalId, ActuatorSwing swing) {
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "swing", () -> gatewayControlClient.controlFanSwingV2(gatewayIp, naturalId, swing));
    return result;
  }

  private ControlDeviceResult handleLightControlCall(String gatewayIp, String naturalId, ActuatorState light) {
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "light", () -> gatewayControlClient.controlFanLightV2(gatewayIp, naturalId, light));
    return result;
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