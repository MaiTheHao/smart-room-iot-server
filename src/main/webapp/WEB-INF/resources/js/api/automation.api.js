import { httpClient } from './http-client.js';

/**
 * @param {number} page
 * @param {number} size
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<AutomationDto>>]>}
 */
export const getAutomations = (page = 0, size = 20) => {
	return httpClient(`/api/v1/automations?page=${page}&size=${size}`);
};

/**
 * @returns {Promise<[Error|null, ApiResponse<AutomationDto[]>]>}
 */
export const getActiveAutomations = () => {
	return httpClient('/api/v1/automations/active');
};

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<AutomationDto>]>}
 */
export const getAutomationById = (id) => {
	return httpClient(`/api/v1/automations/${id}`);
};

/**
 * @param {CreateAutomationDto} data
 * @returns {Promise<[Error|null, ApiResponse<AutomationDto>]>}
 */
export const createAutomation = (data) => {
	return httpClient('/api/v1/automations', {
		method: 'POST',
		body: JSON.stringify(data),
	});
};

/**
 * @param {number|string} id
 * @param {UpdateAutomationDto} data
 * @returns {Promise<[Error|null, ApiResponse<AutomationDto>]>}
 */
export const updateAutomation = (id, data) => {
	return httpClient(`/api/v1/automations/${id}`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});
};

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const deleteAutomation = (id) => {
	return httpClient(`/api/v1/automations/${id}`, {
		method: 'DELETE',
	});
};

/**
 * @param {number|string} id
 * @param {boolean} isActive
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const toggleAutomationStatus = (id, isActive) => {
	return httpClient(`/api/v1/automations/${id}/status?isActive=${isActive}`, {
		method: 'PATCH',
	});
};

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const executeAutomationNow = (id) => {
	return httpClient(`/api/v1/automations/${id}/execute`, {
		method: 'POST',
	});
};

/**
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const reloadAutomationJobs = () => {
	return httpClient('/api/v1/automations/reload-job', {
		method: 'POST',
	});
};

/**
 * @param {number|string} automationId
 * @returns {Promise<[Error|null, ApiResponse<AutomationActionDto[]>]>}
 */
export const getAutomationActions = (automationId) => {
	return httpClient(`/api/v1/automations/${automationId}/actions`);
};

/**
 * @param {number|string} automationId
 * @param {CreateAutomationActionDto} data
 * @returns {Promise<[Error|null, ApiResponse<AutomationActionDto>]>}
 */
export const addAutomationAction = (automationId, data) => {
	return httpClient(`/api/v1/automations/${automationId}/actions`, {
		method: 'POST',
		body: JSON.stringify(data),
	});
};

/**
 * @param {number|string} actionId
 * @param {UpdateAutomationActionDto} data
 * @returns {Promise<[Error|null, ApiResponse<AutomationActionDto>]>}
 */
export const updateAutomationAction = (actionId, data) => {
	return httpClient(`/api/v1/automations/actions/${actionId}`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});
};

/**
 * @param {number|string} actionId
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const deleteAutomationAction = (actionId) => {
	return httpClient(`/api/v1/automations/actions/${actionId}`, {
		method: 'DELETE',
	});
};
