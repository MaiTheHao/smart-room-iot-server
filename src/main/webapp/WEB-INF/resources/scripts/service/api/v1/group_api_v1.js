class GroupApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
		this.api = SMRC_API_V1.GROUP;
	}

	async getAll(page = 0, size = 10) {
		try {
			return await this.client.get(this.api.PATH, { page, size });
		} catch (error) {
			this._handleError('fetch paginated groups', error);
		}
	}

	async getAllWithoutPagination() {
		try {
			return await this.client.get(this.api.ALL);
		} catch (error) {
			this._handleError('fetch all groups', error);
		}
	}

	async getById(groupId) {
		try {
			return await this.client.get(this.api.DETAIL(groupId));
		} catch (error) {
			this._handleError(`fetch group ${groupId}`, error);
		}
	}

	async getByCode(groupCode) {
		try {
			return await this.client.get(this.api.BY_CODE(groupCode));
		} catch (error) {
			this._handleError(`fetch group code ${groupCode}`, error);
		}
	}

	async getWithClientStatus(clientId) {
		try {
			return await this.client.get(this.api.WITH_CLIENT_STATUS(clientId));
		} catch (error) {
			this._handleError(`fetch groups status for client ${clientId}`, error);
		}
	}

	async create(groupData) {
		try {
			return await this.client.post(this.api.PATH, groupData);
		} catch (error) {
			this._handleError('create group', error);
		}
	}

	async update(groupId, updateData) {
		try {
			return await this.client.put(this.api.DETAIL(groupId), updateData);
		} catch (error) {
			this._handleError(`update group ${groupId}`, error);
		}
	}

	async delete(groupId) {
		try {
			return await this.client.delete(this.api.DETAIL(groupId));
		} catch (error) {
			this._handleError(`delete group ${groupId}`, error);
		}
	}

	async getFunctionsAll(groupId) {
		try {
			return await this.client.get(this.api.FUNCTIONS.ALL(groupId));
		} catch (error) {
			this._handleError(`fetch all functions for group ${groupId}`, error);
		}
	}

	async getFunctionsPaginated(groupId, page = 0, size = 10) {
		try {
			return await this.client.get(this.api.FUNCTIONS.ROOT(groupId), { page, size });
		} catch (error) {
			this._handleError(`fetch paginated functions for group ${groupId}`, error);
		}
	}

	async getClientsAll(groupId) {
		try {
			return await this.client.get(this.api.CLIENTS.ALL(groupId));
		} catch (error) {
			this._handleError(`fetch all clients for group ${groupId}`, error);
		}
	}

	async getClientsPaginated(groupId, page = 0, size = 10) {
		try {
			return await this.client.get(this.api.CLIENTS.ROOT(groupId), { page, size });
		} catch (error) {
			this._handleError(`fetch paginated clients for group ${groupId}`, error);
		}
	}

	async getCount() {
		try {
			const response = await this.client.get(this.api.COUNT);
			return response.data;
		} catch (error) {
			this._handleError('get group count', error);
		}
	}

	async getFunctionsCount(groupId) {
		try {
			const response = await this.client.get(this.api.FUNCTIONS.COUNT(groupId));
			return response.data;
		} catch (error) {
			this._handleError(`get function count for group ${groupId}`, error);
		}
	}

	async getClientsCount(groupId) {
		try {
			const response = await this.client.get(this.api.CLIENTS.COUNT(groupId));
			return response.data;
		} catch (error) {
			this._handleError(`get client count for group ${groupId}`, error);
		}
	}

	async getClientGroupsAll(clientId) {
		try {
			return await this.client.get(SMRC_API_V1.CLIENT.GROUPS.ALL(clientId));
		} catch (error) {
			this._handleError(`fetch all groups for client ${clientId}`, error);
		}
	}

	async getClientGroupsPaginated(clientId, page = 0, size = 10) {
		try {
			return await this.client.get(SMRC_API_V1.CLIENT.GROUPS.ROOT(clientId), { page, size });
		} catch (error) {
			this._handleError(`fetch paginated groups for client ${clientId}`, error);
		}
	}

	async getClientGroupsCount(clientId) {
		try {
			const response = await this.client.get(SMRC_API_V1.CLIENT.GROUPS.COUNT(clientId));
			return response.data;
		} catch (error) {
			this._handleError(`get group count for client ${clientId}`, error);
		}
	}

	_handleError(action, error) {
		console.error(`[GroupApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

window.GroupApiV1Service = GroupApiV1Service;
