/**
 * @typedef {Object} AutomationDto
 * @property {number} id
 * @property {string} name
 * @property {string} description
 * @property {string} cronExpression
 * @property {boolean} isActive
 * @property {string} createdAt
 * @property {string} updatedAt
 */

/**
 * @typedef {Object} CreateAutomationDto
 * @property {string} name
 * @property {string} [description]
 * @property {string} cronExpression
 * @property {boolean} isActive
 */

/**
 * @typedef {Object} UpdateAutomationDto
 * @property {string} name
 * @property {string} [description]
 * @property {string} cronExpression
 * @property {boolean} isActive
 */

/**
 * @typedef {Object} AutomationActionDto
 * @property {number} id
 * @property {number} executionOrder
 * @property {number} targetId
 * @property {string} targetType
 * @property {string} actionType
 * @property {string} parameterValue
 */

/**
 * @typedef {Object} CreateAutomationActionDto
 * @property {number} executionOrder
 * @property {number} targetId
 * @property {string} targetType
 * @property {string} actionType
 * @property {string} parameterValue
 */

/**
 * @typedef {Object} UpdateAutomationActionDto
 * @property {number} executionOrder
 * @property {number} targetId
 * @property {string} targetType
 * @property {string} actionType
 * @property {string} parameterValue
 */
