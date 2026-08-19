package com.iviet.ivshs.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.ActionDao;
import com.iviet.ivshs.dto.ActionDto;
import com.iviet.ivshs.dto.CreateActionDto;
import com.iviet.ivshs.dto.UpdateActionDto;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.service.ActionService;
import com.iviet.ivshs.service.strategy.ActionExecutionService;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.shared.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActionServiceImpl implements ActionService {

  private final ActionDao actionDao;
  private final ActionExecutionService actionExecutionService;

  @Override
  @Transactional
  public ActionDto create(CreateActionDto dto) {
    validateTarget(dto.targetCategory(), dto.targetId(), dto.params());
    Action action = dto.toEntity();
    actionDao.save(action);
    return ActionDto.fromEntity(action);
  }

  @Override
  @Transactional
  public ActionDto update(Long id, UpdateActionDto dto) {
    Action action =
        actionDao.findById(id).orElseThrow(() -> new NotFoundException("Action not found: " + id));

    if (dto.targetCategory() != null && dto.targetId() != null && dto.params() != null) {
      validateTarget(dto.targetCategory(), dto.targetId(), dto.params());
    }

    dto.updateEntity(action);
    actionDao.update(action);
    return ActionDto.fromEntity(action);
  }

  private void validateTarget(DeviceCategory category, String targetId, JsonNode params) {
    try {
      actionExecutionService.validateActionParams(category, Long.parseLong(targetId), params);
    } catch (NumberFormatException e) {
      throw new BadRequestException("Invalid targetId format: " + targetId);
    }
  }

  @Override
  @Transactional
  public void delete(Long id) {
    if (!actionDao.existsById(id)) {
      throw new NotFoundException("Action not found: " + id);
    }
    actionDao.deleteById(id);
  }

  @Override
  public ActionDto getById(Long id) {
    return actionDao
        .findById(id)
        .map(ActionDto::fromEntity)
        .orElseThrow(() -> new NotFoundException("Action not found: " + id));
  }

  @Override
  public List<ActionDto> findByOwner(ActionOwnerCategory ownerCategory, String ownerId) {
    return actionDao.findByOwner(ownerCategory, ownerId).stream()
        .map(ActionDto::fromEntity)
        .toList();
  }

  @Override
  public List<ActionDto> findByOwner(ActionOwnerCategory ownerCategory, Long ownerId) {
    return actionDao.findByOwner(ownerCategory, ownerId).stream()
        .map(ActionDto::fromEntity)
        .toList();
  }

  @Override
  @Transactional
  public int deleteByOwner(ActionOwnerCategory ownerCategory, String ownerId) {
    return actionDao.deleteByOwner(ownerCategory, ownerId);
  }

  @Override
  @Transactional
  public int deleteByOwner(ActionOwnerCategory ownerCategory, Long ownerId) {
    return actionDao.deleteByOwner(ownerCategory, ownerId);
  }

  @Override
  public List<ActionDto> findByTarget(DeviceCategory targetCategory, String targetId) {
    return actionDao.findByTarget(targetCategory, targetId).stream()
        .map(ActionDto::fromEntity)
        .toList();
  }

  @Override
  public List<ActionDto> findByTarget(DeviceCategory targetCategory, Long targetId) {
    return actionDao.findByTarget(targetCategory, targetId).stream()
        .map(ActionDto::fromEntity)
        .toList();
  }

  @Override
  @Transactional
  public int deleteByTarget(DeviceCategory targetCategory, String targetId) {
    return actionDao.deleteByTarget(targetCategory, targetId);
  }

  @Override
  @Transactional
  public int deleteByTarget(DeviceCategory targetCategory, Long targetId) {
    return actionDao.deleteByTarget(targetCategory, targetId);
  }
}
