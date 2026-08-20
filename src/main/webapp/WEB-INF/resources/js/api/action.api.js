import { httpClient } from './http-client.js';

/**
 * @param {CreateActionDto} data
 * @returns {Promise<[Error|null, ApiResponse<ActionDto>]>}
 */
export const createAction = (data) => {
	return httpClient('/api/v1/actions', {
		method: 'POST',
		body: JSON.stringify(data),
	});
};

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<ActionDto>]>}
 */
export const getActionById = (id) => {
	return httpClient(`/api/v1/actions/${id}`);
};

/**
 * @param {number|string} id
 * @param {UpdateActionDto} data
 * @returns {Promise<[Error|null, ApiResponse<ActionDto>]>}
 */
export const updateAction = (id, data) => {
	return httpClient(`/api/v1/actions/${id}`, {
		method: 'PATCH',
		body: JSON.stringify(data),
	});
};

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const deleteAction = (id) => {
	return httpClient(`/api/v1/actions/${id}`, {
		method: 'DELETE',
	});
};

/**
 * @param {Object} params
 * @param {string} [params.ownerCategory]
 * @param {string} [params.ownerId]
 * @param {string} [params.targetCategory]
 * @param {string} [params.targetId]
 * @returns {Promise<[Error|null, ApiResponse<ActionDto[]>]>}
 */
export const getActions = (params = {}) => {
	const query = new URLSearchParams();
	Object.entries(params).forEach(([key, val]) => {
		if (val !== undefined && val !== null && val !== '') {
			query.append(key, val);
		}
	});
	return httpClient(`/api/v1/actions?${query.toString()}`);
};

/**
 * @param {string} ownerCategory
 * @param {string|number} ownerId
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const deleteActionsByOwner = (ownerCategory, ownerId) => {
	return httpClient(`/api/v1/actions/by-owner?ownerCategory=${ownerCategory}&ownerId=${ownerId}`, {
		method: 'DELETE',
	});
};

/**
 * @param {string} ownerCategory
 * @param {string|number} ownerId
 * @param {CreateActionDto[]} list
 * @returns {Promise<[Error|null, ApiResponse<ActionDto[]>]>}
 */
export const replaceActionsByOwner = (ownerCategory, ownerId, list) => {
	return httpClient(`/api/v1/actions/by-owner?ownerCategory=${ownerCategory}&ownerId=${ownerId}`, {
		method: 'PUT',
		body: JSON.stringify(list),
	});
};
