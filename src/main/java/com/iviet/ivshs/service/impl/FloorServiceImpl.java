package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.FloorDao;
import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.FloorLan;
import com.iviet.ivshs.enumeration.SysFunctionEnum;
import com.iviet.ivshs.entities.Floor;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.FloorMapperV1;
import com.iviet.ivshs.service.FloorService;
import com.iviet.ivshs.util.LocalContextUtil;
import com.iviet.ivshs.util.RequestContextUtil;
import com.iviet.ivshs.util.SecurityContextUtil;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FloorServiceImpl implements FloorService {

    private final FloorDao floorDao;
    private final LanguageDao languageDao;
    private final FloorMapperV1 floorMapper;

    @Override
    public PaginatedResponse<FloorDto> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<FloorDto> floors = floorDao.findAll(page, size, langCode);

        removeIfNoAccess(floors);

        return new PaginatedResponse<>(
                floors,
                page, size, floorDao.count()
        );
    }

    @Override
    public FloorDto getById(Long id) {
        FloorDto floorDto = floorDao.findById(id, LocalContextUtil.getCurrentLangCode()).orElseThrow(() -> new NotFoundException("Floor not found with ID: " + id));
        requireAccessToFloor(floorDto.code());
        return floorDto;
    }

    @Override
    public Floor getEntityById(Long id) {
        Floor floor = floorDao.findById(id).orElseThrow(() -> new NotFoundException("Floor not found with ID: " + id));
        requireAccessToFloor(floor.getCode());
        return floor;
    }

    @Override
    @Transactional
    public FloorDto create(CreateFloorDto dto) {
        requireManageFloorPermission();

        if (dto == null || !StringUtils.hasText(dto.code())) throw new BadRequestException("Data and Floor code are required");
        
        String code = dto.code().trim();
        _checkDuplicate(code, null);
        
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) throw new NotFoundException("Language not found: " + langCode);

        Floor floor = floorMapper.fromCreateDto(dto);
        floor.setCode(code);

        FloorLan floorLan = new FloorLan();
        floorLan.setLangCode(langCode);
        floorLan.setName(dto.name() != null ? dto.name().trim() : "");
        floorLan.setDescription(dto.description());
        floorLan.setOwner(floor);
        
        floor.getTranslations().add(floorLan);
        floorDao.save(floor);

        return floorMapper.toDto(floor, floorLan); 
    }

    @Override
    @Transactional
    public FloorDto update(Long id, UpdateFloorDto dto) {
        requireManageFloorPermission();
        Floor floor = floorDao.findById(id).orElseThrow(() -> new NotFoundException("Floor not found"));
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) throw new NotFoundException("Language not found: " + langCode);

        if (StringUtils.hasText(dto.code()) && !dto.code().trim().equals(floor.getCode())) {
            String newCode = dto.code().trim();
            _checkDuplicate(newCode, id);
            floor.setCode(newCode);
        }

        if (dto.level() != null) floor.setLevel(dto.level());

        FloorLan floorLan = floor.getTranslations().stream()
                .filter(lan -> langCode.equals(lan.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    FloorLan newLan = new FloorLan();
                    newLan.setLangCode(langCode);
                    newLan.setOwner(floor);
                    floor.getTranslations().add(newLan);
                    return newLan;
                });

        if (dto.name() != null) floorLan.setName(dto.name().trim());
        if (dto.description() != null) floorLan.setDescription(dto.description());

        floorDao.save(floor);
        return floorMapper.toDto(floor, floorLan);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireManageFloorPermission();
        if (!floorDao.existsById(id)) throw new NotFoundException("Floor not found");
        floorDao.deleteById(id);
    }

    private void _checkDuplicate(String code, Long currentId) {
        floorDao.findByCode(code).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BadRequestException("Floor code already exists: " + code);
            }
        });
    }

    // Các method author, sau này tách ra thành Class riêng để clean hơn
    public void requireManageFloorPermission() {
        if (RequestContextUtil.isHttpRequest() && !SecurityContextUtil.hasPermission(SysFunctionEnum.F_MANAGE_ALL.getCode())) {
            SecurityContextUtil.requireAllPermissions(
                List.of(SysFunctionEnum.F_MANAGE_FLOOR.getCode()),
                "Insufficient permissions to manage floor"
            );
        }
    }

    private void requireAccessToFloor(String floorCode) {
        if (RequestContextUtil.isHttpRequest()) {
            SecurityContextUtil.requireFloorAccess(floorCode);
        }
    }

    private void removeIfNoAccess(List<FloorDto> floors) {
        if (RequestContextUtil.isHttpRequest()) {
            Set<String> accessibleFloorCodes = SecurityContextUtil.getAccessibleFloorCodes();
            if (!accessibleFloorCodes.contains("ALL")) {
                floors.removeIf(floor -> !accessibleFloorCodes.contains(floor.code()));
            }
        }
    }
}