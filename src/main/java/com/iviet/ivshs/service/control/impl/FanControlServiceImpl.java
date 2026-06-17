package com.iviet.ivshs.service.control.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.control.ControlDeviceResult;
import com.iviet.ivshs.dto.fan.FanControlRequestBody;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.service.control.FanControlService;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.shared.enumeration.ActuatorMode;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.ActuatorSwing;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FanControlServiceImpl implements FanControlService {

  private final FanDao fanDao;
  private final com.iviet.ivshs.integration.gateway.GatewayFanControlClient gatewayControlClient;

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
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "power", () -> gatewayControlClient.controlFanPower(gatewayIp, fan.getNaturalId(), power, fan.getSpecificType(), fan.getDuration()));
    if (result.getSuccessCount() > 0) {
      fan.setPower(power);
      fanDao.save(fan);
    }
    return result;
  }

  @Override
  @Transactional
  public ControlDeviceResult handleTogglePowerControl(String naturalId) {
    Fan fan = getOrThrow(naturalId);
    ActuatorPower newPowerState = (fan.getPower() == ActuatorPower.ON) ? ActuatorPower.OFF : ActuatorPower.ON;
    String gatewayIp = extractClientIpAddress(fan);
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "power", () -> gatewayControlClient.controlFanPower(gatewayIp, fan.getNaturalId(), newPowerState, fan.getSpecificType(), fan.getDuration()));
    if (result.getSuccessCount() > 0) {
      fan.setPower(newPowerState);
      fanDao.save(fan);
    }
    return result;
  }

  @Override
  @Transactional
  public ControlDeviceResult handleModeControl(String naturalId, ActuatorMode mode) {
    Fan fan = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(fan);
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "mode", () -> gatewayControlClient.controlFanMode(gatewayIp, fan.getNaturalId(), mode, fan.getSpecificType(), fan.getDuration()));
    if (result.getSuccessCount() > 0) {
      fan.setMode(mode);
      fanDao.save(fan);
    }
    return result;
  }

  @Override
  @Transactional
  public ControlDeviceResult handleSpeedControl(String naturalId, int speed) {
    Fan fan = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(fan);
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "speed", () -> gatewayControlClient.controlFanSpeed(gatewayIp, fan.getNaturalId(), speed, fan.getSpecificType(), fan.getDuration()));
    if (result.getSuccessCount() > 0) {
      fan.setSpeed(speed);
      fanDao.save(fan);
    }
    return result;
  }

  @Override
  @Transactional
  public ControlDeviceResult handleSwingControl(String naturalId, ActuatorSwing swing) {
    Fan fan = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(fan);
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "swing", () -> gatewayControlClient.controlFanSwing(gatewayIp, fan.getNaturalId(), swing, fan.getSpecificType(), fan.getDuration()));
    if (result.getSuccessCount() > 0) {
      fan.setSwing(swing);
      fanDao.save(fan);
    }
    return result;
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
    Fan fan = fanDao.findById(id)
        .orElseThrow(() -> new BadRequestException("Fan not found with id: " + id));
    return applyControlParams(fan, body);
  }

  private ControlDeviceResult applyControlParams(Fan fan, FanControlRequestBody body) {
    String gatewayIp = extractClientIpAddress(fan);
    String specificType = fan.getSpecificType();
    Integer duration = fan.getDuration();
    ControlDeviceResult result = new ControlDeviceResult();
    if (body.power() != null) {
      if (executeControl(result, "power", () -> gatewayControlClient.controlFanPower(gatewayIp, fan.getNaturalId(), body.power(), specificType, duration))) {
        fan.setPower(body.power());
      }
    }
    if (body.speed() != null) {
      if (executeControl(result, "speed", () -> gatewayControlClient.controlFanSpeed(gatewayIp, fan.getNaturalId(), body.speed(), specificType, duration))) {
        fan.setSpeed(body.speed());
      }
    }
    if (body.mode() != null) {
      if (executeControl(result, "mode", () -> gatewayControlClient.controlFanMode(gatewayIp, fan.getNaturalId(), body.mode(), specificType, duration))) {
        fan.setMode(body.mode());
      }
    }
    if (body.swing() != null) {
      if (executeControl(result, "swing", () -> gatewayControlClient.controlFanSwing(gatewayIp, fan.getNaturalId(), body.swing(), specificType, duration))) {
        fan.setSwing(body.swing());
      }
    }
    fanDao.save(fan);
    return result;
  }

  private boolean executeControl(ControlDeviceResult result, String parameter, Supplier<ResponseEntity<ApiResponse<String>>> call) {
    try {
      ResponseEntity<ApiResponse<String>> response = call.get();
      if (response.getStatusCode()
          .is2xxSuccessful()) {
        result.addDetail(parameter, true, "Success");
        return true;
      } else {
        result.addDetail(parameter, false, "Gateway error: " + response.getStatusCode());
        return false;
      }
    } catch (Exception e) {
      result.addDetail(parameter, false, e.getMessage());
      return false;
    }
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
