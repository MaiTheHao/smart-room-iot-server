class FunctionApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
	}

	async getAll() {
		try {
			return await this.client.get('functions/all');
		} catch (error) {
			console.error('[FunctionService] Failed to fetch all functions', error);
			throw error;
		}
	}

	async getById(functionId) {
		try {
			return await this.client.get(`functions/${functionId}`);
		} catch (error) {
			console.error(`[FunctionService] Failed to fetch function ${functionId}`, error);
			throw error;
		}
	}

	async create(functionData) {
		try {
			return await this.client.post('functions', functionData);
		} catch (error) {
			console.error('[FunctionService] Failed to create function', error);
			throw error;
		}
	}

	async update(functionId, updateData) {
		try {
			return await this.client.put(`functions/${functionId}`, updateData);
		} catch (error) {
			console.error(`[FunctionService] Failed to update function ${functionId}`, error);
			throw error;
		}
	}

	async delete(functionId) {
		try {
			return await this.client.delete(`functions/${functionId}`);
		} catch (error) {
			console.error(`[FunctionService] Failed to delete function ${functionId}`, error);
			throw error;
		}
	}

	async getByGroupStatus(groupId) {
		try {
			return await this.client.get(`functions/with-group-status/${groupId}`);
		} catch (error) {
			console.error(`[FunctionService] Failed to fetch functions status for group ${groupId}`, error);
			throw error;
		}
	}
}

window.FunctionApiV1Service = FunctionApiV1Service;
