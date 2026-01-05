package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.*;

import java.util.List;

public interface SysFunctionServiceV1 {
    
    /**
     * Lấy danh sách Functions với phân trang
     */
    PaginatedResponseV1<SysFunctionDtoV1> getList(int page, int size);

    /**
     * Lấy tất cả Functions (không phân trang)
     */
    List<SysFunctionDtoV1> getAll();

    /**
     * Lấy Function theo ID
     */
    SysFunctionDtoV1 getById(Long id);

    /**
     * Lấy Function theo code
     */
    SysFunctionDtoV1 getByCode(String functionCode);

    /**
     * Lấy tất cả Functions với trạng thái assigned vào Group
     * Dùng để hiển thị UI khi chọn functions cho group
     */
    List<SysFunctionWithGroupStatusDtoV1> getAllWithGroupStatus(Long groupId);

    /**
     * Tạo Function mới
     */
    SysFunctionDtoV1 create(CreateSysFunctionDtoV1 dto);

    /**
     * Update Function (chỉ update translation: name, description)
     */
    SysFunctionDtoV1 update(Long id, UpdateSysFunctionDtoV1 dto);

    /**
     * Xóa Function (cascade delete SysRoles)
     */
    void delete(Long id);

    /**
     * Đếm tổng số Functions
     */
    long count();
}
