package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dto.CreateLanguageDtoV1;
import com.iviet.ivshs.dto.LanguageDtoV1;
import com.iviet.ivshs.dto.PaginatedResponseV1;
import com.iviet.ivshs.dto.UpdateLanguageDtoV1;
import com.iviet.ivshs.entities.Language;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.LanguageMapperV1;
import com.iviet.ivshs.service.LanguageServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LanguageServiceImplV1 implements LanguageServiceV1 {

    private final LanguageDao languageDao;
    private final LanguageMapperV1 languageMapper;

    @Override
    public PaginatedResponseV1<LanguageDtoV1> getList(int page, int size) {
        List<LanguageDtoV1> content = languageDao.findAll(page, size).stream()
                .map(languageMapper::toDto)
                .toList();

        return new PaginatedResponseV1<>(content, page, size, languageDao.count());
    }

    @Override
    public LanguageDtoV1 getById(Long langId) {
        return languageMapper.toDto(languageDao.findById(langId).orElseThrow(() -> new NotFoundException("Language not found with ID: " + langId)));
    }

    @Override
    public LanguageDtoV1 getByCode(String code) {
        if (!StringUtils.hasText(code)) throw new BadRequestException("Language code is required");

        return languageDao.findByCode(code.trim())
                .map(languageMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Language not found with code: " + code));
    }

    @Override
    @Transactional
    public LanguageDtoV1 create(CreateLanguageDtoV1 dto) {
        if (dto == null || !StringUtils.hasText(dto.code())) {
            throw new BadRequestException("Language data and code are required");
        }

        String code = dto.code().trim();
        _checkDuplicate(code, null);

        Language entity = languageMapper.fromCreateDto(dto);
        entity.setCode(code);
        
        return languageMapper.toDto(languageDao.save(entity));
    }

    @Override
    @Transactional
    public LanguageDtoV1 update(Long langId, UpdateLanguageDtoV1 dto) {
        if (dto == null) throw new BadRequestException("Update data is required");

        Language entity = languageDao.findById(langId).orElseThrow(() -> new NotFoundException("Language not found with ID: " + langId));

        if (StringUtils.hasText(dto.code())) {
            String newCode = dto.code().trim();
            _checkDuplicate(newCode, langId);
            entity.setCode(newCode);
        }

        if (dto.name() != null) entity.setName(dto.name());
        if (dto.description() != null) entity.setDescription(dto.description());

        return languageMapper.toDto(languageDao.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long langId) {
        languageDao.findById(langId).orElseThrow(() -> new NotFoundException("Language not found with ID: " + langId));
        languageDao.deleteById(langId);
    }

    private void _checkDuplicate(String code, Long currentId) {
        languageDao.findByCode(code).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BadRequestException("Language code already exists: " + code);
            }
        });
    }
}