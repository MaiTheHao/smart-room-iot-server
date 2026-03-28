class FloorApiV1Service {
	static instance;

	constructor() {
		if (FloorApiV1Service.instance) return FloorApiV1Service.instance;
		FloorApiV1Service.instance = this;

		this.api = SMRC_API_V1.FLOOR;
	}

	async getAll(page = 0, size = 10) {
		try {
			return await window.http.get(this.api.PATH, { page, size });
		} catch (error) {
			this.#handleError('fetch floors', error);
		}
	}

	async getAllWithoutPagination() {
		try {
			return await window.http.get(SMRC_API_V1.FLOOR.ALL);
		} catch (error) {
			this.#handleError('fetch all floors', error);
		}
	}

	async getById(id) {
		try {
			return await window.http.get(this.api.DETAIL(id));
		} catch (error) {
			this.#handleError(`fetch floor ${id}`, error);
		}
	}

	async create(floorData) {
		try {
			return await window.http.post(this.api.PATH, floorData);
		} catch (error) {
			this.#handleError('create floor', error);
		}
	}

	async update(id, floorData) {
		try {
			return await window.http.put(this.api.DETAIL(id), floorData);
		} catch (error) {
			this.#handleError(`update floor ${id}`, error);
		}
	}

	async delete(id) {
		try {
			return await window.http.delete(this.api.DETAIL(id));
		} catch (error) {
			this.#handleError(`delete floor ${id}`, error);
		}
	}

	#handleError(action, error) {
		console.error(`[FloorApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

if (typeof window == 'undefined') throw new Error('FloorApiV1Service can only be initialized in a browser environment');
window.floorApiV1Service = new FloorApiV1Service();
