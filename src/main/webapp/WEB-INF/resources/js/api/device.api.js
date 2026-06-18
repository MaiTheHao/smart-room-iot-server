import { httpClient } from './http-client.js';

/**
 * Get all devices in a room (unified API)
 * @param {number|string} roomId
 * @param {DeviceCategory} [category] - Optional filter
 * @returns {Promise<[Error|null, ApiResponse<UnifiedDeviceDto[]>]>}
 */
export const getDevicesByRoom = (roomId, category = null) => {
	let endpoint = `/api/v1/rooms/${roomId}/devices`;
	if (category) {
		endpoint += `?category=${category}`;
	}
	return httpClient(endpoint);
};

/**
 * Get all devices in the system (non-paginated)
 * @param {DeviceCategory} [category] - Optional filter
 * @returns {Promise<[Error|null, ApiResponse<Object[]>]>}
 */
export const getAllDevices = (category = null) => {
	let endpoint = '/api/v1/devices/all';
	if (category) {
		endpoint += `?category=${category}`;
	}
	return httpClient(endpoint);
};

/**
 * Control Air Condition
 * @param {string} naturalId
 * @param {AirConditionControlRequestBody} data
 * @returns {Promise<[Error|null, ApiResponse<ControlDeviceResult>]>}
 */
export const controlAc = (naturalId, data) => {
	return httpClient(`/api/v1/air-conditions/${naturalId}/control`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});
};

/**
 * Control Fan
 * @param {string} naturalId
 * @param {FanControlRequestBody} data
 * @returns {Promise<[Error|null, ApiResponse<ControlDeviceResult>]>}
 */
export const controlFan = (naturalId, data) => {
	return httpClient(`/api/v1/fans/${naturalId}/control`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});
};

/**
 * Control Light
 * @param {string} naturalId
 * @param {LightControlRequestBody} data
 * @returns {Promise<[Error|null, ApiResponse<ControlDeviceResult>]>}
 */
export const controlLight = (naturalId, data) => {
	return httpClient(`/api/v1/lights/${naturalId}/control`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});
};


