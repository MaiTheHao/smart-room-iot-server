package com.iviet.ivshs.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dto.AirConditionControlRequestBody;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.AirConditionControlService;

import lombok.RequiredArgsConstructor;

import com.iviet.ivshs.dto.ControlDeviceResult;
import org.springframework.http.ResponseEntity;
import com.iviet.ivshs.dto.ApiResponse;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirConditionControlServiceImpl implements AirConditionControlService {

  private final AirConditionDao airConditionDao;
  private final com.iviet.ivshs.service.client.gateway.GatewayControlClient gatewayControlClient;

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
  public ControlDeviceResult handlePowerControl(String naturalId, ActuatorPower power) {
    AirCondition ac = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(ac);
    ControlDeviceResult result = handlePowerControlCall(gatewayIp, ac.getNaturalId(), power);
    if (result.getSuccessCount() > 0) {
      ac.setPower(power);
      airConditionDao.save(ac);
    }
    return result;
  }

  @Override
  @Transactional
  public ControlDeviceResult handleTogglePowerControl(String naturalId) {
    AirCondition ac = getOrThrow(naturalId);
    ActuatorPower newPowerState = (ac.getPower() == ActuatorPower.ON) ? ActuatorPower.OFF : ActuatorPower.ON;
    String gatewayIp = extractClientIpAddress(ac);
    ControlDeviceResult result = handlePowerControlCall(gatewayIp, ac.getNaturalId(), newPowerState);
    if (result.getSuccessCount() > 0) {
      ac.setPower(newPowerState);
      airConditionDao.save(ac);
    }
    return result;
  }

  @Override
  @Transactional
  public ControlDeviceResult handleTemperatureControl(String naturalId, int temperature) {
    AirCondition ac = getOrThrow(naturalId);
    int currentTemp = ac.getTemperature() != null ? ac.getTemperature() : 25;
    String gatewayIp = extractClientIpAddress(ac);
    ControlDeviceResult result = handleTemperatureControlCall(gatewayIp, ac.getNaturalId(), temperature, currentTemp);
    if (result.getSuccessCount() > 0) {
      ac.setTemperature(temperature);
      airConditionDao.save(ac);
    }
    return result;
  }

  @Override
  @Transactional
  public ControlDeviceResult handleModeControl(String naturalId, ActuatorMode mode) {
    AirCondition ac = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(ac);
    ControlDeviceResult result = handleModeControlCall(gatewayIp, ac.getNaturalId(), mode);
    if (result.getSuccessCount() > 0) {
      ac.setMode(mode);
      airConditionDao.save(ac);
    }
    return result;
  }

  @Override
  @Transactional
  public ControlDeviceResult handleFanSpeedControl(String naturalId, int speed) {
    AirCondition ac = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(ac);
    ControlDeviceResult result = handleFanSpeedControlCall(gatewayIp, ac.getNaturalId(), speed);
    if (result.getSuccessCount() > 0) {
      ac.setFanSpeed(speed);
      airConditionDao.save(ac);
    }
    return result;
  }

  @Override
  @Transactional
  public ControlDeviceResult handleSwingControl(String naturalId, ActuatorSwing swing) {
    AirCondition ac = getOrThrow(naturalId);
    String gatewayIp = extractClientIpAddress(ac);
    ControlDeviceResult result = handleSwingControlCall(gatewayIp, ac.getNaturalId(), swing);
    if (result.getSuccessCount() > 0) {
      ac.setSwing(swing);
      airConditionDao.save(ac);
    }
    return result;
  }

  @Override
  @Transactional
  public ControlDeviceResult control(String naturalId, AirConditionControlRequestBody body) {
    AirCondition ac = getOrThrow(naturalId);
    return applyControlParams(ac, body);
  }

  @Override
  @Transactional
  public ControlDeviceResult control(Long id, AirConditionControlRequestBody body) {
    AirCondition ac = airConditionDao.findById(id)
      .orElseThrow(() -> new BadRequestException("AirCondition not found with id: " + id));
    return applyControlParams(ac, body);
  }

  private ControlDeviceResult applyControlParams(AirCondition ac, AirConditionControlRequestBody body) {
    String gatewayIp = extractClientIpAddress(ac);
    ControlDeviceResult result = new ControlDeviceResult();
    if (body.power() != null) {
      if (executeControl(result, "power", () -> gatewayControlClient.controlAcPowerV2(gatewayIp, ac.getNaturalId(), body.power()))) {
        ac.setPower(body.power());
      }
    }
    if (body.temperature() != null) {
      int currentTemp = ac.getTemperature() != null ? ac.getTemperature() : 25;
      int targetTemp = body.temperature();
      if (executeControl(result, "temperature", () -> {
        if (targetTemp > currentTemp) {
          return gatewayControlClient.controlAcTempUpV2(gatewayIp, ac.getNaturalId(), targetTemp);
        } else {
          return gatewayControlClient.controlAcTempDownV2(gatewayIp, ac.getNaturalId(), targetTemp);
        }
      })) {
        ac.setTemperature(targetTemp);
      }
    }
    if (body.mode() != null) {
      if (executeControl(result, "mode", () -> gatewayControlClient.controlAcModeV2(gatewayIp, ac.getNaturalId(), body.mode()))) {
        ac.setMode(body.mode());
      }
    }
    if (body.fanSpeed() != null) {
      if (executeControl(result, "fanSpeed", () -> gatewayControlClient.controlAcFanV2(gatewayIp, ac.getNaturalId(), body.fanSpeed() == 0 ? "AUTO" : body.fanSpeed()))) {
        ac.setFanSpeed(body.fanSpeed());
      }
    }
    if (body.swing() != null) {
      if (executeControl(result, "swing", () -> gatewayControlClient.controlAcSwingV2(gatewayIp, ac.getNaturalId(), body.swing()))) {
        ac.setSwing(body.swing());
      }
    }
    airConditionDao.save(ac);
    return result;
  }

  private boolean executeControl(ControlDeviceResult result, String parameter, Supplier<ResponseEntity<ApiResponse<String>>> call) {
    try {
      ResponseEntity<ApiResponse<String>> response = call.get();
      if (response.getStatusCode().is2xxSuccessful()) {
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

  private ControlDeviceResult handlePowerControlCall(String gatewayIp, String naturalId, ActuatorPower power) {
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "power", () -> gatewayControlClient.controlAcPowerV2(gatewayIp, naturalId, power));
    return result;
  }

  private ControlDeviceResult handleTemperatureControlCall(String gatewayIp, String naturalId, int temperature, int currentTemp) {
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "temperature", () -> {
      if (temperature > currentTemp) {
        return gatewayControlClient.controlAcTempUpV2(gatewayIp, naturalId, temperature);
      } else {
        return gatewayControlClient.controlAcTempDownV2(gatewayIp, naturalId, temperature);
      }
    });
    return result;
  }

  private ControlDeviceResult handleModeControlCall(String gatewayIp, String naturalId, ActuatorMode mode) {
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "mode", () -> gatewayControlClient.controlAcModeV2(gatewayIp, naturalId, mode));
    return result;
  }

  private ControlDeviceResult handleFanSpeedControlCall(String gatewayIp, String naturalId, int speed) {
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "fanSpeed", () -> gatewayControlClient.controlAcFanV2(gatewayIp, naturalId, speed == 0 ? "AUTO" : speed));
    return result;
  }

  private ControlDeviceResult handleSwingControlCall(String gatewayIp, String naturalId, ActuatorSwing swing) {
    ControlDeviceResult result = new ControlDeviceResult();
    executeControl(result, "swing", () -> gatewayControlClient.controlAcSwingV2(gatewayIp, naturalId, swing));
    return result;
  }

  private AirCondition getOrThrow(String naturalId) {
    return airConditionDao.findByNaturalId(naturalId)
      .orElseThrow(() -> new BadRequestException("AirCondition not found with naturalId: " + naturalId));
  }

  private String extractClientIpAddress(AirCondition ac) {
    HardwareConfig control = ac.getHardwareConfig();
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