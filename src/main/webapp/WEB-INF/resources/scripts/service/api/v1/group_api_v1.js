class GroupApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
	}

	async getAll() {
		try {
			return await this.client.get('groups/all');
		} catch (error) {
			console.error('[GroupService] Failed to fetch all groups', error);
			throw error;
		}
	}

	async getById(groupId) {
		try {
			return await this.client.get(`groups/${groupId}`);
		} catch (error) {
			console.error(`[GroupService] Failed to fetch group ${groupId}`, error);
			throw error;
		}
	}

	async create(groupData) {
		try {
			return await this.client.post('groups', groupData);
		} catch (error) {
			console.error('[GroupService] Failed to create group', error);
			throw error;
		}
	}

	async update(groupId, updateData) {
		try {
			return await this.client.put(`groups/${groupId}`, updateData);
		} catch (error) {
			console.error(`[GroupService] Failed to update group ${groupId}`, error);
			throw error;
		}
	}

	async delete(groupId) {
		try {
			return await this.client.delete(`groups/${groupId}`);
		} catch (error) {
			console.error(`[GroupService] Failed to delete group ${groupId}`, error);
			throw error;
		}
	}

	async getClientCount(groupId) {
		try {
			const response = await this.client.get(`groups/${groupId}/clients/count`);
			return response.data;
		} catch (error) {
			console.error(`[GroupService] Failed to get client count for group ${groupId}`, error);
			throw error;
		}
	}

	async getClients(groupId) {
		try {
			return await this.client.get(`groups/${groupId}/clients/all`);
		} catch (error) {
			console.error(`[GroupService] Failed to fetch clients for group ${groupId}`, error);
			throw error;
		}
	}

	async removeClient(clientId, groupId) {
		try {
			return await this.client.delete(`roles/clients/${clientId}/groups/${groupId}`);
		} catch (error) {
			console.error(`[GroupService] Failed to remove client ${clientId} from group ${groupId}`, error);
			throw error;
		}
	}
}

window.GroupApiV1Service = GroupApiV1Service;
