import { httpClient } from './http-client.js';

/**
 * @file system.api.js
 * @description API service for System operations (Setup, Health Check)
 */

/**
 * Trigger setup process for a specific gateway client
 * @param {number|string} clientId
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<void>]>}
 */
export const setup = (clientId) =>
	httpClient(`/api/v1/setup/${clientId}`, {
		method: 'POST',
	});

/**
 * Get health status of a specific client
 * @param {number|string} clientId
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').HealthCheckResponseDto>]>}
 */
export const getClientHealth = (clientId) =>
	httpClient(`/api/v1/clients/${clientId}/health`);

/**
 * Get health status of a client by IP address
 * @param {string} ip
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').HealthCheckResponseDto>]>}
 */
export const getClientHealthByIp = (ip) =>
	httpClient(`/api/v1/clients/health?ip=${ip}`);

/**
 * Get health score (0-100) of a specific client
 * @param {number|string} clientId
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<number>]>}
 */
export const getClientHealthScore = (clientId) =>
	httpClient(`/api/v1/clients/${clientId}/health-score`);

/**
 * Get health status of all gateways in a room
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<Record<string, import('../types.js').HealthCheckResponseDto>>]>}
 */
export const getRoomHealth = (roomId) =>
	httpClient(`/api/v1/rooms/${roomId}/health`);

/**
 * Get health status of all gateways in a room by room code
 * @param {string} code
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<Record<string, import('../types.js').HealthCheckResponseDto>>]>}
 */
export const getRoomHealthByCode = (code) =>
	httpClient(`/api/v1/rooms/health?code=${code}`);

/**
 * Get aggregated health score (0-100) of a room
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<number>]>}
 */
export const getRoomHealthScore = (roomId) =>
	httpClient(`/api/v1/rooms/${roomId}/health-score`);
