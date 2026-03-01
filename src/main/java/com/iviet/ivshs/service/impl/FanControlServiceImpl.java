package com.iviet.ivshs.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dto.FanControlRequestBody;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanIr;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorState;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.enumeration.DeviceCategory;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.service.FanControlService;
import com.iviet.ivshs.util.HttpClientUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FanControlServiceImpl implements FanControlService {

    private final FanDao fanDao;

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
    public void handlePowerControl(String naturalId, ActuatorPower power) {
        Fan fan = fanDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("Fan not found with naturalId: " + naturalId));
        String gatewayIp = extractClientIpAddress(fan);

        fan.setPower(power);
        fanDao.save(fan);
        
        handlePowerControlCall(gatewayIp, fan.getNaturalId(), power);
    }

    private void handlePowerControlCall(String gatewayIp, String naturalId, ActuatorPower power) {
        String url = UrlConstant.getControlFanPowerUrlV2(gatewayIp, naturalId);
        Map<String, Object> payload = Map.of("data", power);
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    public void handleTogglePowerControl(String naturalId) {
        Fan fan = fanDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("Fan not found with naturalId: " + naturalId));
        ActuatorPower newPowerState = (fan.getPower() == ActuatorPower.ON) ? ActuatorPower.OFF : ActuatorPower.ON;
        String gatewayIp = extractClientIpAddress(fan);

        fan.setPower(newPowerState);
        fanDao.save(fan);

        handlePowerControlCall(gatewayIp, fan.getNaturalId(), newPowerState);
    }

    @Override
    @Transactional
    public void handleModeControl(String naturalId, ActuatorMode mode) {
        Fan fan = fanDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("Fan not found with naturalId: " + naturalId));
        String gatewayIp = extractClientIpAddress(fan);

        if (fan instanceof FanIr fanIr) {
            fanIr.setMode(mode);
            fanDao.save(fanIr);
        }
        
        handleModeControlCall(gatewayIp, fan.getNaturalId(), mode);
    }

    private void handleModeControlCall(String gatewayIp, String naturalId, ActuatorMode mode) {
        String url = UrlConstant.getControlFanModeUrlV2(gatewayIp, naturalId);
        Map<String, Object> payload = Map.of("data", mode);
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    public void handleSpeedControl(String naturalId, int speed) {
        Fan fan = fanDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("Fan not found with naturalId: " + naturalId));
        String gatewayIp = extractClientIpAddress(fan);

        if (fan instanceof FanIr fanIr) {
            fanIr.setSpeed(speed);
            fanDao.save(fanIr);
        }
        
        handleSpeedControlCall(gatewayIp, fan.getNaturalId(), speed);
    }

    private void handleSpeedControlCall(String gatewayIp, String naturalId, int speed) {
        String url = UrlConstant.getControlFanSpeedUrlV2(gatewayIp, naturalId);
        Map<String, Object> payload = Map.of("data", speed);
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    public void handleSwingControl(String naturalId, ActuatorSwing swing) {
        Fan fan = fanDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("Fan not found with naturalId: " + naturalId));
        String gatewayIp = extractClientIpAddress(fan);

        if (fan instanceof FanIr fanIr) {
            fanIr.setSwing(swing);
            fanDao.save(fanIr);
        }
        
        handleSwingControlCall(gatewayIp, fan.getNaturalId(), swing);
    }

    private void handleSwingControlCall(String gatewayIp, String naturalId, ActuatorSwing swing) {
        String url = UrlConstant.getControlFanSwingUrlV2(gatewayIp, naturalId);
        Map<String, Object> payload = Map.of("data", swing);
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    public void handleLightControl(String naturalId, ActuatorState light) {
        Fan fan = fanDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("Fan not found with naturalId: " + naturalId));
        String gatewayIp = extractClientIpAddress(fan);

        if (fan instanceof FanIr fanIr) {
            fanIr.setLight(light);
            fanDao.save(fanIr);
        }
        
        handleLightControlCall(gatewayIp, fan.getNaturalId(), light);
    }

    private void handleLightControlCall(String gatewayIp, String naturalId, ActuatorState light) {
        String url = UrlConstant.getControlFanLightUrlV2(gatewayIp, naturalId);
        Map<String, Object> payload = Map.of("data", light);
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    public void control(String naturalId, FanControlRequestBody body) {
        Fan fan = fanDao.findByNaturalId(naturalId).orElseThrow(() -> new BadRequestException("Fan not found with naturalId: " + naturalId));
        applyControlParams(fan, body);
    }

    @Override
    @Transactional
    public void control(Long id, FanControlRequestBody body) {
        Fan fan = fanDao.findById(id).orElseThrow(() -> new BadRequestException("Fan not found with id: " + id));
        applyControlParams(fan, body);
    }

    private void applyControlParams(Fan fan, FanControlRequestBody body) {
        String gatewayIp = extractClientIpAddress(fan);

        if (body.power() != null) {
            fan.setPower(body.power());
            handlePowerControlCall(gatewayIp, fan.getNaturalId(), body.power());
        }

        if (body.speed() != null) {
            if (fan instanceof FanIr fanIr) {
                fanIr.setSpeed(body.speed());
            }
            handleSpeedControlCall(gatewayIp, fan.getNaturalId(), body.speed());
        }

        if (body.mode() != null) {
            if (fan instanceof FanIr fanIr) {
                fanIr.setMode(body.mode());
            }
            handleModeControlCall(gatewayIp, fan.getNaturalId(), body.mode());
        }

        if (body.swing() != null) {
            if (fan instanceof FanIr fanIr) {
                fanIr.setSwing(body.swing());
            }
            handleSwingControlCall(gatewayIp, fan.getNaturalId(), body.swing());
        }
        
        if (body.light() != null) {
            if (fan instanceof FanIr fanIr) {
                fanIr.setLight(body.light());
            }
            handleLightControlCall(gatewayIp, fan.getNaturalId(), body.light());
        }
        
        fanDao.save(fan);
    }

    private String extractClientIpAddress(Fan fan) {
        DeviceControl control = fan.getDeviceControl();
        if (control == null) throw new BadRequestException("Device control not found for fan with id: " + fan.getId());
        Client client = control.getClient();
        if (client == null) throw new BadRequestException("Client not found for device control with id: " + control.getId());
        String gatewayIp = client.getIpAddress();
        if (gatewayIp == null || gatewayIp.isBlank()) throw new BadRequestException("Gateway IP not found for client with id: " + client.getId());
        return gatewayIp;
    }
}
