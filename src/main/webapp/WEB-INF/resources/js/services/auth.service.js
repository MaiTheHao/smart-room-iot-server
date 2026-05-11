import { authService as authApi } from '../api/auth.api.js';
import { userService as userApi } from '../api/user.api.js';
import { storage } from '../localstorage.js';

export const authService = {
	/**
	 * Performs login and saves session data
	 * @param {import('../types.js').LoginDto} credentials
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').JwtResponse>]>}
	 */
	async login(credentials) {
		const [err, result] = await authApi.login(credentials);

		if (!err && result.status === 200) {
			storage.set('username', result.data.username);
			storage.set('roles', result.data.groups || []);
		}

		return [err, result];
	},

	/**
	 * Performs logout and clears session data
	 * @returns {Promise<[Error|null, void]>}
	 */
	async logout() {
		const [err] = await authApi.logout();
		if (err) {
			console.error('Logout API failed:', err);
		}
		storage.removeMany(['username', 'roles', 'user']);
		return [err, null];
	},

	/**
	 * Fetches current user profile and updates storage
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').ClientDto>]>}
	 */
	async getMe() {
		const [err, result] = await userApi.getMe();

		if (!err && result.status === 200) {
			storage.set('user', result.data);
			storage.set('username', result.data.username);
		}

		return [err, result];
	},
};
