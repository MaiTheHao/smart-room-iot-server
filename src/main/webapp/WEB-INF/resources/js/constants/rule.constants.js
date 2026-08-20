export const RuleDataSource = Object.freeze({
  SYSTEM: 'SYSTEM',
  ROOM: 'ROOM',
  DEVICE: 'DEVICE',
  SENSOR: 'SENSOR'
});

export const ConditionOwnerCategory = Object.freeze({
  RULE: 'RULE',
  AUTOMATION: 'AUTOMATION',
  SYSTEM: 'SYSTEM'
});

export const ActionOwnerCategory = Object.freeze({
  RULE: 'RULE',
  AUTOMATION: 'AUTOMATION',
  SYSTEM: 'SYSTEM',
  ROOM_EVENT: 'ROOM_EVENT'
});

export const ConditionOperator = Object.freeze({
  EQUALS: '=',
  NOT_EQUALS: '!=',
  GREATER_THAN: '>',
  LESS_THAN: '<',
  GREATER_THAN_OR_EQUAL: '>=',
  LESS_THAN_OR_EQUAL: '<='
});

export const ConditionLogic = Object.freeze({
  AND: 'AND',
  OR: 'OR'
});
