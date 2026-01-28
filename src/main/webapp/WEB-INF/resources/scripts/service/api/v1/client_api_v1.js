class ClientApiV1Service {
	static instance;

	constructor() {
		if (ClientApiV1Service.instance) return ClientApiV1Service.instance;
		ClientApiV1Service.instance = this;

		this.api = SMRC_API_V1.CLIENT;
	}

	async getAll(page = 0, size = 10) {
		try {
			return await window.http.get(this.api.PATH, { page, size });
		} catch (error) {
			this.#handleError('fetch clients', error);
		}
	}

	async getById(clientId) {
		try {
			return await window.http.get(this.api.DETAIL(clientId));
		} catch (error) {
			this.#handleError(`fetch client ${clientId}`, error);
		}
	}

	async getByRoomId(roomId, page = 0, size = 10) {
		try {
			return await window.http.get(this.api.BY_ROOM(roomId), { page, size });
		} catch (error) {
			this.#handleError(`fetch clients for room ${roomId}`, error);
		}
	}

	async create(clientData) {
		try {
			return await window.http.post(this.api.PATH, clientData);
		} catch (error) {
			this.#handleError('create client', error);
		}
	}

	async update(clientId, updateData) {
		try {
			return await window.http.put(this.api.DETAIL(clientId), updateData);
		} catch (error) {
			this.#handleError(`update client ${clientId}`, error);
		}
	}

	async delete(clientId) {
		try {
			return await window.http.delete(this.api.DETAIL(clientId));
		} catch (error) {
			this.#handleError(`delete client ${clientId}`, error);
		}
	}

	#handleError(action, error) {
		console.error(`[ClientApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

if (typeof window == 'undefined') throw new Error('ClientApiV1Service can only be initialized in a browser environment');
window.clientApiV1Service = new ClientApiV1Service();
