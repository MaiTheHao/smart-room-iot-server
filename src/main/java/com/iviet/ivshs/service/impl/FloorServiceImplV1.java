package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.FloorDaoV1;
import com.iviet.ivshs.dao.LanguageDaoV1;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.FloorLanV1;
import com.iviet.ivshs.entities.FloorV1;
import com.iviet.ivshs.exception.BadRequestException;
import com.iviet.ivshs.exception.NotFoundException;
import com.iviet.ivshs.mapper.FloorMapperV1;
import com.iviet.ivshs.service.FloorServiceV1;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FloorServiceImplV1 implements FloorServiceV1 {

    private final FloorDaoV1 floorDao;
    private final LanguageDaoV1 languageDao;
    private final FloorMapperV1 floorMapper;

    @Override
    public PaginatedResponseV1<FloorDtoV1> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return new PaginatedResponseV1<>(
                floorDao.findAll(page, size, langCode),
                page, size, floorDao.count()
        );
    }

    @Override
    public FloorDtoV1 getById(Long id) {
        return floorDao.findById(id, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Floor not found with ID: " + id));
    }

    @Override
    @Transactional
    public FloorDtoV1 create(CreateFloorDtoV1 dto) {
        if (dto == null || !StringUtils.hasText(dto.code())) throw new BadRequestException("Data and Floor code are required");

        String code = dto.code().trim();
        _checkDuplicate(code, null);
        
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) throw new NotFoundException("Language not found: " + langCode);

        FloorV1 floor = floorMapper.fromCreateDto(dto);
        floor.setCode(code);

        FloorLanV1 floorLan = new FloorLanV1();
        floorLan.setLangCode(langCode);
        floorLan.setName(dto.name() != null ? dto.name().trim() : "");
        floorLan.setDescription(dto.description());
        floorLan.setFloor(floor);
        
        floor.getFloorLans().add(floorLan);
        floorDao.save(floor);

        return floorMapper.toDto(floor, floorLan); 
    }

    @Override
    @Transactional
    public FloorDtoV1 update(Long id, UpdateFloorDtoV1 dto) {
        FloorV1 floor = floorDao.findById(id).orElseThrow(() -> new NotFoundException("Floor not found"));
        String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
        if (!languageDao.existsByCode(langCode)) throw new NotFoundException("Language not found: " + langCode);

        if (StringUtils.hasText(dto.code()) && !dto.code().trim().equals(floor.getCode())) {
            String newCode = dto.code().trim();
            _checkDuplicate(newCode, id);
            floor.setCode(newCode);
        }

        if (dto.level() != null) floor.setLevel(dto.level());

        FloorLanV1 floorLan = floor.getFloorLans().stream()
                .filter(lan -> langCode.equals(lan.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    FloorLanV1 newLan = new FloorLanV1();
                    newLan.setLangCode(langCode);
                    newLan.setFloor(floor);
                    floor.getFloorLans().add(newLan);
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