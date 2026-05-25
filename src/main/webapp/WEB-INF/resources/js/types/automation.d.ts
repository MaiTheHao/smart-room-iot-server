import { JobTargetType, JobActionType } from './enums';

export interface AutomationDto {
  id: number;
  name: string;
  description?: string;
  cronExpression: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAutomationDto {
  name: string;
  description?: string;
  cronExpression: string;
  isActive?: boolean;
}

export interface UpdateAutomationDto {
  name?: string;
  description?: string;
  cronExpression?: string;
  isActive?: boolean;
}

export interface AutomationActionDto {
  id: number;
  automationId: number;
  targetType: JobTargetType;
  targetId: number;
  actionType: JobActionType;
  parameterValue?: string;
  executionOrder: number;
  targetName?: string;
}

export interface CreateAutomationActionDto {
  targetType: JobTargetType;
  targetId: number;
  actionType: JobActionType;
  parameterValue?: string;
  executionOrder: number;
}

export interface UpdateAutomationActionDto {
  targetType: JobTargetType;
  targetId: number;
  actionType: JobActionType;
  parameterValue?: string;
  executionOrder?: number;
}

