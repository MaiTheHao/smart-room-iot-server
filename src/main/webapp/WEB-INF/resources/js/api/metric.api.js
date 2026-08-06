import { httpClient } from './http-client.js';

/**
 * @param {Object} params
 * @param {EnergyMetricCategory} params.category
 * @param {number} params.targetId
 * @param {string} [params.from]
 * @param {string} [params.to]
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
 * @param {Object} params
 * @param {EnergyMetricCategory} params.category
 * @param {number} params.targetId
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

