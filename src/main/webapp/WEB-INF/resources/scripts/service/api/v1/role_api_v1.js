class RoleApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
	}

	async getClientGroupsStatus(clientId) {
		try {
			return await this.client.get(`groups/with-client-status/${clientId}`);
		} catch (error) {
			console.error(`[RoleService] Failed to fetch groups status for client ${clientId}`, error);
			throw error;
		}
	}

	async assignClient(clientId, groupId) {
		try {
			return await this.client.post(`roles/clients/${clientId}/groups`, {
				groupId: groupId,
			});
		} catch (error) {
			console.error(`[RoleService] Failed to assign client ${clientId} to group ${groupId}`, error);
			throw error;
		}
	}

	async unassignClient(clientId, groupId) {
		try {
			return await this.client.delete(`roles/clients/${clientId}/groups/${groupId}`);
		} catch (error) {
			console.error(`[RoleService] Failed to unassign client ${clientId} from group ${groupId}`, error);
			throw error;
		}
	}

	async getGroupFunctionsStatus(groupId) {
		try {
			return await this.client.get(`functions/with-group-status/${groupId}`);
		} catch (error) {
			console.error(`[RoleService] Failed to fetch functions status for group ${groupId}`, error);
			throw error;
		}
	}

	async toggleFunctions(payload) {
		try {
			return await this.client.post('roles/groups/functions/toggle', payload);
		} catch (error) {
			console.error('[RoleService] Failed to toggle group functions', error);
			throw error;
		}
	}

	async batchUpdateClientGroups(clientId, groupIds = [], removeGroupIds = []) {
		try {
			const promises = [];

			if (groupIds && groupIds.length > 0) {
				groupIds.forEach((groupId) => {
					promises.push(this.assignClient(clientId, groupId));
				});
			}

			if (removeGroupIds && removeGroupIds.length > 0) {
				removeGroupIds.forEach((groupId) => {
					promises.push(this.unassignClient(clientId, groupId));
				});
			}

			return await Promise.all(promises);
		} catch (error) {
			console.error(`[RoleService] Failed to batch update client ${clientId} groups`, error);
			throw error;
		}
	}
}

window.RoleApiV1Service = RoleApiV1Service;
