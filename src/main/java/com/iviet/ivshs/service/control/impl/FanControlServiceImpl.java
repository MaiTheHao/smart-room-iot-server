package com.iviet.ivshs.service.control.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.control.ControlDeviceResult;
import com.iviet.ivshs.dto.control.DeviceControlPayload;
import com.iviet.ivshs.dto.fan.FanControlRequestBody;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.service.control.FanControlService;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.shared.util.DeviceCapabilityRegistry;
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
    ControlDeviceResult result = new ControlDeviceResult();

    if (body.power() != null) {
      DeviceControlPayload powerPayload = DeviceControlPayload.of(fan.getSpecificType(), fan.getDuration(), body.power());
      if (executeControl(result, "power", () -> gatewayControlClient.controlFanPower(gatewayIp, fan.getNaturalId(), powerPayload))) {
        fan.setPower(body.power());
      }
    }
    if (body.speed() != null) {
      if (!DeviceCapabilityRegistry.isSupported(fan, "speed")) {
        result.addDetail("speed", false, "Fan does not support speed control");
      } else {
        DeviceControlPayload speedPayload = DeviceControlPayload.of(fan.getSpecificType(), fan.getDuration(), body.speed());
        if (executeControl(result, "speed", () -> gatewayControlClient.controlFanSpeed(gatewayIp, fan.getNaturalId(), speedPayload))) {
          fan.setSpeed(body.speed());
        }
      }
    }
    if (body.mode() != null) {
      if (!DeviceCapabilityRegistry.isSupported(fan, "mode")) {
        result.addDetail("mode", false, "Fan does not support mode control");
      } else {
        DeviceControlPayload modePayload = DeviceControlPayload.of(fan.getSpecificType(), fan.getDuration(), body.mode());
        if (executeControl(result, "mode", () -> gatewayControlClient.controlFanMode(gatewayIp, fan.getNaturalId(), modePayload))) {
          fan.setMode(body.mode());
        }
      }
    }
    if (body.swing() != null) {
      if (!DeviceCapabilityRegistry.isSupported(fan, "swing")) {
        result.addDetail("swing", false, "Fan does not support swing control");
      } else {
        DeviceControlPayload swingPayload = DeviceControlPayload.of(fan.getSpecificType(), fan.getDuration(), body.swing());
        if (executeControl(result, "swing", () -> gatewayControlClient.controlFanSwing(gatewayIp, fan.getNaturalId(), swingPayload))) {
          fan.setSwing(body.swing());
        }
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
