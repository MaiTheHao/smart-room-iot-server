package com.iviet.ivshs.service.control.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dto.aircondition.AirConditionControlRequestBody;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.control.AcRemoteRequestPayload;
import com.iviet.ivshs.dto.control.ControlDeviceResult;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.integration.gateway.GatewayAcControlClient;
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
  private final GatewayAcControlClient gatewayControlClient;

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
    String gatewayIp = extractClientIpAddress(ac);
    AcRemoteRequestPayload payload = buildPayload(ac, targetOrAutoPower, targetTemp, targetMode, targetSpeed,
        targetSwing);

    ControlDeviceResult result = new ControlDeviceResult();
    boolean hasFailed = false;

    if (isPowerChanged) {
      hasFailed = !executePowerControl(ac, gatewayIp, targetOrAutoPower, payload, result);
    }

    if (!hasFailed && isOtherChanged) {
      executeRemoteControl(ac, gatewayIp, targetTemp, targetMode, targetSpeed, targetSwing, payload, result);
    }

    return result;
  }

  private AcRemoteRequestPayload buildPayload(AirCondition ac, ActuatorPower power, Integer temp, ActuatorMode mode,
      Integer speed, ActuatorSwing swing) {
    return AcRemoteRequestPayload.builder()
        .power(power != null ? power.name() : (ac.getPower() != null ? ac.getPower().name() : null))
        .temperature(temp != null ? temp : ac.getTemperature())
        .mode(mode != null ? mode.name() : (ac.getMode() != null ? ac.getMode().name() : null))
        .speed(speed != null ? speed : ac.getFanSpeed())
        .swing(swing != null ? swing.name() : (ac.getSwing() != null ? ac.getSwing().name() : null))
        .duration(ac.getDuration()).specificType(ac.getSpecificType()).build();
  }

  private boolean executePowerControl(AirCondition ac, String gatewayIp, ActuatorPower targetPower,
      AcRemoteRequestPayload payload, ControlDeviceResult result) {
    try {
      ResponseEntity<ApiResponse<String>> response = gatewayControlClient.controlAcPower(gatewayIp, ac.getNaturalId(),
          payload);
      if (response.getStatusCode().is2xxSuccessful()) {
        result.addDetail("power", true, "Success");
        ac.setPower(targetPower);
        airConditionDao.save(ac);
        return true;
      }
      result.addDetail("power", false, "Gateway error: " + response.getStatusCode());
      return false;
    } catch (Exception e) {
      result.addDetail("power", false, e.getMessage());
      return false;
    }
  }

  private void executeRemoteControl(AirCondition ac, String gatewayIp, Integer temp, ActuatorMode mode, Integer speed,
      ActuatorSwing swing, AcRemoteRequestPayload payload, ControlDeviceResult result) {
    try {
      ResponseEntity<ApiResponse<String>> response = gatewayControlClient.controlAcRemote(gatewayIp, ac.getNaturalId(),
          payload);
      if (response.getStatusCode().is2xxSuccessful()) {
        result.addDetail("remote", true, "Success");
        if (temp != null) ac.setTemperature(temp);
        if (mode != null) ac.setMode(mode);
        if (speed != null) ac.setFanSpeed(speed);
        if (swing != null) ac.setSwing(swing);
        airConditionDao.save(ac);
      } else {
        result.addDetail("remote", false, "Gateway error: " + response.getStatusCode());
      }
    } catch (Exception e) {
      result.addDetail("remote", false, e.getMessage());
    }
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
