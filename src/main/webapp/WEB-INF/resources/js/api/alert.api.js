import { httpClient } from './http-client.js';

// ==========================================
// 1. Quản lý cấu hình alert (Alert Configurations)
// ==========================================

/**
 * @param {Object} dto
 * @returns {Promise<[Error|null, ApiResponse<AlertConfigDto>]>}
 */
export const createConfig = (dto) => {
  return httpClient('/api/v1/alerts', {
    method: 'POST',
    body: JSON.stringify(dto),
  });
};

/**
 * @param {number|string} id
 * @param {Object} dto
 * @returns {Promise<[Error|null, ApiResponse<AlertConfigDto>]>}
 */
export const updateConfig = (id, dto) => {
  return httpClient(`/api/v1/alerts/${id}`, {
    method: 'PUT',
    body: JSON.stringify(dto),
  });
};

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<AlertConfigDto>]>}
 */
export const getConfigById = (id) => {
  return httpClient(`/api/v1/alerts/${id}`);
};

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<Void>]>}
 */
export const deleteConfig = (id) => {
  return httpClient(`/api/v1/alerts/${id}`, {
    method: 'DELETE',
  });
};

/**
 * @param {Object} params
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
 * @param {Object} params
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<AlertInstanceDto>>]>}
 */
export const getAlerts = (params = {}) => {
  const query = new URLSearchParams(
    Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))
  ).toString();
  return httpClient(`/api/v1/alerts/instances?${query}`);
};

/**
 * @param {number|string} alertConfigId
 * @param {Object} params
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<AlertInstanceDto>>]>}
 */
export const getAlertsByConfig = (alertConfigId, params = {}) => {
  const query = new URLSearchParams(params).toString();
  return httpClient(`/api/v1/alerts/${alertConfigId}/instances?${query}`);
};

/**
 * @param {number|string} alertConfigId
 * @param {number|string} instanceId
 * @returns {Promise<[Error|null, ApiResponse<AlertInstanceDto>]>}
 */
export const getAlertById = (alertConfigId, instanceId) => {
  return httpClient(`/api/v1/alerts/${alertConfigId}/instances/${instanceId}`);
};

/**
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
 * @param {number|string} alertConfigId
 * @param {number|string} instanceId
 * @param {Object} params
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<AlertInstanceLogDto>>]>}
 */
export const getAlertLogs = (alertConfigId, instanceId, params = {}) => {
  const query = new URLSearchParams(
    Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))
  ).toString();
  return httpClient(`/api/v1/alerts/${alertConfigId}/instances/${instanceId}/logs?${query}`);
};

/**
 * @param {Object} params
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const countConfigs = (params = {}) => {
  const query = new URLSearchParams(
    Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))
  ).toString();
  return httpClient(`/api/v1/alerts/count?${query}`);
};

/**
 * @param {number|string} alertConfigId
 * @param {Object} params
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const countAlertsByConfig = (alertConfigId, params = {}) => {
  const query = new URLSearchParams(
    Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))
  ).toString();
  return httpClient(`/api/v1/alerts/${alertConfigId}/instances/count?${query}`);
};

/**
 * @param {number|string} alertConfigId
 * @param {number|string} instanceId
 * @param {Object} params
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const countAlertLogs = (alertConfigId, instanceId, params = {}) => {
  const query = new URLSearchParams(
    Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v != null))
  ).toString();
  return httpClient(`/api/v1/alerts/${alertConfigId}/instances/${instanceId}/logs/count?${query}`);
};

