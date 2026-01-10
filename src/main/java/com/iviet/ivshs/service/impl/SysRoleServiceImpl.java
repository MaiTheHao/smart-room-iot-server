package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dao.SysFunctionDao;
import com.iviet.ivshs.dao.SysGroupDao;
import com.iviet.ivshs.dao.SysRoleDao;
import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.SysFunction;
import com.iviet.ivshs.entities.SysGroup;
import com.iviet.ivshs.entities.SysRole;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.ClientFunctionService;
import com.iviet.ivshs.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleDao roleDao;
    private final SysGroupDao groupDao;
    private final SysFunctionDao functionDao;
    private final ClientDao clientDao;
    private final ClientFunctionService cacheService;

    @Override
    public BatchOperationResultDto addFunctionsToGroup(BatchAddFunctionsToGroupDto dto) {
        if (dto == null || dto.getGroupId() == null || dto.getFunctionCodes() == null || dto.getFunctionCodes().isEmpty()) {
            throw new BadRequestException("Invalid request data");
        }

        SysGroup group = groupDao.findById(dto.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found with ID: " + dto.getGroupId()));

        int successCount = 0;
        int skippedCount = 0;
        List<String> errors = new ArrayList<>();
        boolean hasChanges = false;

        for (String functionCode : dto.getFunctionCodes()) {
            try {
                SysFunction function = functionDao.findByCode(functionCode)
                        .orElseThrow(() -> new NotFoundException("Function not found with code: " + functionCode));

                if (roleDao.existsByGroupAndFunction(group.getId(), function.getId())) {
                    skippedCount++;
                    continue;
                }

                SysRole role = new SysRole();
                role.setGroup(group);
                role.setFunction(function);
                
                roleDao.save(role);
                successCount++;
                hasChanges = true;

            } catch (Exception e) {
                String errorMsg = String.format("Failed to add function %s: %s", functionCode, e.getMessage());
                errors.add(errorMsg);
                log.error("Error adding function {} to group {}", functionCode, group.getId(), e);
            }
        }

        if (hasChanges) {
            cacheService.rebuildCacheForGroup(group.getId());
        }

        return buildBatchResult(successCount, skippedCount, errors, "added");
    }

    @Override
    public BatchOperationResultDto removeFunctionsFromGroup(BatchRemoveFunctionsFromGroupDto dto) {
        if (dto == null || dto.getGroupId() == null || dto.getFunctionCodes() == null || dto.getFunctionCodes().isEmpty()) {
            throw new BadRequestException("Invalid request data");
        }

        if (!groupDao.existsById(dto.getGroupId())) {
            throw new NotFoundException("Group not found with ID: " + dto.getGroupId());
        }

        int successCount = 0;
        int skippedCount = 0;
        List<String> errors = new ArrayList<>();
        boolean hasChanges = false;

        for (String functionCode : dto.getFunctionCodes()) {
            try {
                SysFunction function = functionDao.findByCode(functionCode)
                        .orElseThrow(() -> new NotFoundException("Function not found with code: " + functionCode));

                int deleted = roleDao.deleteByGroupAndFunction(dto.getGroupId(), function.getId());
                
                if (deleted > 0) {
                    successCount++;
                    hasChanges = true;
                } else {
                    skippedCount++;
                }

            } catch (Exception e) {
                String errorMsg = String.format("Failed to remove function %s: %s", functionCode, e.getMessage());
                errors.add(errorMsg);
                log.error("Error removing function {} from group {}", functionCode, dto.getGroupId(), e);
            }
        }

        if (hasChanges) {
            cacheService.clearCacheForGroup(dto.getGroupId());
            cacheService.rebuildCacheForGroup(dto.getGroupId());
        }

        return buildBatchResult(successCount, skippedCount, errors, "removed");
    }

    @Override
    public BatchOperationResultDto toggleGroupFunctions(ToggleGroupFunctionsDto dto) {
        if (dto == null || dto.getGroupId() == null || dto.getFunctionToggles() == null || dto.getFunctionToggles().isEmpty()) {
            throw new BadRequestException("Invalid request data");
        }

        SysGroup group = groupDao.findById(dto.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found with ID: " + dto.getGroupId()));

        int processedCount = 0;
        int skippedCount = 0;
        List<String> errors = new ArrayList<>();
        boolean hasChanges = false;

        for (Map.Entry<String, Boolean> entry : dto.getFunctionToggles().entrySet()) {
            String functionCode = entry.getKey();
            Boolean shouldAdd = entry.getValue();

            try {
                SysFunction function = functionDao.findByCode(functionCode)
                        .orElseThrow(() -> new NotFoundException("Function not found with code: " + functionCode));

                boolean exists = roleDao.existsByGroupAndFunction(group.getId(), function.getId());

                if (Boolean.TRUE.equals(shouldAdd)) {
                    if (!exists) {
                        SysRole role = new SysRole();
                        role.setGroup(group);
                        role.setFunction(function);
                        roleDao.save(role);
                        hasChanges = true;
                        processedCount++;
                    } else {
                        skippedCount++;
                    }
                } else {
                    if (exists) {
                        roleDao.deleteByGroupAndFunction(group.getId(), function.getId());
                        hasChanges = true;
                        processedCount++;
                    } else {
                        skippedCount++;
                    }
                }

            } catch (Exception e) {
                String errorMsg = String.format("Failed to toggle function %s: %s", functionCode, e.getMessage());
                errors.add(errorMsg);
                log.error("Error toggling function {} for group {}", functionCode, group.getId(), e);
            }
        }

        if (hasChanges) {
            cacheService.clearCacheForGroup(group.getId());
            cacheService.rebuildCacheForGroup(group.getId());
        }

        String message = String.format("Processed %d, skipped %d function(s)", processedCount, skippedCount);
        if (!errors.isEmpty()) {
            message += ". Errors: " + String.join("; ", errors);
        }

        return BatchOperationResultDto.builder()
                .successCount(processedCount)
                .skippedCount(skippedCount)
                .failedCount(errors.size())
                .message(message)
                .build();
    }

    @Override
    public void addFunctionToGroup(Long groupId, String functionCode) {
        if (groupId == null || functionCode == null || functionCode.isBlank()) {
            throw new BadRequestException("Group ID and Function code are required");
        }

        SysGroup group = groupDao.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found with ID: " + groupId));

        SysFunction function = functionDao.findByCode(functionCode)
                .orElseThrow(() -> new NotFoundException("Function not found with code: " + functionCode));

        if (roleDao.existsByGroupAndFunction(groupId, function.getId())) {
            throw new BadRequestException("Function already exists in group");
        }

        SysRole role = new SysRole();
        role.setGroup(group);
        role.setFunction(function);
        
        roleDao.save(role);

        cacheService.rebuildCacheForGroup(groupId);
    }

    @Override
    public void removeFunctionFromGroup(Long groupId, String functionCode) {
        if (groupId == null || functionCode == null || functionCode.isBlank()) {
            throw new BadRequestException("Group ID and Function code are required");
        }

        SysFunction function = functionDao.findByCode(functionCode)
                .orElseThrow(() -> new NotFoundException("Function not found with code: " + functionCode));

        if (!roleDao.existsByGroupAndFunction(groupId, function.getId())) {
            throw new NotFoundException("Function not found in group");
        }

        roleDao.deleteByGroupAndFunction(groupId, function.getId());

        cacheService.rebuildCacheForGroup(groupId);
    }

    @Override
    public BatchOperationResultDto assignGroupsToClient(AssignGroupsToClientDto dto) {
        if (dto == null || dto.getClientId() == null || dto.getGroupIds() == null || dto.getGroupIds().isEmpty()) {
            throw new BadRequestException("Invalid request data");
        }

        Client client = clientDao.findById(dto.getClientId())
                .orElseThrow(() -> new NotFoundException("Client not found with ID: " + dto.getClientId()));

        int successCount = 0;
        int skippedCount = 0;
        List<String> errors = new ArrayList<>();
        boolean hasChanges = false;

        for (Long groupId : dto.getGroupIds()) {
            try {
                SysGroup group = groupDao.findById(groupId)
                        .orElseThrow(() -> new NotFoundException("Group not found with ID: " + groupId));

                if (client.getGroups().contains(group)) {
                    skippedCount++;
                    continue;
                }

                client.getGroups().add(group);
                successCount++;
                hasChanges = true;

            } catch (Exception e) {
                String errorMsg = String.format("Failed to assign group %d: %s", groupId, e.getMessage());
                errors.add(errorMsg);
                log.error("Error assigning group {} to client {}", groupId, client.getId(), e);
            }
        }

        if (hasChanges) {
            clientDao.save(client);
            cacheService.rebuildCacheForClient(client.getId());
        }

        return buildBatchResult(successCount, skippedCount, errors, "assigned");
    }

    @Override
    public void unassignGroupFromClient(Long clientId, Long groupId) {
        if (clientId == null || groupId == null) {
            throw new BadRequestException("Client ID and Group ID are required");
        }

        Client client = clientDao.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found with ID: " + clientId));

        SysGroup group = groupDao.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found with ID: " + groupId));

        if (!client.getGroups().contains(group)) {
            throw new NotFoundException("Client does not have this group");
        }

        client.getGroups().remove(group);
        clientDao.save(client);

        cacheService.clearCacheForClientGroup(clientId, groupId);
        cacheService.rebuildCacheForClient(clientId);
    }

    @Override
    public void unassignGroupsFromClient(UnassignGroupsFromClientDto dto) {
        if (dto == null || dto.getClientId() == null || dto.getGroupIds() == null || dto.getGroupIds().isEmpty()) {
            throw new BadRequestException("Invalid request data");
        }

        Client client = clientDao.findById(dto.getClientId())
                .orElseThrow(() -> new NotFoundException("Client not found with ID: " + dto.getClientId()));

        boolean hasChanges = false;

        for (Long groupId : dto.getGroupIds()) {
            try {
                SysGroup group = groupDao.findById(groupId)
                        .orElseThrow(() -> new NotFoundException("Group not found with ID: " + groupId));

                if (client.getGroups().remove(group)) {
                    hasChanges = true;
                    cacheService.clearCacheForClientGroup(client.getId(), groupId);
                }

            } catch (Exception e) {
                log.error("Error unassigning group {} from client {}", groupId, client.getId(), e);
            }
        }

        if (hasChanges) {
            clientDao.save(client);
            cacheService.rebuildCacheForClient(client.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasFunction(Long groupId, String functionCode) {
        if (groupId == null || functionCode == null || functionCode.isBlank()) {
            return false;
        }

        return functionDao.findByCode(functionCode)
                .map(function -> roleDao.existsByGroupAndFunction(groupId, function.getId()))
                .orElse(false);
    }

    private BatchOperationResultDto buildBatchResult(int success, int skipped, List<String> errors, String action) {
        String message = String.format("Successfully %s %d item(s), skipped %d", action, success, skipped);
        
        if (!errors.isEmpty()) {
            message += ". Errors: " + String.join("; ", errors);
        }

        return BatchOperationResultDto.builder()
                .successCount(success)
                .skippedCount(skipped)
                .failedCount(errors.size())
                .message(message)
                .build();
    }
}