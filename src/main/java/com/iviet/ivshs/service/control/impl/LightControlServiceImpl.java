package com.iviet.ivshs.service.control.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import java.util.function.Supplier;

@Slf4j
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
		Light light = getOrThrow(naturalId);
		return applyControlParams(light, body);
	}

	@Override
	@Transactional
	public ControlDeviceResult control(Long id, LightControlRequestBody body) {
		Light light = lightDao.findById(id)
				.orElseThrow(() -> new BadRequestException("Light not found with id: " + id));
		return applyControlParams(light, body);
	}

	private ControlDeviceResult applyControlParams(Light light, LightControlRequestBody body) {
		String gatewayIp = extractClientIpAddress(light);
		ControlDeviceResult result = new ControlDeviceResult();

		if (body.power() != null) {
			DeviceControlPayload powerPayload = DeviceControlPayload.of(light.getSpecificType(), body.power());
			if (executeControl(result, "power", () -> gatewayControlClient.controlLightPower(gatewayIp, light.getNaturalId(), powerPayload))) {
				light.setPower(body.power());
			}
		}
		if (body.level() != null) {
			if (!DeviceCapabilityRegistry.isSupported(light, "level")) {
				result.addDetail("level", false, "Light does not support level control");
			} else {
				DeviceControlPayload levelPayload = DeviceControlPayload.of(light.getSpecificType(), body.level());
				if (executeControl(result, "level", () -> gatewayControlClient.controlLightLevel(gatewayIp, light.getNaturalId(), levelPayload))) {
					light.setLevel(body.level());
				}
			}
		}
		lightDao.save(light);
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
