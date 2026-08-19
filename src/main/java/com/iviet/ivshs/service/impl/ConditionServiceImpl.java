package com.iviet.ivshs.service.impl;

import com.iviet.ivshs.dao.ConditionDao;
import com.iviet.ivshs.dto.ConditionDto;
import com.iviet.ivshs.dto.CreateConditionDto;
import com.iviet.ivshs.dto.UpdateConditionDto;
import com.iviet.ivshs.entities.Condition;
import com.iviet.ivshs.service.ConditionService;
import com.iviet.ivshs.shared.enumeration.ConditionDataSource;
import com.iviet.ivshs.shared.enumeration.ConditionOwnerCategory;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.NotFoundException;
import java.util.List;
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
  public ConditionDto update(Long id, UpdateConditionDto dto) {
    Condition condition = conditionDao
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Condition not found: " + id));

    dto.updateEntity(condition);
    conditionDao.update(condition);
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
  public List<ConditionDto> findBySource(ConditionDataSource sourceCategory, Long sourceTargetId) {
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
