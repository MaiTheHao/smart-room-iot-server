package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.*;

import java.util.List;

public interface SysGroupServiceV1 {
    
    /**
     * Lấy danh sách Groups với phân trang
     */
    PaginatedResponseV1<SysGroupDtoV1> getList(int page, int size);

    /**
     * Lấy tất cả Groups (không phân trang)
     */
    List<SysGroupDtoV1> getAll();

    /**
     * Lấy Group theo ID
     */
    SysGroupDtoV1 getById(Long id);

    /**
     * Lấy Group theo code
     */
    SysGroupDtoV1 getByCode(String groupCode);

    /**
     * Tạo Group mới
     */
    SysGroupDtoV1 create(CreateSysGroupDtoV1 dto);

    /**
     * Update Group (chỉ update translation: name, description)
     */
    SysGroupDtoV1 update(Long id, UpdateSysGroupDtoV1 dto);

    /**
     * Xóa Group
     */
    void delete(Long id);

    /**
     * Lấy danh sách Functions của một Group
     */
    List<SysFunctionDtoV1> getFunctionsByGroupId(Long groupId);

    /**
     * Lấy danh sách Functions của một Group với phân trang
     */
    PaginatedResponseV1<SysFunctionDtoV1> getFunctionsByGroupId(Long groupId, int page, int size);

    /**
     * Lấy danh sách Clients của một Group
     */
    List<ClientDtoV1> getClientsByGroupId(Long groupId);

    /**
     * Lấy danh sách Clients của một Group với phân trang
     */
    PaginatedResponseV1<ClientDtoV1> getClientsByGroupId(Long groupId, int page, int size);

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
