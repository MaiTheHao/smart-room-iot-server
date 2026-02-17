package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dao.DeviceControlDao;
import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.AirConditionDto;
import com.iviet.ivshs.dto.CreateAirConditionDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateAirConditionDto;
import com.iviet.ivshs.entities.AirCondition;
import com.iviet.ivshs.entities.AirConditionLan;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.AirConditionService;
import com.iviet.ivshs.util.HttpClientUtil;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirConditionServiceImpl implements AirConditionService {

    private final AirConditionDao airConditionDao;
    private final RoomDao roomDao;
    private final DeviceControlDao deviceControlDao;
    private final LanguageDao languageDao;

    @Override
    public PaginatedResponse<AirConditionDto> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<AirConditionDto> data = airConditionDao.findAll(page, size, langCode);
        Long totalElements = airConditionDao.count();
        return new PaginatedResponse<>(data, page, size, totalElements);
    }

    @Override
    public List<AirConditionDto> getAll() {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return airConditionDao.findAll(langCode);
    }

    @Override
    public PaginatedResponse<AirConditionDto> getListByRoomId(Long roomId, int page, int size) {
        if (roomId == null) {
            throw new BadRequestException("Room ID is required");
        }

        String langCode = LocalContextUtil.getCurrentLangCode();
        List<AirConditionDto> data = airConditionDao.findAllByRoomId(roomId, page, size, langCode);
        Long totalElements = airConditionDao.countByRoomId(roomId);
        return new PaginatedResponse<>(data, page, size, totalElements);
    }

    @Override
    public List<AirConditionDto> getAllByRoomId(Long roomId) {
        if (roomId == null) {
            throw new BadRequestException("Room ID is required");
        }

        String langCode = LocalContextUtil.getCurrentLangCode();
        return airConditionDao.findAllByRoomId(roomId, langCode);
    }

    @Override
    public AirConditionDto getById(Long id) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return airConditionDao.findById(id, langCode)
                .orElseThrow(() -> new NotFoundException("Air Condition not found with ID: " + id));
    }

    @Override
    @Transactional
    public AirConditionDto create(CreateAirConditionDto dto) {
        if (dto == null) {
            throw new BadRequestException("Air Condition data is required");
        }
        if (dto.roomId() == null) {
            throw new BadRequestException("Room ID is required");
        }

        String naturalId = dto.naturalId().trim();
        if (airConditionDao.existsByNaturalId(naturalId)) {
            throw new BadRequestException("Natural ID already exists: " + naturalId);
        }

        Room room = roomDao.findById(dto.roomId())
                .orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));

        DeviceControl deviceControl = null;
        if (dto.deviceControlId() != null) {
            deviceControl = deviceControlDao.findById(dto.deviceControlId())
                    .orElseThrow(() -> new NotFoundException("Device Control not found with ID: " + dto.deviceControlId()));
        }

        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) {
            throw new NotFoundException("Language not found: " + langCode);
        }

        validateControlValues(dto.temperature(), dto.fanSpeed());

        AirCondition ac = new AirCondition();
        ac.setNaturalId(naturalId);
        ac.setIsActive(dto.isActive() != null ? dto.isActive() : false);
        ac.setRoom(room);
        ac.setDeviceControl(deviceControl);

        ac.setPower(dto.power() != null ? dto.power() : ActuatorPower.OFF);
        ac.setTemperature(dto.temperature() != null ? dto.temperature() : 25);
        ac.setMode(dto.mode() != null ? dto.mode() : ActuatorMode.COOL);
        ac.setFanSpeed(dto.fanSpeed() != null ? dto.fanSpeed() : 3);
        ac.setSwing(dto.swing() != null ? dto.swing() : ActuatorSwing.OFF);

        AirConditionLan lan = new AirConditionLan();
        lan.setLangCode(langCode);
        lan.setName(dto.name() != null ? dto.name().trim() : "");
        lan.setDescription(dto.description());
        lan.setOwner(ac);

        ac.getTranslations().add(lan);
        airConditionDao.save(ac);

        return airConditionDao.findById(ac.getId(), langCode)
                .orElseThrow(() -> new InternalServerErrorException("Failed to retrieve created Air Condition"));
    }

    @Override
    @Transactional
    public AirConditionDto update(Long id, UpdateAirConditionDto dto) {
        AirCondition ac = airConditionDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Air Condition not found with ID: " + id));

        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) {
            throw new NotFoundException("Language not found: " + langCode);
        }

        if (StringUtils.hasText(dto.naturalId()) && !dto.naturalId().trim().equals(ac.getNaturalId())) {
            if (airConditionDao.existsByNaturalId(dto.naturalId().trim())) {
                throw new BadRequestException("Natural ID already exists: " + dto.naturalId());
            }
            ac.setNaturalId(dto.naturalId().trim());
        }

        if (dto.roomId() != null && !dto.roomId().equals(ac.getRoom().getId())) {
            Room room = roomDao.findById(dto.roomId())
                    .orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));
            ac.setRoom(room);
        }

        if (dto.deviceControlId() != null) {
            DeviceControl dc = deviceControlDao.findById(dto.deviceControlId())
                    .orElseThrow(() -> new NotFoundException("Device Control not found with ID: " + dto.deviceControlId()));
            ac.setDeviceControl(dc);
        }

        validateControlValues(dto.temperature(), dto.fanSpeed());

        if (dto.isActive() != null) ac.setIsActive(dto.isActive());
        if (dto.power() != null) ac.setPower(dto.power());
        if (dto.temperature() != null) ac.setTemperature(dto.temperature());
        if (dto.mode() != null) ac.setMode(dto.mode());
        if (dto.fanSpeed() != null) ac.setFanSpeed(dto.fanSpeed());
        if (dto.swing() != null) ac.setSwing(dto.swing());

        AirConditionLan lan = ac.getTranslations().stream()
                .filter(l -> langCode.equals(l.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    AirConditionLan newLan = new AirConditionLan();
                    newLan.setLangCode(langCode);
                    newLan.setOwner(ac);
                    ac.getTranslations().add(newLan);
                    return newLan;
                });

        if (StringUtils.hasText(dto.name())) lan.setName(dto.name().trim());
        if (dto.description() != null) lan.setDescription(dto.description());

        airConditionDao.save(ac);

        return airConditionDao.findById(id, langCode)
                .orElseThrow(() -> new InternalServerErrorException("Failed to retrieve updated Air Condition"));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AirCondition ac = airConditionDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Air Condition not found with ID: " + id));

        airConditionDao.delete(ac);
    }

    @Override
    @Transactional
    @Deprecated
    public void controlPower(Long id, ActuatorPower state) {
        AirCondition ac = getAirConditionEntity(id);
        
        ac.setPower(state);
        airConditionDao.save(ac);
        
        String gatewayIp = getGatewayIp(ac);
        String url = UrlConstant.getAcPowerUrlV1(gatewayIp, ac.getNaturalId());
        Map<String, Object> payload = new HashMap<>();
        payload.put("power", state);
        
        sendControlCommand(url, payload);
    }

    @Override
    @Transactional
    public void _v2api_handlePowerControl(Long id, ActuatorPower power) {
        AirCondition ac = getAirConditionEntity(id);
        
        ac.setPower(power);
        airConditionDao.save(ac);
        
        String gatewayIp = getGatewayIp(ac);
        String url = UrlConstant.getAcPowerUrlV2(gatewayIp, ac.getNaturalId());
        Map<String, Object> payload = Map.of("data", power);
        
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    @Deprecated
    public void controlTemperature(Long id, int temperature) {
        if (temperature < AirCondition.MIN_TEMP || temperature > AirCondition.MAX_TEMP) {
            throw new BadRequestException("Temperature must be between " + AirCondition.MIN_TEMP + " and " + AirCondition.MAX_TEMP);
        }

        AirCondition ac = getAirConditionEntity(id);
        int currentTemp = ac.getTemperature() != null ? ac.getTemperature() : 25;
        
        if (temperature == currentTemp) {
            return;
        }

        ac.setTemperature(temperature);
        airConditionDao.save(ac);
        
        String gatewayIp = getGatewayIp(ac);
        String url = temperature > currentTemp 
            ? UrlConstant.getAcTempUpUrlV1(gatewayIp, ac.getNaturalId())
            : UrlConstant.getAcTempDownUrlV1(gatewayIp, ac.getNaturalId());
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("temp", temperature);
        
        sendControlCommand(url, payload);
    }

    @Override
    @Transactional
    public void _v2api_handleTemperatureControl(Long id, int temperature) {
        if (temperature < AirCondition.MIN_TEMP || temperature > AirCondition.MAX_TEMP) {
            throw new BadRequestException("Temperature must be between " + AirCondition.MIN_TEMP + " and " + AirCondition.MAX_TEMP);
        }

        AirCondition ac = getAirConditionEntity(id);
        int currentTemp = ac.getTemperature() != null ? ac.getTemperature() : 25;
        
        if (temperature == currentTemp) {
            return;
        }

        ac.setTemperature(temperature);
        airConditionDao.save(ac);
        
        String gatewayIp = getGatewayIp(ac);
        // Determine Up or Down based on diff
        String url = temperature > currentTemp 
            ? UrlConstant.getAcTempUpUrlV2(gatewayIp, ac.getNaturalId())
            : UrlConstant.getAcTempDownUrlV2(gatewayIp, ac.getNaturalId());
        
        Map<String, Object> payload = Map.of("data", temperature);
        
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    @Deprecated
    public void controlMode(Long id, ActuatorMode mode) {
        AirCondition ac = getAirConditionEntity(id);
        
        ac.setMode(mode);
        airConditionDao.save(ac);
        
        String gatewayIp = getGatewayIp(ac);
        String url = UrlConstant.getAcModeUrlV1(gatewayIp, ac.getNaturalId());
        Map<String, Object> payload = new HashMap<>();
        payload.put("mode", mode);
        
        sendControlCommand(url, payload);
    }

    @Override
    @Transactional
    public void _v2api_handleModeControl(Long id, ActuatorMode mode) {
        AirCondition ac = getAirConditionEntity(id);
        
        ac.setMode(mode);
        airConditionDao.save(ac);
        
        String gatewayIp = getGatewayIp(ac);
        String url = UrlConstant.getAcModeUrlV2(gatewayIp, ac.getNaturalId());
        Map<String, Object> payload = Map.of("data", mode);
        
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    @Deprecated
    public void controlFanSpeed(Long id, int speed) {
        if (speed < AirCondition.MIN_FAN_SPEED || speed > AirCondition.MAX_FAN_SPEED) {
            throw new BadRequestException("Fan speed must be between " + AirCondition.MIN_FAN_SPEED + " and " + AirCondition.MAX_FAN_SPEED);
        }

        AirCondition ac = getAirConditionEntity(id);
        
        ac.setFanSpeed(speed);
        airConditionDao.save(ac);
        
        String gatewayIp = getGatewayIp(ac);
        String url = UrlConstant.getAcFanUrlV1(gatewayIp, ac.getNaturalId());
        Map<String, Object> payload = new HashMap<>();
        payload.put("fan", speed);
        
        sendControlCommand(url, payload);
    }

    @Override
    @Transactional
    public void _v2api_handleFanSpeedControl(Long id, int speed) {
        if (speed < AirCondition.MIN_FAN_SPEED || speed > AirCondition.MAX_FAN_SPEED) {
            throw new BadRequestException("Fan speed must be between " + AirCondition.MIN_FAN_SPEED + " and " + AirCondition.MAX_FAN_SPEED);
        }

        AirCondition ac = getAirConditionEntity(id);
        
        ac.setFanSpeed(speed);
        airConditionDao.save(ac);
        
        String gatewayIp = getGatewayIp(ac);
        String url = UrlConstant.getAcFanUrlV2(gatewayIp, ac.getNaturalId());
        Map<String, Object> payload = Map.of("data", speed);
        
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    @Deprecated
    public void controlSwing(Long id, ActuatorSwing swing) {
        AirCondition ac = getAirConditionEntity(id);
        
        ac.setSwing(swing);
        airConditionDao.save(ac);
        
        String gatewayIp = getGatewayIp(ac);
        String url = UrlConstant.getAcSwingUrlV1(gatewayIp, ac.getNaturalId());
        Map<String, Object> payload = new HashMap<>();
        payload.put("swing", swing);
        
        sendControlCommand(url, payload);
    }

    @Override
    @Transactional
    public void _v2api_handleSwingControl(Long id, ActuatorSwing swing) {
        AirCondition ac = getAirConditionEntity(id);
        
        ac.setSwing(swing);
        airConditionDao.save(ac);
        
        String gatewayIp = getGatewayIp(ac);
        String url = UrlConstant.getAcSwingUrlV2(gatewayIp, ac.getNaturalId());
        Map<String, Object> payload = Map.of("data", swing);
        
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    private AirCondition getAirConditionEntity(Long id) {
        return airConditionDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Air Condition not found with ID: " + id));
    }

    private String getGatewayIp(AirCondition ac) {
        DeviceControl dc = ac.getDeviceControl();
        if (dc == null) {
            throw new BadRequestException("No Device Control associated with Air Condition: " + ac.getNaturalId());
        }

        Client gateway = dc.getClient();
        if (gateway == null || gateway.getIpAddress() == null || gateway.getIpAddress().isEmpty()) {
            throw new BadRequestException("Gateway IP not configured for Air Condition: " + ac.getNaturalId());
        }

        return gateway.getIpAddress();
    }

    private void sendControlCommand(String url, Map<String, Object> payload) {
        HttpClientUtil.postAsync(url, payload)
            .exceptionally(ex -> null);
    }

    private void validateControlValues(Integer temperature, Integer fanSpeed) {
        if (temperature != null && (temperature < AirCondition.MIN_TEMP || temperature > AirCondition.MAX_TEMP)) {
            throw new BadRequestException("Temperature must be between " + AirCondition.MIN_TEMP + " and " + AirCondition.MAX_TEMP);
        }

        if (fanSpeed != null && (fanSpeed < AirCondition.MIN_FAN_SPEED || fanSpeed > AirCondition.MAX_FAN_SPEED)) {
            throw new BadRequestException("Fan speed must be between " + AirCondition.MIN_FAN_SPEED + " and " + AirCondition.MAX_FAN_SPEED);
        }
    }
}
