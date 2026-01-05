package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.LanguageDaoV1;
import com.iviet.ivshs.dao.SysFunctionDaoV1;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.SysFunctionLanV1;
import com.iviet.ivshs.entities.SysFunctionV1;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.SysFunctionMapperV1;
import com.iviet.ivshs.service.ClientFunctionCacheServiceV1;
import com.iviet.ivshs.service.SysFunctionServiceV1;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SysFunctionServiceImplV1 implements SysFunctionServiceV1 {

    private final SysFunctionDaoV1 functionDao;
    private final LanguageDaoV1 languageDao;
    private final SysFunctionMapperV1 functionMapper;
    private final ClientFunctionCacheServiceV1 cacheService;

    @Override
    public PaginatedResponseV1<SysFunctionDtoV1> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return new PaginatedResponseV1<>(
                functionDao.findAll(page, size, langCode),
                page, size, functionDao.countAll()
        );
    }

    @Override
    public List<SysFunctionDtoV1> getAll() {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return functionDao.findAll(langCode);
    }

    @Override
    public SysFunctionDtoV1 getById(Long id) {
        return functionDao.findById(id, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Function not found with ID: " + id));
    }

    @Override
    public SysFunctionDtoV1 getByCode(String functionCode) {
        if (!StringUtils.hasText(functionCode)) {
            throw new BadRequestException("Function code is required");
        }
        return functionDao.findByCode(functionCode, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Function not found with code: " + functionCode));
    }

    @Override
    public List<SysFunctionWithGroupStatusDtoV1> getAllWithGroupStatus(Long groupId) {
        if (groupId == null) {
            throw new BadRequestException("Group ID is required");
        }
        String langCode = LocalContextUtil.getCurrentLangCode();
        return functionDao.findAllWithGroupStatus(groupId, langCode);
    }

    @Override
    @Transactional
    public SysFunctionDtoV1 create(CreateSysFunctionDtoV1 dto) {
        if (dto == null || !StringUtils.hasText(dto.getFunctionCode())) {
            throw new BadRequestException("Data and Function code are required");
        }

        String code = dto.getFunctionCode().trim();
        _checkDuplicate(code, null);
        
        String langCode = LocalContextUtil.resolveLangCode(dto.getLangCode());
        if (!languageDao.existsByCode(langCode)) {
            throw new NotFoundException("Language not found: " + langCode);
        }

        SysFunctionV1 function = functionMapper.fromCreateDto(dto);
        function.setFunctionCode(code);

        SysFunctionLanV1 functionLan = new SysFunctionLanV1();
        functionLan.setLangCode(langCode);
        functionLan.setName(dto.getName() != null ? dto.getName().trim() : "");
        functionLan.setDescription(dto.getDescription());
        functionLan.setOwner(function);
        
        function.getTranslations().add(functionLan);
        functionDao.save(function);

        return functionMapper.toDto(function, functionLan);
    }

    @Override
    @Transactional
    public SysFunctionDtoV1 update(Long id, UpdateSysFunctionDtoV1 dto) {
        SysFunctionV1 function = functionDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Function not found with ID: " + id));
        
        String langCode = LocalContextUtil.resolveLangCode(dto.getLangCode());
        if (!languageDao.existsByCode(langCode)) {
            throw new NotFoundException("Language not found: " + langCode);
        }

        // Tìm hoặc tạo translation cho langCode
        SysFunctionLanV1 functionLan = function.getTranslations().stream()
                .filter(lan -> langCode.equals(lan.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    SysFunctionLanV1 newLan = new SysFunctionLanV1();
                    newLan.setLangCode(langCode);
                    newLan.setOwner(function);
                    function.getTranslations().add(newLan);
                    return newLan;
                });

        // Update translation fields
        if (dto.getName() != null) {
            functionLan.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            functionLan.setDescription(dto.getDescription());
        }

        functionDao.save(function);
        return functionMapper.toDto(function, functionLan);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!functionDao.existsById(id)) {
            throw new NotFoundException("Function not found with ID: " + id);
        }
        
        // Lấy functionCode trước khi xóa để clear cache
        SysFunctionV1 function = functionDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Function not found"));
        String functionCode = function.getFunctionCode();
        
        // Clear cache trước khi xóa (cascade sẽ xóa roles)
        cacheService.clearCacheForFunction(functionCode);
        
        // Xóa function (cascade sẽ xóa roles và translations)
        functionDao.deleteById(id);
    }

    @Override
    public long count() {
        return functionDao.countAll();
    }

    private void _checkDuplicate(String code, Long currentId) {
        functionDao.findByCode(code).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BadRequestException("Function code already exists: " + code);
            }
        });
    }
}
