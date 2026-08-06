import { httpClient } from './http-client.js';

/**
 * @param {number} [page=0]
 * @param {number} [size=10]
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<FloorDto>>]>}
 */
export const getFloors = (page = 0, size = 10) =>
	httpClient(`/api/v1/floors?page=${page}&size=${size}`);

/**
 * @returns {Promise<[Error|null, ApiResponse<FloorDto[]>]>}
 */
export const getAllFloors = () =>
	httpClient('/api/v1/floors/all');

/**
 * @param {number|string} floorId
 * @returns {Promise<[Error|null, ApiResponse<FloorDto>]>}
 */
export const getFloorById = (floorId) =>
	httpClient(`/api/v1/floors/${floorId}`);

/**
 * @param {number|string} floorId
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const getFloorVersion = (floorId) =>
	httpClient(`/api/v1/floors/${floorId}/v`);

/**
 * @param {CreateFloorDto} data
 * @returns {Promise<[Error|null, ApiResponse<FloorDto>]>}
 */
export const createFloor = (data) =>
	httpClient('/api/v1/floors', {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * @param {number|string} floorId
 * @param {UpdateFloorDto} data
 * @returns {Promise<[Error|null, ApiResponse<FloorDto>]>}
 */
export const updateFloor = (floorId, data) =>
	httpClient(`/api/v1/floors/${floorId}`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});

/**
 * @param {number|string} floorId
 * @param {UpdateFloorDto} data
 * @returns {Promise<[Error|null, ApiResponse<FloorDto>]>}
 */
export const patchFloor = (floorId, data) =>
	httpClient(`/api/v1/floors/${floorId}`, {
		method: 'PATCH',
		body: JSON.stringify(data),
	});

/**
 * @param {number|string} floorId
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const deleteFloor = (floorId) =>
	httpClient(`/api/v1/floors/${floorId}`, {
		method: 'DELETE',
	});
