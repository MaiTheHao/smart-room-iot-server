import { httpClient } from './http-client.js';

/**
 * Get energy metric history (domain=ENERGY)
 * @param {Object} params - The query parameters
 * @param {EnergyMetricCategory} params.category - The category of the metric (LIGHT, FAN, etc.)
 * @param {number} params.targetId - The ID of the target
 * @param {string} [params.from] - ISO timestamp
 * @param {string} [params.to] - ISO timestamp
 * @returns {Promise<[Error|null, ApiResponse<EnergyMetricDto[]>]>}
 */
export const getEnergyMetricHistory = (params) => {
	const query = new URLSearchParams({
		domain: 'ENERGY',
		latest: false,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

/**
 * Get latest energy metric (domain=ENERGY)
 * @param {Object} params - The query parameters
 * @param {EnergyMetricCategory} params.category - The category of the metric
 * @param {number} params.targetId - The ID of the target
 * @returns {Promise<[Error|null, ApiResponse<EnergyMetricDto>]>}
 */
export const getEnergyMetricLatest = (params) => {
	const query = new URLSearchParams({
		domain: 'ENERGY',
		latest: true,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

