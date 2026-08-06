import { httpClient } from './http-client.js';

/**
 * @param {number} [page=0]
 * @param {number} [size=10]
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<RoomDto>>]>}
 */
export const getRooms = (page = 0, size = 10) =>
	httpClient(`/api/v1/rooms?page=${page}&size=${size}`);

/**
 * @returns {Promise<[Error|null, ApiResponse<RoomDto[]>]>}
 */
export const getAllRooms = () =>
	httpClient('/api/v1/rooms/all');

/**
 * @param {number|string} floorId
 * @param {number} [page=0]
 * @param {number} [size=10]
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<RoomDto>>]>}
 */
export const getRoomsByFloor = (floorId, page = 0, size = 10) =>
	httpClient(`/api/v1/floors/${floorId}/rooms?page=${page}&size=${size}`);

/**
 * @param {number|string} floorId
 * @returns {Promise<[Error|null, ApiResponse<RoomDto[]>]>}
 */
export const getAllRoomsByFloor = (floorId) =>
	httpClient(`/api/v1/floors/${floorId}/rooms/all`);

/**
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, ApiResponse<RoomDto>]>}
 */
export const getRoomById = (roomId) =>
	httpClient(`/api/v1/rooms/${roomId}`);

/**
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const getRoomVersion = (roomId) =>
	httpClient(`/api/v1/rooms/${roomId}/v`);

/**
 * @param {number|string} floorId
 * @param {CreateRoomDto} data
 * @returns {Promise<[Error|null, ApiResponse<RoomDto>]>}
 */
export const createRoom = (floorId, data) =>
	httpClient(`/api/v1/floors/${floorId}/rooms`, {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * @param {number|string} roomId
 * @param {UpdateRoomDto} data
 * @returns {Promise<[Error|null, ApiResponse<RoomDto>]>}
 */
export const updateRoom = (roomId, data) =>
	httpClient(`/api/v1/rooms/${roomId}`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});

/**
 * @param {number|string} roomId
 * @param {UpdateRoomDto} data
 * @returns {Promise<[Error|null, ApiResponse<RoomDto>]>}
 */
export const patchRoom = (roomId, data) =>
	httpClient(`/api/v1/rooms/${roomId}`, {
		method: 'PATCH',
		body: JSON.stringify(data),
	});

/**
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const deleteRoom = (roomId) =>
	httpClient(`/api/v1/rooms/${roomId}`, {
		method: 'DELETE',
	});
