import { httpClient } from './http-client.js';

/**
 * @param {number} page
 * @param {number} size
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<SysFunctionDto>>]>}
 */
export const getFunctions = (page = 0, size = 10) =>
	httpClient(`/api/v1/functions?page=${page}&size=${size}`);

/**
 * @returns {Promise<[Error|null, ApiResponse<SysFunctionDto[]>]>}
 */
export const getAllFunctions = () =>
	httpClient('/api/v1/functions/all');

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<SysFunctionDto>]>}
 */
export const getFunctionById = (id) =>
	httpClient(`/api/v1/functions/${id}`);

/**
 * @param {string} code
 * @returns {Promise<[Error|null, ApiResponse<SysFunctionDto>]>}
 */
export const getFunctionByCode = (code) =>
	httpClient(`/api/v1/functions/code/${code}`);

/**
 * @param {number|string} groupId
 * @returns {Promise<[Error|null, ApiResponse<SysFunctionWithGroupStatusDto[]>]>}
 */
export const getFunctionsWithGroupStatus = (groupId) =>
	httpClient(`/api/v1/functions/with-group-status/${groupId}`);

/**
 * @param {CreateSysFunctionDto} data
 * @returns {Promise<[Error|null, ApiResponse<SysFunctionDto>]>}
 */
export const createFunction = (data) =>
	httpClient('/api/v1/functions', {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * @param {number|string} id
 * @param {UpdateSysFunctionDto} data
 * @returns {Promise<[Error|null, ApiResponse<SysFunctionDto>]>}
 */
export const updateFunction = (id, data) =>
	httpClient(`/api/v1/functions/${id}`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const deleteFunction = (id) =>
	httpClient(`/api/v1/functions/${id}`, {
		method: 'DELETE',
	});

/**
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const countFunctions = () =>
	httpClient('/api/v1/functions/count');
