class ClientApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
		this.api = SMRC_API_V1.CLIENT;
	}

	async getAll(page = 0, size = 10) {
		try {
			return await this.client.get(this.api.PATH, { page, size });
		} catch (error) {
			this._handleError('fetch clients', error);
		}
	}

	async getById(clientId) {
		try {
			return await this.client.get(this.api.DETAIL(clientId));
		} catch (error) {
			this._handleError(`fetch client ${clientId}`, error);
		}
	}

	async getByRoomId(roomId, page = 0, size = 10) {
		try {
			return await this.client.get(this.api.BY_ROOM(roomId), { page, size });
		} catch (error) {
			this._handleError(`fetch clients for room ${roomId}`, error);
		}
	}

	async create(clientData) {
		try {
			return await this.client.post(this.api.PATH, clientData);
		} catch (error) {
			this._handleError('create client', error);
		}
	}

	async update(clientId, updateData) {
		try {
			return await this.client.put(this.api.DETAIL(clientId), updateData);
		} catch (error) {
			this._handleError(`update client ${clientId}`, error);
		}
	}

	async delete(clientId) {
		try {
			return await this.client.delete(this.api.DETAIL(clientId));
		} catch (error) {
			this._handleError(`delete client ${clientId}`, error);
		}
	}

	_handleError(action, error) {
		console.error(`[ClientApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

window.ClientApiV1Service = ClientApiV1Service;
