package com.iviet.ivshs.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.iviet.ivshs.constant.UrlConstant;
import com.iviet.ivshs.dao.DeviceControlDao;
import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dao.RoomDao;
import com.iviet.ivshs.dto.CreateFanDto;
import com.iviet.ivshs.dto.FanDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateFanDto;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.entities.Fan;
import com.iviet.ivshs.entities.FanIr;
import com.iviet.ivshs.entities.FanLan;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.enumeration.ActuatorMode;
import com.iviet.ivshs.enumeration.ActuatorPower;
import com.iviet.ivshs.enumeration.ActuatorState;
import com.iviet.ivshs.enumeration.ActuatorSwing;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.FanService;
import com.iviet.ivshs.util.HttpClientUtil;
import com.iviet.ivshs.util.LocalContextUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FanServiceImpl implements FanService {

    private final FanDao fanDao;
    private final RoomDao roomDao;
    private final DeviceControlDao deviceControlDao;
    private final LanguageDao languageDao;

    @Override
    public PaginatedResponse<FanDto> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<FanDto> data = fanDao.findAll(page, size, langCode);
        Long totalElements = fanDao.count();
        return new PaginatedResponse<>(data, page, size, totalElements);
    }

    @Override
    public List<FanDto> getAll() {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return fanDao.findAll(langCode);
    }

    @Override
    public PaginatedResponse<FanDto> getListByRoomId(Long roomId, int page, int size) {
        if (roomId == null) {
            throw new BadRequestException("Room ID is required");
        }

        String langCode = LocalContextUtil.getCurrentLangCode();
        List<FanDto> data = fanDao.findAllByRoomId(roomId, page, size, langCode);
        Long totalElements = fanDao.countByRoomId(roomId);
        return new PaginatedResponse<>(data, page, size, totalElements);
    }

    @Override
    public List<FanDto> getAllByRoomId(Long roomId) {
        if (roomId == null) {
            throw new BadRequestException("Room ID is required");
        }

        String langCode = LocalContextUtil.getCurrentLangCode();
        return fanDao.findAllByRoomId(roomId, langCode);
    }

    @Override
    public FanDto getById(Long id) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return fanDao.findById(id, langCode)
                .orElseThrow(() -> new NotFoundException("Fan not found with ID: " + id));
    }

    @Override
    @Transactional
    public FanDto create(CreateFanDto dto) {
        if (dto == null) {
            throw new BadRequestException("Fan data is required");
        }
        if (dto.roomId() == null) {
            throw new BadRequestException("Room ID is required");
        }

        String naturalId = dto.naturalId().trim();
        if (fanDao.existsByNaturalId(naturalId)) {
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

        Fan fan = dto.toEntity();
        fan.setRoom(room);
        fan.setDeviceControl(deviceControl);

        fanDao.save(fan);

        return fanDao.findById(fan.getId(), langCode)
                .orElseThrow(() -> new InternalServerErrorException("Failed to retrieve created Fan"));
    }

    @Override
    @Transactional
    public FanDto update(Long id, UpdateFanDto dto) {
        Fan baseFan = fanDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Fan not found with ID: " + id));

        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) {
            throw new NotFoundException("Language not found: " + langCode);
        }

        if (StringUtils.hasText(dto.naturalId()) && !dto.naturalId().trim().equals(baseFan.getNaturalId())) {
            if (fanDao.existsByNaturalId(dto.naturalId().trim())) {
                throw new BadRequestException("Natural ID already exists: " + dto.naturalId());
            }
            baseFan.setNaturalId(dto.naturalId().trim());
        }

        if (dto.roomId() != null && !dto.roomId().equals(baseFan.getRoom().getId())) {
            Room room = roomDao.findById(dto.roomId())
                    .orElseThrow(() -> new NotFoundException("Room not found with ID: " + dto.roomId()));
            baseFan.setRoom(room);
        }

        if (dto.deviceControlId() != null) {
            DeviceControl dc = deviceControlDao.findById(dto.deviceControlId())
                    .orElseThrow(() -> new NotFoundException("Device Control not found with ID: " + dto.deviceControlId()));
            baseFan.setDeviceControl(dc);
        }

        if (dto.isActive() != null) baseFan.setIsActive(dto.isActive());
        if (dto.power() != null) baseFan.setPower(dto.power());

        if (baseFan instanceof FanIr fanIr) {
            if (dto.mode() != null) fanIr.setMode(dto.mode());
            if (dto.speed() != null) fanIr.setSpeed(dto.speed());
            if (dto.swing() != null) fanIr.setSwing(dto.swing());
            if (dto.light() != null) fanIr.setLight(dto.light());
        }

        FanLan lan = baseFan.getTranslations().stream()
                .filter(l -> langCode.equals(l.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    FanLan newLan = new FanLan();
                    newLan.setLangCode(langCode);
                    newLan.setOwner(baseFan);
                    baseFan.getTranslations().add(newLan);
                    return newLan;
                });

        if (StringUtils.hasText(dto.name())) lan.setName(dto.name().trim());
        if (dto.description() != null) lan.setDescription(dto.description());

        fanDao.save(baseFan);

        return fanDao.findById(id, langCode)
                .orElseThrow(() -> new InternalServerErrorException("Failed to retrieve updated Fan"));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Fan fan = fanDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Fan not found with ID: " + id));
        fanDao.delete(fan);
    }

    @Override
    @Transactional
    public void _v2api_handlePowerControl(Long id, ActuatorPower power) {
        Fan fan = getFanEntity(id);
        fan.setPower(power);
        fanDao.save(fan);
        
        String gatewayIp = getGatewayIp(fan);
        String url = UrlConstant.getControlFanPowerUrlV2(gatewayIp, fan.getNaturalId());
        Map<String, Object> payload = Map.of("data", power);
        
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    public void _v2api_handleTogglePowerControl(Long id) {
        Fan fan = getFanEntity(id);
        ActuatorPower currentPower = fan.getPower() != null ? fan.getPower() : ActuatorPower.OFF;
        ActuatorPower newPower = (currentPower == ActuatorPower.ON) ? ActuatorPower.OFF : ActuatorPower.ON;

        fan.setPower(newPower);
        fanDao.save(fan);

        String gatewayIp = getGatewayIp(fan);
        String url = UrlConstant.getControlFanPowerUrlV2(gatewayIp, fan.getNaturalId());
        Map<String, Object> payload = Map.of("data", newPower);

        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    public void _v2api_handleModeControl(Long id, ActuatorMode mode) {
        Fan fan = getFanEntity(id);
        if (fan instanceof FanIr fanIr) {
            fanIr.setMode(mode);
            fanDao.save(fanIr);
        }
        
        String gatewayIp = getGatewayIp(fan);
        String url = UrlConstant.getControlFanModeUrlV2(gatewayIp, fan.getNaturalId());
        Map<String, Object> payload = Map.of("data", mode);
        
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    public void _v2api_handleSpeedControl(Long id, int speed) {
        Fan fan = getFanEntity(id);
        if (fan instanceof FanIr fanIr) {
            fanIr.setSpeed(speed);
            fanDao.save(fanIr);
        }
        
        String gatewayIp = getGatewayIp(fan);
        String url = UrlConstant.getControlFanSpeedUrlV2(gatewayIp, fan.getNaturalId());
        Map<String, Object> payload = Map.of("data", speed);
        
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    public void _v2api_handleSwingControl(Long id, ActuatorSwing swing) {
        Fan fan = getFanEntity(id);
        if (fan instanceof FanIr fanIr) {
            fanIr.setSwing(swing);
            fanDao.save(fanIr);
        }
        
        String gatewayIp = getGatewayIp(fan);
        String url = UrlConstant.getControlFanSwingUrlV2(gatewayIp, fan.getNaturalId());
        Map<String, Object> payload = Map.of("data", swing);
        
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    @Override
    @Transactional
    public void _v2api_handleLightControl(Long id, ActuatorState light) {
        Fan fan = getFanEntity(id);
        if (fan instanceof FanIr fanIr) {
            fanIr.setLight(light);
            fanDao.save(fanIr);
        }
        
        String gatewayIp = getGatewayIp(fan);
        String url = UrlConstant.getControlFanLightUrlV2(gatewayIp, fan.getNaturalId());
        Map<String, Object> payload = Map.of("data", light);
        
        HttpClientUtil.putAsync(url, payload).exceptionally(ex -> null);
    }

    private Fan getFanEntity(Long id) {
        return fanDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Fan not found with ID: " + id));
    }

    private String getGatewayIp(Fan fan) {
        DeviceControl dc = fan.getDeviceControl();
        if (dc == null) {
            throw new BadRequestException("No Device Control associated with Fan: " + fan.getNaturalId());
        }

        Client gateway = dc.getClient();
        if (gateway == null || gateway.getIpAddress() == null || gateway.getIpAddress().isEmpty()) {
            throw new BadRequestException("Gateway IP not configured for Fan: " + fan.getNaturalId());
        }

        return gateway.getIpAddress();
    }
}
