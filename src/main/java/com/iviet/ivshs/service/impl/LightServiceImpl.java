package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.*;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.enumeration.GatewayCommand;
import com.iviet.ivshs.enumeration.LightPower;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.ExternalServiceException;
import com.iviet.ivshs.exception.domain.InternalServerErrorException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.LightMapper;
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
    private final LightMapper lightMapper;
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

        Room room = roomDao.findById(dto.roomId())
                .orElseThrow(() -> new NotFoundException("Room not found"));
        DeviceControl deviceControl = deviceControlDao.findById(dto.deviceControlId())
                .orElseThrow(() -> new NotFoundException("Device Control not found"));
        
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) throw new NotFoundException("Language not found");

        Light light = lightMapper.fromCreateDto(dto);
        light.setRoom(room);
        light.setDeviceControl(deviceControl);
        light.setIsActive(dto.isActive() != null ? dto.isActive() : false);
        light.setLevel(dto.level() != null ? dto.level() : 0);

        LightLan lightLan = new LightLan();
        lightLan.setLangCode(langCode);
        lightLan.setName(dto.name() != null ? dto.name().trim() : "");
        lightLan.setDescription(dto.description());
        lightLan.setOwner(light);

        light.getTranslations().add(lightLan);
        lightDao.save(light);

        return lightMapper.toDto(light, lightLan);
    }

    @Override
    @Transactional
    public LightDto update(Long lightId, UpdateLightDto dto) {
        Light light = lightDao.findById(lightId)
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

        LightLan lan = light.getTranslations().stream()
                .filter(ll -> langCode.equals(ll.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    LightLan newLan = new LightLan();
                    newLan.setLangCode(langCode);
                    newLan.setOwner(light);
                    light.getTranslations().add(newLan);
                    return newLan;
                });

        if (dto.name() != null) lan.setName(dto.name().trim());
        if (dto.description() != null) lan.setDescription(dto.description());

        lightDao.save(light);
        return lightMapper.toDto(light, lan);
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
        Light light = lightDao.findById(lightId).orElseThrow(() -> new NotFoundException("Light not found"));
        
        DeviceControl dc = light.getDeviceControl();
        if (dc == null) throw new BadRequestException("No control associated");
        Client gateway = dc.getClient();

        GatewayCommand command = state == LightPower.ON ? GatewayCommand.ON : GatewayCommand.OFF;
        light.setIsActive(state == LightPower.ON);
        lightDao.save(light);
        controlService.sendCommand(gateway.getIpAddress(), light.getNaturalId(), command);

        // if (200 == resp.status()) {
        //     light.setIsActive(state == LightPower.ON);
        //     lightDao.save(light);
        // } else {
        //     log.error("Failed to set light state. Response status: {}, message: {}", resp.status(), resp.message());
        //     switch (resp.status()) {
        //         case 400 -> throw new BadRequestException(resp.message());
        //         case 404 -> throw new NotFoundException(resp.message());
        //         case 502, 503 -> throw new ExternalServiceException(resp.message());
        //         case 500 -> throw new InternalServerErrorException(resp.message());
        //         default -> throw new InternalServerErrorException("Unexpected response from gateway: " + resp.status());
        //     }
        // }
    }

    @Override
    @Transactional
    public void handleToggleStateControl(Long lightId) {
        Light light = lightDao.findById(lightId).orElseThrow(() -> new NotFoundException("Light not found"));
        LightPower newState = light.getIsActive() ? LightPower.OFF : LightPower.ON;
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