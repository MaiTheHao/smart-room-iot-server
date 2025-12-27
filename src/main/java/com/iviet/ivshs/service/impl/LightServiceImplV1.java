package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.*;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.*;
import com.iviet.ivshs.enumeration.GatewayCommandV1;
import com.iviet.ivshs.exception.BadRequestException;
import com.iviet.ivshs.exception.NotFoundException;
import com.iviet.ivshs.mapper.LightMapperV1;
import com.iviet.ivshs.service.ControlServiceV1;
import com.iviet.ivshs.service.HealthCheckServiceV1;
import com.iviet.ivshs.service.LightServiceV1;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LightServiceImplV1 implements LightServiceV1 {

    private final LightDaoV1 lightDao;
    private final RoomDaoV1 roomDao;
    private final LanguageDaoV1 languageDao;
    private final DeviceControlDaoV1 deviceControlDao;
    private final LightMapperV1 lightMapper;
    private final ControlServiceV1 controlService;
    private final HealthCheckServiceV1 healthCheckService;
    private final PlatformTransactionManager transactionManager;

    @Override
    public PaginatedResponseV1<LightDtoV1> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return new PaginatedResponseV1<>(
                lightDao.findAll(page, size, langCode),
                page, size, lightDao.count()
        );
    }

    @Override
    public PaginatedResponseV1<LightDtoV1> getListByRoomId(Long roomId, int page, int size) {
        if (roomId == null) throw new BadRequestException("Room ID is required");
        String langCode = LocalContextUtil.getCurrentLangCode();
        return new PaginatedResponseV1<>(
                lightDao.findAllByRoomId(roomId, page, size, langCode),
                page, size, lightDao.countByRoomId(roomId)
        );
    }

    @Override
    public LightDtoV1 getById(Long lightId) {
        return lightDao.findById(lightId, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Light not found with ID: " + lightId));
    }

    @Override
    @Transactional
    public LightDtoV1 create(CreateLightDtoV1 dto) {
        if (dto == null || !StringUtils.hasText(dto.naturalId())) 
            throw new BadRequestException("Light data and naturalId are required");

        _checkDuplicate(dto.naturalId().trim(), null);

        RoomV1 room = roomDao.findById(dto.roomId())
                .orElseThrow(() -> new NotFoundException("Room not found"));
        DeviceControlV1 deviceControl = deviceControlDao.findById(dto.deviceControlId())
                .orElseThrow(() -> new NotFoundException("Device Control not found"));
        
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) throw new NotFoundException("Language not found");

        LightV1 light = lightMapper.fromCreateDto(dto);
        light.setRoom(room);
        light.setDeviceControl(deviceControl);
        light.setIsActive(dto.isActive() != null ? dto.isActive() : false);
        light.setLevel(dto.level() != null ? dto.level() : 0);

        LightLanV1 lightLan = new LightLanV1();
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
    public LightDtoV1 update(Long lightId, UpdateLightDtoV1 dto) {
        LightV1 light = lightDao.findById(lightId)
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

        LightLanV1 lan = light.getTranslations().stream()
                .filter(ll -> langCode.equals(ll.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    LightLanV1 newLan = new LightLanV1();
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
    public ControlDeviceResponseV1 toggleState(Long lightId) {
        LightV1 light = lightDao.findById(lightId).orElseThrow(() -> new NotFoundException("Light not found"));
        DeviceControlV1 dc = light.getDeviceControl();
        if (dc == null) throw new BadRequestException("No control associated");

        ControlDeviceResponseV1 resp = light.getIsActive() ? controlService.turnOff(dc) : controlService.turnOn(dc);

        if ("200".equals(resp.getStatus())) {
            light.setIsActive(!light.getIsActive());
            lightDao.save(light);
        }
        return resp;
    }

    @Override
    @Transactional
    public ControlDeviceResponseV1 setLevel(Long lightId, int newLevel) {
        LightV1 light = lightDao.findById(lightId).orElseThrow(() -> new NotFoundException("Light not found"));
        DeviceControlV1 dc = light.getDeviceControl();
        if (dc == null) throw new BadRequestException("No control associated");

        return controlService.setLevel(dc, newLevel);
    }

    @Override
    public HealthCheckResponseDtoV1 healthCheck(Long lightId) {
        TransactionTemplate tm = new TransactionTemplate(transactionManager);
        HealthCheckRequestDtoV1 reqDto = tm.execute(status -> {
            LightV1 light = lightDao.findById(lightId).orElseThrow(() -> new NotFoundException("Light not found"));
            DeviceControlV1 dc = light.getDeviceControl();
            if (dc == null) throw new BadRequestException("No control associated");

            return HealthCheckRequestDtoV1.builder()
                .deviceControlType(dc.getDeviceControlType())
                .clientId(dc.getClient().getId())
                .clientIpAddress(dc.getClient().getIpAddress())
                .gpioPin(dc.getGpioPin())
                .command(GatewayCommandV1.HEALTH_CHECK.toString())
                .build();
        });
        return healthCheckService.check(reqDto);
    }

    private void _checkDuplicate(String nid, Long cid) {
        lightDao.findByNaturalId(nid).ifPresent(e -> {
            if (cid == null || !e.getId().equals(cid)) throw new BadRequestException("Natural ID already exists: " + nid);
        });
    }
}