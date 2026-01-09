package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dao.SysFunctionDao;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.SysFunctionLan;
import com.iviet.ivshs.entities.SysFunction;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.SysFunctionMapper;
import com.iviet.ivshs.service.ClientFunctionService;
import com.iviet.ivshs.service.SysFunctionService;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SysFunctionServiceImpl implements SysFunctionService {

    private final SysFunctionDao functionDao;
    private final LanguageDao languageDao;
    private final SysFunctionMapper functionMapper;
    private final ClientFunctionService cacheService;

    @Override
    public PaginatedResponse<SysFunctionDto> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return new PaginatedResponse<>(
                functionDao.findAll(page, size, langCode),
                page, size, functionDao.countAll()
        );
    }

    @Override
    public List<SysFunctionDto> getAll() {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return functionDao.findAll(langCode);
    }

    @Override
    public SysFunctionDto getById(Long id) {
        return functionDao.findById(id, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Function not found with ID: " + id));
    }

    @Override
    public SysFunctionDto getByCode(String functionCode) {
        if (!StringUtils.hasText(functionCode)) {
            throw new BadRequestException("Function code is required");
        }
        return functionDao.findByCode(functionCode, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Function not found with code: " + functionCode));
    }

    @Override
    public List<SysFunctionWithGroupStatusDto> getAllWithGroupStatus(Long groupId) {
        if (groupId == null) {
            throw new BadRequestException("Group ID is required");
        }
        String langCode = LocalContextUtil.getCurrentLangCode();
        return functionDao.findAllWithGroupStatus(groupId, langCode);
    }

    @Override
    @Transactional
    public SysFunctionDto create(CreateSysFunctionDto dto) {
        if (dto == null || !StringUtils.hasText(dto.getFunctionCode())) {
            throw new BadRequestException("Data and Function code are required");
        }

        String code = dto.getFunctionCode().trim();
        _checkDuplicate(code, null);
        
        String langCode = LocalContextUtil.resolveLangCode(dto.getLangCode());
        if (!languageDao.existsByCode(langCode)) {
            throw new NotFoundException("Language not found: " + langCode);
        }

        SysFunction function = functionMapper.fromCreateDto(dto);
        function.setFunctionCode(code);

        SysFunctionLan functionLan = new SysFunctionLan();
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
    public SysFunctionDto update(Long id, UpdateSysFunctionDto dto) {
        SysFunction function = functionDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Function not found with ID: " + id));
        
        String langCode = LocalContextUtil.resolveLangCode(dto.getLangCode());
        if (!languageDao.existsByCode(langCode)) {
            throw new NotFoundException("Language not found: " + langCode);
        }

        // Tìm hoặc tạo translation cho langCode
        SysFunctionLan functionLan = function.getTranslations().stream()
                .filter(lan -> langCode.equals(lan.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    SysFunctionLan newLan = new SysFunctionLan();
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
        SysFunction function = functionDao.findById(id)
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
