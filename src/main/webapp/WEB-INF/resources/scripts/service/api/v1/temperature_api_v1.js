class TemperatureApiV1Service {
  static instance;

  constructor() {
    if (TemperatureApiV1Service.instance) return TemperatureApiV1Service.instance;
    TemperatureApiV1Service.instance = this;

    this.api = SMRC_API_V1.TEMPERATURE;
  }

  async getList(page = 0, size = 20) {
    try {
      return await window.http.get(this.api.PATH, { page, size });
    } catch (error) {
      this.#handleError('get paginated temperature sensors', error);
    }
  }

  async getAll() {
    try {
      return await window.http.get(this.api.ALL);
    } catch (error) {
      this.#handleError('get all temperature sensors', error);
    }
  }

  async getListByRoom(roomId, page = 0, size = 20) {
    try {
      return await window.http.get(this.api.BY_ROOM(roomId), { page, size });
    } catch (error) {
      this.#handleError(`get temperature sensors for room ${roomId}`, error);
    }
  }

  async getAllByRoom(roomId) {
    try {
      return await window.http.get(this.api.ALL_BY_ROOM(roomId));
    } catch (error) {
      this.#handleError(`get all temperature sensors for room ${roomId}`, error);
    }
  }

  async getById(id) {
    try {
      return await window.http.get(this.api.DETAIL(id));
    } catch (error) {
      this.#handleError(`get temperature sensor with ID ${id}`, error);
    }
  }

  async create(dto) {
    try {
      return await window.http.post(this.api.PATH, dto);
    } catch (error) {
      this.#handleError('create temperature sensor', error);
    }
  }

  async update(id, dto) {
    try {
      return await window.http.put(this.api.DETAIL(id), dto);
    } catch (error) {
      this.#handleError(`update temperature sensor with ID ${id}`, error);
    }
  }

  async delete(id) {
    try {
      return await window.http.delete(this.api.DETAIL(id));
    } catch (error) {
      this.#handleError(`delete temperature sensor with ID ${id}`, error);
    }
  }

  #handleError(action, error) {
    console.error(`[TemperatureApiV1Service] Failed to ${action}:`, error);
    throw error;
  }
}

if (typeof window === 'undefined')
  throw new Error('TemperatureApiV1Service can only be initialized in a browser environment');
window.temperatureApiV1Service = new TemperatureApiV1Service();
