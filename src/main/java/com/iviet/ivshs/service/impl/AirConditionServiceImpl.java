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
import com.iviet.ivshs.enumeration.AcMode;
import com.iviet.ivshs.enumeration.AcPower;
import com.iviet.ivshs.enumeration.AcSwing;
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

    private static final int MIN_TEMP = 16;
    private static final int MAX_TEMP = 32;
    private static final int MIN_FAN_SPEED = 0;
    private static final int MAX_FAN_SPEED = 5;

    @Override
    public PaginatedResponse<AirConditionDto> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<AirConditionDto> data = airConditionDao.findAll(page, size, langCode);
        Long totalElements = airConditionDao.count();
        return new PaginatedResponse<>(data, page, size, totalElements);
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

        ac.setPower(dto.power() != null ? dto.power() : AcPower.OFF);
        ac.setTemperature(dto.temperature() != null ? dto.temperature() : 25);
        ac.setMode(dto.mode() != null ? dto.mode() : AcMode.COOL);
        ac.setFanSpeed(dto.fanSpeed() != null ? dto.fanSpeed() : 3);
        ac.setSwing(dto.swing() != null ? dto.swing() : AcSwing.OFF);

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
    public void controlPower(Long id, AcPower state) {
        AirCondition ac = getAirConditionEntity(id);
        String gatewayIp = getGatewayIp(ac);

        Map<String, Object> payload = new HashMap<>();
        payload.put("power", state.getValue());

        String url = UrlConstant.getAcPowerUrlV1(gatewayIp, ac.getNaturalId());
        executeControlCommand(url, payload);

        ac.setPower(state);
        airConditionDao.save(ac);
    }

    @Override
    @Transactional
    public void controlTemperature(Long id, int temperature) {
        if (temperature < MIN_TEMP || temperature > MAX_TEMP) {
            throw new BadRequestException("Temperature must be between " + MIN_TEMP + " and " + MAX_TEMP);
        }

        AirCondition ac = getAirConditionEntity(id);
        String gatewayIp = getGatewayIp(ac);

        int currentTemp = ac.getTemperature();
        String url;

        if (temperature > currentTemp) {
            url = UrlConstant.getAcTempUpUrlV1(gatewayIp, ac.getNaturalId());
        } else if (temperature < currentTemp) {
            url = UrlConstant.getAcTempDownUrlV1(gatewayIp, ac.getNaturalId());
        } else {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("temp", temperature);

        executeControlCommand(url, payload);

        ac.setTemperature(temperature);
        airConditionDao.save(ac);
    }

    @Override
    @Transactional
    public void controlMode(Long id, AcMode mode) {
        AirCondition ac = getAirConditionEntity(id);
        String gatewayIp = getGatewayIp(ac);

        Map<String, Object> payload = new HashMap<>();
        payload.put("mode", mode.getValue());

        String url = UrlConstant.getAcModeUrlV1(gatewayIp, ac.getNaturalId());
        executeControlCommand(url, payload);

        ac.setMode(mode);
        airConditionDao.save(ac);
    }

    @Override
    @Transactional
    public void controlFanSpeed(Long id, int speed) {
        if (speed < MIN_FAN_SPEED || speed > MAX_FAN_SPEED) {
            throw new BadRequestException("Fan speed must be between " + MIN_FAN_SPEED + " and " + MAX_FAN_SPEED);
        }

        AirCondition ac = getAirConditionEntity(id);
        String gatewayIp = getGatewayIp(ac);

        Map<String, Object> payload = new HashMap<>();
        payload.put("fan", speed);

        String url = UrlConstant.getAcFanUrlV1(gatewayIp, ac.getNaturalId());
        executeControlCommand(url, payload);

        ac.setFanSpeed(speed);
        airConditionDao.save(ac);
    }

    @Override
    @Transactional
    public void controlSwing(Long id, AcSwing swing) {
        AirCondition ac = getAirConditionEntity(id);
        String gatewayIp = getGatewayIp(ac);

        Map<String, Object> payload = new HashMap<>();
        payload.put("swing", swing.getValue());

        String url = UrlConstant.getAcSwingUrlV1(gatewayIp, ac.getNaturalId());
        executeControlCommand(url, payload);

        ac.setSwing(swing);
        airConditionDao.save(ac);
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

    private void executeControlCommand(String url, Map<String, Object> payload) {
        HttpClientUtil.Response response = HttpClientUtil.post(url, payload);
        HttpClientUtil.handleThrowException(response);
    }

    private void validateControlValues(Integer temperature, Integer fanSpeed) {
        if (temperature != null && (temperature < MIN_TEMP || temperature > MAX_TEMP)) {
            throw new BadRequestException("Temperature must be between " + MIN_TEMP + " and " + MAX_TEMP);
        }

        if (fanSpeed != null && (fanSpeed < MIN_FAN_SPEED || fanSpeed > MAX_FAN_SPEED)) {
            throw new BadRequestException("Fan speed must be between " + MIN_FAN_SPEED + " and " + MAX_FAN_SPEED);
        }
    }
}
