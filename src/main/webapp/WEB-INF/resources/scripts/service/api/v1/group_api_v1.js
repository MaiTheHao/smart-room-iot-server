class GroupApiV1Service {
	static instance;

	constructor() {
		if (GroupApiV1Service.instance) return GroupApiV1Service.instance;
		GroupApiV1Service.instance = this;
		this.api = SMRC_API_V1.GROUP;
	}

	async getAll(page = 0, size = 10) {
		try {
			return await window.http.get(this.api.PATH, { page, size });
		} catch (error) {
			this.#handleError('fetch paginated groups', error);
		}
	}

	async getAllWithoutPagination() {
		try {
			return await window.http.get(this.api.ALL);
		} catch (error) {
			this.#handleError('fetch all groups', error);
		}
	}

	async getById(groupId) {
		try {
			return await window.http.get(this.api.DETAIL(groupId));
		} catch (error) {
			this.#handleError(`fetch group ${groupId}`, error);
		}
	}

	async getByCode(groupCode) {
		try {
			return await window.http.get(this.api.BY_CODE(groupCode));
		} catch (error) {
			this.#handleError(`fetch group code ${groupCode}`, error);
		}
	}

	async getWithClientStatus(clientId) {
		try {
			return await window.http.get(this.api.WITH_CLIENT_STATUS(clientId));
		} catch (error) {
			this.#handleError(`fetch groups status for client ${clientId}`, error);
		}
	}

	async create(groupData) {
		try {
			return await window.http.post(this.api.PATH, groupData);
		} catch (error) {
			this.#handleError('create group', error);
		}
	}

	async update(groupId, updateData) {
		try {
			return await window.http.put(this.api.DETAIL(groupId), updateData);
		} catch (error) {
			this.#handleError(`update group ${groupId}`, error);
		}
	}

	async delete(groupId) {
		try {
			return await window.http.delete(this.api.DETAIL(groupId));
		} catch (error) {
			this.#handleError(`delete group ${groupId}`, error);
		}
	}

	async getFunctionsAll(groupId) {
		try {
			return await window.http.get(SMRC_API_V1.FUNCTION.BY_GROUP_ALL(groupId));
		} catch (error) {
			this.#handleError(`fetch all functions for group ${groupId}`, error);
		}
	}

	async getFunctionsPaginated(groupId, page = 0, size = 10) {
		try {
			return await window.http.get(SMRC_API_V1.FUNCTION.BY_GROUP(groupId), { page, size });
		} catch (error) {
			this.#handleError(`fetch paginated functions for group ${groupId}`, error);
		}
	}

	async getClientsAll(groupId) {
		try {
			return await window.http.get(SMRC_API_V1.CLIENT.BY_GROUP_ALL(groupId));
		} catch (error) {
			this.#handleError(`fetch all clients for group ${groupId}`, error);
		}
	}

	async getClientsPaginated(groupId, page = 0, size = 10) {
		try {
			return await window.http.get(SMRC_API_V1.CLIENT.BY_GROUP(groupId), { page, size });
		} catch (error) {
			this.#handleError(`fetch paginated clients for group ${groupId}`, error);
		}
	}

	async getCount() {
		try {
			const response = await window.http.get(this.api.COUNT);
			return response.data;
		} catch (error) {
			this.#handleError('get group count', error);
		}
	}

	async getFunctionsCount(groupId) {
		try {
			const response = await window.http.get(SMRC_API_V1.FUNCTION.BY_GROUP_COUNT(groupId));
			return response.data;
		} catch (error) {
			this.#handleError(`get function count for group ${groupId}`, error);
		}
	}

	async getClientsCount(groupId) {
		try {
			const response = await window.http.get(SMRC_API_V1.CLIENT.BY_GROUP_COUNT(groupId));
			return response.data;
		} catch (error) {
			this.#handleError(`get client count for group ${groupId}`, error);
		}
	}

	async getClientGroupsAll(clientId) {
		try {
			return await window.http.get(SMRC_API_V1.GROUP.FOR_CLIENT_ALL(clientId));
		} catch (error) {
			this.#handleError(`fetch all groups for client ${clientId}`, error);
		}
	}

	async getClientGroupsPaginated(clientId, page = 0, size = 10) {
		try {
			return await window.http.get(SMRC_API_V1.GROUP.FOR_CLIENT(clientId), { page, size });
		} catch (error) {
			this.#handleError(`fetch paginated groups for client ${clientId}`, error);
		}
	}

	async getClientGroupsCount(clientId) {
		try {
			const response = await window.http.get(SMRC_API_V1.GROUP.FOR_CLIENT_COUNT(clientId));
			return response.data;
		} catch (error) {
			this.#handleError(`get group count for client ${clientId}`, error);
		}
	}

	#handleError(action, error) {
		console.error(`[GroupApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

if (typeof window == 'undefined') throw new Error('GroupApiV1Service can only be initialized in a browser environment');
window.groupApiV1Service = new GroupApiV1Service();
