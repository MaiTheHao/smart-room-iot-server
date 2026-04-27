class HardwareConfigApiV1Service {
  static instance;

  constructor() {
    if (HardwareConfigApiV1Service.instance) return HardwareConfigApiV1Service.instance;
    HardwareConfigApiV1Service.instance = this;

    this.api = SMRC_API_V1.HARDWARE_CONFIG;
  }

  async getById(id) {
    try {
      return await window.http.get(this.api.DETAIL(id));
    } catch (error) {
      this.#handleError(`get hardware config ${id}`, error);
    }
  }

  async create(dto) {
    try {
      return await window.http.post(this.api.PATH, dto);
    } catch (error) {
      this.#handleError('create hardware config', error);
    }
  }

  async update(id, dto) {
    try {
      return await window.http.put(this.api.DETAIL(id), dto);
    } catch (error) {
      this.#handleError(`update hardware config ${id}`, error);
    }
  }

  async delete(id) {
    try {
      return await window.http.delete(this.api.DETAIL(id));
    } catch (error) {
      this.#handleError(`delete hardware config ${id}`, error);
    }
  }

  async getByClient(clientId, page = 0, size = 10) {
    try {
      return await window.http.get(this.api.BY_CLIENT(clientId), { page, size });
    } catch (error) {
      this.#handleError(`get hardware configs for client ${clientId}`, error);
    }
  }

  async getByRoom(roomId, page = 0, size = 10) {
    try {
      return await window.http.get(this.api.BY_ROOM(roomId), { page, size });
    } catch (error) {
      this.#handleError(`get hardware configs for room ${roomId}`, error);
    }
  }

  #handleError(action, error) {
    console.error(`[HardwareConfigApiV1Service] Failed to ${action}:`, error);
    throw error;
  }
}

if (typeof window === 'undefined')
  throw new Error('HardwareConfigApiV1Service can only be initialized in a browser environment');
window.hardwareConfigApiV1Service = new HardwareConfigApiV1Service();
