package com.iviet.ivshs.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.quartz.CronScheduleBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.component.AutomationProcessor;
import com.iviet.ivshs.dao.AutomationDao;
import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dto.AutomationDto;
import com.iviet.ivshs.dto.CreateAutomationDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateAutomationDto;
import com.iviet.ivshs.entities.Automation;
import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.enumeration.JobTargetType;
import com.iviet.ivshs.exception.domain.BadRequestException;
import com.iviet.ivshs.exception.domain.NotFoundException;
import com.iviet.ivshs.service.AutomationService;
import com.iviet.ivshs.util.QuartzHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutomationServiceImpl implements AutomationService {

	private final AutomationDao automationDao;
	private final LightDao lightDao;
	private final QuartzHelper quartzHelper;
	private final AutomationProcessor processor;

	@Override
	@Transactional
	public AutomationDto createAutomation(CreateAutomationDto dto) {
		log.info("Creating automation: {}", dto.getName());

		if (automationDao.existsByName(dto.getName())) throw new BadRequestException("Automation name already exists: " + dto.getName());

		validateCronExpression(dto.getCronExpression());

		Automation automation = new Automation();
		automation.setName(dto.getName());
		automation.setCronExpression(dto.getCronExpression());
		automation.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
		automation.setDescription(dto.getDescription());

		for (CreateAutomationDto.AutomationActionDto actionDto : dto.getActions()) {
			validateAction(actionDto.getTargetType(), actionDto.getTargetId());

			AutomationAction action = new AutomationAction();
			action.setTargetType(actionDto.getTargetType());
			action.setTargetId(actionDto.getTargetId());
			action.setActionType(actionDto.getActionType());
			action.setParameterValue(actionDto.getParameterValue());
			action.setExecutionOrder(actionDto.getExecutionOrder() != null ? actionDto.getExecutionOrder() : 0);
			automation.addAction(action);
		}

		automationDao.save(automation);

		if (Boolean.TRUE.equals(automation.getIsActive())) {
			quartzHelper.schedule(automation);
		}

		log.info("Created automation ID: {}", automation.getId());
		return mapToDto(automation);
	}

	@Override
	@Transactional
	public AutomationDto updateAutomation(Long automationId, UpdateAutomationDto dto) {
		log.info("Updating automation ID: {}", automationId);

		Automation automation = automationDao.findByIdWithActions(automationId)
				.orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));

		if (!automation.getName().equals(dto.getName()) && automationDao.existsByNameAndIdNot(dto.getName(), automationId)) throw new BadRequestException("Automation name already exists: " + dto.getName());
		validateCronExpression(dto.getCronExpression());

		automation.setName(dto.getName());
		automation.setCronExpression(dto.getCronExpression());
		if (dto.getIsActive() != null) {
			automation.setIsActive(dto.getIsActive());
		}
		automation.setDescription(dto.getDescription());

		if (dto.getActions() != null && !dto.getActions().isEmpty()) {
			automation.getActions().clear();
			for (UpdateAutomationDto.AutomationActionDto actionDto : dto.getActions()) {
				validateAction(actionDto.getTargetType(), actionDto.getTargetId());

				AutomationAction action = new AutomationAction();
				action.setTargetType(actionDto.getTargetType());
				action.setTargetId(actionDto.getTargetId());
				action.setActionType(actionDto.getActionType());
				action.setParameterValue(actionDto.getParameterValue());
				action.setExecutionOrder(actionDto.getExecutionOrder() != null ? actionDto.getExecutionOrder() : 0);
				automation.addAction(action);
			}
		}

		automationDao.update(automation);
		quartzHelper.sync(automation);

		log.info("Updated automation ID: {}", automationId);
		return mapToDto(automation);
	}

	@Override
	@Transactional
	public void deleteAutomation(Long automationId) {
		log.info("Deleting automation ID: {}", automationId);

		if (!automationDao.existsById(automationId)) {
			throw new NotFoundException("Automation not found: " + automationId);
		}

		quartzHelper.delete(automationId);
		automationDao.deleteById(automationId);

		log.info("Deleted automation ID: {}", automationId);
	}

	@Override
	public AutomationDto getAutomationById(Long automationId) {
		Automation automation = automationDao.findByIdWithActions(automationId)
				.orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));
		return mapToDto(automation);
	}

	@Override
	public PaginatedResponse<AutomationDto> getAllAutomations(int page, int size) {
		List<Automation> automations = automationDao.findAllPaginated(page, size);
		List<AutomationDto> dtos = automations.stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());

		long total = automationDao.countAll();
		return new PaginatedResponse<>(dtos, page, size, total);
	}

	@Override
	public List<AutomationDto> getAllActiveAutomations() {
		return automationDao.findAllActiveWithActions().stream()
				.map(this::mapToDto)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void toggleAutomationStatus(Long automationId, boolean isActive) {
		log.info("Toggle automation ID: {} to {}", automationId, isActive);

		Automation automation = automationDao.findByIdWithActions(automationId)
				.orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));

		if (automation.getIsActive().equals(isActive)) {
			return;
		}

		automation.setIsActive(isActive);
		automationDao.update(automation);
		quartzHelper.sync(automation);

		log.info("Toggled automation ID: {} to {}", automationId, isActive);
	}

	@Override
	public void scheduleJob(Automation automation) {
		quartzHelper.schedule(automation);
	}

	@Override
	public void rescheduleJob(Automation automation) {
		quartzHelper.sync(automation);
	}

	@Override
	public void unscheduleJob(Long automationId) {
		quartzHelper.delete(automationId);
	}

	@Override
	@Transactional
	public void executeAutomationLogic(Long automationId) {
		log.info("Executing automation logic for ID: {}", automationId);

		Automation automation = automationDao.findByIdWithActions(automationId)
				.orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));

		if (Boolean.FALSE.equals(automation.getIsActive())) {
			log.warn("Automation is inactive, skipping execution: {}", automationId);
			return;
		}

		processor.process(automation);

		log.info("Completed automation logic for ID: {}", automationId);
	}

	@Override
	@Transactional
	public void executeAutomationImmediately(Long automationId) {
		log.info("Manual execution for automation ID: {}", automationId);
		executeAutomationLogic(automationId);
	}

	@Override
	@Transactional
	public void reloadAllAutomations() {
		log.info("Reloading all active automations...");

		quartzHelper.deleteAllInGroup();

		List<Automation> activeAutomations = automationDao.findAllActiveWithActions();
		for (Automation automation : activeAutomations) {
			try {
				quartzHelper.schedule(automation);
			} catch (Exception e) {
				log.error("Failed to reload automation ID {}: {}", automation.getId(), e.getMessage(), e);
			}
		}

		log.info("Reloaded {} automations", activeAutomations.size());
	}

	private void validateAction(JobTargetType targetType, Long targetId) {
		if (targetType == JobTargetType.LIGHT) {
			if (!lightDao.existsById(targetId)) {
				throw new BadRequestException("Light not found: " + targetId);
			}
		}
	}

	private void validateCronExpression(String cronExpression) {
		try {
			CronScheduleBuilder.cronSchedule(cronExpression);
		} catch (Exception e) {
			throw new BadRequestException("Invalid cron expression: " + e.getMessage());
		}
	}

	private AutomationDto mapToDto(Automation automation) {
		List<AutomationDto.AutomationActionDto> actionDtos = automation.getActions().stream()
				.map(action -> {
					String targetName = getTargetName(action.getTargetType(), action.getTargetId());
					return new AutomationDto.AutomationActionDto(
							action.getId(),
							action.getTargetType(),
							action.getTargetId(),
							action.getActionType(),
							action.getParameterValue(),
							action.getExecutionOrder(),
							targetName);
				})
				.collect(Collectors.toList());

		return new AutomationDto(
				automation.getId(),
				automation.getName(),
				automation.getCronExpression(),
				automation.getIsActive(),
				automation.getDescription(),
				automation.getCreatedAt() != null
						? LocalDateTime.ofInstant(automation.getCreatedAt(), ZoneId.systemDefault())
						: null,
				automation.getUpdatedAt() != null
						? LocalDateTime.ofInstant(automation.getUpdatedAt(), ZoneId.systemDefault())
						: null,
				actionDtos);
	}

	private String getTargetName(JobTargetType targetType, Long targetId) {
		try {
			if (targetType == JobTargetType.LIGHT) {
				return lightDao.findById(targetId)
						.map(light -> light.getNaturalId())
						.orElse("Unknown Light");
			}
			return "Unknown Device";
		} catch (Exception e) {
			return "Unknown Device";
		}
	}
}