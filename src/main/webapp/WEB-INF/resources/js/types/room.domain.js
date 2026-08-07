import { Validator } from '../common/validator.js';
import { DomainValidationError } from './common.domain.js';

export class RoomDto {
  constructor(builder) {
    this.id = builder._id;
    this.name = builder._name;
    this.code = builder._code;
    this.floorId = builder._floorId;
    this.description = builder._description;
    this.version = builder._version;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setName(name) { this._name = name; return this; }
      setCode(code) { this._code = code; return this; }
      setFloorId(floorId) { this._floorId = floorId; return this; }
      setDescription(description) { this._description = description; return this; }
      setVersion(version) { this._version = version; return this; }
      build() { return new RoomDto(this); }
    }
    return Builder;
  }
}

export class CreateRoomDto {
  constructor(builder) {
    this.name = builder._name;
    this.code = builder._code;
    this.floorId = builder._floorId;
    this.description = builder._description;
    this.langCode = builder._langCode;
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setCode(code) { this._code = code; return this; }
      setFloorId(floorId) { this._floorId = floorId; return this; }
      setDescription(description) { this._description = description; return this; }
      setLangCode(langCode) { this._langCode = langCode; return this; }
      validate() {
        const errors = {};
        if (!Validator.name.isNull(this._name) || !Validator.name.isBlank(this._name)) {
          errors.name = 'valNameRequired';
        } else if (!Validator.name.isLowerMin(this._name) || !Validator.name.isHigherMax(this._name)) {
          errors.name = 'valNameLen';
        }
        if (this._code && !Validator.code.isHigherMax(this._code)) {
          errors.code = 'valCodeLen';
        }
        if (!Validator.id.isNull(this._floorId)) {
          errors.floorId = 'valFloorRequired';
        }
        if (this._description && !Validator.description.isHigherMax(this._description)) {
          errors.description = 'valDescriptionLen';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('CreateRoomDto validation failed', errors);
        }
        return new CreateRoomDto(this);
      }
    }
    return Builder;
  }
}

export class UpdateRoomDto {
  constructor(builder) {
    this.name = builder._name;
    this.floorId = builder._floorId;
    this.description = builder._description;
    this.langCode = builder._langCode;
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setFloorId(floorId) { this._floorId = floorId; return this; }
      setDescription(description) { this._description = description; return this; }
      setLangCode(langCode) { this._langCode = langCode; return this; }
      validate() {
        const errors = {};
        if (!Validator.name.isNull(this._name) || !Validator.name.isBlank(this._name)) {
          errors.name = 'valNameRequired';
        } else if (!Validator.name.isLowerMin(this._name) || !Validator.name.isHigherMax(this._name)) {
          errors.name = 'valNameLen';
        }
        if (!Validator.id.isNull(this._floorId)) {
          errors.floorId = 'valFloorRequired';
        }
        if (this._description && !Validator.description.isHigherMax(this._description)) {
          errors.description = 'valDescriptionLen';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('UpdateRoomDto validation failed', errors);
        }
        return new UpdateRoomDto(this);
      }
    }
    return Builder;
  }
}
