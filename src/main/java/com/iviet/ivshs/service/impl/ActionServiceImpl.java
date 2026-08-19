package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.ActionDao;
import com.iviet.ivshs.dto.ActionDto;
import com.iviet.ivshs.dto.CreateActionDto;
import com.iviet.ivshs.dto.UpdateActionDto;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.service.ActionService;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActionServiceImpl implements ActionService {

  private final ActionDao actionDao;

  @Override
  @Transactional
  public ActionDto create(CreateActionDto dto) {
    Action action = dto.toEntity();
    actionDao.save(action);
    return ActionDto.fromEntity(action);
  }

  @Override
  @Transactional
  public ActionDto update(Long id, UpdateActionDto dto) {
    Action action =
        actionDao.findById(id).orElseThrow(() -> new NotFoundException("Action not found: " + id));

    dto.updateEntity(action);
    actionDao.update(action);
    return ActionDto.fromEntity(action);
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
