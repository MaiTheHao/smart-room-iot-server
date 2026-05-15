/**
 * @file types.js
 * @description JSDoc type definitions for Smart Room IoT Server based on Backend DTOs
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

/**
 * @typedef {Object} TemperatureValueDto
 * @property {string} timestamp - ISO timestamp
 * @property {number} avgTempC - Average temperature in Celsius
 */

/**
 * @typedef {Object} EnergyMetricDto
 * @property {string} timestamp - ISO timestamp
 * @property {number} [voltage]
 * @property {number} [current]
 * @property {number} [power]
 * @property {number} [energy]
 * @property {number} [frequency]
 * @property {number} [powerFactor]
 */

/**
 * @typedef {'LIGHT' | 'FAN' | 'AIR_CONDITION'} DeviceCategory
 */

/**
 * @typedef {'ON' | 'OFF'} ActuatorPower
 */

/**
 * @typedef {'COOL' | 'HEAT' | 'DRY' | 'FAN' | 'AUTO' | 'NORMAL' | 'SLEEP' | 'NATURAL'} ActuatorMode
 */

/**
 * @typedef {'ON' | 'OFF'} ActuatorSwing
 */

/**
 * @typedef {'ON' | 'OFF'} ActuatorState
 */

/**
 * @typedef {Object} UnifiedDeviceDto
 * @property {number} id
 * @property {string} naturalId
 * @property {string} name
 * @property {string} [description]
 * @property {boolean} isActive
 * @property {ActuatorPower} power
 * @property {number} roomId
 * @property {number} deviceControlId
 * @property {DeviceCategory} category
 * @property {number} [level] - For Light
 * @property {number} [speed] - For Fan (0-9999)
 * @property {ActuatorMode} [mode] - For AC/Fan
 * @property {ActuatorSwing} [swing] - For AC/Fan
 * @property {ActuatorState} [light] - For Fan light
 * @property {number} [temperature] - For AC (16-32)
 * @property {number} [fanSpeed] - For AC (0-5)
 * @property {string} [type] - For Fan (GPIO/IR)
 */

/**
 * @typedef {Object} AirConditionControlRequestBody
 * @property {ActuatorPower} [power]
 * @property {number} [temperature]
 * @property {ActuatorMode} [mode]
 * @property {number} [fanSpeed]
 * @property {ActuatorSwing} [swing]
 */

/**
 * @typedef {Object} FanControlRequestBody
 * @property {ActuatorPower} [power]
 * @property {ActuatorMode} [mode]
 * @property {number} [speed]
 * @property {ActuatorSwing} [swing]
 * @property {ActuatorState} [light]
 */

/**
 * @typedef {Object} LightControlRequestBody
 * @property {ActuatorPower} [power]
 * @property {number} [level]
 */

/**
 * @typedef {Object} HealthDeviceDto
 * @property {string} naturalId
 * @property {string} category
 * @property {boolean} isActive
 */

/**
 * @typedef {Object} HealthDataDto
 * @property {HealthDeviceDto[]} devices
 * @property {string} [roomCode]
 */

/**
 * @typedef {Object} HealthCheckResponseDto
 * @property {number} status
 * @property {string} message
 * @property {HealthDataDto} [data]
 * @property {string} timestamp
 */

/**
 * @typedef {Object} FloorDto
 * @property {number} id
 * @property {string} name
 * @property {string} code
 * @property {number} level
 * @property {string} [description]
 */

/**
 * @typedef {Object} CreateFloorDto
 * @property {string} name
 * @property {string} code
 * @property {number} level
 * @property {string} [description]
 * @property {string} [langCode]
 */

/**
 * @typedef {Object} UpdateFloorDto
 * @property {string} [name]
 * @property {number} [level]
 * @property {string} [description]
 * @property {string} [langCode]
 */

/**
 * @typedef {Object} RoomDto
 * @property {number} id
 * @property {string} name
 * @property {string} code
 * @property {number} floorId
 * @property {string} [description]
 */

/**
 * @typedef {Object} CreateRoomDto
 * @property {string} name
 * @property {string} code
 * @property {number} floorId
 * @property {string} [description]
 * @property {string} [langCode]
 */

/**
 * @typedef {Object} UpdateRoomDto
 * @property {string} [name]
 * @property {number} [floorId]
 * @property {string} [description]
 * @property {string} [langCode]
 */

/**
 * @typedef {Object} SysFunctionDto
 * @property {number} id
 * @property {string} functionCode
 * @property {string} name
 * @property {string} [description]
 */

/**
 * @typedef {Object} CreateSysFunctionDto
 * @property {string} functionCode
 * @property {string} name
 * @property {string} [description]
 * @property {string} [langCode]
 */

/**
 * @typedef {Object} UpdateSysFunctionDto
 * @property {string} [name]
 * @property {string} [description]
 * @property {string} [langCode]
 */

/**
 * @typedef {Object} SysFunctionWithGroupStatusDto
 * @property {number} id
 * @property {string} functionCode
 * @property {string} name
 * @property {string} [description]
 * @property {boolean} isAssignedToGroup
 * @property {number} [roleId]
 */

export {};
