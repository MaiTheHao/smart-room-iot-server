import { request } from './http-client.js';

/**
 * Authentication Service
 * Handles login, registration, and logout operations.
 */
export const authService = {
	/**
	 * Sign in to the system
	 * @param {import('../types.js').LoginDto} credentials
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').JwtResponse>]>}
	 */
	login: (credentials) =>
		request('/api/v1/auth/signin', {
			method: 'POST',
			body: JSON.stringify(credentials),
		}),

	/**
	 * Register a new client
	 * @param {import('../types.js').CreateClientDto} userData
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
	 */
	register: (userData) =>
		request('/api/v1/auth/signup', {
			method: 'POST',
			body: JSON.stringify(userData),
		}),

	/**
	 * Logout from the system
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<void>]>}
	 */
	logout: () =>
		request('/api/v1/auth/logout', {
			method: 'POST',
		}),

};
