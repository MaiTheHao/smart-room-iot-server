package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.*;

import java.util.List;

public interface SysFunctionService {
    
    PaginatedResponse<SysFunctionDto> getList(int page, int size);

    List<SysFunctionDto> getAll();

    SysFunctionDto getById(Long id);

    SysFunctionDto getByCode(String functionCode);

    List<SysFunctionWithGroupStatusDto> getAllWithGroupStatus(Long groupId);

    SysFunctionDto create(CreateSysFunctionDto dto);

    SysFunctionDto update(Long id, UpdateSysFunctionDto dto);

    void delete(Long id);

    long count();
}
