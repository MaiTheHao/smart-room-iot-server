package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.*;

import java.util.List;

public interface SysFunctionService {

  List<SysFunctionDto> getAll();

  PaginatedResponse<SysFunctionDto> getList(int page, int size);

  SysFunctionDto getById(Long id);

  SysFunctionDto getByCode(String functionCode);

  List<SysFunctionDto> getAllByGroupId(Long groupId);

  PaginatedResponse<SysFunctionDto> getListByGroupId(Long groupId, int page, int size);

  List<SysFunctionDto> getAllByGroupCode(String groupCode);

  PaginatedResponse<SysFunctionDto> getListByGroupCode(String groupCode, int page, int size);

  List<SysFunctionDto> getAllByClientId(Long clientId);

  PaginatedResponse<SysFunctionDto> getListByClientId(Long clientId, int page, int size);

  List<SysFunctionWithGroupStatusDto> getAllWithGroupStatus(Long groupId);

  SysFunctionDto create(CreateSysFunctionDto dto);

  SysFunctionDto update(Long id, UpdateSysFunctionDto dto);

  void delete(Long id);

  long count();
}
