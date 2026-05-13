const UserApiService = {
	/**
	 * Get all clients with pagination
	 * @param {Object} params - { page, size }
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').PaginatedResponse<import('../types.js').ClientDto>>]>}
	 */
	getAll: (params = {}) => {
		const query = new URLSearchParams(params).toString();
		return http_client(`/api/v1/clients?${query}`);
	},

	/**
	 * Get client by ID
	 * @param {number|string} id
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
	 */
	getById: (id) => http_client(`/api/v1/clients/${id}`),

	/**
	 * Get clients by room ID
	 * @param {number|string} roomId
	 * @param {Object} params - { page, size }
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../active-types.js').PaginatedResponse<import('../types.js').ClientDto>>]>}
	 */
	getByRoomId: (roomId, params = {}) => {
		const query = new URLSearchParams(params).toString();
		return http_client(`/api/v1/clients/room/${roomId}?${query}`);
	},

	/**
	 * Create a new client
	 * @param {import('../types.js').CreateClientDto} data
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
	 */
	create: (data) =>
		http_client('/api/v1/clients', {
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
		http_client(`/api/v1/clients/${id}`, {
			method: 'PUT',
			body: JSON.stringify(data),
		}),

	/**
	 * Delete a client
	 * @param {number|string} id
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<void>]>}
	 */
	delete: (id) =>
		http_client(`/api/v1/clients/${id}`, {
			method: 'DELETE',
		}),

	/**
	 * Delete all device controls of a client
	 * @param {number|string} id
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<void>]>}
	 */
	deleteDeviceControls: (id) =>
		http_client(`/api/v1/clients/${id}/device-controls`, {
			method: 'DELETE',
		}),

	/**
	 * Get current authenticated user info
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
	 */
	getMe: () => http_client('/api/v1/clients/me'),
};
