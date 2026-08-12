import { httpClient } from './http-client.js';

/**
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, ApiResponse<TemperatureDto[]>]>}
 */
export const getAllTemperaturesByRoom = (roomId) =>
  httpClient(`/api/v1/rooms/${roomId}/temperatures/all`);

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<TemperatureDto>]>}
 */
export const getTemperatureById = (id) => httpClient(`/api/v1/temperatures/${id}`);

/**
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, ApiResponse<PowerConsumptionDto[]>]>}
 */
export const getAllPowerConsumptionsByRoom = (roomId) =>
  httpClient(`/api/v1/rooms/${roomId}/power-consumptions/all`);

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<PowerConsumptionDto>]>}
 */
export const getPowerConsumptionById = (id) => httpClient(`/api/v1/power-consumptions/${id}`);

/**
 * @param {number|string} roomId
 * @param {string} [category]
 * @returns {Promise<[Error|null, ApiResponse<SensorMetadataDto[]>]>}
 */
export const getSensorsByRoom = (roomId, category) => {
  const query = category ? `?category=${encodeURIComponent(category)}` : '';
  return httpClient(`/api/v1/rooms/${roomId}/sensors${query}`);
};

/**
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const getSensorCountByRoom = (roomId) =>
  httpClient(`/api/v1/rooms/${roomId}/sensors/count`);

/**
 * @param {string} [category]
 * @returns {Promise<[Error|null, ApiResponse<SensorMetadataDto[]>]>}
 */
export const getAllSensors = (category) => {
  const query = category ? `?category=${encodeURIComponent(category)}` : '';
  return httpClient(`/api/v1/sensors/all${query}`);
};

