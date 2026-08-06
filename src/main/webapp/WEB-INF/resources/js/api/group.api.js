import { httpClient } from './http-client.js';

/**
 * @param {number} page
 * @param {number} size
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<SysGroupDto>>]>}
 */
export const getGroups = (page = 0, size = 10) =>
	httpClient(`/api/v1/groups?page=${page}&size=${size}`);

/**
 * @returns {Promise<[Error|null, ApiResponse<SysGroupDto[]>]>}
 */
export const getAllGroups = () =>
	httpClient('/api/v1/groups/all');

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<SysGroupDto>]>}
 */
export const getGroupById = (id) =>
	httpClient(`/api/v1/groups/${id}`);

/**
 * @param {string} code
 * @returns {Promise<[Error|null, ApiResponse<SysGroupDto>]>}
 */
export const getGroupByCode = (code) =>
	httpClient(`/api/v1/groups/code/${code}`);

/**
 * @param {CreateSysGroupDto} data
 * @returns {Promise<[Error|null, ApiResponse<SysGroupDto>]>}
 */
export const createGroup = (data) =>
	httpClient('/api/v1/groups', {
		method: 'POST',
		body: JSON.stringify(data),
	});

/**
 * @param {number|string} id
 * @param {UpdateSysGroupDto} data
 * @returns {Promise<[Error|null, ApiResponse<SysGroupDto>]>}
 */
export const updateGroup = (id, data) =>
	httpClient(`/api/v1/groups/${id}`, {
		method: 'PUT',
		body: JSON.stringify(data),
	});

/**
 * @param {number|string} id
 * @returns {Promise<[Error|null, ApiResponse<void>]>}
 */
export const deleteGroup = (id) =>
	httpClient(`/api/v1/groups/${id}`, {
		method: 'DELETE',
	});

/**
 * @param {number|string} clientId
 * @returns {Promise<[Error|null, ApiResponse<SysGroupWithClientStatusDto[]>]>}
 */
export const getGroupsWithClientStatus = (clientId) =>
	httpClient(`/api/v1/groups/with-client-status/${clientId}`);

/**
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const countGroups = () =>
	httpClient('/api/v1/groups/count');

/**
 * @param {number|string} groupId
 * @returns {Promise<[Error|null, ApiResponse<number>]>}
 */
export const getClientsCountByGroup = (groupId) =>
	httpClient(`/api/v1/groups/${groupId}/clients/count`);

/**
 * @param {number|string} groupId
 * @returns {Promise<[Error|null, ApiResponse<ClientDto[]>]>}
 */
export const getAllClientsByGroupId = (groupId) =>
	httpClient(`/api/v1/groups/${groupId}/clients/all`);

/**
 * @param {number|string} groupId
 * @param {number} page
 * @param {number} size
 * @returns {Promise<[Error|null, ApiResponse<PaginatedResponse<ClientDto>>]>}
 */
export const getClientsByGroup = (groupId, page = 0, size = 10) =>
	httpClient(`/api/v1/groups/${groupId}/clients?page=${page}&size=${size}`);
