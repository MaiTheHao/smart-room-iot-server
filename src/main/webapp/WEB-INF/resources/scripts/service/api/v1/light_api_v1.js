class LightApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
		this.api = SMRC_API_V1.LIGHT;
	}

	async getAll(page = 0, size = 20) {
		try {
			return await this.client.get(this.api.BASE, { page, size });
		} catch (error) {
			this._handleError('get all lights', error);
		}
	}

	async getByRoom(roomId, page = 0, size = 20) {
		try {
			return await this.client.get(this.api.BY_ROOM(roomId), { page, size });
		} catch (error) {
			this._handleError(`get lights for room ${roomId}`, error);
		}
	}

	async getById(id) {
		try {
			return await this.client.get(this.api.DETAIL(id));
		} catch (error) {
			this._handleError(`get light ${id}`, error);
		}
	}

	async create(lightData) {
		try {
			return await this.client.post(this.api.BASE, lightData);
		} catch (error) {
			this._handleError('create light', error);
		}
	}

	async update(id, lightData) {
		try {
			return await this.client.put(this.api.DETAIL(id), lightData);
		} catch (error) {
			this._handleError(`update light ${id}`, error);
		}
	}

	async delete(id) {
		try {
			return await this.client.delete(this.api.DETAIL(id));
		} catch (error) {
			this._handleError(`delete light ${id}`, error);
		}
	}

	async toggleState(id) {
		try {
			return await this.client.put(this.api.TOGGLE(id));
		} catch (error) {
			this._handleError(`toggle state for light ${id}`, error);
		}
	}

	async setLevel(id, newLevel) {
		try {
			return await this.client.put(this.api.LEVEL(id, newLevel));
		} catch (error) {
			this._handleError(`set level for light ${id} to ${newLevel}`, error);
		}
	}

	_handleError(action, error) {
		console.error(`[LightApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

window.LightApiV1Service = LightApiV1Service;
