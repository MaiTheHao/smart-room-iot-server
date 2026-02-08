package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.*;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.enumeration.GatewayCommand;
import com.iviet.ivshs.enumeration.LightPower;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.ControlService;
import com.iviet.ivshs.service.LightService;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LightServiceImpl implements LightService {

    private final LightDao lightDao;
    private final RoomDao roomDao;
    private final LanguageDao languageDao;
    private final DeviceControlDao deviceControlDao;
    private final ControlService controlService;

    @Override
    public PaginatedResponse<LightDto> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return new PaginatedResponse<>(
                lightDao.findAll(page, size, langCode),
                page, size, lightDao.count()
        );
    }

    @Override
    public List<LightDto> getAll() {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return lightDao.findAll(langCode);
    }

    @Override
    public PaginatedResponse<LightDto> getListByRoomId(Long roomId, int page, int size) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
        String langCode = LocalContextUtil.getCurrentLangCode();
        return new PaginatedResponse<>(
                lightDao.findAllByRoomId(roomId, page, size, langCode),
                page, size, lightDao.countByRoomId(roomId)
        );
    }

    @Override
    public List<LightDto> getAllByRoomId(Long roomId) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
        String langCode = LocalContextUtil.getCurrentLangCode();
        return lightDao.findAllByRoomId(roomId, langCode);
    }

    @Override
    public LightDto getById(Long lightId) {
        return lightDao.findById(lightId, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Light not found with ID: " + lightId));
    }

    @Override
    @Transactional
    public LightDto create(CreateLightDto dto) {
        if (dto == null || !StringUtils.hasText(dto.naturalId())) 
            throw new BadRequestException("Light data and naturalId are required");

        _checkDuplicate(dto.naturalId().trim(), null);

        var room = roomDao.findById(dto.roomId())
                .orElseThrow(() -> new NotFoundException("Room not found"));
        var deviceControl = deviceControlDao.findById(dto.deviceControlId())
                .orElseThrow(() -> new NotFoundException("Device Control not found"));
        
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) throw new NotFoundException("Language not found");

        var light = dto.toEntity();
        light.setRoom(room);
        light.setDeviceControl(deviceControl);

        var lightLan = new LightLan();
        lightLan.setLangCode(langCode);
        lightLan.setName(dto.name() != null ? dto.name().trim() : "");
        lightLan.setDescription(dto.description());
        lightLan.setOwner(light);

        light.getTranslations().add(lightLan);
        lightDao.save(light);

        return LightDto.from(light, lightLan);
    }

    @Override
    @Transactional
    public LightDto update(Long lightId, UpdateLightDto dto) {
        var light = lightDao.findById(lightId)
                .orElseThrow(() -> new NotFoundException("Light not found"));
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());

        if (StringUtils.hasText(dto.naturalId()) && !dto.naturalId().trim().equals(light.getNaturalId())) {
            _checkDuplicate(dto.naturalId().trim(), lightId);
            light.setNaturalId(dto.naturalId().trim());
        }

        if (dto.roomId() != null && !dto.roomId().equals(light.getRoom().getId())) {
            light.setRoom(roomDao.findById(dto.roomId()).orElseThrow(() -> new NotFoundException("Room not found")));
        }

        if (dto.deviceControlId() != null && !dto.deviceControlId().equals(light.getDeviceControl().getId())) {
            light.setDeviceControl(deviceControlDao.findById(dto.deviceControlId()).orElseThrow(() -> new NotFoundException("Device Control not found")));
        }

        if (dto.isActive() != null) light.setIsActive(dto.isActive());
        if (dto.level() != null) light.setLevel(dto.level());

        var lan = light.getTranslations().stream()
                .filter(ll -> langCode.equals(ll.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    var newLan = new LightLan();
                    newLan.setLangCode(langCode);
                    newLan.setOwner(light);
                    light.getTranslations().add(newLan);
                    return newLan;
                });

        if (dto.name() != null) lan.setName(dto.name().trim());
        if (dto.description() != null) lan.setDescription(dto.description());

        lightDao.save(light);
        return LightDto.from(light, lan);
    }

    @Override
    @Transactional
    public void delete(Long lightId) {
        if (!lightDao.existsById(lightId)) throw new NotFoundException("Light not found");
        lightDao.deleteById(lightId);
    }

    @Override
    @Transactional
    public void handleStateControl(Long lightId, LightPower state) {
        var light = lightDao.findById(lightId).orElseThrow(() -> new NotFoundException("Light not found"));
        var client = light.getDeviceControl().getClient();

        try {
            controlService.sendCommand(
                client.getIpAddress(),
                light.getNaturalId(),
                ((state == LightPower.ON) ? GatewayCommand.ON : GatewayCommand.OFF)
            );
            light.setIsActive(state == LightPower.ON);
            lightDao.save(light);
        } catch (Exception e) {
            log.error("Failed to control light state: lightId={}, state={}, error={}", lightId, state, e.getMessage());
            throw new BadRequestException("Failed to control light state: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleToggleStateControl(Long lightId) {
        var light = lightDao.findById(lightId).orElseThrow(() -> new NotFoundException("Light not found"));
        var newState = light.getIsActive() ? LightPower.OFF : LightPower.ON;
        handleStateControl(lightId, newState);
    }

    @Override
    @Transactional
    public void handleSetLevelControl(Long lightId, int newLevel) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private void _checkDuplicate(String nid, Long cid) {
        lightDao.findByNaturalId(nid).ifPresent(e -> {
            if (cid == null || !e.getId().equals(cid)) throw new BadRequestException("Natural ID already exists: " + nid);
        });
    }
}