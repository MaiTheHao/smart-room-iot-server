import { httpClient } from './http-client.js';

/**
 * Get energy metric history (domain=ENERGY)
 * @param {Object} params - { category, targetId, from, to }
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').EnergyMetricDto[]>]>}
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
 * @param {Object} params - { category, targetId }
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').EnergyMetricDto>]>}
 */
export const getEnergyMetricLatest = (params) => {
	const query = new URLSearchParams({
		domain: 'ENERGY',
		latest: true,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

