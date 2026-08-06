import { httpClient } from './http-client.js';

/**
 * @param {number|string} roomId
 * @param {string} from
 * @param {string} to
 * @returns {Promise<[Error|null, ApiResponse<TemperatureValueDto[]>]>}
 */
export const getAverageHistory = (roomId, from, to) => {
	return httpClient(`/api/v1/rooms/${roomId}/temperature-values/average?from=${from}&to=${to}`);
};

