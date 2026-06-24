import { httpClient } from './http-client.js';

/**
 * @file alert.api.js
 * @description API service for Alert Management based on AlertController
 */

// ==========================================
// 1. Quản lý cấu hình alert (Alert Configurations)
// ==========================================

/**
 * Create alert configuration
 * @param {Object} dto - CreateAlertConfigDto
 * @returns {Promise<[Error|null, ApiResponse<AlertConfigDto>]>}
 */
export const createConfig = (dto) => {
  return httpClient('/api/v1/alerts', {
    method: 'POST',
    body: JSON.stringify(dto),
  });
};

/**
 * Update alert configuration
 * @param {number|string} id
 * @param {Object} dto - UpdateAlertConfigDto
 * @returns {Promise<[Error|null, ApiResponse<AlertConfigDto>]>}
 */
export const updateConfig = (id, dto) => {
  return httpClient(`/api/v1/alerts/${id}`, {
    method: 'PUT',
    body: JSON.stringify(dto),
  });
};

/**
 * Get alert configuration by ID
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<AlertConfigDto>]>}
 */
export const getConfigById = (id) => {
  return httpClient(`/api/v1/alerts/${id}`);
};

/**
 * Delete alert configuration
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<Void>]>}
 */
export const deleteConfig = (id) => {
  return httpClient(`/api/v1/alerts/${id}`, {
    method: 'DELETE',
  });
};

/**
 * Get alert configurations (paginated). If namespace and sourceId are both provided,
 * filters by source. Otherwise returns all configs (for standalone manage page).
 * @param {Object} params - { namespace?, sourceId?, page?, size? }
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<AlertConfigDto>>]>}
 */
export const getConfigs = (params = {}) => {
  const query = new URLSearchParams(
    Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))
  ).toString();
  return httpClient(`/api/v1/alerts?${query}`);
};

// ==========================================
// 2. Quản lý sự kiện alert thực tế (Alert Instances)
// ==========================================

/**
 * Get all alert instances (paginated) with optional namespace + date range filters
 * @param {Object} params - { status, severity, namespace, from, to, page, size }
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<AlertInstanceDto>>]>}
 */
export const getAlerts = (params = {}) => {
  const query = new URLSearchParams(
    Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))
  ).toString();
  return httpClient(`/api/v1/alerts/instances?${query}`);
};

/**
 * Get alert instances by configuration ID (paginated)
 * @param {number|string} alertConfigId
 * @param {Object} params - { status, severity, page, size }
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<AlertInstanceDto>>]>}
 */
export const getAlertsByConfig = (alertConfigId, params = {}) => {
  const query = new URLSearchParams(params).toString();
  return httpClient(`/api/v1/alerts/${alertConfigId}/instances?${query}`);
};

/**
 * Get detailed alert instance
 * @param {number|string} alertConfigId
 * @param {number|string} instanceId
 * @returns {Promise<[Error|null, ApiResponse<AlertInstanceDto>]>}
 */
export const getAlertById = (alertConfigId, instanceId) => {
  return httpClient(`/api/v1/alerts/${alertConfigId}/instances/${instanceId}`);
};

/**
 * Acknowledge alert instance
 * @param {number|string} alertConfigId
 * @param {number|string} instanceId
 * @returns {Promise<[Error|null, ApiResponse<AlertInstanceDto>]>}
 */
export const acknowledgeAlert = (alertConfigId, instanceId) => {
  return httpClient(`/api/v1/alerts/${alertConfigId}/instances/${instanceId}/acknowledge`, {
    method: 'POST',
  });
};

/**
 * Resolve alert instance
 * @param {number|string} alertConfigId
 * @param {number|string} instanceId
 * @returns {Promise<[Error|null, ApiResponse<AlertInstanceDto>]>}
 */
export const resolveAlert = (alertConfigId, instanceId) => {
  return httpClient(`/api/v1/alerts/${alertConfigId}/instances/${instanceId}/resolve`, {
    method: 'POST',
  });
};

/**
 * Get alert logs of an instance (paginated)
 * @param {number|string} alertConfigId
 * @param {number|string} instanceId
 * @param {Object} params - { actionType, actorType, page, size }
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<AlertInstanceLogDto>>]>}
 */
export const getAlertLogs = (alertConfigId, instanceId, params = {}) => {
  const query = new URLSearchParams(
    Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))
  ).toString();
  return httpClient(`/api/v1/alerts/${alertConfigId}/instances/${instanceId}/logs?${query}`);
};

/**
 * Count alert configurations based on filters
 * @param {Object} params - { namespace, alertCode, sourceId }
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const countConfigs = (params = {}) => {
  const query = new URLSearchParams(
    Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))
  ).toString();
  return httpClient(`/api/v1/alerts/count?${query}`);
};

/**
 * Count alert instances based on configuration and filters
 * @param {number|string} alertConfigId
 * @param {Object} params - { status, severity, from, to }
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const countAlertsByConfig = (alertConfigId, params = {}) => {
  const query = new URLSearchParams(
    Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))
  ).toString();
  return httpClient(`/api/v1/alerts/${alertConfigId}/instances/count?${query}`);
};

/**
 * Count alert instance logs based on filters
 * @param {number|string} alertConfigId
 * @param {number|string} instanceId
 * @param {Object} params - { actionType, actorType }
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const countAlertLogs = (alertConfigId, instanceId, params = {}) => {
  const query = new URLSearchParams(
    Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))
  ).toString();
  return httpClient(`/api/v1/alerts/${alertConfigId}/instances/${instanceId}/logs/count?${query}`);
};

