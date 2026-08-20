/**
 * Rule Engine Domain Types and Builders
 */

import { DeviceCategory } from '../constants/device.constants.js';
import {
  RuleDataSource,
  ConditionOwnerCategory,
  ActionOwnerCategory,
  ConditionOperator,
  ConditionLogic
} from '../constants/rule.constants.js';
import { Validator } from '../common/validator.js';
import { DomainValidationError } from './common.domain.js';

export {
  DeviceCategory,
  RuleDataSource,
  ConditionOwnerCategory,
  ActionOwnerCategory,
  ConditionOperator,
  ConditionLogic
};

export class ConditionDto {
  constructor(builder) {
    this.id = builder._id;
    this.ownerCategory = builder._ownerCategory;
    this.ownerId = builder._ownerId;
    this.sourceCategory = builder._sourceCategory;
    this.sourceTargetId = builder._sourceTargetId;
    this.sourceTargetType = builder._sourceTargetType;
    this.property = builder._property;
    this.operator = builder._operator;
    this.value = builder._value;
    this.extraParams = builder._extraParams;
    this.sortOrder = builder._sortOrder;
    this.nextLogic = builder._nextLogic;
    this.createdAt = builder._createdAt;
    this.updatedAt = builder._updatedAt;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setOwnerCategory(ownerCategory) { this._ownerCategory = ownerCategory; return this; }
      setOwnerId(ownerId) { this._ownerId = ownerId; return this; }
      setSourceCategory(sourceCategory) { this._sourceCategory = sourceCategory; return this; }
      setSourceTargetId(sourceTargetId) { this._sourceTargetId = sourceTargetId; return this; }
      setSourceTargetType(sourceTargetType) { this._sourceTargetType = sourceTargetType; return this; }
      setProperty(property) { this._property = property; return this; }
      setOperator(operator) { this._operator = operator; return this; }
      setValue(value) { this._value = value; return this; }
      setExtraParams(extraParams) { this._extraParams = extraParams; return this; }
      setSortOrder(sortOrder) { this._sortOrder = sortOrder; return this; }
      setNextLogic(nextLogic) { this._nextLogic = nextLogic; return this; }
      setCreatedAt(createdAt) { this._createdAt = createdAt; return this; }
      setUpdatedAt(updatedAt) { this._updatedAt = updatedAt; return this; }
      build() { return new ConditionDto(this); }
    }
    return Builder;
  }
}
export const RuleConditionDto = ConditionDto;

export class CreateConditionDto {
  constructor(builder) {
    this.id = builder._id;
    this.ownerCategory = builder._ownerCategory;
    this.ownerId = builder._ownerId;
    this.sourceCategory = builder._sourceCategory;
    this.sourceTargetId = builder._sourceTargetId;
    this.sourceTargetType = builder._sourceTargetType;
    this.property = builder._property;
    this.operator = builder._operator;
    this.value = builder._value;
    this.extraParams = builder._extraParams;
    this.sortOrder = builder._sortOrder;
    this.nextLogic = builder._nextLogic;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setOwnerCategory(ownerCategory) { this._ownerCategory = ownerCategory; return this; }
      setOwnerId(ownerId) { this._ownerId = ownerId; return this; }
      setSourceCategory(sourceCategory) { this._sourceCategory = sourceCategory; return this; }
      setSourceTargetId(sourceTargetId) { this._sourceTargetId = sourceTargetId; return this; }
      setSourceTargetType(sourceTargetType) { this._sourceTargetType = sourceTargetType; return this; }
      setProperty(property) { this._property = property; return this; }
      setOperator(operator) { this._operator = operator; return this; }
      setValue(value) { this._value = value; return this; }
      setExtraParams(extraParams) { this._extraParams = extraParams; return this; }
      setSortOrder(sortOrder) { this._sortOrder = sortOrder; return this; }
      setNextLogic(nextLogic) { this._nextLogic = nextLogic; return this; }
      validate() {
        const errors = {};
        if (!Validator.generic.isNull(this._sourceCategory) || !Validator.generic.isBlank(this._sourceCategory)) {
          errors.sourceCategory = 'valSourceCategoryRequired';
        }
        if (!Validator.generic.isNull(this._sourceTargetId) || !Validator.generic.isBlank(this._sourceTargetId)) {
          errors.sourceTargetId = 'valSourceTargetIdRequired';
        }
        if (!Validator.generic.isNull(this._property) || !Validator.generic.isBlank(this._property)) {
          errors.property = 'valPropertyRequired';
        }
        if (!Validator.generic.isNull(this._operator) || !Validator.generic.isBlank(this._operator)) {
          errors.operator = 'valOperatorRequired';
        }
        if (!Validator.generic.isNull(this._value) || !Validator.generic.isBlank(this._value)) {
          errors.value = 'valValueRequired';
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
          throw new DomainValidationError('CreateConditionDto validation failed', errors);
        }
        return new CreateConditionDto(this);
      }
    }
    return Builder;
  }
}
export const CreateRuleConditionDto = CreateConditionDto;

export class UpdateConditionDto {
  constructor(builder) {
    this.sourceCategory = builder._sourceCategory;
    this.sourceTargetId = builder._sourceTargetId;
    this.sourceTargetType = builder._sourceTargetType;
    this.property = builder._property;
    this.operator = builder._operator;
    this.value = builder._value;
    this.extraParams = builder._extraParams;
    this.sortOrder = builder._sortOrder;
    this.nextLogic = builder._nextLogic;
  }

