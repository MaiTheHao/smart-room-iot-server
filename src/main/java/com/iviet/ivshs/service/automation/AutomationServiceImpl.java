package com.iviet.ivshs.service.automation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.AirConditionDao;
import com.iviet.ivshs.dao.AutomationActionDao;
import com.iviet.ivshs.dao.AutomationDao;
import com.iviet.ivshs.dao.FanDao;
import com.iviet.ivshs.dao.LightDao;
import com.iviet.ivshs.dto.automation.AutomationActionDto;
import com.iviet.ivshs.dto.automation.AutomationDto;
import com.iviet.ivshs.dto.automation.CreateAutomationActionDto;
import com.iviet.ivshs.dto.automation.CreateAutomationDto;
import com.iviet.ivshs.dto.automation.UpdateAutomationActionDto;
import com.iviet.ivshs.dto.system.PaginatedResponse;
import com.iviet.ivshs.dto.automation.UpdateAutomationDto;
import com.iviet.ivshs.entities.Automation;
import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.scheduler.automation.AutomationProcessor;
import com.iviet.ivshs.shared.enumeration.JobTargetType;
import com.iviet.ivshs.shared.exception.BadRequestException;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.shared.util.CronExpressionUtil;
import com.iviet.ivshs.shared.util.LocalContextUtil;
import com.iviet.ivshs.shared.util.ScheduleUtil;

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
	private final FanDao fanDao;
	private final ScheduleUtil scheduleUtil;
	private final AutomationProcessor processor;

	@Override
	@Transactional
	public AutomationDto create(CreateAutomationDto dto) {
		log.info("Creating automation: name={}", dto.getName());

		if (automationDao.existsByName(dto.getName())) {
			throw new BadRequestException("Automation name already exists: " + dto.getName());
		}

		if (!CronExpressionUtil.isValid(dto.getCronExpression())) {
			throw new BadRequestException("Invalid cron expression: " + dto.getCronExpression());
		}

		Automation automation = CreateAutomationDto.toEntity(dto);
		automationDao.save(automation);

		scheduleUtil.sync(automation);

		log.info("Automation created successfully: id={}, name={}", automation.getId(), automation.getName());
		return AutomationDto.from(automation);
	}

	@Override
	@Transactional
	public AutomationDto update(Long automationId, UpdateAutomationDto dto) {
		log.info("Updating automation: id={}", automationId);

		Automation automation = automationDao.findById(automationId).orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));

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
		scheduleUtil.sync(automation);

		log.info("Automation updated successfully: id={}, name={}", automationId, automation.getName());
		return AutomationDto.from(automation);
	}

	@Override
	@Transactional
	public void delete(Long automationId) {
		log.info("Deleting automation: id={}", automationId);
		var automation = automationDao.findById(automationId).orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));
		scheduleUtil.delete(automation);
		automationDao.deleteById(automationId);
	}

	@Override
	public AutomationDto getById(Long automationId) {
		Automation automation = automationDao.findById(automationId).orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));
		return AutomationDto.from(automation);
	}

	@Override
	public PaginatedResponse<AutomationDto> getAll(int page, int size) {
		List<Automation> automations = automationDao.findAllPaginated(page, size);
		List<AutomationDto> dtos = automations.stream().map(AutomationDto::from).collect(Collectors.toList());

		long total = automationDao.countAll();
		return new PaginatedResponse<>(dtos, page, size, total);
	}

	@Override
	public List<AutomationDto> getAllActive() {
		return automationDao.findAllActive().stream().map(AutomationDto::from).collect(Collectors.toList());
	}

	@Override
	public List<AutomationActionDto> getActions(Long automationId) {
		Automation automation = automationDao.findByIdWithActions(automationId).orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));

		return automation.getActions().stream().map(action -> {
			String targetName = getTargetName(action.getTargetType(), action.getTargetId());
			return AutomationActionDto.from(action, targetName);
		}).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public AutomationActionDto addAction(Long automationId, CreateAutomationActionDto dto) {
		log.info("Adding action to automation: id={}", automationId);
		Automation automation = automationDao.findById(automationId).orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));

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
		log.info("Updating action: id={}", actionId);
		AutomationAction action = automationActionDao.findById(actionId).orElseThrow(() -> new NotFoundException("Action not found: " + actionId));

		if (dto.getTargetType() == null)
			throw new BadRequestException("Target type is required");

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
		log.info("Removing action: id={}", actionId);
		if (!automationActionDao.existsById(actionId)) {
			throw new NotFoundException("Action not found: " + actionId);
		}
		automationActionDao.deleteById(actionId);
	}

	@Override
	@Transactional
	public void toggleIsActive(Long automationId, boolean isActive) {
		log.info("Toggling automation status: id={}, isActive={}", automationId, isActive);
		Automation automation = automationDao.findById(automationId).orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));

		if (automation.getIsActive().equals(isActive))
			return;

		automation.setIsActive(isActive);
		automationDao.update(automation);
		scheduleUtil.sync(automation);
	}

	@Override
	public void scheduleJob(Automation automation) {
		scheduleUtil.sync(automation);
	}

	@Override
	public void rescheduleJob(Automation automation) {
		scheduleUtil.sync(automation);
	}

	@Override
	public void unscheduleJob(Long automationId) {
		var automation = automationDao.findById(automationId).orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));
		scheduleUtil.delete(automation);
	}

	@Override
	@Transactional
	public void executeAutomationLogic(Long automationId) {
		Automation automation = automationDao.findByIdWithActions(automationId).orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));

		if (Boolean.FALSE.equals(automation.getIsActive()))
			return;
		processor.process(automation);
	}

	@Override
	@Transactional
	public void executeAutomationImmediately(Long automationId) {
		Automation automation = automationDao.findById(automationId).orElseThrow(() -> new NotFoundException("Automation not found: " + automationId));
		scheduleUtil.triggerNow(automation);
	}

	@Override
	@Transactional
	public void reloadAllAutomations() {
		scheduleUtil.deleteJobGroup(Automation.JOB_GROUP);
		List<Automation> activeAutomations = automationDao.findAllActive();
		for (Automation automation : activeAutomations) {
			try {
				scheduleUtil.sync(automation);
			} catch (Exception e) {
				log.error("Failed to reload automation: id={}", automation.getId(), e);
			}
		}
	}

	private void validateAction(JobTargetType targetType, Long targetId) {
		if (targetType == JobTargetType.LIGHT) {
			if (!lightDao.existsById(targetId)) {
				throw new BadRequestException("Light not found: " + targetId);
			}
		} else if (targetType == JobTargetType.AIR_CONDITION) {
			if (!airConditionDao.existsById(targetId)) {
				throw new BadRequestException("Air conditioner not found: " + targetId);
			}
		} else if (targetType == JobTargetType.FAN) {
			if (!fanDao.existsById(targetId)) {
				throw new BadRequestException("Fan not found: " + targetId);
			}
		}
	}

	private String getTargetName(JobTargetType targetType, Long targetId) {
		try {
			String langCode = LocalContextUtil.getCurrentLangCode();
			if (targetType == JobTargetType.LIGHT) {
				return lightDao.findById(targetId, langCode).map(light -> light.name()).orElse("Unknown Light");
			} else if (targetType == JobTargetType.AIR_CONDITION) {
				return airConditionDao.findById(targetId, langCode).map(ac -> ac.name()).orElse("Unknown Air Conditioner");
			} else if (targetType == JobTargetType.FAN) {
				return fanDao.findById(targetId, langCode).map(fan -> fan.name()).orElse("Unknown Fan");
			}

			return "Unknown Device";
		} catch (Exception e) {
			return "Unknown Device";
		}
	}
}
