package com.iviet.ivshs.service.control.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.control.ControlDeviceResult;
import com.iviet.ivshs.dto.control.DeviceControlPayload;
import com.iviet.ivshs.dto.light.LightControlRequestBody;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Light;
import com.iviet.ivshs.integration.gateway.GatewayLightControlClient;
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
  private final GatewayLightControlClient gatewayControlClient;

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
    String gatewayIp = extractClientIpAddress(light);
    String naturalId = light.getNaturalId();
    ControlDeviceResult result = new ControlDeviceResult();

    processParameter(light, result, "power", body.power(), () -> gatewayControlClient.controlLightPower(gatewayIp,
        naturalId, DeviceControlPayload.of(light.getSpecificType(), body.power())), light::setPower);

    processParameter(light, result, "level", body.level(), () -> gatewayControlClient.controlLightLevel(gatewayIp,
        naturalId, DeviceControlPayload.of(light.getSpecificType(), body.level())), light::setLevel);

    lightDao.save(light);
    return result;
  }

  private <T> void processParameter(Light light, ControlDeviceResult result, String parameter, T value,
      Supplier<ResponseEntity<ApiResponse<String>>> gatewayCall, Consumer<T> entitySetter) {
    if (value == null) {
      return;
    }
    if (!DeviceCapabilityRegistry.isSupported(light, parameter)) {
      result.addDetail(parameter, false,
          "Light of type " + light.getSpecificType() + " does not support " + parameter + " control");
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
      if (response.getStatusCode().is2xxSuccessful()) {
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
