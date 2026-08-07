import { Validator } from '../common/validator.js';
import { DomainValidationError } from './common.domain.js';

export class SysFunctionDto {
  constructor(builder) {
    this.id = builder._id;
    this.functionCode = builder._functionCode;
    this.name = builder._name;
    this.description = builder._description;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setFunctionCode(functionCode) { this._functionCode = functionCode; return this; }
      setName(name) { this._name = name; return this; }
      setDescription(description) { this._description = description; return this; }
      build() { return new SysFunctionDto(this); }
    }
    return Builder;
  }
}

export class CreateSysFunctionDto {
  constructor(builder) {
    this.functionCode = builder._functionCode;
    this.name = builder._name;
    this.description = builder._description;
    this.langCode = builder._langCode;
  }

  static get Builder() {
    class Builder {
      setFunctionCode(functionCode) { this._functionCode = functionCode; return this; }
      setName(name) { this._name = name; return this; }
      setDescription(description) { this._description = description; return this; }
      setLangCode(langCode) { this._langCode = langCode; return this; }
      validate() {
        const errors = {};
        if (!Validator.functionCode.isNull(this._functionCode) || !Validator.functionCode.isBlank(this._functionCode)) {
          errors.functionCode = 'valFunctionCodeRequired';
        } else if (!Validator.functionCode.isValidFormat(this._functionCode) || !Validator.functionCode.isHigherMax(this._functionCode)) {
          errors.functionCode = 'valFunctionCodeFormat';
        }
        if (!Validator.name.isNull(this._name) || !Validator.name.isBlank(this._name)) {
          errors.name = 'valNameRequired';
        } else if (!Validator.name.isLowerMin(this._name) || !Validator.name.isHigherMax(this._name)) {
          errors.name = 'valNameLen';
        }
        if (this._description && !Validator.description.isHigherMax(this._description)) {
          errors.description = 'valDescriptionLen';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('CreateSysFunctionDto validation failed', errors);
        }
        return new CreateSysFunctionDto(this);
      }
    }
    return Builder;
  }
}

export class UpdateSysFunctionDto {
  constructor(builder) {
    this.name = builder._name;
    this.description = builder._description;
    this.langCode = builder._langCode;
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setDescription(description) { this._description = description; return this; }
      setLangCode(langCode) { this._langCode = langCode; return this; }
      validate() {
        const errors = {};
        if (!Validator.name.isNull(this._name) || !Validator.name.isBlank(this._name)) {
          errors.name = 'valNameRequired';
        } else if (!Validator.name.isLowerMin(this._name) || !Validator.name.isHigherMax(this._name)) {
          errors.name = 'valNameLen';
        }
        if (this._description && !Validator.description.isHigherMax(this._description)) {
          errors.description = 'valDescriptionLen';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('UpdateSysFunctionDto validation failed', errors);
        }
        return new UpdateSysFunctionDto(this);
      }
    }
    return Builder;
  }
}

export class SysFunctionWithGroupStatusDto {
  constructor(builder) {
    this.id = builder._id;
    this.functionCode = builder._functionCode;
    this.name = builder._name;
    this.description = builder._description;
    this.isAssignedToGroup = builder._isAssignedToGroup;
    this.roleId = builder._roleId;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setFunctionCode(functionCode) { this._functionCode = functionCode; return this; }
      setName(name) { this._name = name; return this; }
      setDescription(description) { this._description = description; return this; }
      setIsAssignedToGroup(isAssignedToGroup) { this._isAssignedToGroup = isAssignedToGroup; return this; }
      setRoleId(roleId) { this._roleId = roleId; return this; }
      build() { return new SysFunctionWithGroupStatusDto(this); }
    }
    return Builder;
  }
}

export class SysGroupDto {
  constructor(builder) {
    this.id = builder._id;
    this.groupCode = builder._groupCode;
    this.name = builder._name;
    this.description = builder._description;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setGroupCode(groupCode) { this._groupCode = groupCode; return this; }
      setName(name) { this._name = name; return this; }
      setDescription(description) { this._description = description; return this; }
      build() { return new SysGroupDto(this); }
    }
    return Builder;
  }
}

