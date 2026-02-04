package com.iviet.ivshs.dto;

import com.iviet.ivshs.entities.Automation;
import com.iviet.ivshs.entities.AutomationAction;
import com.iviet.ivshs.enumeration.JobActionType;
import com.iviet.ivshs.enumeration.JobTargetType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutomationActionDto {
	private Long id;
	private Long automationId;
	private JobTargetType targetType;
	private Long targetId;
	private JobActionType actionType;
	private String parameterValue;
	private Integer executionOrder;
	private String targetName;

	public static AutomationActionDto from(AutomationAction action, String targetName) {
		if (action == null) return null;
		AutomationActionDto dto = new AutomationActionDto();
		dto.setId(action.getId());
		dto.setAutomationId(action.getAutomation().getId());
		dto.setTargetType(action.getTargetType());
		dto.setTargetId(action.getTargetId());
		dto.setActionType(action.getActionType());
		dto.setParameterValue(action.getParameterValue());
		dto.setExecutionOrder(action.getExecutionOrder());
		dto.setTargetName(targetName);
		return dto;
	}

	public static AutomationAction toEntity(Automation automation, AutomationActionDto dto) {
		if (dto == null) return null;
		AutomationAction action = new AutomationAction();
		action.setId(dto.getId());
		action.setAutomation(automation);
		action.setTargetType(dto.getTargetType());
		action.setTargetId(dto.getTargetId());
		action.setActionType(dto.getActionType());
		action.setParameterValue(dto.getParameterValue());
		action.setExecutionOrder(dto.getExecutionOrder());
		return action;
	}
}
