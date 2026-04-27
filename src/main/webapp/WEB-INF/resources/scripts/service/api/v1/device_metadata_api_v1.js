class DeviceMetadataApiV1Service {
  static instance;

  constructor() {
    if (DeviceMetadataApiV1Service.instance) return DeviceMetadataApiV1Service.instance;
    DeviceMetadataApiV1Service.instance = this;

    this.api = SMRC_API_V1.DEVICE_METADATA;
  }

  async getAll(category = null) {
    try {
      const params = category ? { category } : {};
      return await window.http.get(this.api.ALL, params);
    } catch (error) {
      this.#handleError('get all devices', error);
    }
  }

  async getAllByRoom(roomId, category = null) {
    try {
      const params = category ? { category } : {};
      return await window.http.get(this.api.BY_ROOM(roomId), params);
    } catch (error) {
      this.#handleError(`get all devices for room ${roomId}`, error);
    }
  }

  async getCountByRoom(roomId) {
    try {
      return await window.http.get(this.api.COUNT_BY_ROOM(roomId));
    } catch (error) {
      this.#handleError(`get device count for room ${roomId}`, error);
    }
  }

  #handleError(action, error) {
    console.error(`[DeviceMetadataApiV1Service] Failed to ${action}:`, error);
    throw error;
  }
}

if (typeof window === 'undefined')
  throw new Error('DeviceMetadataApiV1Service can only be initialized in a browser environment');
window.deviceMetadataApiV1Service = new DeviceMetadataApiV1Service();
