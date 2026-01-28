class LightApiV1Service {
	static instance;

	constructor() {
		if (LightApiV1Service.instance) return LightApiV1Service.instance;
		LightApiV1Service.instance = this;

		this.api = SMRC_API_V1.LIGHT;
	}

	async getAll(page = 0, size = 20) {
		try {
			return await window.http.get(this.api.PATH, { page, size });
		} catch (error) {
			this.#handleError('get all lights', error);
		}
	}

	async getByRoom(roomId, page = 0, size = 20) {
		try {
			return await window.http.get(this.api.BY_ROOM(roomId), { page, size });
		} catch (error) {
			this.#handleError(`get lights for room ${roomId}`, error);
		}
	}

	async getAllByRoom(roomId) {
		try {
			return await window.http.get(this.api.ALL_BY_ROOM(roomId));
		} catch (error) {
			this.#handleError(`get all lights for room ${roomId}`, error);
		}
	}

	async getById(id) {
		try {
			return await window.http.get(this.api.DETAIL(id));
		} catch (error) {
			this.#handleError(`get light ${id}`, error);
		}
	}

	async create(lightData) {
		try {
			return await window.http.post(this.api.PATH, lightData);
		} catch (error) {
			this.#handleError('create light', error);
		}
	}

	async update(id, lightData) {
		try {
			return await window.http.put(this.api.DETAIL(id), lightData);
		} catch (error) {
			this.#handleError(`update light ${id}`, error);
		}
	}

	async delete(id) {
		try {
			return await window.http.delete(this.api.DETAIL(id));
		} catch (error) {
			this.#handleError(`delete light ${id}`, error);
		}
	}

	async toggleState(id) {
		try {
			return await window.http.put(this.api.TOGGLE(id));
		} catch (error) {
			this.#handleError(`toggle state for light ${id}`, error);
		}
	}

	async setLevel(id, newLevel) {
		try {
			return await window.http.put(this.api.LEVEL(id, newLevel));
		} catch (error) {
			this.#handleError(`set level for light ${id} to ${newLevel}`, error);
		}
	}

	#handleError(action, error) {
		console.error(`[LightApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

if (typeof window == 'undefined') throw new Error('LightApiV1Service can only be initialized in a browser environment');
window.lightApiV1Service = new LightApiV1Service();
