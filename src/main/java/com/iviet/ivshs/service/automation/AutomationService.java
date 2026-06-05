package com.iviet.ivshs.service.automation;

import java.util.List;

import com.iviet.ivshs.dto.automation.AutomationActionDto;
import com.iviet.ivshs.dto.automation.AutomationDto;
import com.iviet.ivshs.dto.automation.CreateAutomationActionDto;
import com.iviet.ivshs.dto.automation.CreateAutomationDto;
import com.iviet.ivshs.dto.automation.UpdateAutomationActionDto;
import com.iviet.ivshs.dto.automation.UpdateAutomationDto;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.entities.Automation;

public interface AutomationService {

	// AUTOMATION MANAGEMENT
	AutomationDto create(CreateAutomationDto dto);

	AutomationDto update(Long automationId, UpdateAutomationDto dto);

	void delete(Long automationId);

	AutomationDto getById(Long automationId);

	PaginatedResponse<AutomationDto> getAll(int page, int size);

	List<AutomationDto> getAllActive();

	// ACTION MANAGEMENT
	List<AutomationActionDto> getActions(Long automationId);

	AutomationActionDto addAction(Long automationId, CreateAutomationActionDto dto);

	AutomationActionDto updateAction(Long actionId, UpdateAutomationActionDto dto);

	void removeAction(Long actionId);

	// SYSTEM / JOB CONTROL
	void toggleIsActive(Long automationId, boolean isActive);

	void scheduleJob(Automation automation);

	void rescheduleJob(Automation automation);

	void unscheduleJob(Long automationId);

	void executeAutomationLogic(Long automationId);

	void executeAutomationImmediately(Long automationId);

	void reloadAllAutomations();
}
