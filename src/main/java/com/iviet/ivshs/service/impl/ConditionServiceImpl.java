package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.ConditionDao;
import com.iviet.ivshs.dto.ConditionDto;
import com.iviet.ivshs.dto.CreateConditionDto;
import com.iviet.ivshs.dto.ReplaceConditionDto;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.service.ConditionService;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
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
public class ConditionServiceImpl implements ConditionService {

  private final ConditionDao conditionDao;

  @Override
  @Transactional
  public ConditionDto create(CreateConditionDto dto) {
    Condition condition = dto.toEntity();
    conditionDao.save(condition);
    return ConditionDto.fromEntity(condition);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    if (!conditionDao.existsById(id)) {
      throw new NotFoundException("Condition not found: " + id);
    }
    conditionDao.deleteById(id);
  }

  @Override
  public ConditionDto getById(Long id) {
    return conditionDao
        .findById(id)
        .map(ConditionDto::fromEntity)
        .orElseThrow(() -> new NotFoundException("Condition not found: " + id));
  }

  @Override
  public List<ConditionDto> findByOwner(ConditionOwnerCategory ownerCategory, String ownerId) {
    return conditionDao.findByOwner(ownerCategory, ownerId).stream()
        .map(ConditionDto::fromEntity)
        .toList();
  }

  @Override
  public List<ConditionDto> findByOwner(ConditionOwnerCategory ownerCategory, Long ownerId) {
    return conditionDao.findByOwner(ownerCategory, ownerId).stream()
        .map(ConditionDto::fromEntity)
        .toList();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public List<ConditionDto> replaceByOwner(
      ConditionOwnerCategory ownerCategory, String ownerId, List<ReplaceConditionDto> items) {
    List<Condition> existing = conditionDao.findByOwner(ownerCategory, ownerId);
    if (items == null || items.isEmpty()) {
      existing.forEach(conditionDao::delete);
      return List.of();
    }

    Map<Long, Condition> existingById =
        existing.stream().collect(Collectors.toMap(Condition::getId, Function.identity()));

    List<Long> requestedIds =
        items.stream().map(ReplaceConditionDto::id).filter(Objects::nonNull).toList();
    if (new HashSet<>(requestedIds).size() != requestedIds.size()) {
      throw new BadRequestException("Duplicate condition id in request");
    }
    Set<Long> requestedIdSet = new HashSet<>(requestedIds);

    existing.stream()
        .filter(e -> !requestedIdSet.contains(e.getId()))
        .forEach(conditionDao::delete);

    List<ConditionDto> result = new ArrayList<>(items.size());
    for (ReplaceConditionDto item : items) {
      if (item.id() != null && existingById.containsKey(item.id())) {
        Condition managed = existingById.get(item.id());
        managed.setOwnerCategory(ownerCategory);
        managed.setOwnerId(ownerId);
        item.updateEntity(managed);
        result.add(ConditionDto.fromEntity(managed));
      } else {
        result.add(
            ConditionDto.fromEntity(conditionDao.save(item.toEntity(ownerCategory, ownerId))));
      }
    }
    return result;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public List<ConditionDto> replaceByOwner(
      ConditionOwnerCategory ownerCategory, Long ownerId, List<ReplaceConditionDto> items) {
    return replaceByOwner(ownerCategory, String.valueOf(ownerId), items);
  }

  @Override
  @Transactional
  public int deleteByOwner(ConditionOwnerCategory ownerCategory, String ownerId) {
    return conditionDao.deleteByOwner(ownerCategory, ownerId);
  }

  @Override
  @Transactional
  public int deleteByOwner(ConditionOwnerCategory ownerCategory, Long ownerId) {
    return conditionDao.deleteByOwner(ownerCategory, ownerId);
  }

  @Override
  public List<ConditionDto> findBySource(
      ConditionDataSource sourceCategory, String sourceTargetId) {
    return conditionDao.findBySource(sourceCategory, sourceTargetId).stream()
        .map(ConditionDto::fromEntity)
        .toList();
  }

  @Override
  public List<ConditionDto> findBySource(
      ConditionDataSource sourceCategory, Long sourceTargetId) {
    return conditionDao.findBySource(sourceCategory, sourceTargetId).stream()
        .map(ConditionDto::fromEntity)
        .toList();
  }

  @Override
  public List<ConditionDto> findBySourceAndType(
      ConditionDataSource sourceCategory, String sourceTargetId, DeviceCategory sourceTargetType) {
    return conditionDao
        .findBySourceAndType(sourceCategory, sourceTargetId, sourceTargetType)
        .stream()
        .map(ConditionDto::fromEntity)
        .toList();
  }

  @Override
  public List<ConditionDto> findBySourceAndType(
      ConditionDataSource sourceCategory, Long sourceTargetId, DeviceCategory sourceTargetType) {
    return conditionDao
        .findBySourceAndType(sourceCategory, sourceTargetId, sourceTargetType)
        .stream()
        .map(ConditionDto::fromEntity)
        .toList();
  }

  @Override
  @Transactional
  public int deleteBySourceTarget(ConditionDataSource sourceCategory, String sourceTargetId) {
    return conditionDao.deleteBySourceTarget(sourceCategory, sourceTargetId);
  }

  @Override
  @Transactional
  public int deleteBySourceTarget(ConditionDataSource sourceCategory, Long sourceTargetId) {
    return conditionDao.deleteBySourceTarget(sourceCategory, sourceTargetId);
  }

  @Override
  @Transactional
  public int deleteBySourceTargetAndType(
      ConditionDataSource sourceCategory, String sourceTargetId, DeviceCategory sourceTargetType) {
    return conditionDao.deleteBySourceTargetAndType(
        sourceCategory, sourceTargetId, sourceTargetType);
  }

  @Override
  @Transactional
  public int deleteBySourceTargetAndType(
      ConditionDataSource sourceCategory, Long sourceTargetId, DeviceCategory sourceTargetType) {
    return conditionDao.deleteBySourceTargetAndType(
        sourceCategory, sourceTargetId, sourceTargetType);
  }
}
