package com.iviet.ivshs.service.control.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dto.control.ControlDeviceResult;
import com.iviet.ivshs.dto.light.LightControlRequestBody;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.integration.gateway.GatewayAdapter;
import com.iviet.ivshs.integration.gateway.GatewayAdapterRegistry;
import com.iviet.ivshs.integration.gateway.GatewayCommand;
import com.iviet.ivshs.integration.gateway.GatewayOperationResult;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.service.control.LightControlService;
import com.iviet.ivshs.shared.util.DeviceCapabilityRegistry;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LightControlServiceImpl implements LightControlService {

  private final LightDao lightDao;
  private final GatewayAdapterRegistry gatewayAdapterRegistry;

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
  public ControlDeviceResult control(String naturalId, LightControlRequestBody body) {
    return applyControlParams(getOrThrow(naturalId), body);
  }

  @Override
  @Transactional
  public ControlDeviceResult control(Long id, LightControlRequestBody body) {
    Light light = lightDao.findById(id).orElseThrow(() -> new BadRequestException("Light not found with id: " + id));
    return applyControlParams(light, body);
  }

  private ControlDeviceResult applyControlParams(Light light, LightControlRequestBody body) {
    Client gatewayClient = extractClient(light);
    GatewayAdapter adapter = gatewayAdapterRegistry.get(gatewayClient.getClientType());
    String ip = gatewayClient.getIpAddress();
    String naturalId = light.getNaturalId();
    ControlDeviceResult result = new ControlDeviceResult();

    processParameter(light, result, "power", body.power(), () ->
        adapter.controlDevice(ip, GatewayCommand.of(
            naturalId, DeviceCategory.LIGHT, light.getSpecificType(), "power", body.power())),
        light::setPower);

    processParameter(light, result, "level", body.level(), () ->
        adapter.controlDevice(ip, GatewayCommand.of(
            naturalId, DeviceCategory.LIGHT, light.getSpecificType(), "level", body.level())),
        light::setLevel);

    lightDao.save(light);
    return result;
  }

  private <T> void processParameter(Light light, ControlDeviceResult result, String parameter, T value,
      Supplier<GatewayOperationResult> gatewayCall, Consumer<T> entitySetter) {
    if (value == null) {
      return;
    }
    if (!DeviceCapabilityRegistry.isSupported(light, parameter)) {
      result.addDetail(parameter, false,
          "Light of type " + light.getSpecificType() + " does not support " + parameter + " control");
      return;
    }
    GatewayOperationResult opResult = gatewayCall.get();
    result.addDetail(parameter, opResult.success(), opResult.message());
    if (opResult.success()) {
      entitySetter.accept(value);
    }
  }

  private Light getOrThrow(String naturalId) {
    return lightDao.findByNaturalId(naturalId)
        .orElseThrow(() -> new BadRequestException("Light not found with naturalId: " + naturalId));
  }

  private Client extractClient(Light light) {
    HardwareConfig control = light.getHardwareConfig();
    if (control == null) {
      throw new BadRequestException("DeviceControl not found for Light: " + light.getId());
    }
    Client client = control.getClient();
    if (client == null) {
      throw new BadRequestException("Client not found for DeviceControl: " + control.getId());
    }
    if (client.getIpAddress() == null || client.getIpAddress().isBlank()) {
      throw new BadRequestException("IP Address not found for Client: " + client.getId());
    }
    return client;
  }
}
