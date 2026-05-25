import { RuleDataSource, ConditionOperator, ConditionLogic, DeviceCategory } from './enums';

export interface RuleDto {
  id: number;
  name: string;
  priority: number;
  isActive: boolean;
  intervalSeconds: number;
  conditions: RuleConditionDto[];
  actions: RuleActionDto[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateRuleDto {
  name: string;
  priority: number;
  intervalSeconds: number;
  conditions: CreateRuleConditionDto[];
  actions: CreateRuleActionDto[];
}

export interface UpdateRuleDto {
  name?: string;
  priority?: number;
  isActive?: boolean;
  intervalSeconds?: number;
  conditions?: UpdateRuleConditionDto[];
  actions?: UpdateRuleActionDto[];
}

export interface RuleConditionDto {
  id: number;
  sortOrder: number;
  dataSource: RuleDataSource;
  resourceParam: any;
  operator: ConditionOperator;
  value: string;
  nextLogic?: ConditionLogic;
}

export interface CreateRuleConditionDto {
  sortOrder: number;
  dataSource: RuleDataSource;
  resourceParam: any;
  operator: ConditionOperator;
  value: string;
  nextLogic?: ConditionLogic;
}

export interface UpdateRuleConditionDto {
  sortOrder: number;
  dataSource: RuleDataSource;
  resourceParam: any;
  operator: ConditionOperator;
  value: string;
  nextLogic?: ConditionLogic;
}

export interface RuleActionDto {
  id: number;
  targetDeviceId: number;
  targetDeviceCategory: DeviceCategory;
  actionParams: any;
  executionOrder: number;
}

export interface CreateRuleActionDto {
  targetDeviceId: number;
  targetDeviceCategory: DeviceCategory;
  actionParams: any;
  executionOrder: number;
}

export interface UpdateRuleActionDto {
  targetDeviceId?: number;
  targetDeviceCategory?: DeviceCategory;
  actionParams?: any;
  executionOrder?: number;
}

export interface UpdateRuleStatusDto {
  isActive: boolean;
}


