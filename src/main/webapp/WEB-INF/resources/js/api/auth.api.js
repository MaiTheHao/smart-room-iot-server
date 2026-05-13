const AuthApiService = {
	/**
	 * Sign in to the system
	 * @param {import('../types.js').LoginDto} credentials
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').JwtResponse>]>}
	 */
	login: (credentials) =>
		http_client('/api/v1/auth/signin', {
			method: 'POST',
			body: JSON.stringify(credentials),
		}),

	/**
	 * Register a new client
	 * @param {import('../types.js').CreateClientDto} userData
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
	 */
	register: (userData) =>
		http_client('/api/v1/auth/signup', {
			method: 'POST',
			body: JSON.stringify(userData),
		}),

	/**
	 * Logout from the system
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<void>]>}
	 */
	logout: () =>
		http_client('/api/v1/auth/logout', {
			method: 'POST',
		}),
};