export class CreateSysGroupDto {
  constructor(builder) {
    this.groupCode = builder._groupCode;
    this.name = builder._name;
    this.description = builder._description;
    this.langCode = builder._langCode;
  }

  static get Builder() {
    class Builder {
      setGroupCode(groupCode) {
        this._groupCode = (groupCode === null || groupCode === undefined) ? groupCode : String(groupCode).toUpperCase();
        return this;
      }
      setName(name) { this._name = name; return this; }
      setDescription(description) { this._description = description; return this; }
      setLangCode(langCode) { this._langCode = langCode; return this; }
      validate() {
        const errors = {};
        if (!Validator.groupCode.isNull(this._groupCode) || !Validator.groupCode.isBlank(this._groupCode)) {
          errors.groupCode = 'valGroupCodeRequired';
        } else if (!Validator.groupCode.isValidFormat(this._groupCode) || !Validator.groupCode.isHigherMax(this._groupCode)) {
          errors.groupCode = 'valGroupCodeFormat';
        }
        if (!Validator.name.isNull(this._name) || !Validator.name.isBlank(this._name)) {
          errors.name = 'valNameRequired';
        } else if (!Validator.name.isLowerMin(this._name) || !Validator.name.isHigherMax(this._name)) {
          errors.name = 'valNameLen';
        }
        if (this._description && !Validator.description.isHigherMax(this._description)) {
          errors.description = 'valDescriptionLen';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('CreateSysGroupDto validation failed', errors);
        }
        return new CreateSysGroupDto(this);
      }
    }
    return Builder;
  }
}

export class UpdateSysGroupDto {
  constructor(builder) {
    this.name = builder._name;
    this.description = builder._description;
    this.langCode = builder._langCode;
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setDescription(description) { this._description = description; return this; }
      setLangCode(langCode) { this._langCode = langCode; return this; }
      validate() {
        const errors = {};
        if (!Validator.name.isNull(this._name) || !Validator.name.isBlank(this._name)) {
          errors.name = 'valNameRequired';
        } else if (!Validator.name.isLowerMin(this._name) || !Validator.name.isHigherMax(this._name)) {
          errors.name = 'valNameLen';
        }
        if (this._description && !Validator.description.isHigherMax(this._description)) {
          errors.description = 'valDescriptionLen';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('UpdateSysGroupDto validation failed', errors);
        }
        return new UpdateSysGroupDto(this);
      }
    }
    return Builder;
  }
}

export class SysGroupWithClientStatusDto {
  constructor(builder) {
    this.id = builder._id;
    this.groupCode = builder._groupCode;
    this.name = builder._name;
    this.description = builder._description;
    this.isAssignedToClient = builder._isAssignedToClient;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setGroupCode(groupCode) { this._groupCode = groupCode; return this; }
      setName(name) { this._name = name; return this; }
      setDescription(description) { this._description = description; return this; }
      setIsAssignedToClient(isAssignedToClient) { this._isAssignedToClient = isAssignedToClient; return this; }
      build() { return new SysGroupWithClientStatusDto(this); }
    }
    return Builder;
  }
}

export class BatchOperationResultDto {
  constructor(builder) {
    this.successCount = builder._successCount;
    this.failedCount = builder._failedCount;
    this.skippedCount = builder._skippedCount;
    this.message = builder._message;
  }

  static get Builder() {
    class Builder {
      setSuccessCount(successCount) { this._successCount = successCount; return this; }
      setFailedCount(failedCount) { this._failedCount = failedCount; return this; }
      setSkippedCount(skippedCount) { this._skippedCount = skippedCount; return this; }
      setMessage(message) { this._message = message; return this; }
      build() { return new BatchOperationResultDto(this); }
    }
    return Builder;
  }
}
