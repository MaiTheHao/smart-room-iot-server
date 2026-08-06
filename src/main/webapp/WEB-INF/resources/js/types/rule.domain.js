/**
 * Rule Engine Domain Types and Builders
 */

import { DeviceCategory } from '../constants/device.constants.js';
import { RuleDataSource, ConditionOperator, ConditionLogic } from '../constants/rule.constants.js';

export { DeviceCategory, RuleDataSource, ConditionOperator, ConditionLogic };

export class RuleConditionDto {
  constructor(builder) {
    this.id = builder._id;
    this.sortOrder = builder._sortOrder;
    this.dataSource = builder._dataSource;
    this.resourceParam = builder._resourceParam;
    this.operator = builder._operator;
    this.value = builder._value;
    this.nextLogic = builder._nextLogic;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setSortOrder(sortOrder) { this._sortOrder = sortOrder; return this; }
      setDataSource(dataSource) { this._dataSource = dataSource; return this; }
      setResourceParam(resourceParam) { this._resourceParam = resourceParam; return this; }
      setOperator(operator) { this._operator = operator; return this; }
      setValue(value) { this._value = value; return this; }
      setNextLogic(nextLogic) { this._nextLogic = nextLogic; return this; }
      build() { return new RuleConditionDto(this); }
    }
    return Builder;
  }
}

export class CreateRuleConditionDto {
  constructor(builder) {
    this.sortOrder = builder._sortOrder;
    this.dataSource = builder._dataSource;
    this.resourceParam = builder._resourceParam;
    this.operator = builder._operator;
    this.value = builder._value;
    this.nextLogic = builder._nextLogic;
  }

  static get Builder() {
    class Builder {
      setSortOrder(sortOrder) { this._sortOrder = sortOrder; return this; }
      setDataSource(dataSource) { this._dataSource = dataSource; return this; }
      setResourceParam(resourceParam) { this._resourceParam = resourceParam; return this; }
      setOperator(operator) { this._operator = operator; return this; }
      setValue(value) { this._value = value; return this; }
      setNextLogic(nextLogic) { this._nextLogic = nextLogic; return this; }
      build() { return new CreateRuleConditionDto(this); }
    }
    return Builder;
  }
}

export class UpdateRuleConditionDto {
  constructor(builder) {
    this.sortOrder = builder._sortOrder;
    this.dataSource = builder._dataSource;
    this.resourceParam = builder._resourceParam;
    this.operator = builder._operator;
    this.value = builder._value;
    this.nextLogic = builder._nextLogic;
  }

  static get Builder() {
    class Builder {
      setSortOrder(sortOrder) { this._sortOrder = sortOrder; return this; }
      setDataSource(dataSource) { this._dataSource = dataSource; return this; }
      setResourceParam(resourceParam) { this._resourceParam = resourceParam; return this; }
      setOperator(operator) { this._operator = operator; return this; }
      setValue(value) { this._value = value; return this; }
      setNextLogic(nextLogic) { this._nextLogic = nextLogic; return this; }
      build() { return new UpdateRuleConditionDto(this); }
    }
    return Builder;
  }
}

export class RuleActionDto {
  constructor(builder) {
    this.id = builder._id;
    this.targetDeviceId = builder._targetDeviceId;
    this.targetDeviceCategory = builder._targetDeviceCategory;
    this.actionParams = builder._actionParams;
    this.executionOrder = builder._executionOrder;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setTargetDeviceId(targetDeviceId) { this._targetDeviceId = targetDeviceId; return this; }
      setTargetDeviceCategory(targetDeviceCategory) { this._targetDeviceCategory = targetDeviceCategory; return this; }
      setActionParams(actionParams) { this._actionParams = actionParams; return this; }
      setExecutionOrder(executionOrder) { this._executionOrder = executionOrder; return this; }
      build() { return new RuleActionDto(this); }
    }
    return Builder;
  }
}

export class CreateRuleActionDto {
  constructor(builder) {
    this.targetDeviceId = builder._targetDeviceId;
    this.targetDeviceCategory = builder._targetDeviceCategory;
    this.actionParams = builder._actionParams;
    this.executionOrder = builder._executionOrder;
  }

