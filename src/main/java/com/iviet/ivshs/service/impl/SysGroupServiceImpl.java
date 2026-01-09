package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.LanguageDao;
import com.iviet.ivshs.dao.SysGroupDao;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.SysGroupLan;
import com.iviet.ivshs.entities.SysGroup;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.mapper.SysGroupMapper;
import com.iviet.ivshs.service.SysGroupService;
import com.iviet.ivshs.util.LocalContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SysGroupServiceImpl implements SysGroupService {

    private final SysGroupDao groupDao;
    private final LanguageDao languageDao;
    private final SysGroupMapper groupMapper;

    @Override
    public PaginatedResponse<SysGroupDto> getList(int page, int size) {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return new PaginatedResponse<>(
                groupDao.findAll(page, size, langCode),
                page, size, groupDao.countAll()
        );
    }

    @Override
    public List<SysGroupDto> getAll() {
        String langCode = LocalContextUtil.getCurrentLangCode();
        return groupDao.findAll(langCode);
    }

    @Override
    public SysGroupDto getById(Long id) {
        return groupDao.findById(id, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Group not found with ID: " + id));
    }

    @Override
    public SysGroupDto getByCode(String groupCode) {
        if (!StringUtils.hasText(groupCode)) {
            throw new BadRequestException("Group code is required");
        }
        return groupDao.findByCode(groupCode, LocalContextUtil.getCurrentLangCode())
                .orElseThrow(() -> new NotFoundException("Group not found with code: " + groupCode));
    }

    @Override
    @Transactional
    public SysGroupDto create(CreateSysGroupDto dto) {
        if (dto == null || !StringUtils.hasText(dto.getGroupCode())) {
            throw new BadRequestException("Data and Group code are required");
        }

        String code = dto.getGroupCode().trim();
        _checkDuplicate(code, null);
        
        String langCode = LocalContextUtil.resolveLangCode(dto.getLangCode());
        if (!languageDao.existsByCode(langCode)) {
            throw new NotFoundException("Language not found: " + langCode);
        }

        SysGroup group = groupMapper.fromCreateDto(dto);
        group.setGroupCode(code);

        SysGroupLan groupLan = new SysGroupLan();
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
    public SysGroupDto update(Long id, UpdateSysGroupDto dto) {
        SysGroup group = groupDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Group not found with ID: " + id));
        
        String langCode = LocalContextUtil.resolveLangCode(dto.getLangCode());
        if (!languageDao.existsByCode(langCode)) {
            throw new NotFoundException("Language not found: " + langCode);
        }

        // Tìm hoặc tạo translation cho langCode
        SysGroupLan groupLan = group.getTranslations().stream()
                .filter(lan -> langCode.equals(lan.getLangCode()))
                .findFirst()
                .orElseGet(() -> {
                    SysGroupLan newLan = new SysGroupLan();
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
    public List<SysFunctionDto> getFunctionsByGroupId(Long groupId) {
        if (groupId == null) {
            throw new BadRequestException("Group ID is required");
        }
        String langCode = LocalContextUtil.getCurrentLangCode();
        return groupDao.findFunctionsByGroupId(groupId, langCode);
    }

    @Override
    public PaginatedResponse<SysFunctionDto> getFunctionsByGroupId(Long groupId, int page, int size) {
        if (groupId == null) {
            throw new BadRequestException("Group ID is required");
        }
        String langCode = LocalContextUtil.getCurrentLangCode();
        List<SysFunctionDto> functions = groupDao.findFunctionsByGroupId(groupId, langCode, page, size);
        long total = groupDao.countFunctionsByGroupId(groupId);
        return new PaginatedResponse<>(functions, page, size, total);
    }

    @Override
    public List<ClientDto> getClientsByGroupId(Long groupId) {
        if (groupId == null) {
            throw new BadRequestException("Group ID is required");
        }
        return groupDao.findClientsByGroupId(groupId);
    }

    @Override
    public PaginatedResponse<ClientDto> getClientsByGroupId(Long groupId, int page, int size) {
        if (groupId == null) {
            throw new BadRequestException("Group ID is required");
        }
        List<ClientDto> clients = groupDao.findClientsByGroupId(groupId, page, size);
        long total = groupDao.countClientsByGroupId(groupId);
        return new PaginatedResponse<>(clients, page, size, total);
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

    @Override
    public long countByClient(Long clientId) {
        if (clientId == null) {
            throw new BadRequestException("Client ID is required");
        }
        return groupDao.countAllByClientId(clientId);
    }

    private void _checkDuplicate(String code, Long currentId) {
        groupDao.findEntityByCode(code).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BadRequestException("Group code already exists: " + code);
            }
        });
    }

    @Override
    public List<SysGroupDto> getAllByClientId(Long clientId) {
        if (clientId == null) throw new BadRequestException("Client ID is required");
        return groupDao.findAllByClientId(clientId, LocalContextUtil.getCurrentLangCode());
    }

    @Override
    public PaginatedResponse<SysGroupDto> getAllByClientId(Long clientId, int page, int size) {
        if (clientId == null) throw new BadRequestException("Client ID is required");
        List<SysGroupDto> groups = groupDao.findAllByClientId(clientId, LocalContextUtil.getCurrentLangCode(), page, size);
        long total = groupDao.countAllByClientId(clientId);
        return new PaginatedResponse<>(groups, page, size, total);
    }

    @Override
    public List<SysGroup> getEntitiesByClientId(Long clientId) {
        if (clientId == null) throw new BadRequestException("Client ID is required");
        return groupDao.findEntitiesByClientId(clientId);
    }

    @Override
    public PaginatedResponse<SysGroup> getEntitiesByClientId(Long clientId, int page, int size) {
        if (clientId == null) throw new BadRequestException("Client ID is required");
        List<SysGroup> groups = groupDao.findEntitiesByClientId(clientId, page, size);
        long total = groupDao.countAllByClientId(clientId);
        return new PaginatedResponse<>(groups, page, size, total);
    }

    @Override
    public List<SysGroupWithClientStatusDto> getAllWithClientStatus(Long clientId) {
        if (clientId == null) throw new BadRequestException("Client ID is required");
        String langCode = LocalContextUtil.getCurrentLangCode();
        return groupDao.findAllWithClientStatus(clientId, langCode);
    }
}
