package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.*;

import java.util.List;

public interface SysFunctionServiceV1 {
    
    /**
     * Lấy danh sách Functions với phân trang
     */
    PaginatedResponse<SysFunctionDto> getList(int page, int size);

    /**
     * Lấy tất cả Functions (không phân trang)
     */
    List<SysFunctionDto> getAll();

    /**
     * Lấy Function theo ID
     */
    SysFunctionDto getById(Long id);

    /**
     * Lấy Function theo code
     */
    SysFunctionDto getByCode(String functionCode);

    /**
     * Lấy tất cả Functions với trạng thái assigned vào Group
     * Dùng để hiển thị UI khi chọn functions cho group
     */
    List<SysFunctionWithGroupStatusDto> getAllWithGroupStatus(Long groupId);

    /**
     * Tạo Function mới
     */
    SysFunctionDto create(CreateSysFunctionDto dto);

    /**
     * Update Function (chỉ update translation: name, description)
     */
    SysFunctionDto update(Long id, UpdateSysFunctionDto dto);

    /**
     * Xóa Function (cascade delete SysRoles)
     */
    void delete(Long id);

    /**
     * Đếm tổng số Functions
     */
    long count();
}
