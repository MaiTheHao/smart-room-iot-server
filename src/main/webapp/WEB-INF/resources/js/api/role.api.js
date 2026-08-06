import { httpClient } from './http-client.js';

/**
 * @param {Object} data
 * @param {number|string} data.groupId
 * @param {string[]} data.functionCodes
 * @returns {Promise<[Error|null, ApiResponse<BatchOperationResultDto>]>}
 */
export const batchAddFunctionsToGroup = (data) =>
	httpClient('/api/v1/roles/groups/functions/batch-add', {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * @param {Object} data
 * @param {number|string} data.groupId
 * @param {string[]} data.functionCodes
 * @returns {Promise<[Error|null, ApiResponse<BatchOperationResultDto>]>}
 */
export const batchRemoveFunctionsFromGroup = (data) =>
	httpClient('/api/v1/roles/groups/functions/batch-remove', {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * @param {Object} data
 * @param {number|string} data.groupId
 * @param {Object.<string, boolean>} data.functionToggles
 * @returns {Promise<[Error|null, ApiResponse<BatchOperationResultDto>]>}
 */
export const toggleGroupFunctions = (data) =>
	httpClient('/api/v1/roles/groups/functions/toggle', {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * @param {Object} data
 * @param {number|string} data.clientId
 * @param {number[]} data.groupIds
 * @returns {Promise<[Error|null, ApiResponse<BatchOperationResultDto>]>}
 */
export const assignGroupsToClient = (data) =>
	httpClient('/api/v1/roles/clients/groups/assign', {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * @param {Object} data
 * @param {number|string} data.clientId
 * @param {number[]} data.groupIds
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const unassignGroupsFromClient = (data) =>
	httpClient('/api/v1/roles/clients/groups/unassign', {
		method: 'POST',
		body: JSON.stringify(data),
	});
