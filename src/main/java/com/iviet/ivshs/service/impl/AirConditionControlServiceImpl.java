package com.iviet.ivshs.service.control.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dto.AirConditionControlRequestBody;
import com.iviet.ivshs.dto.ControlDeviceResult;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.integration.gateway.GatewayAdapter;
import com.iviet.ivshs.integration.gateway.GatewayAdapterRegistry;
import com.iviet.ivshs.integration.gateway.GatewayCommand;
import com.iviet.ivshs.integration.gateway.GatewayOperationResult;
import com.iviet.ivshs.service.control.AirConditionControlService;
import com.iviet.ivshs.shared.enumeration.ActuatorMode;
import com.iviet.ivshs.shared.enumeration.ActuatorPower;
import com.iviet.ivshs.shared.enumeration.ActuatorSwing;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirConditionControlServiceImpl implements AirConditionControlService {

  private final AirConditionDao airConditionDao;
  private final GatewayAdapterRegistry gatewayAdapterRegistry;

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
  public ControlDeviceResult control(String naturalId, AirConditionControlRequestBody body) {
    return executeAcControl(getOrThrow(naturalId), body.power(), body.temperature(), body.mode(), body.fanSpeed(),
        body.swing());
  }

  @Override
  @Transactional
  public ControlDeviceResult control(Long id, AirConditionControlRequestBody body) {
    AirCondition ac = airConditionDao.findById(id)
        .orElseThrow(() -> new BadRequestException("AirCondition not found with id: " + id));
    return executeAcControl(ac, body.power(), body.temperature(), body.mode(), body.fanSpeed(), body.swing());
  }

  private ControlDeviceResult executeAcControl(AirCondition ac, ActuatorPower targetPower, Integer targetTemp,
      ActuatorMode targetMode, Integer targetSpeed, ActuatorSwing targetSwing) {
    boolean isOtherChanged = targetTemp != null || targetMode != null || targetSpeed != null || targetSwing != null;

    ActuatorPower targetOrAutoPower = targetPower;
    if (targetOrAutoPower == null && isOtherChanged && ac.getPower() != ActuatorPower.ON) {
      targetOrAutoPower = ActuatorPower.ON;
    }

    boolean isPowerChanged = targetOrAutoPower != null && targetOrAutoPower != ac.getPower();
    Client gatewayClient = extractClient(ac);
    GatewayAdapter adapter = gatewayAdapterRegistry.get(gatewayClient.getClientType());
    String ip = gatewayClient.getIpAddress();
    GatewayCommand command = buildCommand(ac, targetOrAutoPower, targetTemp, targetMode, targetSpeed, targetSwing);

    ControlDeviceResult result = new ControlDeviceResult();
    boolean hasFailed = false;

    if (isPowerChanged) {
      hasFailed = !executeControl(adapter, ip, command, result, "power");
      if (ac.getPower() != null && targetOrAutoPower != null) {
        ac.setPower(targetOrAutoPower);
      }
    }

    if (!hasFailed && isOtherChanged) {
      if (executeControl(adapter, ip, command, result, "remote")) {
        if (targetTemp != null) ac.setTemperature(targetTemp);
        if (targetMode != null) ac.setMode(targetMode);
        if (targetSpeed != null) ac.setFanSpeed(targetSpeed);
        if (targetSwing != null) ac.setSwing(targetSwing);
      }
    }

    airConditionDao.save(ac);
    return result;
  }

  private GatewayCommand buildCommand(AirCondition ac, ActuatorPower power, Integer temp, ActuatorMode mode,
      Integer speed, ActuatorSwing swing) {
    Map<String, Object> acParams = new LinkedHashMap<>();
    if (power != null) acParams.put("power", power.name());
    if (temp != null) acParams.put("temperature", temp);
    if (mode != null) acParams.put("mode", mode.name());
    if (speed != null) acParams.put("speed", speed);
    if (swing != null) acParams.put("swing", swing.name());

    return new GatewayCommand(
        ac.getNaturalId(), DeviceCategory.AIR_CONDITION,
        ac.getSpecificType(), ac.getDuration(), acParams, Map.of());
  }

  private boolean executeControl(GatewayAdapter adapter, String ip, GatewayCommand command,
      ControlDeviceResult result, String paramName) {
    try {
      GatewayOperationResult opResult = adapter.controlDevice(ip, command);
      result.addDetail(paramName, opResult.success(), opResult.message());
      return opResult.success();
    } catch (Exception e) {
      result.addDetail(paramName, false, e.getMessage());
      return false;
    }
  }

  private AirCondition getOrThrow(String naturalId) {
    return airConditionDao.findByNaturalId(naturalId)
        .orElseThrow(() -> new BadRequestException("AirCondition not found with naturalId: " + naturalId));
  }

  private Client extractClient(AirCondition ac) {
    HardwareConfig control = ac.getHardwareConfig();
    if (control == null) {
      throw new BadRequestException("DeviceControl not found for AirCondition: " + ac.getId());
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
