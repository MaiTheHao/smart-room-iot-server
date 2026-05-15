import { httpClient } from './http-client.js';

/**
 * @file function.api.js
 * @description API service for SysFunction operations
 */

/**
 * Get paginated list of functions
 * @param {number} page
 * @param {number} size
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').PaginatedResponse<import('../types.js').SysFunctionDto>>]>}
 */
export const getFunctions = (page = 0, size = 10) =>
	httpClient(`/api/v1/functions?page=${page}&size=${size}`);

/**
 * Get all functions
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').SysFunctionDto[]>]>}
 */
export const getAllFunctions = () =>
	httpClient('/api/v1/functions/all');

/**
 * Get function by ID
 * @param {number|string} id
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').SysFunctionDto>]>}
 */
export const getFunctionById = (id) =>
	httpClient(`/api/v1/functions/${id}`);

/**
 * Get function by code
 * @param {string} code
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').SysFunctionDto>]>}
 */
export const getFunctionByCode = (code) =>
	httpClient(`/api/v1/functions/code/${code}`);

/**
 * Get functions with group status
 * @param {number|string} groupId
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').SysFunctionWithGroupStatusDto[]>]>}
 */
export const getFunctionsWithGroupStatus = (groupId) =>
	httpClient(`/api/v1/functions/with-group-status/${groupId}`);

/**
 * Create a new function
 * @param {import('../types.js').CreateSysFunctionDto} data
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').SysFunctionDto>]>}
 */
export const createFunction = (data) =>
	httpClient('/api/v1/functions', {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * Update a function
 * @param {number|string} id
 * @param {import('../types.js').UpdateSysFunctionDto} data
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').SysFunctionDto>]>}
 */
export const updateFunction = (id, data) =>
	httpClient(`/api/v1/functions/${id}`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});

/**
 * Delete a function
 * @param {number|string} id
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<void>]>}
 */
export const deleteFunction = (id) =>
	httpClient(`/api/v1/functions/${id}`, {
		method: 'DELETE',
	});

/**
 * Get total count of functions
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<number>]>}
 */
export const countFunctions = () =>
	httpClient('/api/v1/functions/count');
