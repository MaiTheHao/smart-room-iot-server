/**
 * General Device Domain Types and Builders
 */

import { DeviceCategory } from '../constants/device.constants.js';

export { DeviceCategory };

export class UnifiedDeviceDto {
  constructor(builder) {
    this.id = builder._id;
    this.naturalId = builder._naturalId;
    this.name = builder._name;
    this.description = builder._description;
    this.isActive = builder._isActive;
    this.power = builder._power;
    this.roomId = builder._roomId;
    this.deviceControlId = builder._deviceControlId;
    this.category = builder._category;
    this.level = builder._level;
    this.speed = builder._speed;
    this.mode = builder._mode;
    this.swing = builder._swing;
    this.light = builder._light;
    this.temperature = builder._temperature;
    this.fanSpeed = builder._fanSpeed;
    this.type = builder._type;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setNaturalId(naturalId) { this._naturalId = naturalId; return this; }
      setName(name) { this._name = name; return this; }
      setDescription(description) { this._description = description; return this; }
      setIsActive(isActive) { this._isActive = isActive; return this; }
      setPower(power) { this._power = power; return this; }
      setRoomId(roomId) { this._roomId = roomId; return this; }
      setDeviceControlId(deviceControlId) { this._deviceControlId = deviceControlId; return this; }
      setCategory(category) { this._category = category; return this; }
      setLevel(level) { this._level = level; return this; }
      setSpeed(speed) { this._speed = speed; return this; }
      setMode(mode) { this._mode = mode; return this; }
      setSwing(swing) { this._swing = swing; return this; }
      setLight(light) { this._light = light; return this; }
      setTemperature(temperature) { this._temperature = temperature; return this; }
      setFanSpeed(fanSpeed) { this._fanSpeed = fanSpeed; return this; }
      setType(type) { this._type = type; return this; }
      build() { return new UnifiedDeviceDto(this); }
    }
    return Builder;
  }
}

export class ControlDeviceDetail {
  constructor(builder) {
    this.parameter = builder._parameter;
    this.success = builder._success;
    this.message = builder._message;
  }

  static get Builder() {
    class Builder {
      setParameter(parameter) { this._parameter = parameter; return this; }
      setSuccess(success) { this._success = success; return this; }
      setMessage(message) { this._message = message; return this; }
      build() { return new ControlDeviceDetail(this); }
    }
    return Builder;
  }
}

export class ControlDeviceResult {
  constructor(builder) {
    this.successCount = builder._successCount;
    this.totalCount = builder._totalCount;
    this.details = builder._details || [];
  }

  static get Builder() {
    class Builder {
      setSuccessCount(successCount) { this._successCount = successCount; return this; }
      setTotalCount(totalCount) { this._totalCount = totalCount; return this; }
      setDetails(details) { this._details = details; return this; }
      addDetail(detail) {
        if (!this._details) this._details = [];
        this._details.push(detail);
        return this;
      }
      build() { return new ControlDeviceResult(this); }
    }
    return Builder;
  }
}
