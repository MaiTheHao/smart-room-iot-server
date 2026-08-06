import { httpClient } from './http-client.js';

/**
 * @param {number} page
 * @param {number} size
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<RuleDto>>]>}
 */
export const getRules = (page = 0, size = 10) => {
	return httpClient(`/api/v1/rules?page=${page}&size=${size}`);
};

/**
 * @returns {Promise<[Error|null, ApiResponse<RuleDto[]>]>}
 */
export const getAllActiveRules = () => {
	return httpClient('/api/v1/rules/all');
};

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<RuleDto>]>}
 */
export const getRuleById = (id) => {
	return httpClient(`/api/v1/rules/${id}`);
};

/**
 * @param {CreateRuleDto} data
 * @returns {Promise<[Error|null, ApiResponse<RuleDto>]>}
 */
export const createRule = (data) => {
	return httpClient('/api/v1/rules', {
		method: 'POST',
		body: JSON.stringify(data),
	});
};

/**
 * @param {number|string} id
 * @param {UpdateRuleDto} data
 * @returns {Promise<[Error|null, ApiResponse<RuleDto>]>}
 */
export const updateRule = (id, data) => {
	return httpClient(`/api/v1/rules/${id}`, {
		method: 'PATCH',
		body: JSON.stringify(data),
	});
};

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const deleteRule = (id) => {
	return httpClient(`/api/v1/rules/${id}`, {
		method: 'DELETE',
	});
};

/**
 * @param {number|string} id
 * @param {boolean} isActive
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const toggleRuleStatus = (id, isActive) => {
	return httpClient(`/api/v1/rules/${id}/status`, {
		method: 'PATCH',
		body: JSON.stringify({ isActive }),
	});
};

/**
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const reloadRules = () => {
	return httpClient('/api/v1/rules/reload', {
		method: 'POST',
	});
};

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const executeRuleNow = (id) => {
	return httpClient(`/api/v1/rules/${id}/execute`, {
		method: 'POST',
	});
};


