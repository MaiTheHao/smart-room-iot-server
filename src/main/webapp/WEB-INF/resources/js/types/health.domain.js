export class HealthDeviceDto {
  constructor(builder) {
    this.naturalId = builder._naturalId;
    this.category = builder._category;
    this.isActive = builder._isActive;
  }

  static get Builder() {
    class Builder {
      setNaturalId(naturalId) { this._naturalId = naturalId; return this; }
      setCategory(category) { this._category = category; return this; }
      setIsActive(isActive) { this._isActive = isActive; return this; }
      build() { return new HealthDeviceDto(this); }
    }
    return Builder;
  }
}

export class HealthDataDto {
  constructor(builder) {
    this.devices = builder._devices || [];
    this.roomCode = builder._roomCode;
  }

  static get Builder() {
    class Builder {
      setDevices(devices) { this._devices = devices; return this; }
      addDevice(device) {
        if (!this._devices) this._devices = [];
        this._devices.push(device);
        return this;
      }
      setRoomCode(roomCode) { this._roomCode = roomCode; return this; }
      build() { return new HealthDataDto(this); }
    }
    return Builder;
  }
}

export class HealthCheckResponseDto {
  constructor(builder) {
    this.status = builder._status;
    this.message = builder._message;
    this.data = builder._data;
    this.timestamp = builder._timestamp;
  }

  static get Builder() {
    class Builder {
      setStatus(status) { this._status = status; return this; }
      setMessage(message) { this._message = message; return this; }
      setData(data) { this._data = data; return this; }
      setTimestamp(timestamp) { this._timestamp = timestamp; return this; }
      build() { return new HealthCheckResponseDto(this); }
    }
    return Builder;
  }
}
