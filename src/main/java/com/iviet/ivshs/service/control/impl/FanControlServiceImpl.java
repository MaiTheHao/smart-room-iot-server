package com.iviet.ivshs.service.control.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.control.ControlDeviceResult;
import com.iviet.ivshs.dto.control.DeviceControlPayload;
import com.iviet.ivshs.dto.fan.FanControlRequestBody;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.service.control.FanControlService;
import com.iviet.ivshs.integration.gateway.GatewayFanControlClient;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.shared.util.DeviceCapabilityRegistry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FanControlServiceImpl implements FanControlService {

  private final FanDao fanDao;
  private final GatewayFanControlClient gatewayControlClient;

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
    return applyControlParams(getOrThrow(naturalId), body);
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
    String naturalId = fan.getNaturalId();
    ControlDeviceResult result = new ControlDeviceResult();

    processParameter(
        fan,
        result,
        "power",
        body.power(),
        () -> gatewayControlClient.controlFanPower(
            gatewayIp,
            naturalId,
            DeviceControlPayload.of(fan.getSpecificType(), fan.getDuration(), body.power())),
        fan::setPower);

    processParameter(
        fan,
        result,
        "speed",
        body.speed(),
        () -> gatewayControlClient.controlFanSpeed(
            gatewayIp,
            naturalId,
            DeviceControlPayload.of(fan.getSpecificType(), fan.getDuration(), body.speed())),
        fan::setSpeed);

    processParameter(
        fan,
        result,
        "mode",
        body.mode(),
        () -> gatewayControlClient.controlFanMode(
            gatewayIp,
            naturalId,
            DeviceControlPayload.of(fan.getSpecificType(), fan.getDuration(), body.mode())),
        fan::setMode);

    processParameter(
        fan,
        result,
        "swing",
        body.swing(),
        () -> gatewayControlClient.controlFanSwing(
            gatewayIp,
            naturalId,
            DeviceControlPayload.of(fan.getSpecificType(), fan.getDuration(), body.swing())),
        fan::setSwing);

    fanDao.save(fan);
    return result;
  }

  private <T> void processParameter(Fan fan, ControlDeviceResult result, String parameter, T value,
      Supplier<ResponseEntity<ApiResponse<String>>> gatewayCall, Consumer<T> entitySetter) {
    if (value == null) { return; }
    if (!DeviceCapabilityRegistry.isSupported(fan, parameter)) {
      result.addDetail(
          parameter,
          false,
          "Fan of type " + fan.getSpecificType() + " does not support " + parameter + " control");
      return;
    }
    if (executeControl(result, parameter, gatewayCall)) {
      entitySetter.accept(value);
    }
  }

  private boolean executeControl(ControlDeviceResult result, String parameter,
      Supplier<ResponseEntity<ApiResponse<String>>> call) {
    try {
      ResponseEntity<ApiResponse<String>> response = call.get();
      if (response.getStatusCode()
          .is2xxSuccessful()) {
        result.addDetail(parameter, true, "Success");
        return true;
      }
      result.addDetail(parameter, false, "Gateway error: " + response.getStatusCode());
      return false;
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
    if (control == null) { throw new BadRequestException("DeviceControl not found for Fan: " + fan.getId()); }
    Client client = control.getClient();
    if (client == null) { throw new BadRequestException("Client not found for DeviceControl: " + control.getId()); }
    String gatewayIp = client.getIpAddress();
    if (gatewayIp == null || gatewayIp.isBlank()) {
      throw new BadRequestException("IP Address not found for Client: " + client.getId());
    }
    return gatewayIp;
  }
}
