import { httpClient } from './http-client.js';

/**
 * Get all temperature sensors by room
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, ApiResponse<TemperatureDto[]>]>}
 */
export const getAllTemperaturesByRoom = (roomId) =>
  httpClient(`/api/v1/rooms/${roomId}/temperatures/all`);

/**
 * Get temperature sensor by ID
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<TemperatureDto>]>}
 */
export const getTemperatureById = (id) => httpClient(`/api/v1/temperatures/${id}`);

/**
 * Get all power consumption sensors by room
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, ApiResponse<PowerConsumptionDto[]>]>}
 */
export const getAllPowerConsumptionsByRoom = (roomId) =>
  httpClient(`/api/v1/rooms/${roomId}/power-consumptions/all`);

/**
 * Get power consumption sensor by ID
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<PowerConsumptionDto>]>}
 */
export const getPowerConsumptionById = (id) => httpClient(`/api/v1/power-consumptions/${id}`);
