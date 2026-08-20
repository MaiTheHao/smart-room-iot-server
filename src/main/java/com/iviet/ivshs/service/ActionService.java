package com.iviet.ivshs.service;

import com.iviet.ivshs.dto.ActionDto;
import com.iviet.ivshs.dto.CreateActionDto;
import com.iviet.ivshs.dto.ReplaceActionDto;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import java.util.List;

public interface ActionService {

  ActionDto create(CreateActionDto dto);

  void delete(Long id);

  ActionDto getById(Long id);

  List<ActionDto> findByOwner(ActionOwnerCategory ownerCategory, String ownerId);

  List<ActionDto> findByOwner(ActionOwnerCategory ownerCategory, Long ownerId);

  List<ActionDto> replaceByOwner(
      ActionOwnerCategory ownerCategory, String ownerId, List<ReplaceActionDto> items);

  List<ActionDto> replaceByOwner(
      ActionOwnerCategory ownerCategory, Long ownerId, List<ReplaceActionDto> items);

  int deleteByOwner(ActionOwnerCategory ownerCategory, String ownerId);

  int deleteByOwner(ActionOwnerCategory ownerCategory, Long ownerId);

  List<ActionDto> findByTarget(DeviceCategory targetCategory, String targetId);

  List<ActionDto> findByTarget(DeviceCategory targetCategory, Long targetId);

  int deleteByTarget(DeviceCategory targetCategory, String targetId);

  int deleteByTarget(DeviceCategory targetCategory, Long targetId);
}
