package com.iviet.ivshs.service;

import java.util.List;

import com.iviet.ivshs.dto.AutomationDto;
import com.iviet.ivshs.dto.CreateAutomationDto;
import com.iviet.ivshs.dto.PaginatedResponse;
import com.iviet.ivshs.dto.UpdateAutomationDto;
import com.iviet.ivshs.entities.Automation;

public interface AutomationService {
	
	AutomationDto createAutomation(CreateAutomationDto dto);
	
	AutomationDto updateAutomation(Long automationId, UpdateAutomationDto dto);

	void deleteAutomation(Long automationId);
	
	AutomationDto getAutomationById(Long automationId);
	
	PaginatedResponse<AutomationDto> getAllAutomations(int page, int size);

	List<AutomationDto> getAllActiveAutomations();
	
	void toggleAutomationStatus(Long automationId, boolean isActive);
	
	void scheduleJob(Automation automation);
	
	void rescheduleJob(Automation automation);
	
	void unscheduleJob(Long automationId);
	
	void executeAutomationLogic(Long automationId);

	void executeAutomationImmediately(Long automationId);

	void reloadAllAutomations();
}
