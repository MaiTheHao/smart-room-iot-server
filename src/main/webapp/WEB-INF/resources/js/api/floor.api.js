import { httpClient } from './http-client.js';

/**
 * @file floor.api.js
 * @description API service for Floor operations
 */

/**
 * Get paginated list of floors
 * @param {number} [page=0]
 * @param {number} [size=10]
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').PaginatedResponse<import('../types.js').FloorDto>>]>}
 */
export const getFloors = (page = 0, size = 10) =>
	httpClient(`/api/v1/floors?page=${page}&size=${size}`);

/**
 * Get all floors (non-paginated)
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').FloorDto[]>]>}
 */
export const getAllFloors = () =>
	httpClient('/api/v1/floors/all');

/**
 * Get floor details by ID
 * @param {number|string} floorId
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').FloorDto>]>}
 */
export const getFloorById = (floorId) =>
	httpClient(`/api/v1/floors/${floorId}`);

/**
 * Get floor version by ID
 * @param {number|string} floorId
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<number>]>}
 */
export const getFloorVersion = (floorId) =>
	httpClient(`/api/v1/floors/${floorId}/v`);

/**
 * Create a new floor
 * @param {import('../types.js').CreateFloorDto} data
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').FloorDto>]>}
 */
export const createFloor = (data) =>
	httpClient('/api/v1/floors', {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * Update an existing floor
 * @param {number|string} floorId
 * @param {import('../types.js').UpdateFloorDto} data
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').FloorDto>]>}
 */
export const updateFloor = (floorId, data) =>
	httpClient(`/api/v1/floors/${floorId}`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});

/**
 * Patch an existing floor (selective update)
 * @param {number|string} floorId
 * @param {import('../types.js').UpdateFloorDto} data
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<import('../types.js').FloorDto>]>}
 */
export const patchFloor = (floorId, data) =>
	httpClient(`/api/v1/floors/${floorId}`, {
		method: 'PATCH',
		body: JSON.stringify(data),
	});

/**
 * Delete a floor
 * @param {number|string} floorId
 * @returns {Promise<[Error|null, import('../types.js').ApiResponse<void>]>}
 */
export const deleteFloor = (floorId) =>
	httpClient(`/api/v1/floors/${floorId}`, {
		method: 'DELETE',
	});