  static get Builder() {
    class Builder {
      setTargetDeviceId(targetDeviceId) { this._targetDeviceId = targetDeviceId; return this; }
      setTargetDeviceCategory(targetDeviceCategory) { this._targetDeviceCategory = targetDeviceCategory; return this; }
      setActionParams(actionParams) { this._actionParams = actionParams; return this; }
      setExecutionOrder(executionOrder) { this._executionOrder = executionOrder; return this; }
      build() { return new CreateRuleActionDto(this); }
    }
    return Builder;
  }
}

export class UpdateRuleActionDto {
  constructor(builder) {
    this.targetDeviceId = builder._targetDeviceId;
    this.targetDeviceCategory = builder._targetDeviceCategory;
    this.actionParams = builder._actionParams;
    this.executionOrder = builder._executionOrder;
  }

  static get Builder() {
    class Builder {
      setTargetDeviceId(targetDeviceId) { this._targetDeviceId = targetDeviceId; return this; }
      setTargetDeviceCategory(targetDeviceCategory) { this._targetDeviceCategory = targetDeviceCategory; return this; }
      setActionParams(actionParams) { this._actionParams = actionParams; return this; }
      setExecutionOrder(executionOrder) { this._executionOrder = executionOrder; return this; }
      build() { return new UpdateRuleActionDto(this); }
    }
    return Builder;
  }
}

export class RuleDto {
  constructor(builder) {
    this.id = builder._id;
    this.name = builder._name;
    this.priority = builder._priority;
    this.isActive = builder._isActive;
    this.intervalSeconds = builder._intervalSeconds;
    this.conditions = builder._conditions || [];
    this.actions = builder._actions || [];
    this.createdAt = builder._createdAt;
    this.updatedAt = builder._updatedAt;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setName(name) { this._name = name; return this; }
      setPriority(priority) { this._priority = priority; return this; }
      setIsActive(isActive) { this._isActive = isActive; return this; }
      setIntervalSeconds(intervalSeconds) { this._intervalSeconds = intervalSeconds; return this; }
      setConditions(conditions) { this._conditions = conditions; return this; }
      setActions(actions) { this._actions = actions; return this; }
      setCreatedAt(createdAt) { this._createdAt = createdAt; return this; }
      setUpdatedAt(updatedAt) { this._updatedAt = updatedAt; return this; }
      build() { return new RuleDto(this); }
    }
    return Builder;
  }
}

export class CreateRuleDto {
  constructor(builder) {
    this.name = builder._name;
    this.priority = builder._priority;
    this.intervalSeconds = builder._intervalSeconds;
    this.conditions = builder._conditions || [];
    this.actions = builder._actions || [];
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setPriority(priority) { this._priority = priority; return this; }
      setIntervalSeconds(intervalSeconds) { this._intervalSeconds = intervalSeconds; return this; }
      setConditions(conditions) { this._conditions = conditions; return this; }
      setActions(actions) { this._actions = actions; return this; }
      build() { return new CreateRuleDto(this); }
    }
    return Builder;
  }
}

export class UpdateRuleDto {
  constructor(builder) {
    this.name = builder._name;
    this.priority = builder._priority;
    this.isActive = builder._isActive;
    this.intervalSeconds = builder._intervalSeconds;
    this.conditions = builder._conditions;
    this.actions = builder._actions;
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setPriority(priority) { this._priority = priority; return this; }
      setIsActive(isActive) { this._isActive = isActive; return this; }
      setIntervalSeconds(intervalSeconds) { this._intervalSeconds = intervalSeconds; return this; }
      setConditions(conditions) { this._conditions = conditions; return this; }
      setActions(actions) { this._actions = actions; return this; }
      build() { return new UpdateRuleDto(this); }
    }
    return Builder;
  }
}

export class UpdateRuleStatusDto {
  constructor(builder) {
    this.isActive = builder._isActive;
  }

  static get Builder() {
    class Builder {
      setIsActive(isActive) { this._isActive = isActive; return this; }
      build() { return new UpdateRuleStatusDto(this); }
    }
    return Builder;
  }
}
