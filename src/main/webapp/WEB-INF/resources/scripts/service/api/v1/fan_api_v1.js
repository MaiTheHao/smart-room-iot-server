class FanApiV1Service {
  static instance;

  constructor() {
    if (FanApiV1Service.instance) return FanApiV1Service.instance;
    FanApiV1Service.instance = this;

    this.api = SMRC_API_V1.FAN;
  }

  async getAll(page = 0, size = 20) {
    try {
      return await window.http.get(this.api.PATH, { page, size });
    } catch (error) {
      this.#handleError('get all fans', error);
    }
  }

  async getAllWithoutPaging() {
    try {
      return await window.http.get(this.api.ALL);
    } catch (error) {
      this.#handleError('get all fans without paging', error);
    }
  }

  async getByRoom(roomId, page = 0, size = 20) {
    try {
      return await window.http.get(this.api.BY_ROOM(roomId), { page, size });
    } catch (error) {
      this.#handleError(`get fans for room ${roomId}`, error);
    }
  }

  async getAllByRoom(roomId) {
    try {
      return await window.http.get(this.api.ALL_BY_ROOM(roomId));
    } catch (error) {
      this.#handleError(`get all fans for room ${roomId}`, error);
    }
  }

  async getByRoomAndNaturalId(roomId, naturalId) {
    try {
      return await window.http.get(this.api.BY_ROOM_NATURAL_ID(roomId, naturalId));
    } catch (error) {
      this.#handleError(`get fan for room ${roomId} with natural ID ${naturalId}`, error);
    }
  }

  async getById(id) {
    try {
      return await window.http.get(this.api.DETAIL(id));
    } catch (error) {
      this.#handleError(`get fan ${id}`, error);
    }
  }

  async create(fanData) {
    try {
      return await window.http.post(this.api.PATH, fanData);
    } catch (error) {
      this.#handleError('create fan', error);
    }
  }

  async update(id, fanData) {
    try {
      return await window.http.put(this.api.DETAIL(id), fanData);
    } catch (error) {
      this.#handleError(`update fan ${id}`, error);
    }
  }

  async delete(id) {
    try {
      return await window.http.delete(this.api.DETAIL(id));
    } catch (error) {
      this.#handleError(`delete fan ${id}`, error);
    }
  }

  async control(naturalId, { power, mode, speed, swing, light }) {
    try {
      return await window.http.put(this.api.CONTROL(naturalId), {
        power,
        mode,
        speed,
        swing,
        light,
      });
    } catch (error) {
      this.#handleError(`control fan with natural ID ${naturalId}`, error);
    }
  }

  #handleError(action, error) {
    console.error(`[FanApiV1Service] Failed to ${action}:`, error);
    throw error;
  }
}

if (typeof window == 'undefined')
  throw new Error('FanApiV1Service can only be initialized in a browser environment');
window.fanApiV1Service = new FanApiV1Service();
