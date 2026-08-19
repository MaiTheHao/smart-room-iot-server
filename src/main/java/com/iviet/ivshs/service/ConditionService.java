package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.ConditionDto;
import com.iviet.ivshs.dto.CreateConditionDto;
import com.iviet.ivshs.dto.UpdateConditionDto;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.util.List;

public interface ConditionService {

  ConditionDto create(CreateConditionDto dto);

  ConditionDto update(Long id, UpdateConditionDto dto);

  void delete(Long id);

  ConditionDto getById(Long id);

  List<ConditionDto> findByOwner(ConditionOwnerCategory ownerCategory, String ownerId);

  List<ConditionDto> findByOwner(ConditionOwnerCategory ownerCategory, Long ownerId);

  int deleteByOwner(ConditionOwnerCategory ownerCategory, String ownerId);

  int deleteByOwner(ConditionOwnerCategory ownerCategory, Long ownerId);

  List<ConditionDto> findBySource(ConditionDataSource sourceCategory, String sourceTargetId);

  List<ConditionDto> findBySource(ConditionDataSource sourceCategory, Long sourceTargetId);

  List<ConditionDto> findBySourceAndType(
      ConditionDataSource sourceCategory, String sourceTargetId, DeviceCategory sourceTargetType);

  List<ConditionDto> findBySourceAndType(
      ConditionDataSource sourceCategory, Long sourceTargetId, DeviceCategory sourceTargetType);

  int deleteBySourceTarget(ConditionDataSource sourceCategory, String sourceTargetId);

  int deleteBySourceTarget(ConditionDataSource sourceCategory, Long sourceTargetId);

  int deleteBySourceTargetAndType(
      ConditionDataSource sourceCategory, String sourceTargetId, DeviceCategory sourceTargetType);

  int deleteBySourceTargetAndType(
      ConditionDataSource sourceCategory, Long sourceTargetId, DeviceCategory sourceTargetType);
}
