/**
 * @typedef {Object} RuleDto
 * @property {number} id
 * @property {string} name
 * @property {string} description
 * @property {number} priority
 * @property {number} intervalSeconds
 * @property {boolean} isActive
 * @property {RuleConditionDto[]} conditions
 * @property {RuleActionDto[]} actions
 * @property {string} createdAt
 * @property {string} updatedAt
 */

/**
 * @typedef {Object} CreateRuleDto
 * @property {string} name
 * @property {string} [description]
 * @property {number} priority
 * @property {number} intervalSeconds
 * @property {boolean} isActive
 * @property {CreateRuleConditionDto[]} conditions
 * @property {CreateRuleActionDto[]} actions
 */

/**
 * @typedef {Object} UpdateRuleDto
 * @property {string} [name]
 * @property {string} [description]
 * @property {number} [priority]
 * @property {number} [intervalSeconds]
 * @property {boolean} [isActive]
 * @property {CreateRuleConditionDto[]} [conditions]
 * @property {CreateRuleActionDto[]} [actions]
 */

/**
 * @typedef {Object} RuleConditionDto
 * @property {number} id
 * @property {number} sensorId
 * @property {string} metricType
 * @property {string} operator
 * @property {number} threshold
 */

/**
 * @typedef {Object} CreateRuleConditionDto
 * @property {number} sensorId
 * @property {string} metricType
 * @property {string} operator
 * @property {number} threshold
 */

/**
 * @typedef {Object} RuleActionDto
 * @property {number} id
 * @property {number} executionOrder
 * @property {number} targetId
 * @property {string} targetType
 * @property {string} actionType
 * @property {string} parameterValue
 */

/**
 * @typedef {Object} CreateRuleActionDto
 * @property {number} executionOrder
 * @property {number} targetId
 * @property {string} targetType
 * @property {string} actionType
 * @property {string} parameterValue
 */
