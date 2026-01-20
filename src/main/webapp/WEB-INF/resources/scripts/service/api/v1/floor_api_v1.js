class FloorApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
		this.baseUrl = 'floors';
	}

	async getAll(page = 0, size = 100) {
		try {
			const response = await this.client.get(this.baseUrl, { page, size });
			return response.data;
		} catch (error) {
			console.error('[FloorService] Failed to fetch floors', error);
			throw error;
		}
	}

	async getById(id) {
		try {
			const response = await this.client.get(`${this.baseUrl}/${id}`);
			return response.data;
		} catch (error) {
			console.error(`[FloorService] Failed to fetch floor ${id}`, error);
			throw error;
		}
	}
}

window.FloorApiV1Service = FloorApiV1Service;
