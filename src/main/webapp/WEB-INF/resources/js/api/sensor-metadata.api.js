import { httpClient } from './http-client.js';

/**
 * @param {number|string} roomId
 * @param {string} [category]
 * @param {number} [page]
 * @param {number} [size]
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<SensorMetadataDto>>]>}
 */
export const getSensorsByRoom = (roomId, category, page = 0, size = 20) => {
  const query = new URLSearchParams({ page, size });
  if (category) query.set('category', category);
  return httpClient(`/api/v1/rooms/${roomId}/sensors?${query}`);
};

/**
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const getSensorCountByRoom = (roomId) =>
  httpClient(`/api/v1/rooms/${roomId}/sensors/count`);

/**
 * @param {string} [category]
 * @param {number} [page]
 * @param {number} [size]
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<SensorMetadataDto>>]>}
 */
export const getAllSensors = (category, page = 0, size = 20) => {
  const query = new URLSearchParams({ page, size });
  if (category) query.set('category', category);
  return httpClient(`/api/v1/sensors?${query}`);
};

/**
 * @param {number|string} id
 * @param {string} category
 * @returns {Promise<[Error|null, ApiResponse<SensorMetadataDto>]>}
 */
export const getSensorById = (id, category) =>
  httpClient(`/api/v1/sensors/${id}?category=${encodeURIComponent(category)}`);

/**
 * @param {string} naturalId
 * @param {string} category
 * @returns {Promise<[Error|null, ApiResponse<SensorMetadataDto>]>}
 */
export const getSensorByNaturalId = (naturalId, category) =>
  httpClient(`/api/v1/sensors/natural/${encodeURIComponent(naturalId)}?category=${encodeURIComponent(category)}`);
