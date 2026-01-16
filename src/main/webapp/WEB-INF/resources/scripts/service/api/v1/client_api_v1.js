class ClientApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
	}

	async getAll() {
		try {
			return await this.client.get('clients');
		} catch (error) {
			console.error('[ClientService] Failed to fetch clients', error);
			throw error;
		}
	}

	async getById(clientId) {
		try {
			return await this.client.get(`clients/${clientId}`);
		} catch (error) {
			console.error(`[ClientService] Failed to fetch client ${clientId}`, error);
			throw error;
		}
	}

	async create(clientData) {
		try {
			return await this.client.post('clients', clientData);
		} catch (error) {
			console.error('[ClientService] Failed to create client', error);
			throw error;
		}
	}

	async update(clientId, updateData) {
		try {
			return await this.client.put(`clients/${clientId}`, updateData);
		} catch (error) {
			console.error(`[ClientService] Failed to update client ${clientId}`, error);
			throw error;
		}
	}

	async delete(clientId) {
		try {
			return await this.client.delete(`clients/${clientId}`);
		} catch (error) {
			console.error(`[ClientService] Failed to delete client ${clientId}`, error);
			throw error;
		}
	}

	async getGroupsStatus(clientId) {
		try {
			return await this.client.get(`groups/with-client-status/${clientId}`);
		} catch (error) {
			console.error(`[ClientService] Failed to get groups status for client ${clientId}`, error);
			throw error;
		}
	}
}

window.ClientApiV1Service = ClientApiV1Service;
