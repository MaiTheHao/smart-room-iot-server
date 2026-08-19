package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.ActionDto;
import com.iviet.ivshs.dto.CreateActionDto;
import com.iviet.ivshs.dto.UpdateActionDto;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.util.List;

public interface ActionService {

  ActionDto create(CreateActionDto dto);

  ActionDto update(Long id, UpdateActionDto dto);

  void delete(Long id);

  ActionDto getById(Long id);

  List<ActionDto> findByOwner(ActionOwnerCategory ownerCategory, String ownerId);

  List<ActionDto> findByOwner(ActionOwnerCategory ownerCategory, Long ownerId);

  int deleteByOwner(ActionOwnerCategory ownerCategory, String ownerId);

  int deleteByOwner(ActionOwnerCategory ownerCategory, Long ownerId);

  List<ActionDto> findByTarget(DeviceCategory targetCategory, String targetId);

  List<ActionDto> findByTarget(DeviceCategory targetCategory, Long targetId);

  int deleteByTarget(DeviceCategory targetCategory, String targetId);

  int deleteByTarget(DeviceCategory targetCategory, Long targetId);
}
