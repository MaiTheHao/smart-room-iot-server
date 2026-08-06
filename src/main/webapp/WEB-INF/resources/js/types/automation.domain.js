/**
 * Automation Cron Job Domain Types and Builders
 */

import { JobTargetType, JobActionType } from '../constants/automation.constants.js';

export { JobTargetType, JobActionType };

export class AutomationDto {
  constructor(builder) {
    this.id = builder._id;
    this.name = builder._name;
    this.description = builder._description;
    this.cronExpression = builder._cronExpression;
    this.isActive = builder._isActive;
    this.createdAt = builder._createdAt;
    this.updatedAt = builder._updatedAt;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setName(name) { this._name = name; return this; }
      setDescription(description) { this._description = description; return this; }
      setCronExpression(cronExpression) { this._cronExpression = cronExpression; return this; }
      setIsActive(isActive) { this._isActive = isActive; return this; }
      setCreatedAt(createdAt) { this._createdAt = createdAt; return this; }
      setUpdatedAt(updatedAt) { this._updatedAt = updatedAt; return this; }
      build() { return new AutomationDto(this); }
    }
    return Builder;
  }
}

export class CreateAutomationDto {
  constructor(builder) {
    this.name = builder._name;
    this.description = builder._description;
    this.cronExpression = builder._cronExpression;
    this.isActive = builder._isActive;
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setDescription(description) { this._description = description; return this; }
      setCronExpression(cronExpression) { this._cronExpression = cronExpression; return this; }
      setIsActive(isActive) { this._isActive = isActive; return this; }
      build() { return new CreateAutomationDto(this); }
    }
    return Builder;
  }
}

export class UpdateAutomationDto {
  constructor(builder) {
    this.name = builder._name;
    this.description = builder._description;
    this.cronExpression = builder._cronExpression;
    this.isActive = builder._isActive;
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setDescription(description) { this._description = description; return this; }
      setCronExpression(cronExpression) { this._cronExpression = cronExpression; return this; }
      setIsActive(isActive) { this._isActive = isActive; return this; }
      build() { return new UpdateAutomationDto(this); }
    }
    return Builder;
  }
}

export class AutomationActionDto {
  constructor(builder) {
    this.id = builder._id;
    this.automationId = builder._automationId;
    this.targetType = builder._targetType;
    this.targetId = builder._targetId;
    this.actionType = builder._actionType;
    this.parameterValue = builder._parameterValue;
    this.executionOrder = builder._executionOrder;
    this.targetName = builder._targetName;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setAutomationId(automationId) { this._automationId = automationId; return this; }
      setTargetType(targetType) { this._targetType = targetType; return this; }
      setTargetId(targetId) { this._targetId = targetId; return this; }
      setActionType(actionType) { this._actionType = actionType; return this; }
      setParameterValue(parameterValue) { this._parameterValue = parameterValue; return this; }
      setExecutionOrder(executionOrder) { this._executionOrder = executionOrder; return this; }
      setTargetName(targetName) { this._targetName = targetName; return this; }
      build() { return new AutomationActionDto(this); }
    }
    return Builder;
  }
}

export class CreateAutomationActionDto {
  constructor(builder) {
    this.targetType = builder._targetType;
    this.targetId = builder._targetId;
    this.actionType = builder._actionType;
    this.parameterValue = builder._parameterValue;
    this.executionOrder = builder._executionOrder;
  }

  static get Builder() {
    class Builder {
      setTargetType(targetType) { this._targetType = targetType; return this; }
      setTargetId(targetId) { this._targetId = targetId; return this; }
      setActionType(actionType) { this._actionType = actionType; return this; }
      setParameterValue(parameterValue) { this._parameterValue = parameterValue; return this; }
      setExecutionOrder(executionOrder) { this._executionOrder = executionOrder; return this; }
      build() { return new CreateAutomationActionDto(this); }
    }
    return Builder;
  }
}

export class UpdateAutomationActionDto {
  constructor(builder) {
    this.targetType = builder._targetType;
    this.targetId = builder._targetId;
    this.actionType = builder._actionType;
    this.parameterValue = builder._parameterValue;
    this.executionOrder = builder._executionOrder;
  }

  static get Builder() {
    class Builder {
      setTargetType(targetType) { this._targetType = targetType; return this; }
      setTargetId(targetId) { this._targetId = targetId; return this; }
      setActionType(actionType) { this._actionType = actionType; return this; }
      setParameterValue(parameterValue) { this._parameterValue = parameterValue; return this; }
      setExecutionOrder(executionOrder) { this._executionOrder = executionOrder; return this; }
      build() { return new UpdateAutomationActionDto(this); }
    }
    return Builder;
  }
}
