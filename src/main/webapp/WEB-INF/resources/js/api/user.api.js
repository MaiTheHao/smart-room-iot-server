import { httpClient } from './http-client.js';

/**
 * @file user.api.js
 * @description API service for Client/User management based on ClientController and AuthController
 */

/**
 * Get current authenticated client info
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
 */
export const getMe = () => httpClient('/api/v1/clients/me');

/**
 * Get all clients with pagination
 * @param {Object} params - { page, size }
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').PaginatedResponse<import('../types.js').ClientDto>>]>}
 */
export const getAll = (params = {}) => {
	const query = new URLSearchParams(params).toString();
	return httpClient(`/api/v1/clients?${query}`);
};

/**
 * Get client by ID
 * @param {number|string} id
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
 */
export const getById = (id) => httpClient(`/api/v1/clients/${id}`);

/**
 * Get clients (Gateways) by room ID
 * @param {number|string} roomId
 * @param {Object} params - { page, size }
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').PaginatedResponse<import('../types.js').ClientDto>>]>}
 */
export const getByRoomId = (roomId, params = {}) => {
	const query = new URLSearchParams(params).toString();
	return httpClient(`/api/v1/clients/room/${roomId}?${query}`);
};

/**
 * Create a new client (Admin)
 * @param {import('../types.js').CreateClientDto} data
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
 */
export const create = (data) =>
	httpClient('/api/v1/clients', {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * Update an existing client
 * @param {number|string} id
 * @param {import('../types.js').UpdateClientDto} data
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
 */
export const update = (id, data) =>
	httpClient(`/api/v1/clients/${id}`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});

/**
 * Partial update of a client
 * @param {number|string} id
 * @param {Object} data
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
 */
export const patchUpdate = (id, data) =>
	httpClient(`/api/v1/clients/${id}`, {
		method: 'PATCH',
		body: JSON.stringify(data),
	});

/**
 * Delete a client
 * @param {number|string} id
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<void>]>}
 */
export const deleteClient = (id) =>
	httpClient(`/api/v1/clients/${id}`, {
		method: 'DELETE',
	});

/**
 * Delete all device controls of a client
 * @param {number|string} id
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<void>]>}
 */
export const deleteAllHardwareConfigs = (id) =>
	httpClient(`/api/v1/clients/${id}/hardware-configs`, {
		method: 'DELETE',
	});

/**
 * Sign in (Authentication)
 * @param {import('../types.js').LoginDto} data
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').JwtResponse>]>}
 */
export const signin = (data) =>
	httpClient('/api/v1/auth/signin', {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * Sign up (Public registration)
 * @param {import('../types.js').CreateClientDto} data
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
 */
export const signup = (data) =>
	httpClient('/api/v1/auth/signup', {
		method: 'POST',
		body: JSON.stringify(data),
	});
