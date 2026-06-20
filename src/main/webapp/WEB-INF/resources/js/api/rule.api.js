import { httpClient } from './http-client.js';

/**
 * Get paginated rules
 * @param {number} page
 * @param {number} size
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<RuleDto>>]>}
 */
export const getRules = (page = 0, size = 10) => {
	return httpClient(`/api/v1/rules?page=${page}&size=${size}`);
};

/**
 * Get all active rules
 * @returns {Promise<[Error|null, ApiResponse<RuleDto[]>]>}
 */
export const getAllActiveRules = () => {
	return httpClient('/api/v1/rules/all');
};

/**
 * Get rule by ID
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<RuleDto>]>}
 */
export const getRuleById = (id) => {
	return httpClient(`/api/v1/rules/${id}`);
};

/**
 * Create new rule
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
 * Update rule (Partial update / Replace arrays)
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
 * Delete rule
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const deleteRule = (id) => {
	return httpClient(`/api/v1/rules/${id}`, {
		method: 'DELETE',
	});
};

/**
 * Toggle rule active status
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
 * Reload all rules in Quartz from DB
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const reloadRules = () => {
	return httpClient('/api/v1/rules/reload', {
		method: 'POST',
	});
};

/**
 * Execute rule immediately
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const executeRuleNow = (id) => {
	return httpClient(`/api/v1/rules/${id}/execute`, {
		method: 'POST',
	});
};


