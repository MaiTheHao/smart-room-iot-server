const MetricApiService = {
	/**
	 * Get energy metric history (domain=ENERGY)
	 * @param {Object} params - { category, targetId, from, to }
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').EnergyMetricDto[]>]>}
	 */
	getEnergyMetricHistory: (params) => {
		const query = new URLSearchParams({
			domain: 'ENERGY',
			latest: false,
			...params,
		}).toString();
		return http_client(`/api/v1/metrics?${query}`);
	},

	/**
	 * Get latest energy metric (domain=ENERGY)
	 * @param {Object} params - { category, targetId }
	 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').EnergyMetricDto>]>}
	 */
	getEnergyMetricLatest: (params) => {
		const query = new URLSearchParams({
			domain: 'ENERGY',
			latest: true,
			...params,
		}).toString();
		return http_client(`/api/v1/metrics?${query}`);
	},
};
