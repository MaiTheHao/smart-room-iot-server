package com.iviet.ivshs.service.impl;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.automation.processor.AutomationProcessor;
import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dao.AutomationActionDao;
import com.iviet.ivshs.dao.AutomationDao;
import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dto.AutomationActionDto;
import com.iviet.ivshs.dto.AutomationDto;
import com.iviet.ivshs.dto.CreateAutomationActionDto;
import com.iviet.ivshs.dto.CreateAutomationDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateAutomationActionDto;
import com.iviet.ivshs.dto.UpdateAutomationDto;
import com.iviet.ivshs.entities.Automation;
import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.enumeration.JobTargetType;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.AutomationService;
import com.iviet.ivshs.util.CronExpressionUtil;
import com.iviet.ivshs.util.QuartzHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutomationServiceImpl implements AutomationService {

	private final AutomationDao automationDao;
	private final AutomationActionDao automationActionDao;
	private final LightDao lightDao;
	private final AirConditionDao airConditionDao;
	private final QuartzHelper quartzHelper;
	private final AutomationProcessor processor;

	@Override
	@Transactional
	public AutomationDto create(CreateAutomationDto dto) {
		log.info("Creating automation: {}", dto.getName());

		if (automationDao.existsByName(dto.getName())) {
			throw new BadRequestException("Automation name already exists: " + dto.getName());
		}

		if (!CronExpressionUtil.isValid(dto.getCronExpression())) {
			throw new BadRequestException("Invalid cron expression: " + dto.getCronExpression());
		}

		Automation automation = CreateAutomationDto.toEntity(dto);
		automationDao.save(automation);

		quartzHelper.sync(automation);

		log.info("Created automation ID: {}", automation.getId());
		return AutomationDto.from(automation);
	}

	@Override
	@Transactional
	public AutomationDto update(Long automationId, UpdateAutomationDto dto) {
		log.info("Updating automation ID: {}", automationId);

		Automation automation = automationDao.findById(automationId)
				.orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));

		if (!automation.getName().equals(dto.getName()) && automationDao.existsByNameAndIdNot(dto.getName(), automationId)) {
			throw new BadRequestException("Automation name already exists: " + dto.getName());
		}
		
		if (!CronExpressionUtil.isValid(dto.getCronExpression())) {
			throw new BadRequestException("Invalid cron expression: " + dto.getCronExpression());
		}

		automation.setName(dto.getName());
		automation.setCronExpression(dto.getCronExpression());
		if (dto.getIsActive() != null) {
			automation.setIsActive(dto.getIsActive());
		}
		automation.setDescription(dto.getDescription());

		automationDao.update(automation);
		quartzHelper.sync(automation);

		log.info("Updated automation ID: {}", automationId);
		return AutomationDto.from(automation);
	}

	@Override
	@Transactional
	public void delete(Long automationId) {
		log.info("Deleting automation ID: {}", automationId);
		var automation = automationDao.findById(automationId).orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));
		quartzHelper.delete(automation);
		automationDao.deleteById(automationId);
	}

	@Override
	public AutomationDto getById(Long automationId) {
		Automation automation = automationDao.findById(automationId)
				.orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));
		return AutomationDto.from(automation);
	}

	@Override
	public PaginatedResponse<AutomationDto> getAll(int page, int size) {
		List<Automation> automations = automationDao.findAllPaginated(page, size);
		List<AutomationDto> dtos = automations.stream()
				.map(AutomationDto::from)
				.collect(Collectors.toList());

		long total = automationDao.countAll();
		return new PaginatedResponse<>(dtos, page, size, total);
	}

	@Override
	public List<AutomationDto> getAllActive() {
		return automationDao.findAllActive().stream()
				.map(AutomationDto::from)
				.collect(Collectors.toList());
	}

	@Override
	public List<AutomationActionDto> getActions(Long automationId) {
		Automation automation = automationDao.findByIdWithActions(automationId)
				.orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));
		
		return automation.getActions().stream()
				.map(action -> {
					String targetName = getTargetName(action.getTargetType(), action.getTargetId());
					return AutomationActionDto.from(action, targetName);
				})
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public AutomationActionDto addAction(Long automationId, CreateAutomationActionDto dto) {
		log.info("Adding action to automation ID: {}", automationId);
		Automation automation = automationDao.findById(automationId)
				.orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));

		validateAction(dto.getTargetType(), dto.getTargetId());

		AutomationAction action = new AutomationAction();
		action.setAutomation(automation);
		action.setTargetType(dto.getTargetType());
		action.setTargetId(dto.getTargetId());
		action.setActionType(dto.getActionType());
		action.setParameterValue(dto.getParameterValue());
		action.setExecutionOrder(dto.getExecutionOrder() != null ? dto.getExecutionOrder() : 0);

		automationActionDao.save(action);

		String targetName = getTargetName(action.getTargetType(), action.getTargetId());
		return AutomationActionDto.from(action, targetName);
	}

	@Override
	@Transactional
	public AutomationActionDto updateAction(Long actionId, UpdateAutomationActionDto dto) {
		log.info("Updating action ID: {}", actionId);
		AutomationAction action = automationActionDao.findById(actionId)
				.orElseThrow(() -> new NotFoundException("Action not found: " + actionId));

		validateAction(dto.getTargetType(), dto.getTargetId());

		action.setTargetType(dto.getTargetType());
		action.setTargetId(dto.getTargetId());
		action.setActionType(dto.getActionType());
		action.setParameterValue(dto.getParameterValue());
		action.setExecutionOrder(dto.getExecutionOrder() != null ? dto.getExecutionOrder() : 0);

		automationActionDao.update(action);

		String targetName = getTargetName(action.getTargetType(), action.getTargetId());
		return AutomationActionDto.from(action, targetName);
	}

	@Override
	@Transactional
	public void removeAction(Long actionId) {
		log.info("Removing action ID: {}", actionId);
		if (!automationActionDao.existsById(actionId)) {
			throw new NotFoundException("Action not found: " + actionId);
		}
		automationActionDao.deleteById(actionId);
	}

	@Override
	@Transactional
	public void toggleIsActive(Long automationId, boolean isActive) {
		log.info("Toggle automation ID: {} to {}", automationId, isActive);
		Automation automation = automationDao.findById(automationId)
				.orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));

		if (automation.getIsActive().equals(isActive)) return;

		automation.setIsActive(isActive);
		automationDao.update(automation);
		quartzHelper.sync(automation);
	}

	@Override
	public void scheduleJob(Automation automation) {
		quartzHelper.sync(automation);
	}

	@Override
	public void rescheduleJob(Automation automation) {
		quartzHelper.sync(automation);
	}

	@Override
	public void unscheduleJob(Long automationId) {
		var automation = automationDao.findById(automationId).orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));
		quartzHelper.delete(automation);
	}

	@Override
	@Transactional
	public void executeAutomationLogic(Long automationId) {
		Automation automation = automationDao.findByIdWithActions(automationId)
				.orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));

		if (Boolean.FALSE.equals(automation.getIsActive())) return;
		processor.process(automation);
	}

	@Override
	@Transactional
	public void executeAutomationImmediately(Long automationId) {
		Automation automation = automationDao.findById(automationId)
				.orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));
		quartzHelper.triggerNow(automation);
	}

	@Override
	@Transactional
	public void reloadAllAutomations() {
		quartzHelper.deleteAllInGroup(Automation.JOB_GROUP);
		List<Automation> activeAutomations = automationDao.findAllActive(); 
		for (Automation automation : activeAutomations) {
			try {
				quartzHelper.sync(automation);
			} catch (Exception e) {
				log.error("Failed to reload automation ID {}: {}", automation.getId(), e.getMessage());
			}
		}
	}
	
	private void validateAction(JobTargetType targetType, Long targetId) {
		if (targetType == JobTargetType.LIGHT) {
			if (!lightDao.existsById(targetId)) {
				throw new BadRequestException("Light not found: " + targetId);
			}
		}
	}

	private String getTargetName(JobTargetType targetType, Long targetId) {
		try {
			if (targetType == JobTargetType.LIGHT) {
				return lightDao.findById(targetId)
						.map(light -> light.getNaturalId())
						.orElse("Unknown Light");
			} else if (targetType == JobTargetType.AIR_CONDITION) {
				return airConditionDao.findById(targetId)
						.map(ac -> ac.getNaturalId())
						.orElse("Unknown Air Conditioner");
			}
			
			return "Unknown Device";
		} catch (Exception e) {
			return "Unknown Device";
		}
	}
}
