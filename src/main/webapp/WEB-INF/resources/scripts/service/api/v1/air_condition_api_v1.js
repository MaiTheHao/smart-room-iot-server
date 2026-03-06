class AirConditionApiV1Service {
  static instance;

  constructor() {
    if (AirConditionApiV1Service.instance) return AirConditionApiV1Service.instance;
    AirConditionApiV1Service.instance = this;

    this.api = SMRC_API_V1.AIR_CONDITION;
  }

  async getAll(page = 0, size = 20) {
    try {
      return await window.http.get(this.api.PATH, { page, size });
    } catch (error) {
      this.#handleError('get all air conditions', error);
    }
  }

  async getByRoom(roomId, page = 0, size = 20) {
    try {
      return await window.http.get(this.api.BY_ROOM(roomId), { page, size });
    } catch (error) {
      this.#handleError(`get air conditions for room ${roomId}`, error);
    }
  }

  async getAllByRoom(roomId) {
    try {
      return await window.http.get(this.api.ALL_BY_ROOM(roomId));
    } catch (error) {
      this.#handleError(`get all air conditions for room ${roomId}`, error);
    }
  }

  async getById(acId) {
    try {
      return await window.http.get(this.api.DETAIL(acId));
    } catch (error) {
      this.#handleError(`get air condition ${acId}`, error);
    }
  }

  async create(acData) {
    try {
      return await window.http.post(this.api.PATH, acData);
    } catch (error) {
      this.#handleError('create air condition', error);
    }
  }

  async update(acId, acData) {
    try {
      return await window.http.put(this.api.DETAIL(acId), acData);
    } catch (error) {
      this.#handleError(`update air condition ${acId}`, error);
    }
  }

  async delete(acId) {
    try {
      return await window.http.delete(this.api.DETAIL(acId));
    } catch (error) {
      this.#handleError(`delete air condition ${acId}`, error);
    }
  }

  async control(naturalId, { power, temperature, mode, fanSpeed, swing }) {
    try {
      return await window.http.put(this.api.CONTROL(naturalId), {
        power,
        temperature,
        mode,
        fanSpeed,
        swing,
      });
    } catch (error) {
      this.#handleError(`control AC with natural ID ${naturalId}`, error);
    }
  }

  #handleError(action, error) {
    console.error(`[AirConditionApiV1Service] Failed to ${action}:`, error);
    throw error;
  }
}

if (typeof window == 'undefined')
  throw new Error('AirConditionApiV1Service can only be initialized in a browser environment');
window.airConditionApiV1Service = new AirConditionApiV1Service();