  static get Builder() {
    class Builder {
      setSourceCategory(sourceCategory) { this._sourceCategory = sourceCategory; return this; }
      setSourceTargetId(sourceTargetId) { this._sourceTargetId = sourceTargetId; return this; }
      setSourceTargetType(sourceTargetType) { this._sourceTargetType = sourceTargetType; return this; }
      setProperty(property) { this._property = property; return this; }
      setOperator(operator) { this._operator = operator; return this; }
      setValue(value) { this._value = value; return this; }
      setExtraParams(extraParams) { this._extraParams = extraParams; return this; }
      setSortOrder(sortOrder) { this._sortOrder = sortOrder; return this; }
      setNextLogic(nextLogic) { this._nextLogic = nextLogic; return this; }
      validate() {
        const errors = {};
        if (this._sortOrder !== null && this._sortOrder !== undefined && this._sortOrder !== ''
            && !Validator.integer.isValidFormat(this._sortOrder)) {
          errors.sortOrder = 'valSortOrderInvalid';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('UpdateConditionDto validation failed', errors);
        }
        return new UpdateConditionDto(this);
      }
    }
    return Builder;
  }
}
export const UpdateRuleConditionDto = UpdateConditionDto;

export class ActionDto {
  constructor(builder) {
    this.id = builder._id;
    this.ownerCategory = builder._ownerCategory;
    this.ownerId = builder._ownerId;
    this.targetCategory = builder._targetCategory;
    this.targetId = builder._targetId;
    this.params = builder._params;
    this.executionOrder = builder._executionOrder;
    this.createdAt = builder._createdAt;
    this.updatedAt = builder._updatedAt;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setOwnerCategory(ownerCategory) { this._ownerCategory = ownerCategory; return this; }
      setOwnerId(ownerId) { this._ownerId = ownerId; return this; }
      setTargetCategory(targetCategory) { this._targetCategory = targetCategory; return this; }
      setTargetId(targetId) { this._targetId = targetId; return this; }
      setParams(params) { this._params = params; return this; }
      setExecutionOrder(executionOrder) { this._executionOrder = executionOrder; return this; }
      setCreatedAt(createdAt) { this._createdAt = createdAt; return this; }
      setUpdatedAt(updatedAt) { this._updatedAt = updatedAt; return this; }
      build() { return new ActionDto(this); }
    }
    return Builder;
  }
}
export const RuleActionDto = ActionDto;

export class CreateActionDto {
  constructor(builder) {
    this.id = builder._id;
    this.ownerCategory = builder._ownerCategory;
    this.ownerId = builder._ownerId;
    this.targetCategory = builder._targetCategory;
    this.targetId = builder._targetId;
    this.params = builder._params;
    this.executionOrder = builder._executionOrder;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setOwnerCategory(ownerCategory) { this._ownerCategory = ownerCategory; return this; }
      setOwnerId(ownerId) { this._ownerId = ownerId; return this; }
      setTargetCategory(targetCategory) { this._targetCategory = targetCategory; return this; }
      setTargetId(targetId) { this._targetId = targetId; return this; }
      setParams(params) { this._params = params; return this; }
      setExecutionOrder(executionOrder) { this._executionOrder = executionOrder; return this; }
      validate() {
        const errors = {};
        if (!Validator.generic.isNull(this._targetCategory) || !Validator.generic.isBlank(this._targetCategory)) {
          errors.targetCategory = 'valTargetCategoryRequired';
        }
        if (!Validator.generic.isNull(this._targetId) || !Validator.generic.isBlank(this._targetId)) {
          errors.targetId = 'valTargetIdRequired';
        }
        if (!Validator.generic.isNull(this._params)) {
          errors.params = 'valParamsRequired';
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
          throw new DomainValidationError('CreateActionDto validation failed', errors);
        }
        return new CreateActionDto(this);
      }
    }
    return Builder;
  }
}
export const CreateRuleActionDto = CreateActionDto;

export class UpdateActionDto {
  constructor(builder) {
    this.targetCategory = builder._targetCategory;
    this.targetId = builder._targetId;
    this.params = builder._params;
    this.executionOrder = builder._executionOrder;
  }

  static get Builder() {
    class Builder {
      setTargetCategory(targetCategory) { this._targetCategory = targetCategory; return this; }
      setTargetId(targetId) { this._targetId = targetId; return this; }
      setParams(params) { this._params = params; return this; }
      setExecutionOrder(executionOrder) { this._executionOrder = executionOrder; return this; }
      validate() {
        const errors = {};
        if (this._executionOrder !== null && this._executionOrder !== undefined && this._executionOrder !== ''
            && !Validator.integer.isValidFormat(this._executionOrder)) {
          errors.executionOrder = 'valExecutionOrderInvalid';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('UpdateActionDto validation failed', errors);
        }
        return new UpdateActionDto(this);
      }
    }
    return Builder;
  }
}
export const UpdateRuleActionDto = UpdateActionDto;

export class RuleDto {
  constructor(builder) {
    this.id = builder._id;
    this.name = builder._name;
    this.priority = builder._priority;
    this.isActive = builder._isActive;
    this.intervalSeconds = builder._intervalSeconds;
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
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setPriority(priority) { this._priority = priority; return this; }
      setIntervalSeconds(intervalSeconds) { this._intervalSeconds = intervalSeconds; return this; }
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
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setPriority(priority) { this._priority = priority; return this; }
      setIsActive(isActive) { this._isActive = isActive; return this; }
      setIntervalSeconds(intervalSeconds) { this._intervalSeconds = intervalSeconds; return this; }
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
