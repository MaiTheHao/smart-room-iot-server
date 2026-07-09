package com.iviet.ivshs.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dto.ControlDeviceResult;
import com.iviet.ivshs.dto.FanControlRequestBody;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.service.FanControlService;
import com.iviet.ivshs.integration.gateway.GatewayAdapter;
import com.iviet.ivshs.integration.gateway.GatewayAdapterRegistry;
import com.iviet.ivshs.integration.gateway.GatewayCommand;
import com.iviet.ivshs.integration.gateway.GatewayOperationResult;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.shared.util.DeviceCapabilityRegistry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FanControlServiceImpl implements FanControlService {

  private final FanDao fanDao;
  private final GatewayAdapterRegistry gatewayAdapterRegistry;

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
    Client gatewayClient = extractClient(fan);
    GatewayAdapter adapter = gatewayAdapterRegistry.get(gatewayClient.getClientType());
    String ip = gatewayClient.getIpAddress();
    String naturalId = fan.getNaturalId();
    ControlDeviceResult result = new ControlDeviceResult();

    processParameter(
        fan,
        result,
        "power",
        body.power(),
        () -> adapter.controlDevice(ip, GatewayCommand.of(
            naturalId, DeviceCategory.FAN, fan.getSpecificType(), fan.getDuration(), "power", body.power())),
        fan::setPower);

    processParameter(
        fan,
        result,
        "speed",
        body.speed(),
        () -> adapter.controlDevice(ip, GatewayCommand.of(
            naturalId, DeviceCategory.FAN, fan.getSpecificType(), fan.getDuration(), "speed", body.speed())),
        fan::setSpeed);

    processParameter(
        fan,
        result,
        "mode",
        body.mode(),
        () -> adapter.controlDevice(ip, GatewayCommand.of(
            naturalId, DeviceCategory.FAN, fan.getSpecificType(), fan.getDuration(), "mode", body.mode())),
        fan::setMode);

    processParameter(
        fan,
        result,
        "swing",
        body.swing(),
        () -> adapter.controlDevice(ip, GatewayCommand.of(
            naturalId, DeviceCategory.FAN, fan.getSpecificType(), fan.getDuration(), "swing", body.swing())),
        fan::setSwing);

    fanDao.save(fan);
    return result;
  }

  private <T> void processParameter(Fan fan, ControlDeviceResult result, String parameter, T value,
      Supplier<GatewayOperationResult> gatewayCall, Consumer<T> entitySetter) {
    if (value == null) { return; }
    if (!DeviceCapabilityRegistry.isSupported(fan, parameter)) {
      result.addDetail(
          parameter,
          false,
          "Fan of type " + fan.getSpecificType() + " does not support " + parameter + " control");
      return;
    }
    GatewayOperationResult opResult = gatewayCall.get();
    result.addDetail(parameter, opResult.success(), opResult.message());
    if (opResult.success()) {
      entitySetter.accept(value);
    }
  }

  private Fan getOrThrow(String naturalId) {
    return fanDao.findByNaturalId(naturalId)
        .orElseThrow(() -> new BadRequestException("Fan not found with naturalId: " + naturalId));
  }

  private Client extractClient(Fan fan) {
    HardwareConfig control = fan.getHardwareConfig();
    if (control == null) { throw new BadRequestException("DeviceControl not found for Fan: " + fan.getId()); }
    Client client = control.getClient();
    if (client == null) { throw new BadRequestException("Client not found for DeviceControl: " + control.getId()); }
    if (client.getIpAddress() == null || client.getIpAddress().isBlank()) {
      throw new BadRequestException("IP Address not found for Client: " + client.getId());
    }
    return client;
  }
}
