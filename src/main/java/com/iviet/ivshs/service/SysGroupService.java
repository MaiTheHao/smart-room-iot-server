package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.ClientDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.SysFunctionDto;
import com.iviet.ivshs.dto.CreateSysGroupDto;
import com.iviet.ivshs.dto.SysGroupDto;
import com.iviet.ivshs.dto.SysGroupWithClientStatusDto;
import com.iviet.ivshs.dto.UpdateSysGroupDto;
import com.iviet.ivshs.entities.SysGroup;

import java.util.List;

public interface SysGroupService {

  List<SysGroupDto> getAll();

  PaginatedResponse<SysGroupDto> getList(int page, int size);

  SysGroupDto getById(Long id);

  SysGroupDto getByCode(String groupCode);

  List<SysGroupDto> getAllByClientId(Long clientId);

  PaginatedResponse<SysGroupDto> getListByClientId(Long clientId, int page, int size);

  List<SysGroup> getAllGroupsByClientId(Long clientId);

  PaginatedResponse<SysGroup> getListGroupsByClientId(Long clientId, int page, int size);

  List<SysFunctionDto> getAllFunctionsByGroupId(Long groupId);

  PaginatedResponse<SysFunctionDto> getListFunctionsByGroupId(Long groupId, int page, int size);

  List<ClientDto> getAllClientsByGroupId(Long groupId);

  PaginatedResponse<ClientDto> getListClientsByGroupId(Long groupId, int page, int size);

  List<SysGroupWithClientStatusDto> getAllWithClientStatus(Long clientId);

  SysGroupDto create(CreateSysGroupDto dto);

  SysGroupDto update(Long id, UpdateSysGroupDto dto);

  void delete(Long id);

  long count();

  long countByClientId(Long clientId);

  long countFunctionsByGroupId(Long groupId);

  long countClientsByGroupId(Long groupId);
}
