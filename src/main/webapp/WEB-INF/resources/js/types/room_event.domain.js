import { DomainValidationError } from './common.domain.js';

export class RoomEventCodeDto {
  constructor(builder) {
    this.id = builder._id;
    this.code = builder._code;
    this.description = builder._description;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setCode(code) { this._code = code; return this; }
      setDescription(description) { this._description = description; return this; }
      build() { return new RoomEventCodeDto(this); }
    }
    return Builder;
  }

  static fromApi(data = {}) {
    const payload = data || {};
    return new RoomEventCodeDto.Builder()
      .setId(payload.id)
      .setCode(payload.code)
      .setDescription(payload.description)
      .build();
  }
}

export class RoomEventConfigDto {
  constructor(builder) {
    this.id = builder._id;
    this.roomId = builder._roomId;
    this.roomName = builder._roomName;
    this.roomEventId = builder._roomEventId;
    this.eventCode = builder._eventCode;
    this.eventDescription = builder._eventDescription;
    this.isActive = builder._isActive !== false;
    this.cooldownSeconds = Number(builder._cooldownSeconds ?? 60);
    this.lastTriggeredAt = builder._lastTriggeredAt || '';
    this.createdAt = builder._createdAt || '';
    this.updatedAt = builder._updatedAt || '';
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setRoomId(roomId) { this._roomId = roomId; return this; }
      setRoomName(roomName) { this._roomName = roomName; return this; }
      setRoomEventId(roomEventId) { this._roomEventId = roomEventId; return this; }
      setEventCode(eventCode) { this._eventCode = eventCode; return this; }
      setEventDescription(eventDescription) { this._eventDescription = eventDescription; return this; }
      setIsActive(isActive) { this._isActive = isActive; return this; }
      setCooldownSeconds(cooldownSeconds) { this._cooldownSeconds = cooldownSeconds; return this; }
      setLastTriggeredAt(lastTriggeredAt) { this._lastTriggeredAt = lastTriggeredAt; return this; }
      setCreatedAt(createdAt) { this._createdAt = createdAt; return this; }
      setUpdatedAt(updatedAt) { this._updatedAt = updatedAt; return this; }
      build() { return new RoomEventConfigDto(this); }
    }
    return Builder;
  }

  static fromApi(data = {}) {
    const payload = data || {};
    return new RoomEventConfigDto.Builder()
      .setId(payload.id)
      .setRoomId(payload.roomId)
      .setRoomName(payload.roomName)
      .setRoomEventId(payload.roomEventId)
      .setEventCode(payload.eventCode)
      .setEventDescription(payload.eventDescription)
      .setIsActive(payload.isActive)
      .setCooldownSeconds(payload.cooldownSeconds)
      .setLastTriggeredAt(payload.lastTriggeredAt)
      .setCreatedAt(payload.createdAt)
      .setUpdatedAt(payload.updatedAt)
      .build();
  }
}

export class CreateRoomEventConfigDto {
  constructor(builder) {
    this.eventCode = builder._eventCode;
    this.isActive = builder._isActive !== false;
    this.cooldownSeconds = Number(builder._cooldownSeconds ?? 60);
  }

  static get Builder() {
    class Builder {
      setEventCode(eventCode) { this._eventCode = eventCode; return this; }
      setIsActive(isActive) { this._isActive = isActive; return this; }
      setCooldownSeconds(cooldownSeconds) { this._cooldownSeconds = cooldownSeconds; return this; }
      build() { return new CreateRoomEventConfigDto(this); }
    }
    return Builder;
  }

  validate() {
    const errors = {};
    if (!this.eventCode) {
      errors.eventCode = 'Event code is required';
    }
    if (this.cooldownSeconds < 0) {
      errors.cooldownSeconds = 'Cooldown seconds must be >= 0';
    }
    if (Object.keys(errors).length > 0) {
      throw new DomainValidationError('Invalid CreateRoomEventConfigDto data', errors);
    }
    return true;
  }

  toApi() {
    return {
      eventCode: this.eventCode,
      isActive: this.isActive,
      cooldownSeconds: this.cooldownSeconds,
    };
  }

  static fromForm(form = {}) {
    return new CreateRoomEventConfigDto.Builder()
      .setEventCode(form.eventCode)
      .setIsActive(form.isActive !== false)
      .setCooldownSeconds(Number(form.cooldownSeconds ?? 60))
      .build();
  }
}

export class UpdateRoomEventConfigDto {
  constructor(builder) {
    this.isActive = builder._isActive !== false;
    this.cooldownSeconds = Number(builder._cooldownSeconds ?? 60);
  }

  static get Builder() {
    class Builder {
      setIsActive(isActive) { this._isActive = isActive; return this; }
      setCooldownSeconds(cooldownSeconds) { this._cooldownSeconds = cooldownSeconds; return this; }
      build() { return new UpdateRoomEventConfigDto(this); }
    }
    return Builder;
  }

  validate() {
    const errors = {};
    if (this.cooldownSeconds < 0) {
      errors.cooldownSeconds = 'Cooldown seconds must be >= 0';
    }
    if (Object.keys(errors).length > 0) {
      throw new DomainValidationError('Invalid UpdateRoomEventConfigDto data', errors);
    }
    return true;
  }

  toApi() {
    return {
      isActive: this.isActive,
      cooldownSeconds: this.cooldownSeconds,
    };
  }

  static fromForm(form = {}) {
    return new UpdateRoomEventConfigDto.Builder()
      .setIsActive(form.isActive !== false)
      .setCooldownSeconds(Number(form.cooldownSeconds ?? 60))
      .build();
  }
}
