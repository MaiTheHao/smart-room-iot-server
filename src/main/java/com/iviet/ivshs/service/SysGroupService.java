package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.*;
import com.iviet.ivshs.entities.SysGroup;

import java.util.List;

public interface SysGroupService {

    PaginatedResponse<SysGroupDto> getList(int page, int size);

    List<SysGroupDto> getAll();

    SysGroupDto getById(Long id);

    SysGroupDto getByCode(String groupCode);

    SysGroupDto create(CreateSysGroupDto dto);

    SysGroupDto update(Long id, UpdateSysGroupDto dto);

    void delete(Long id);

    long count();

    List<SysGroupDto> getAllByClientId(Long clientId);

    PaginatedResponse<SysGroupDto> getAllByClientId(Long clientId, int page, int size);

    List<SysGroup> getEntitiesByClientId(Long clientId);
    
    PaginatedResponse<SysGroup> getEntitiesByClientId(Long clientId, int page, int size);

    List<SysFunctionDto> getFunctionsByGroupId(Long groupId);

    PaginatedResponse<SysFunctionDto> getFunctionsByGroupId(Long groupId, int page, int size);

    long countFunctionsByGroupId(Long groupId);

    List<ClientDto> getClientsByGroupId(Long groupId);

    PaginatedResponse<ClientDto> getClientsByGroupId(Long groupId, int page, int size);

    long countClientsByGroupId(Long groupId);

    long countByClient(Long clientId);

    /**
     * Lấy tất cả Groups với trạng thái đã assign cho Client hay chưa
     * @param clientId ID của Client
     * @return List các Groups với status
     */
    List<SysGroupWithClientStatusDto> getAllWithClientStatus(Long clientId);
}