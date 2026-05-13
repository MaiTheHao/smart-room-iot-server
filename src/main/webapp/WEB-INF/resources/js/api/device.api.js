const DeviceApiService = {
	/**
	 * Get all devices in a room (unified API)
	 * @param {number|string} roomId
	 * @param {import('../types.js').DeviceCategory} [category] - Optional filter
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').UnifiedDeviceDto[]>]>}
	 */
	getDevicesByRoom: (roomId, category = null) => {
		let endpoint = `/api/v1/rooms/${roomId}/devices`;
		if (category) {
			endpoint += `?category=${category}`;
		}
		return http_client(endpoint);
	},

	/**
	 * Control Air Condition
	 * @param {string} naturalId
	 * @param {import('../types.js').AirConditionControlRequestBody} data
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<any>]>}
	 */
	controlAc: (naturalId, data) => {
		return http_client(`/api/v1/air-conditions/${naturalId}/control`, {
			method: 'PUT',
			body: JSON.stringify(data),
		});
	},

	/**
	 * Control Fan
	 * @param {string} naturalId
	 * @param {import('../types.js').FanControlRequestBody} data
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<any>]>}
	 */
	controlFan: (naturalId, data) => {
		return http_client(`/api/v1/fans/${naturalId}/control`, {
			method: 'PUT',
			body: JSON.stringify(data),
		});
	},

	/**
	 * Control Light
	 * @param {string} naturalId
	 * @param {import('../types.js').LightControlRequestBody} data
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<any>]>}
	 */
	controlLight: (naturalId, data) => {
		return http_client(`/api/v1/lights/${naturalId}/control`, {
			method: 'PUT',
			body: JSON.stringify(data),
		});
	},
};
