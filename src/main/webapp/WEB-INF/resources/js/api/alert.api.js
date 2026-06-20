import { httpClient } from './http-client.js';

/**
 * @file alert.api.js
 * @description API service for Alert Management based on AlertController
 */

/**
 * Get paginated alerts
 * @param {Object} params - { status, severity, page, size }
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<AlertResponseDto>>]>}
 */
export const getAlerts = (params = {}) => {
	const query = new URLSearchParams(params).toString();
	return httpClient(`/api/v1/alerts?${query}`);
};

/**
 * Get alert by ID
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<AlertResponseDto>]>}
 */
export const getAlertById = (id) => {
	return httpClient(`/api/v1/alerts/${id}`);
};

/**
 * Get alerts of a single rule
 * @param {number|string} ruleId
 * @param {Object} params - { status, severity, page, size }
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<AlertResponseDto>>]>}
 */
export const getAlertsByRuleId = (ruleId, params = {}) => {
	const query = new URLSearchParams(params).toString();
	return httpClient(`/api/v1/rules/${ruleId}/alerts?${query}`);
};

/**
 * Acknowledge alert
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<AlertResponseDto>]>}
 */
export const acknowledgeAlert = (id) => {
	return httpClient(`/api/v1/alerts/${id}`, {
		method: 'PATCH',
		body: JSON.stringify({ status: 'ACKNOWLEDGED' }),
	});
};

/**
 * Resolve alert
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<AlertResponseDto>]>}
 */
export const resolveAlert = (id) => {
	return httpClient(`/api/v1/alerts/${id}`, {
		method: 'PATCH',
		body: JSON.stringify({ status: 'RESOLVED' }),
	});
};
