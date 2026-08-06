import { httpClient } from './http-client.js';

/**
 * @returns {Promise<[Error|null, ApiResponse<ClientDto>]>}
 */
export const getMe = () => httpClient('/api/v1/clients/me');

/**
 * @param {Object} params
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<ClientDto>>]>}
 */
export const getAll = (params = {}) => {
	const query = new URLSearchParams(params).toString();
	return httpClient(`/api/v1/clients?${query}`);
};

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<ClientDto>]>}
 */
export const getById = (id) => httpClient(`/api/v1/clients/${id}`);

/**
 * @param {number|string} roomId
 * @param {Object} params
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<ClientDto>>]>}
 */
export const getByRoomId = (roomId, params = {}) => {
	const query = new URLSearchParams(params).toString();
	return httpClient(`/api/v1/clients/room/${roomId}?${query}`);
};

/**
 * @param {CreateClientDto} data
 * @returns {Promise<[Error|null, ApiResponse<ClientDto>]>}
 */
export const create = (data) =>
	httpClient('/api/v1/clients', {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * @param {number|string} id
 * @param {UpdateClientDto} data
 * @returns {Promise<[Error|null, ApiResponse<ClientDto>]>}
 */
export const update = (id, data) =>
	httpClient(`/api/v1/clients/${id}`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});

/**
 * @param {number|string} id
 * @param {Object} data
 * @returns {Promise<[Error|null, ApiResponse<ClientDto>]>}
 */
export const patchUpdate = (id, data) =>
	httpClient(`/api/v1/clients/${id}`, {
		method: 'PATCH',
		body: JSON.stringify(data),
	});

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const deleteClient = (id) =>
	httpClient(`/api/v1/clients/${id}`, {
		method: 'DELETE',
	});

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const deleteAllHardwareConfigs = (id) =>
	httpClient(`/api/v1/clients/${id}/hardware-configs`, {
		method: 'DELETE',
	});

/**
 * @param {LoginDto} data
 * @returns {Promise<[Error|null, ApiResponse<JwtResponse>]>}
 */
export const signin = (data) =>
	httpClient('/api/v1/auth/signin', {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * @param {CreateClientDto} data
 * @returns {Promise<[Error|null, ApiResponse<ClientDto>]>}
 */
export const signup = (data) =>
	httpClient('/api/v1/auth/signup', {
		method: 'POST',
		body: JSON.stringify(data),
	});
