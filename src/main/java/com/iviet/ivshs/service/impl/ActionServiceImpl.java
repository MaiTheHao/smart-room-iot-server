package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.ActionDao;
import com.iviet.ivshs.dto.ActionDto;
import com.iviet.ivshs.dto.CreateActionDto;
import com.iviet.ivshs.dto.ReplaceActionDto;
import com.iviet.ivshs.entities.Action;
import com.iviet.ivshs.service.ActionService;
import com.iviet.ivshs.shared.enumeration.ActionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.shared.exception.NotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
  @Transactional(rollbackFor = Exception.class)
  public List<ActionDto> replaceByOwner(
      ActionOwnerCategory ownerCategory, String ownerId, List<ReplaceActionDto> items) {
    List<Action> existing = actionDao.findByOwner(ownerCategory, ownerId);
    if (items == null || items.isEmpty()) {
      existing.forEach(actionDao::delete);
      return List.of();
    }

    Map<Long, Action> existingById =
        existing.stream().collect(Collectors.toMap(Action::getId, Function.identity()));

    List<Long> requestedIds =
        items.stream().map(ReplaceActionDto::id).filter(Objects::nonNull).toList();
    if (new HashSet<>(requestedIds).size() != requestedIds.size()) {
      throw new BadRequestException("Duplicate action id in request");
    }

    Set<Long> incomingIds = new HashSet<>(requestedIds);
    existing.stream()
        .filter((action) -> !incomingIds.contains(action.getId()))
        .forEach(actionDao::delete);

    List<ActionDto> result = new ArrayList<>(items.size());
    for (ReplaceActionDto item : items) {
      if (item.id() != null && existingById.containsKey(item.id())) {
        Action managed = existingById.get(item.id());
        managed.setOwnerCategory(ownerCategory);
        managed.setOwnerId(ownerId);
        item.updateEntity(managed);
        result.add(ActionDto.fromEntity(managed));
      } else {
        result.add(ActionDto.fromEntity(actionDao.save(item.toEntity(ownerCategory, ownerId))));
      }
    }
    return result;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public List<ActionDto> replaceByOwner(
      ActionOwnerCategory ownerCategory, Long ownerId, List<ReplaceActionDto> items) {
    return replaceByOwner(ownerCategory, String.valueOf(ownerId), items);
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
