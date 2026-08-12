import { httpClient } from './http-client.js';

/**
 * @param {number|string} sensorId
 * @param {Object} params
 * @param {string} params.category - 'TEMPERATURE' | 'POWER_CONSUMPTION' | 'HUMIDITY' | 'SENSOR_CO2' | 'SENSOR_LUX'
 * @param {string} params.from - ISO-8601 string
 * @param {string} params.to - ISO-8601 string
 * @returns {Promise<[Error|null, ApiResponse<any[]>]>}
 */
export const getSensorHistoryById = (sensorId, params) => {
  const query = new URLSearchParams(params).toString();
  return httpClient(`/api/v1/sensors/${sensorId}/history?${query}`);
};

/**
 * @param {string} naturalId
 * @param {Object} params
 * @param {string} params.category - 'TEMPERATURE' | 'POWER_CONSUMPTION' | 'HUMIDITY' | 'SENSOR_CO2' | 'SENSOR_LUX'
 * @param {string} params.from
 * @param {string} params.to
 * @returns {Promise<[Error|null, ApiResponse<any[]>]>}
 */
export const getSensorHistoryByNaturalId = (naturalId, params) => {
  const query = new URLSearchParams(params).toString();
  return httpClient(`/api/v1/sensors/natural/${encodeURIComponent(naturalId)}/history?${query}`);
};
