class PowerConsumptionApiV1Service {
  static instance;

  constructor() {
    if (PowerConsumptionApiV1Service.instance) return PowerConsumptionApiV1Service.instance;
    PowerConsumptionApiV1Service.instance = this;

    this.api = SMRC_API_V1.POWER_CONSUMPTION;
  }

  async getList(page = 0, size = 20) {
    try {
      return await window.http.get(this.api.PATH, { page, size });
    } catch (error) {
      this.#handleError('get paginated power consumption sensors', error);
    }
  }

  async getAll() {
    try {
      return await window.http.get(this.api.ALL);
    } catch (error) {
      this.#handleError('get all power consumption sensors', error);
    }
  }

  async getListByRoom(roomId, page = 0, size = 20) {
    try {
      return await window.http.get(this.api.BY_ROOM(roomId), { page, size });
    } catch (error) {
      this.#handleError(`get power consumption sensors for room ${roomId}`, error);
    }
  }

  async getAllByRoom(roomId) {
    try {
      return await window.http.get(this.api.ALL_BY_ROOM(roomId));
    } catch (error) {
      this.#handleError(`get all power consumption sensors for room ${roomId}`, error);
    }
  }

  async getById(id) {
    try {
      return await window.http.get(this.api.DETAIL(id));
    } catch (error) {
      this.#handleError(`get power consumption sensor with ID ${id}`, error);
    }
  }

  async create(dto) {
    try {
      return await window.http.post(this.api.PATH, dto);
    } catch (error) {
      this.#handleError('create power consumption sensor', error);
    }
  }

  async update(id, dto) {
    try {
      return await window.http.put(this.api.DETAIL(id), dto);
    } catch (error) {
      this.#handleError(`update power consumption sensor with ID ${id}`, error);
    }
  }

  async delete(id) {
    try {
      return await window.http.delete(this.api.DETAIL(id));
    } catch (error) {
      this.#handleError(`delete power consumption sensor with ID ${id}`, error);
    }
  }

  #handleError(action, error) {
    console.error(`[PowerConsumptionApiV1Service] Failed to ${action}:`, error);
    throw error;
  }
}

if (typeof window === 'undefined')
  throw new Error('PowerConsumptionApiV1Service can only be initialized in a browser environment');
window.powerConsumptionApiV1Service = new PowerConsumptionApiV1Service();
