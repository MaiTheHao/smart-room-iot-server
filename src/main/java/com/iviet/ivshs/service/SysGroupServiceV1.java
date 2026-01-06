package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.*;

import java.util.List;

public interface SysGroupServiceV1 {
    
    /**
     * Lấy danh sách Groups với phân trang
     */
    PaginatedResponse<SysGroupDto> getList(int page, int size);

    /**
     * Lấy tất cả Groups (không phân trang)
     */
    List<SysGroupDto> getAll();

    /**
     * Lấy Group theo ID
     */
    SysGroupDto getById(Long id);

    /**
     * Lấy Group theo code
     */
    SysGroupDto getByCode(String groupCode);

    /**
     * Tạo Group mới
     */
    SysGroupDto create(CreateSysGroupDto dto);

    /**
     * Update Group (chỉ update translation: name, description)
     */
    SysGroupDto update(Long id, UpdateSysGroupDto dto);

    /**
     * Xóa Group
     */
    void delete(Long id);

    /**
     * Lấy danh sách Functions của một Group
     */
    List<SysFunctionDto> getFunctionsByGroupId(Long groupId);

    /**
     * Lấy danh sách Functions của một Group với phân trang
     */
    PaginatedResponse<SysFunctionDto> getFunctionsByGroupId(Long groupId, int page, int size);

    /**
     * Lấy danh sách Clients của một Group
     */
    List<ClientDto> getClientsByGroupId(Long groupId);

    /**
     * Lấy danh sách Clients của một Group với phân trang
     */
    PaginatedResponse<ClientDto> getClientsByGroupId(Long groupId, int page, int size);

    /**
     * Đếm tổng số Groups
     */
    long count();

    /**
     * Đếm số Functions của một Group
     */
    long countFunctionsByGroupId(Long groupId);

    /**
     * Đếm số Clients của một Group
     */
    long countClientsByGroupId(Long groupId);
}
