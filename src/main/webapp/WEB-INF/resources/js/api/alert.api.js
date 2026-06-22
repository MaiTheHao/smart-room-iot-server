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
 * @param {number|string} alertId
 * @param {Object} params - { status, severity, page, size }
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<AlertInstanceDto>>]>}
 */
export const getAlertsByConfig = (alertId, params = {}) => {
  const query = new URLSearchParams(params).toString();
  return httpClient(`/api/v1/alerts/${alertId}/instances?${query}`);
};

/**
 * Get detailed alert instance
 * @param {number|string} alertId
 * @param {number|string} instanceId
 * @returns {Promise<[Error|null, ApiResponse<AlertInstanceDto>]>}
 */
export const getAlertById = (alertId, instanceId) => {
  return httpClient(`/api/v1/alerts/${alertId}/instances/${instanceId}`);
};

/**
 * Acknowledge alert instance
 * @param {number|string} alertId
 * @param {number|string} instanceId
 * @returns {Promise<[Error|null, ApiResponse<AlertInstanceDto>]>}
 */
export const acknowledgeAlert = (alertId, instanceId) => {
  return httpClient(`/api/v1/alerts/${alertId}/instances/${instanceId}/acknowledge`, {
    method: 'POST',
  });
};

/**
 * Resolve alert instance
 * @param {number|string} alertId
 * @param {number|string} instanceId
 * @returns {Promise<[Error|null, ApiResponse<AlertInstanceDto>]>}
 */
export const resolveAlert = (alertId, instanceId) => {
  return httpClient(`/api/v1/alerts/${alertId}/instances/${instanceId}/resolve`, {
    method: 'POST',
  });
};

/**
 * Get alert logs of an instance
 * @param {number|string} alertId
 * @param {number|string} instanceId
 * @returns {Promise<[Error|null, ApiResponse<AlertInstanceLogDto[]>]>}
 */
export const getAlertLogs = (alertId, instanceId) => {
  return httpClient(`/api/v1/alerts/${alertId}/instances/${instanceId}/logs`);
};
