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
    AirCondition ac = getOrThrow(naturalId);
    return executeAcControl(ac, body.power(), body.temperature(), body.mode(), body.fanSpeed(), body.swing());
  }

  @Override
  @Transactional
  public ControlDeviceResult control(Long id, AirConditionControlRequestBody body) {
    AirCondition ac = airConditionDao.findById(id)
        .orElseThrow(() -> new BadRequestException("AirCondition not found with id: " + id));
    return executeAcControl(ac, body.power(), body.temperature(), body.mode(), body.fanSpeed(), body.swing());
  }

  /**
   * Core control logic. duration and specificType are always read from the entity
   * (device setup config) — they are not overridable per-request.
   */
  private ControlDeviceResult executeAcControl(AirCondition ac,
      ActuatorPower targetPower,
      Integer targetTemp,
      ActuatorMode targetMode,
      Integer targetSpeed,
      ActuatorSwing targetSwing) {

    boolean isOtherChanged = targetTemp != null || targetMode != null || targetSpeed != null || targetSwing != null;

    ActuatorPower targetOrAutoPower = targetPower;
    if (targetOrAutoPower == null && isOtherChanged && ac.getPower() != ActuatorPower.ON) {
      targetOrAutoPower = ActuatorPower.ON;
    }

    boolean isPowerChanged = targetOrAutoPower != null && targetOrAutoPower != ac.getPower();

    String gatewayIp = extractClientIpAddress(ac);
    ControlDeviceResult result = new ControlDeviceResult();

    // Merge: target value takes priority, fall back to current entity state
    ActuatorPower finalPower = targetOrAutoPower != null ? targetOrAutoPower : ac.getPower();
    Integer finalTemp = targetTemp != null ? targetTemp : ac.getTemperature();
    ActuatorMode finalMode = targetMode != null ? targetMode : ac.getMode();
    Integer finalSpeed = targetSpeed != null ? targetSpeed : ac.getFanSpeed();
    ActuatorSwing finalSwing = targetSwing != null ? targetSwing : ac.getSwing();

    // duration and specificType are device setup config — always from entity
    AcRemoteRequestPayload payload = AcRemoteRequestPayload.builder()
        .power(finalPower != null ? finalPower.name() : null)
        .temperature(finalTemp)
        .mode(finalMode != null ? finalMode.name() : null)
        .speed(finalSpeed)
        .swing(finalSwing != null ? finalSwing.name() : null)
        .duration(ac.getDuration())
        .specificType(ac.getSpecificType())
        .build();

    boolean hasFailed = false;

    if (isPowerChanged) {
      try {
        ResponseEntity<ApiResponse<String>> response = gatewayControlClient.controlAcPower(gatewayIp, ac.getNaturalId(), payload);
        if (response.getStatusCode().is2xxSuccessful()) {
          result.addDetail("power", true, "Success");
          ac.setPower(targetOrAutoPower);
          airConditionDao.save(ac);
        } else {
          result.addDetail("power", false, "Gateway error: " + response.getStatusCode());
          hasFailed = true;
        }
      } catch (Exception e) {
        result.addDetail("power", false, e.getMessage());
        hasFailed = true;
      }
    }

    if (!hasFailed && isOtherChanged) {
      try {
        ResponseEntity<ApiResponse<String>> response = gatewayControlClient.controlAcRemote(gatewayIp, ac.getNaturalId(), payload);
        if (response.getStatusCode().is2xxSuccessful()) {
          result.addDetail("remote", true, "Success");

          if (targetTemp != null) ac.setTemperature(targetTemp);
          if (targetMode != null) ac.setMode(targetMode);
          if (targetSpeed != null) ac.setFanSpeed(targetSpeed);
          if (targetSwing != null) ac.setSwing(targetSwing);

          airConditionDao.save(ac);
        } else {
          result.addDetail("remote", false, "Gateway error: " + response.getStatusCode());
        }
      } catch (Exception e) {
        result.addDetail("remote", false, e.getMessage());
      }
    }

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
