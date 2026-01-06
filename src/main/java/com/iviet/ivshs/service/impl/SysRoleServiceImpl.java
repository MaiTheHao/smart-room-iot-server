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

                // Kiểm tra đã tồn tại chưa
                if (roleDao.existsByGroupAndFunction(group.getId(), function.getId())) {
                    skippedCount++;
                    log.debug("Role already exists for group {} and function {}", group.getId(), functionCode);
                    continue;
                }

                // Tạo role mới
                SysRole role = new SysRole();
                role.setGroup(group);
                role.setFunction(function);
                role.setIsActive(true);
                
                roleDao.save(role);
                successCount++;
                hasChanges = true;

            } catch (Exception e) {
                errors.add("Failed to add function " + functionCode + ": " + e.getMessage());
                log.error("Error adding function {} to group {}", functionCode, group.getId(), e);
            }
        }

        // Rebuild cache 1 lần duy nhất sau khi hoàn tất tất cả DB operations
        if (hasChanges) {
            cacheService.rebuildCacheForGroup(group.getId());
        }

        String message = String.format(
            "Added %d function(s), skipped %d (already exists)",
            successCount, skippedCount
        );
        
        if (!errors.isEmpty()) {
            message += ". Errors: " + String.join("; ", errors);
        }

        return BatchOperationResultDto.builder()
                .successCount(successCount)
                .skippedCount(skippedCount)
                .failedCount(errors.size())
                .message(message)
                .build();
    }

    @Override
    public BatchOperationResultDto removeFunctionsFromGroup(BatchRemoveFunctionsFromGroupDto dto) {
        if (dto == null || dto.getGroupId() == null || dto.getFunctionCodes() == null || dto.getFunctionCodes().isEmpty()) {
            throw new BadRequestException("Invalid request data");
        }

        // Verify group exists
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

                // Kiểm tra role có tồn tại không
                if (!roleDao.existsByGroupAndFunction(dto.getGroupId(), function.getId())) {
                    skippedCount++;
                    log.debug("Role not found for group {} and function {}", dto.getGroupId(), functionCode);
                    continue;
                }

                // Xóa role
                int deleted = roleDao.deleteByGroupAndFunction(dto.getGroupId(), function.getId());
                if (deleted > 0) {
                    successCount++;
                    hasChanges = true;
                }

            } catch (Exception e) {
                errors.add("Failed to remove function " + functionCode + ": " + e.getMessage());
                log.error("Error removing function {} from group {}", functionCode, dto.getGroupId(), e);
            }
        }

        // Rebuild cache 1 lần duy nhất sau khi hoàn tất tất cả DB operations
        if (hasChanges) {
            cacheService.clearCacheForGroup(dto.getGroupId());
            cacheService.rebuildCacheForGroup(dto.getGroupId());
        }

        String message = String.format(
            "Removed %d function(s), skipped %d (not found)",
            successCount, skippedCount
        );
        
        if (!errors.isEmpty()) {
            message += ". Errors: " + String.join("; ", errors);
        }

        return BatchOperationResultDto.builder()
                .successCount(successCount)
                .skippedCount(skippedCount)
                .failedCount(errors.size())
                .message(message)
                .build();
    }

    @Override
    public BatchOperationResultDto toggleGroupFunctions(ToggleGroupFunctionsDto dto) {
        if (dto == null || dto.getGroupId() == null || dto.getFunctionToggles() == null || dto.getFunctionToggles().isEmpty()) {
            throw new BadRequestException("Invalid request data");
        }

        SysGroup group = groupDao.findById(dto.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found with ID: " + dto.getGroupId()));

        int addedCount = 0;
        int removedCount = 0;
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
                    // Add function to group
                    if (exists) {
                        skippedCount++;
                    } else {
                        SysRole role = new SysRole();
                        role.setGroup(group);
                        role.setFunction(function);
                        role.setIsActive(true);
                        roleDao.save(role);
                        addedCount++;
                        hasChanges = true;
                    }
                } else {
                    // Remove function from group
                    if (!exists) {
                        skippedCount++;
                    } else {
                        roleDao.deleteByGroupAndFunction(group.getId(), function.getId());
                        removedCount++;
                        hasChanges = true;
                    }
                }

            } catch (Exception e) {
                errors.add("Failed to toggle function " + functionCode + ": " + e.getMessage());
                log.error("Error toggling function {} for group {}", functionCode, group.getId(), e);
            }
        }

        // Rebuild cache 1 lần duy nhất sau khi hoàn tất tất cả DB operations
        if (hasChanges) {
            cacheService.clearCacheForGroup(group.getId());
            cacheService.rebuildCacheForGroup(group.getId());
        }

        String message = String.format(
            "Added %d, removed %d, skipped %d function(s)",
            addedCount, removedCount, skippedCount
        );
        
        if (!errors.isEmpty()) {
            message += ". Errors: " + String.join("; ", errors);
        }

        return BatchOperationResultDto.builder()
                .successCount(addedCount + removedCount)
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

        // Kiểm tra đã tồn tại chưa
        if (roleDao.existsByGroupAndFunction(groupId, function.getId())) {
            throw new BadRequestException("Function already exists in group");
        }

        // Tạo role mới
        SysRole role = new SysRole();
        role.setGroup(group);
        role.setFunction(function);
        role.setIsActive(true);
        
        roleDao.save(role);

        // Rebuild cache sau khi DB operation hoàn tất
        cacheService.rebuildCacheForGroup(groupId);
    }

    @Override
    public void removeFunctionFromGroup(Long groupId, String functionCode) {
        if (groupId == null || functionCode == null || functionCode.isBlank()) {
            throw new BadRequestException("Group ID and Function code are required");
        }

        SysFunction function = functionDao.findByCode(functionCode)
                .orElseThrow(() -> new NotFoundException("Function not found with code: " + functionCode));

        // Kiểm tra role có tồn tại không
        if (!roleDao.existsByGroupAndFunction(groupId, function.getId())) {
            throw new NotFoundException("Function not found in group");
        }

        // Xóa role
        roleDao.deleteByGroupAndFunction(groupId, function.getId());

        // Rebuild cache cho group
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

        for (Long groupId : dto.getGroupIds()) {
            try {
                SysGroup group = groupDao.findById(groupId)
                        .orElseThrow(() -> new NotFoundException("Group not found with ID: " + groupId));

                // Kiểm tra client đã có group chưa
                if (client.getGroups().contains(group)) {
                    skippedCount++;
                    log.debug("Client {} already has group {}", client.getId(), groupId);
                    continue;
                }

                // Thêm group vào client
                client.getGroups().add(group);
                successCount++;

            } catch (Exception e) {
                errors.add("Failed to assign group " + groupId + ": " + e.getMessage());
                log.error("Error assigning group {} to client {}", groupId, client.getId(), e);
            }
        }

        clientDao.save(client);

        // Rebuild cache cho client sau khi assign groups
        cacheService.rebuildCacheForClient(client.getId());

        String message = String.format(
            "Assigned %d group(s), skipped %d (already assigned)",
            successCount, skippedCount
        );
        
        if (!errors.isEmpty()) {
            message += ". Errors: " + String.join("; ", errors);
        }

        return BatchOperationResultDto.builder()
                .successCount(successCount)
                .skippedCount(skippedCount)
                .failedCount(errors.size())
                .message(message)
                .build();
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

        // Kiểm tra client có group không
        if (!client.getGroups().contains(group)) {
            throw new NotFoundException("Client does not have this group");
        }

        // Remove group khỏi client
        client.getGroups().remove(group);
        clientDao.save(client);

        // Clear cache cho client-group và rebuild
        cacheService.clearCacheForClientGroup(clientId, groupId);
        cacheService.rebuildCacheForClient(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasFunction(Long groupId, String functionCode) {
        if (groupId == null || functionCode == null || functionCode.isBlank()) {
            return false;
        }

        SysFunction function = functionDao.findByCode(functionCode).orElse(null);
        if (function == null) {
            return false;
        }

        return roleDao.existsActiveByGroupAndFunction(groupId, function.getId());
    }
}
