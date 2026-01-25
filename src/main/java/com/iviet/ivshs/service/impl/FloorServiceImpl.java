package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.FloorDao;
import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.FloorLan;
import com.iviet.ivshs.entities.Floor;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.FloorMapper;
import com.iviet.ivshs.service.FloorService;
import com.iviet.ivshs.service.PermissionService;
import com.iviet.ivshs.util.LocalContextUtil;

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
    private final FloorMapper floorMapper;
    private final PermissionService permissionService;

    @Override
    public PaginatedResponse<FloorDto> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<FloorDto> floors = floorDao.findAll(page, size, langCode);

        Set<String> accessibleFloorCodes = permissionService.getAccessibleFloorCodes();
        if (!accessibleFloorCodes.contains(PermissionService.ACCESS_ALL)) floors.removeIf(floor -> !accessibleFloorCodes.contains(floor.code()));

        return new PaginatedResponse<>(
                floors,
                page, size, floorDao.count()
        );
    }

    @Override
    public List<FloorDto> getAll() {
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<FloorDto> floors = floorDao.findAll(langCode);

        Set<String> accessibleFloorCodes = permissionService.getAccessibleFloorCodes();
        if (!accessibleFloorCodes.contains(PermissionService.ACCESS_ALL)) floors.removeIf(floor -> !accessibleFloorCodes.contains(floor.code()));

        return floors;
    }

    @Override
    public FloorDto getById(Long id) {
        FloorDto floorDto = floorDao.findById(id, LocalContextUtil.getCurrentLangCode()).orElseThrow(() -> new NotFoundException("Floor not found with ID: " + id));
        permissionService.requireAccessFloor(floorDto.code());
        return floorDto;
    }

    @Override
    public Floor getEntityById(Long id) {
        Floor floor = floorDao.findById(id).orElseThrow(() -> new NotFoundException("Floor not found with ID: " + id));
        permissionService.requireAccessFloor(floor.getCode());
        return floor;
    }

    @Override
    @Transactional
    public FloorDto create(CreateFloorDto dto) {
        permissionService.requireManageFloor();

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
        permissionService.requireManageFloor();

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
        permissionService.requireManageFloor();

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
}