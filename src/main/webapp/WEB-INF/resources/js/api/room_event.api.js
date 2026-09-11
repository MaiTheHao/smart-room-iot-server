import { httpClient } from './http-client.js';

/**
 * Lấy danh sách tất cả các mã sự kiện khả dụng từ Database
 * @returns {Promise<[Error|null, ApiResponse<RoomEventCodeDto[]>]>}
 */
export const getEventCodes = () => {
  return httpClient('/api/v1/room-events/codes');
};

/**
 * Lấy tất cả cấu hình sự kiện của một phòng
 * @param {number|string} roomId
 * @returns {Promise<[Error|null, ApiResponse<RoomEventConfigDto[]>]>}
 */
export const getConfigsByRoom = (roomId) => {
  return httpClient(`/api/v1/rooms/${roomId}/events`);
};

/**
 * Lấy chi tiết một cấu hình sự kiện của phòng
 * @param {number|string} roomId
 * @param {number|string} configId
 * @returns {Promise<[Error|null, ApiResponse<RoomEventConfigDto>]>}
 */
export const getConfigById = (roomId, configId) => {
  return httpClient(`/api/v1/rooms/${roomId}/events/${configId}`);
};

/**
 * Tạo mới cấu hình sự kiện cho phòng
 * @param {number|string} roomId
 * @param {object} data - { eventCode, isActive, cooldownSeconds }
 * @returns {Promise<[Error|null, ApiResponse<RoomEventConfigDto>]>}
 */
export const createConfig = (roomId, data) => {
  return httpClient(`/api/v1/rooms/${roomId}/events`, {
    method: 'POST',
    body: JSON.stringify(data),
  });
};

/**
 * Cập nhật cấu hình sự kiện phòng
 * @param {number|string} roomId
 * @param {number|string} configId
 * @param {object} data - { isActive, cooldownSeconds }
 * @returns {Promise<[Error|null, ApiResponse<RoomEventConfigDto>]>}
 */
export const updateConfig = (roomId, configId, data) => {
  return httpClient(`/api/v1/rooms/${roomId}/events/${configId}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
};

/**
 * Xóa cấu hình sự kiện phòng
 * @param {number|string} roomId
 * @param {number|string} configId
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const deleteConfig = (roomId, configId) => {
  return httpClient(`/api/v1/rooms/${roomId}/events/${configId}`, {
    method: 'DELETE',
  });
};

/**
 * Lấy danh sách điều kiện của cấu hình sự kiện
 * @param {number|string} roomId
 * @param {number|string} configId
 * @returns {Promise<[Error|null, ApiResponse<ConditionDto[]>]>}
 */
export const getConditions = (roomId, configId) => {
  return httpClient(`/api/v1/rooms/${roomId}/events/${configId}/conditions`);
};

/**
 * Thay thế toàn bộ danh sách điều kiện của cấu hình sự kiện
 * @param {number|string} roomId
 * @param {number|string} configId
 * @param {object[]} list
 * @returns {Promise<[Error|null, ApiResponse<ConditionDto[]>]>}
 */
export const replaceConditions = (roomId, configId, list) => {
  return httpClient(`/api/v1/rooms/{roomId}/events/{configId}/conditions`.replace('{roomId}', roomId).replace('{configId}', configId), {
    method: 'PUT',
    body: JSON.stringify(list),
  });
};

/**
 * Lấy danh sách hành động của cấu hình sự kiện
 * @param {number|string} roomId
 * @param {number|string} configId
 * @returns {Promise<[Error|null, ApiResponse<ActionDto[]>]>}
 */
export const getActions = (roomId, configId) => {
  return httpClient(`/api/v1/rooms/${roomId}/events/${configId}/actions`);
};

/**
 * Thay thế toàn bộ danh sách hành động của cấu hình sự kiện
 * @param {number|string} roomId
 * @param {number|string} configId
 * @param {object[]} list
 * @returns {Promise<[Error|null, ApiResponse<ActionDto[]>]>}
 */
export const replaceActions = (roomId, configId, list) => {
  return httpClient(`/api/v1/rooms/{roomId}/events/{configId}/actions`.replace('{roomId}', roomId).replace('{configId}', configId), {
    method: 'PUT',
    body: JSON.stringify(list),
  });
};
