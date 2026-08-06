import { httpClient } from './http-client.js';

/**
 * @param {number|string} clientId
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const setup = (clientId) =>
	httpClient(`/api/v1/setup/${clientId}`, {
		method: 'POST',
	});

/**
 * @param {number|string} clientId
 * @returns {Promise<[Error|null, ApiResponse<HealthCheckResponseDto>]>}
 */
export const getClientHealth = (clientId) =>
	httpClient(`/api/v1/clients/${clientId}/health`);

/**
 * @param {string} ip
 * @returns {Promise<[Error|null, ApiResponse<HealthCheckResponseDto>]>}
 */
export const getClientHealthByIp = (ip) =>
	httpClient(`/api/v1/clients/health?ip=${ip}`);

/**
 * @param {number|string} clientId
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const getClientHealthScore = (clientId) =>
	httpClient(`/api/v1/clients/${clientId}/health-score`);

/**
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, ApiResponse<Record<string, HealthCheckResponseDto>>]>}
 */
export const getRoomHealth = (roomId) =>
	httpClient(`/api/v1/rooms/${roomId}/health`);

/**
 * @param {string} code
 * @returns {Promise<[Error|null, ApiResponse<Record<string, HealthCheckResponseDto>>]>}
 */
export const getRoomHealthByCode = (code) =>
	httpClient(`/api/v1/rooms/health?code=${code}`);

/**
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const getRoomHealthScore = (roomId) =>
	httpClient(`/api/v1/rooms/${roomId}/health-score`);
