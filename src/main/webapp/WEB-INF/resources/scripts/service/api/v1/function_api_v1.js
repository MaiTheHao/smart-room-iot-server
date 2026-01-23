class FunctionApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
		this.api = SMRC_API_V1.FUNCTION;
	}

	async getAll(page = 0, size = 10) {
		try {
			return await this.client.get(this.api.PATH, { page, size });
		} catch (error) {
			this._handleError('fetch functions', error);
		}
	}

	async getAllWithoutPagination() {
		try {
			return await this.client.get(this.api.ALL);
		} catch (error) {
			this._handleError('fetch all functions', error);
		}
	}

	async getById(functionId) {
		try {
			return await this.client.get(this.api.DETAIL(functionId));
		} catch (error) {
			this._handleError(`fetch function ${functionId}`, error);
		}
	}

	async getByCode(functionCode) {
		try {
			return await this.client.get(this.api.BY_CODE(functionCode));
		} catch (error) {
			this._handleError(`fetch function by code ${functionCode}`, error);
		}
	}

	async getByGroupStatus(groupId) {
		try {
			return await this.client.get(this.api.WITH_GROUP_STATUS(groupId));
		} catch (error) {
			this._handleError(`fetch functions status for group ${groupId}`, error);
		}
	}

	async create(functionData) {
		try {
			return await this.client.post(this.api.PATH, functionData);
		} catch (error) {
			this._handleError('create function', error);
		}
	}

	async update(functionId, updateData) {
		try {
			return await this.client.put(this.api.DETAIL(functionId), updateData);
		} catch (error) {
			this._handleError(`update function ${functionId}`, error);
		}
	}

	async delete(functionId) {
		try {
			return await this.client.delete(this.api.DETAIL(functionId));
		} catch (error) {
			this._handleError(`delete function ${functionId}`, error);
		}
	}

	async getCount() {
		try {
			return await this.client.get(this.api.COUNT);
		} catch (error) {
			this._handleError('fetch function count', error);
		}
	}

	_handleError(action, error) {
		console.error(`[FunctionApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

window.FunctionApiV1Service = FunctionApiV1Service;
