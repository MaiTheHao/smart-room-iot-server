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
