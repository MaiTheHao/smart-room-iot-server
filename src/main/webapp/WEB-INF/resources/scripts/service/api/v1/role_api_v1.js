class RoleApiV1Service {
	static instance;

	constructor() {
		if (RoleApiV1Service.instance) return RoleApiV1Service.instance;
		RoleApiV1Service.instance = this;

		this.api = SMRC_API_V1.ROLE;
	}

	async batchAddFunctionsToGroup(groupId, functionCodes) {
		try {
			return await window.http.post(this.api.GROUPS.BATCH_ADD, { groupId, functionCodes });
		} catch (error) {
			this.#handleError('batch add functions to group', error);
		}
	}

	async batchRemoveFunctionsFromGroup(groupId, functionCodes) {
		try {
			return await window.http.post(this.api.GROUPS.BATCH_REMOVE, { groupId, functionCodes });
		} catch (error) {
			console.error('[RoleService] Failed to batch remove functions from group', error);
			throw error;
		}
	}

	async toggleGroupFunctions(groupId, functionToggles) {
		try {
			return await window.http.post(this.api.GROUPS.TOGGLE, { groupId, functionToggles });
		} catch (error) {
			console.error('[RoleService] Failed to toggle group functions', error);
			throw error;
		}
	}

	async addFunctionToGroup(groupId, functionCode) {
		try {
			return await window.http.post(this.api.GROUPS.ASSIGN(groupId, functionCode));
		} catch (error) {
			console.error(`[RoleService] Failed to add function ${functionCode} to group ${groupId}`, error);
			throw error;
		}
	}

	async removeFunctionFromGroup(groupId, functionCode) {
		try {
			return await window.http.delete(this.api.GROUPS.ASSIGN(groupId, functionCode));
		} catch (error) {
			console.error(`[RoleService] Failed to remove function ${functionCode} from group ${groupId}`, error);
			throw error;
		}
	}

	async assignGroupsToClient(clientId, groupIds) {
		try {
			return await window.http.post(this.api.CLIENTS.ASSIGN, { clientId, groupIds });
		} catch (error) {
			console.error(`[RoleService] Failed to assign groups to client ${clientId}`, error);
			throw error;
		}
	}

	async unassignGroupsFromClient(clientId, groupIds) {
		try {
			return await window.http.post(this.api.CLIENTS.UNASSIGN, { clientId, groupIds });
		} catch (error) {
			console.error(`[RoleService] Failed to unassign groups from client ${clientId}`, error);
			throw error;
		}
	}

	async unassignGroupFromClient(clientId, groupId) {
		try {
			return await window.http.delete(this.api.CLIENTS.MAPPING(clientId, groupId));
		} catch (error) {
			console.error(`[RoleService] Failed to unassign group ${groupId} from client ${clientId}`, error);
			throw error;
		}
	}

	async checkGroupFunction(groupId, functionCode) {
		try {
			return await window.http.get(this.api.GROUPS.CHECK(groupId, functionCode));
		} catch (error) {
			this.#handleError(`check function ${functionCode} for group ${groupId}`, error);
		}
	}

	#handleError(action, error) {
		console.error(`[RoleApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

if (typeof window == 'undefined') throw new Error('RoleApiV1Service can only be initialized in a browser environment');
window.roleApiV1Service = new RoleApiV1Service();
