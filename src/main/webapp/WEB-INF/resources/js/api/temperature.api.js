const TemperatureApiService = {
	/**
	 * Get average temperature history for a room
	 * @param {number|string} roomId
	 * @param {string} from - ISO timestamp
	 * @param {string} to - ISO timestamp
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').TemperatureValueDto[]>]>}
	 */
	getAverageHistory: (roomId, from, to) => {
		return http_client(`/api/v1/rooms/${roomId}/temperature-values/average?from=${from}&to=${to}`);
	},
};
