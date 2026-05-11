import { request } from './http-client.js';

/**
 * User/Client Service (mapped to Client Module API)
 * Handles client/user management operations as per doc/api/client.md
 */
export const userService = {
	/**
	 * Get all clients with pagination
	 * @param {Object} params - { page, size }
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').PaginatedResponse<import('../types.js').ClientDto>>]>}
	 */
	getAll: (params = {}) => {
		const query = new URLSearchParams(params).toString();
		return request(`/api/v1/clients?${query}`);
	},

	/**
	 * Get client by ID
	 * @param {number|string} id
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
	 */
	getById: (id) => request(`/api/v1/clients/${id}`),

	/**
	 * Get clients by room ID
	 * @param {number|string} roomId
	 * @param {Object} params - { page, size }
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').PaginatedResponse<import('../types.js').ClientDto>>]>}
	 */
	getByRoomId: (roomId, params = {}) => {
		const query = new URLSearchParams(params).toString();
		return request(`/api/v1/clients/room/${roomId}?${query}`);
	},

	/**
	 * Create a new client
	 * @param {import('../types.js').CreateClientDto} data
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
	 */
	create: (data) =>
		request('/api/v1/clients', {
			method: 'POST',
			body: JSON.stringify(data),
		}),

	/**
	 * Update an existing client
	 * @param {number|string} id
	 * @param {import('../types.js').UpdateClientDto} data
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
	 */
	update: (id, data) =>
		request(`/api/v1/clients/${id}`, {
			method: 'PUT',
			body: JSON.stringify(data),
		}),

	/**
	 * Delete a client
	 * @param {number|string} id
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<void>]>}
	 */
	delete: (id) =>
		request(`/api/v1/clients/${id}`, {
			method: 'DELETE',
		}),

	/**
	 * Delete all device controls of a client
	 * @param {number|string} id
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<void>]>}
	 */
	deleteDeviceControls: (id) =>
		request(`/api/v1/clients/${id}/device-controls`, {
			method: 'DELETE',
		}),

	/**
	 * Get current authenticated user info
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
	 */
	getMe: () => request('/api/v1/clients/me'),

};
