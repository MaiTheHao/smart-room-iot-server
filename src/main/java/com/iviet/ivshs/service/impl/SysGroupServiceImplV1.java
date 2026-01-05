package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.LanguageDaoV1;
import com.iviet.ivshs.dao.SysGroupDaoV1;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.SysGroupLanV1;
import com.iviet.ivshs.entities.SysGroupV1;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.SysGroupMapperV1;
import com.iviet.ivshs.service.SysGroupServiceV1;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SysGroupServiceImplV1 implements SysGroupServiceV1 {

    private final SysGroupDaoV1 groupDao;
    private final LanguageDaoV1 languageDao;
    private final SysGroupMapperV1 groupMapper;

    @Override
    public PaginatedResponseV1<SysGroupDtoV1> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return new PaginatedResponseV1<>(
                groupDao.findAll(page, size, langCode),
                page, size, groupDao.countAll()
        );
    }

    @Override
    public List<SysGroupDtoV1> getAll() {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return groupDao.findAll(langCode);
    }

    @Override
    public SysGroupDtoV1 getById(Long id) {
        return groupDao.findById(id, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Group not found with ID: " + id));
    }

    @Override
    public SysGroupDtoV1 getByCode(String groupCode) {
        if (!StringUtils.hasText(groupCode)) {
            throw new BadRequestException("Group code is required");
        }
        return groupDao.findByCode(groupCode, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Group not found with code: " + groupCode));
    }

    @Override
    @Transactional
    public SysGroupDtoV1 create(CreateSysGroupDtoV1 dto) {
        if (dto == null || !StringUtils.hasText(dto.getGroupCode())) {
            throw new BadRequestException("Data and Group code are required");
        }

        String code = dto.getGroupCode().trim();
        _checkDuplicate(code, null);
        
        String langCode = LocalContextUtil.resolveLangCode(dto.getLangCode());
        if (!languageDao.existsByCode(langCode)) {
            throw new NotFoundException("Language not found: " + langCode);
        }

        SysGroupV1 group = groupMapper.fromCreateDto(dto);
        group.setGroupCode(code);

        SysGroupLanV1 groupLan = new SysGroupLanV1();
        groupLan.setLangCode(langCode);
        groupLan.setName(dto.getName() != null ? dto.getName().trim() : "");
        groupLan.setDescription(dto.getDescription());
        groupLan.setOwner(group);
        
        group.getTranslations().add(groupLan);
        groupDao.save(group);

        return groupMapper.toDto(group, groupLan);
    }

    @Override
    @Transactional
    public SysGroupDtoV1 update(Long id, UpdateSysGroupDtoV1 dto) {
        SysGroupV1 group = groupDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Group not found with ID: " + id));
        
        String langCode = LocalContextUtil.resolveLangCode(dto.getLangCode());
        if (!languageDao.existsByCode(langCode)) {
            throw new NotFoundException("Language not found: " + langCode);
        }

        // Tìm hoặc tạo translation cho langCode
        SysGroupLanV1 groupLan = group.getTranslations().stream()
                .filter(lan -> langCode.equals(lan.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    SysGroupLanV1 newLan = new SysGroupLanV1();
                    newLan.setLangCode(langCode);
                    newLan.setOwner(group);
                    group.getTranslations().add(newLan);
                    return newLan;
                });

        // Update translation fields
        if (dto.getName() != null) {
            groupLan.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            groupLan.setDescription(dto.getDescription());
        }

        groupDao.save(group);
        return groupMapper.toDto(group, groupLan);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!groupDao.existsById(id)) {
            throw new NotFoundException("Group not found with ID: " + id);
        }
        
        // Check if group has clients
        long clientCount = groupDao.countClientsByGroupId(id);
        if (clientCount > 0) {
            throw new BadRequestException(
                "Cannot delete group. It has " + clientCount + " client(s). " +
                "Please remove all clients from this group first."
            );
        }
        
        // Xóa group (cascade sẽ xóa roles và translations)
        groupDao.deleteById(id);
    }

    @Override
    public List<SysFunctionDtoV1> getFunctionsByGroupId(Long groupId) {
        if (groupId == null) {
            throw new BadRequestException("Group ID is required");
        }
        String langCode = LocalContextUtil.getCurrentLangCode();
        return groupDao.findFunctionsByGroupId(groupId, langCode);
    }

    @Override
    public PaginatedResponseV1<SysFunctionDtoV1> getFunctionsByGroupId(Long groupId, int page, int size) {
        if (groupId == null) {
            throw new BadRequestException("Group ID is required");
        }
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<SysFunctionDtoV1> functions = groupDao.findFunctionsByGroupId(groupId, langCode, page, size);
        long total = groupDao.countFunctionsByGroupId(groupId);
        return new PaginatedResponseV1<>(functions, page, size, total);
    }

    @Override
    public List<ClientDtoV1> getClientsByGroupId(Long groupId) {
        if (groupId == null) {
            throw new BadRequestException("Group ID is required");
        }
        return groupDao.findClientsByGroupId(groupId);
    }

    @Override
    public PaginatedResponseV1<ClientDtoV1> getClientsByGroupId(Long groupId, int page, int size) {
        if (groupId == null) {
            throw new BadRequestException("Group ID is required");
        }
        List<ClientDtoV1> clients = groupDao.findClientsByGroupId(groupId, page, size);
        long total = groupDao.countClientsByGroupId(groupId);
        return new PaginatedResponseV1<>(clients, page, size, total);
    }

    @Override
    public long count() {
        return groupDao.countAll();
    }

    @Override
    public long countFunctionsByGroupId(Long groupId) {
        if (groupId == null) {
            throw new BadRequestException("Group ID is required");
        }
        return groupDao.countFunctionsByGroupId(groupId);
    }

    @Override
    public long countClientsByGroupId(Long groupId) {
        if (groupId == null) {
            throw new BadRequestException("Group ID is required");
        }
        return groupDao.countClientsByGroupId(groupId);
    }

    private void _checkDuplicate(String code, Long currentId) {
        groupDao.findByCode(code).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BadRequestException("Group code already exists: " + code);
            }
        });
    }
}
