class FunctionApiV1Service {
	static instance;

	constructor() {
		if (FunctionApiV1Service.instance) return FunctionApiV1Service.instance;
		FunctionApiV1Service.instance = this;

		this.client = new HttpClient('/api/v1');
		this.api = SMRC_API_V1.FUNCTION;
	}

	async getAll(page = 0, size = 10) {
		try {
			return await this.client.get(this.api.PATH, { page, size });
		} catch (error) {
			this.#handleError('fetch functions', error);
		}
	}

	async getAllWithoutPagination() {
		try {
			return await this.client.get(this.api.ALL);
		} catch (error) {
			this.#handleError('fetch all functions', error);
		}
	}

	async getById(functionId) {
		try {
			return await this.client.get(this.api.DETAIL(functionId));
		} catch (error) {
			this.#handleError(`fetch function ${functionId}`, error);
		}
	}

	async getByCode(functionCode) {
		try {
			return await this.client.get(this.api.BY_CODE(functionCode));
		} catch (error) {
			this.#handleError(`fetch function by code ${functionCode}`, error);
		}
	}

	async getByGroupStatus(groupId) {
		try {
			return await this.client.get(this.api.WITH_GROUP_STATUS(groupId));
		} catch (error) {
			this.#handleError(`fetch functions status for group ${groupId}`, error);
		}
	}

	async create(functionData) {
		try {
			return await this.client.post(this.api.PATH, functionData);
		} catch (error) {
			this.#handleError('create function', error);
		}
	}

	async update(functionId, updateData) {
		try {
			return await this.client.put(this.api.DETAIL(functionId), updateData);
		} catch (error) {
			this.#handleError(`update function ${functionId}`, error);
		}
	}

	async delete(functionId) {
		try {
			return await this.client.delete(this.api.DETAIL(functionId));
		} catch (error) {
			this.#handleError(`delete function ${functionId}`, error);
		}
	}

	async getCount() {
		try {
			return await this.client.get(this.api.COUNT);
		} catch (error) {
			this.#handleError('fetch function count', error);
		}
	}

	#handleError(action, error) {
		console.error(`[FunctionApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

if (typeof window == 'undefined') throw new Error('FunctionApiV1Service can only be initialized in a browser environment');
window.functionApiV1Service = new FunctionApiV1Service();
