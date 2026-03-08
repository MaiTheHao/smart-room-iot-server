package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dao.SysFunctionDao;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.SysFunctionLan;
import com.iviet.ivshs.entities.SysFunction;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
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

  @Override
  public List<SysFunctionDto> getAll() {
    String langCode = LocalContextUtil.getCurrentLangCode();
    return functionDao.findAll(langCode);
  }

  @Override
  public PaginatedResponse<SysFunctionDto> getList(int page, int size) {
    String langCode = LocalContextUtil.getCurrentLangCode();
    return new PaginatedResponse<>(
      functionDao.findAll(page, size, langCode),
      page, size, functionDao.countAll()
    );
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
  public List<SysFunctionDto> getAllByGroupId(Long groupId) {
    String langCode = LocalContextUtil.getCurrentLangCode();
    return functionDao.findAllByGroupId(groupId, langCode);
  }

  @Override
  public PaginatedResponse<SysFunctionDto> getListByGroupId(Long groupId, int page, int size) {
    String langCode = LocalContextUtil.getCurrentLangCode();
    return new PaginatedResponse<>(
      functionDao.findAllByGroupId(groupId, langCode, page, size),
      page, size, functionDao.countByGroupId(groupId)
    );
  }

  @Override
  public List<SysFunctionDto> getAllByGroupCode(String groupCode) {
    String langCode = LocalContextUtil.getCurrentLangCode();
    return functionDao.findAllByGroupCode(groupCode, langCode);
  }

  @Override
  public PaginatedResponse<SysFunctionDto> getListByGroupCode(String groupCode, int page, int size) {
    String langCode = LocalContextUtil.getCurrentLangCode();
    return new PaginatedResponse<>(
      functionDao.findAllByGroupCode(groupCode, langCode, page, size),
      page, size, functionDao.countByGroupCode(groupCode)
    );
  }

  @Override
  public List<SysFunctionDto> getAllByClientId(Long clientId) {
    String langCode = LocalContextUtil.getCurrentLangCode();
    return functionDao.findAllByClientId(clientId, langCode);
  }

  @Override
  public PaginatedResponse<SysFunctionDto> getListByClientId(Long clientId, int page, int size) {
    String langCode = LocalContextUtil.getCurrentLangCode();
    return new PaginatedResponse<>(
      functionDao.findAllByClientId(clientId, langCode, page, size),
      page, size, functionDao.countByClientId(clientId)
    );
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
    if (dto == null || !StringUtils.hasText(dto.functionCode())) {
      throw new BadRequestException("Data and Function code are required");
    }

    String code = dto.functionCode().trim();
    _checkDuplicate(code, null);

    String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
    if (!languageDao.existsByCode(langCode)) {
      throw new NotFoundException("Language not found: " + langCode);
    }

    SysFunction function = dto.toEntity();
    function.setFunctionCode(code);

    var functionLan = new SysFunctionLan();
    functionLan.setLangCode(langCode);
    functionLan.setName(dto.name() != null ? dto.name().trim() : "");
    functionLan.setDescription(dto.description());
    functionLan.setOwner(function);

    function.getTranslations().add(functionLan);
    functionDao.save(function);

    return SysFunctionDto.from(function, functionLan);
  }

  @Override
  @Transactional
  public SysFunctionDto update(Long id, UpdateSysFunctionDto dto) {
    SysFunction function = functionDao.findById(id)
      .orElseThrow(() -> new NotFoundException("Function not found with ID: " + id));

    String langCode = LocalContextUtil.resolveLangCode(dto.langCode());
    if (!languageDao.existsByCode(langCode)) {
      throw new NotFoundException("Language not found: " + langCode);
    }

    var functionLan = function.getTranslations().stream()
      .filter(lan -> langCode.equals(lan.getLangCode()))
      .findFirst()
      .orElseGet(() -> {
        var newLan = new SysFunctionLan();
        newLan.setLangCode(langCode);
        newLan.setOwner(function);
        function.getTranslations().add(newLan);
        return newLan;
      });

    if (dto.name() != null) {
      functionLan.setName(dto.name().trim());
    }
    if (dto.description() != null) {
      functionLan.setDescription(dto.description());
    }

    functionDao.save(function);
    return SysFunctionDto.from(function, functionLan);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    if (!functionDao.existsById(id)) {
      throw new NotFoundException("Function not found with ID: " + id);
    }
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
