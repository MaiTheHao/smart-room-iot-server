class FloorApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
		this.api = SMRC_API_V1.FLOOR;
	}

	async getAll(page = 0, size = 10) {
		try {
			const response = await this.client.get(this.api.PATH, { page, size });
			return response.data;
		} catch (error) {
			this._handleError('fetch floors', error);
		}
	}

	async getById(id) {
		try {
			const response = await this.client.get(this.api.DETAIL(id));
			return response.data;
		} catch (error) {
			this._handleError(`fetch floor ${id}`, error);
		}
	}

	async create(floorData) {
		try {
			const response = await this.client.post(this.api.PATH, floorData);
			return response.data;
		} catch (error) {
			this._handleError('create floor', error);
		}
	}

	async update(id, floorData) {
		try {
			const response = await this.client.put(this.api.DETAIL(id), floorData);
			return response.data;
		} catch (error) {
			this._handleError(`update floor ${id}`, error);
		}
	}

	async delete(id) {
		try {
			const response = await this.client.delete(this.api.DETAIL(id));
			return response.data;
		} catch (error) {
			this._handleError(`delete floor ${id}`, error);
		}
	}

	_handleError(action, error) {
		console.error(`[FloorApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

window.FloorApiV1Service = FloorApiV1Service;
