/**
 * Rule Engine Domain Types and Builders
 */

import { DeviceCategory } from '../constants/device.constants.js';
import { RuleDataSource, ConditionOperator, ConditionLogic } from '../constants/rule.constants.js';
import { Validator } from '../common/validator.js';
import { DomainValidationError } from './common.domain.js';

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
      validate() {
        const errors = {};
        if (!Validator.generic.isNull(this._dataSource) || !Validator.generic.isBlank(this._dataSource)) {
          errors.dataSource = 'valDataSourceRequired';
        }
        if (!Validator.generic.isNull(this._operator) || !Validator.generic.isBlank(this._operator)) {
          errors.operator = 'valOperatorRequired';
        }
        if (this._sortOrder !== null && this._sortOrder !== undefined && this._sortOrder !== ''
            && !Validator.integer.isValidFormat(this._sortOrder)) {
          errors.sortOrder = 'valSortOrderInvalid';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('CreateRuleConditionDto validation failed', errors);
        }
        return new CreateRuleConditionDto(this);
      }
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
      validate() {
        const errors = {};
        if (!Validator.generic.isNull(this._dataSource) || !Validator.generic.isBlank(this._dataSource)) {
          errors.dataSource = 'valDataSourceRequired';
        }
        if (!Validator.generic.isNull(this._operator) || !Validator.generic.isBlank(this._operator)) {
          errors.operator = 'valOperatorRequired';
        }
        if (this._sortOrder !== null && this._sortOrder !== undefined && this._sortOrder !== ''
            && !Validator.integer.isValidFormat(this._sortOrder)) {
          errors.sortOrder = 'valSortOrderInvalid';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('UpdateRuleConditionDto validation failed', errors);
        }
        return new UpdateRuleConditionDto(this);
      }
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
      validate() {
        const errors = {};
        if (!Validator.generic.isNull(this._targetDeviceId) || !Validator.generic.isBlank(this._targetDeviceId)) {
          errors.targetDeviceId = 'valTargetDeviceIdRequired';
        }
        if (!Validator.generic.isNull(this._targetDeviceCategory) || !Validator.generic.isBlank(this._targetDeviceCategory)) {
          errors.targetDeviceCategory = 'valTargetCategoryRequired';
        }
        if (this._executionOrder !== null && this._executionOrder !== undefined && this._executionOrder !== ''
            && !Validator.integer.isValidFormat(this._executionOrder)) {
          errors.executionOrder = 'valExecutionOrderInvalid';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('CreateRuleActionDto validation failed', errors);
        }
        return new CreateRuleActionDto(this);
      }
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
      validate() {
        const errors = {};
        if (!Validator.generic.isNull(this._targetDeviceId) || !Validator.generic.isBlank(this._targetDeviceId)) {
          errors.targetDeviceId = 'valTargetDeviceIdRequired';
        }
        if (!Validator.generic.isNull(this._targetDeviceCategory) || !Validator.generic.isBlank(this._targetDeviceCategory)) {
          errors.targetDeviceCategory = 'valTargetCategoryRequired';
        }
        if (this._executionOrder !== null && this._executionOrder !== undefined && this._executionOrder !== ''
            && !Validator.integer.isValidFormat(this._executionOrder)) {
          errors.executionOrder = 'valExecutionOrderInvalid';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('UpdateRuleActionDto validation failed', errors);
        }
        return new UpdateRuleActionDto(this);
      }
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
      validate() {
        const errors = {};
        if (!Validator.name.isNull(this._name) || !Validator.name.isBlank(this._name)) {
          errors.name = 'valNameRequired';
        } else if (!Validator.name.isLowerMin(this._name) || !Validator.name.isHigherMax(this._name)) {
          errors.name = 'valNameLen';
        }
        if (!Validator.generic.isNull(this._priority) || !Validator.integer.isValidFormat(this._priority)) {
          errors.priority = 'valPriorityRequired';
        } else if (Number(this._priority) < 0) {
          errors.priority = 'valPriorityMin';
        }
        if (!Validator.generic.isNull(this._intervalSeconds) || !Validator.integer.isValidFormat(this._intervalSeconds)) {
          errors.intervalSeconds = 'valIntervalRequired';
        } else if (Number(this._intervalSeconds) < 60) {
          errors.intervalSeconds = 'valIntervalMin';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('CreateRuleDto validation failed', errors);
        }
        return new CreateRuleDto(this);
      }
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
      validate() {
        const errors = {};
        if (this._name !== null && this._name !== undefined && this._name !== ''
            && (!Validator.name.isLowerMin(this._name) || !Validator.name.isHigherMax(this._name))) {
          errors.name = 'valNameLen';
        }
        if (this._priority !== null && this._priority !== undefined && this._priority !== ''
            && !Validator.integer.isValidFormat(this._priority)) {
          errors.priority = 'valPriorityRequired';
        } else if (this._priority !== null && this._priority !== undefined && this._priority !== '' && Number(this._priority) < 0) {
          errors.priority = 'valPriorityMin';
        }
        if (this._intervalSeconds !== null && this._intervalSeconds !== undefined && this._intervalSeconds !== ''
            && !Validator.integer.isValidFormat(this._intervalSeconds)) {
          errors.intervalSeconds = 'valIntervalRequired';
        } else if (this._intervalSeconds !== null && this._intervalSeconds !== undefined && this._intervalSeconds !== '' && Number(this._intervalSeconds) < 60) {
          errors.intervalSeconds = 'valIntervalMin';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('UpdateRuleDto validation failed', errors);
        }
        return new UpdateRuleDto(this);
      }
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
