/**
 * @file types.js
 * @description JSDoc type definitions for IUH Smart Home based on Backend DTOs
 */

/**
 * @template T
 * @typedef {Object} ApiResponse
 * @property {number} status - HTTP status code
 * @property {string} message - Response message
 * @property {T} data - The actual data payload
 * @property {string} timestamp - ISO timestamp
 */

/**
 * @template T
 * @typedef {Object} PaginatedResponse
 * @property {T[]} content - List of items in current page
 * @property {number} page - Current page number (0-indexed)
 * @property {number} size - Number of items per page
 * @property {number} totalElements - Total number of items across all pages
 * @property {number} totalPages - Total number of pages
 */

/**
 * @typedef {'USER' | 'HARDWARE_GATEWAY'} ClientType
 */

/**
 * @typedef {Object} ClientDto
 * @property {number} id
 * @property {string} username
 * @property {ClientType} clientType
 * @property {string} [ipAddress]
 * @property {string} [macAddress]
 * @property {string} [avatarUrl]
 * @property {string} [lastLoginAt]
 * @property {string} [gatewayPassword]
 */

/**
 * @typedef {Object} CreateClientDto
 * @property {string} username
 * @property {string} password
 * @property {ClientType} clientType
 * @property {string} [ipAddress]
 * @property {string} [macAddress]
 * @property {string} [avatarUrl]
 * @property {string} [gatewayPassword]
 */

/**
 * @typedef {Object} UpdateClientDto
 * @property {string} [password]
 * @property {ClientType} [clientType]
 * @property {string} [ipAddress]
 * @property {string} [macAddress]
 * @property {string} [avatarUrl]
 * @property {string} [gatewayPassword]
 */

/**
 * @typedef {Object} LoginDto
 * @property {string} username
 * @property {string} password
 */

/**
 * @typedef {Object} JwtResponse
 * @property {string} token
 * @property {string} type - Usually "Bearer"
 * @property {string} username
 * @property {string[]} groups - User roles/groups (e.g., ["G_ADMIN", "G_USER"])
 */

export {};
