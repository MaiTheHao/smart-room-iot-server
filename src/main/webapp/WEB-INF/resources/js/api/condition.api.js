import { httpClient } from './http-client.js';

/**
 * @param {CreateConditionDto} data
 * @returns {Promise<[Error|null, ApiResponse<ConditionDto>]>}
 */
export const createCondition = (data) => {
	return httpClient('/api/v1/conditions', {
		method: 'POST',
		body: JSON.stringify(data),
	});
};

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<ConditionDto>]>}
 */
export const getConditionById = (id) => {
	return httpClient(`/api/v1/conditions/${id}`);
};

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const deleteCondition = (id) => {
	return httpClient(`/api/v1/conditions/${id}`, {
		method: 'DELETE',
	});
};

/**
 * @param {Object} params
 * @param {string} [params.ownerCategory]
 * @param {string} [params.ownerId]
 * @param {string} [params.sourceCategory]
 * @param {string} [params.sourceTargetId]
 * @param {string} [params.sourceTargetType]
 * @returns {Promise<[Error|null, ApiResponse<ConditionDto[]>]>}
 */
export const getConditions = (params = {}) => {
	const query = new URLSearchParams();
	Object.entries(params).forEach(([key, val]) => {
		if (val !== undefined && val !== null && val !== '') {
			query.append(key, val);
		}
	});
	return httpClient(`/api/v1/conditions?${query.toString()}`);
};

/**
 * @param {string} ownerCategory
 * @param {string|number} ownerId
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const deleteConditionsByOwner = (ownerCategory, ownerId) => {
	return httpClient(`/api/v1/conditions/by-owner?ownerCategory=${ownerCategory}&ownerId=${ownerId}`, {
		method: 'DELETE',
	});
};

/**
 * @param {string} ownerCategory
 * @param {string|number} ownerId
 * @param {CreateConditionDto[]} list
 * @returns {Promise<[Error|null, ApiResponse<ConditionDto[]>]>}
 */
export const replaceConditionsByOwner = (ownerCategory, ownerId, list) => {
	return httpClient(`/api/v1/conditions/by-owner?ownerCategory=${ownerCategory}&ownerId=${ownerId}`, {
		method: 'PUT',
		body: JSON.stringify(list),
	});
};
