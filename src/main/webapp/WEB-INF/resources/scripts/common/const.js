const SMRC_API_V1 = {
  AUTOMATION: {
    PATH: '/api/v1/automations',
    DETAIL: (id) => `/api/v1/automations/${id}`,
    STATUS: (id) => `/api/v1/automations/${id}/status`,
    EXECUTE: (id) => `/api/v1/automations/${id}/execute`,
    ACTIVE: '/api/v1/automations/active',
    RELOAD: '/api/v1/automations/reload-job',
  },

  AUTOMATION_ACTION: {
    BY_AUTOMATION: (automationId) => `/api/v1/automations/${automationId}/actions`,
    DETAIL: (actionId) => `/api/v1/automations/actions/${actionId}`,
  },

  FLOOR: {
    PATH: '/api/v1/floors',
    ALL: '/api/v1/floors/all',
    DETAIL: (id) => `/api/v1/floors/${id}`,
  },

  ROOM: {
    PATH: '/api/v1/rooms',
    DETAIL: (id) => `/api/v1/rooms/${id}`,
    HEALTH: (id) => `/api/v1/rooms/${id}/health`,
    HEALTH_SCORE: (id) => `/api/v1/rooms/${id}/health-score`,
    HEALTH_CODE: '/api/v1/rooms/health',
    BY_FLOOR: (floorId) => `/api/v1/floors/${floorId}/rooms`,
    BY_FLOOR_ALL: (floorId) => `/api/v1/floors/${floorId}/rooms/all`,
  },

  CLIENT: {
    PATH: '/api/v1/clients',
    DETAIL: (id) => `/api/v1/clients/${id}`,
    HEALTH: (id) => `/api/v1/clients/${id}/health`,
    HEALTH_SCORE: (id) => `/api/v1/clients/${id}/health-score`,
    HEALTH_IP: '/api/v1/clients/health',
    BY_ROOM: (roomId) => `/api/v1/clients/room/${roomId}`,
    BY_GROUP: (groupId) => `/api/v1/groups/${groupId}/clients`,
    BY_GROUP_ALL: (groupId) => `/api/v1/groups/${groupId}/clients/all`,
    BY_GROUP_COUNT: (groupId) => `/api/v1/groups/${groupId}/clients/count`,
    DELETE_ALL_DEVICE_CONTROLS: (clientId) => `/api/v1/clients/${clientId}/device-controls`,
  },

  SETUP: {
    TRIGGER: (clientId) => `/api/v1/setup/${clientId}`,
  },

  GROUP: {
    PATH: '/api/v1/groups',
    ALL: '/api/v1/groups/all',
    COUNT: '/api/v1/groups/count',
    DETAIL: (id) => `/api/v1/groups/${id}`,
    BY_CODE: (code) => `/api/v1/groups/code/${code}`,
    FOR_CLIENT: (clientId) => `/api/v1/clients/${clientId}/groups`,
    FOR_CLIENT_ALL: (clientId) => `/api/v1/clients/${clientId}/groups/all`,
    FOR_CLIENT_COUNT: (clientId) => `/api/v1/clients/${clientId}/groups/count`,
    WITH_CLIENT_STATUS: (clientId) => `/api/v1/groups/with-client-status/${clientId}`,
  },

  FUNCTION: {
    PATH: '/api/v1/functions',
    ALL: '/api/v1/functions/all',
    COUNT: '/api/v1/functions/count',
    DETAIL: (id) => `/api/v1/functions/${id}`,
    BY_CODE: (code) => `/api/v1/functions/code/${code}`,
    BY_GROUP: (groupId) => `/api/v1/groups/${groupId}/functions`,
    BY_GROUP_ALL: (groupId) => `/api/v1/groups/${groupId}/functions/all`,
    BY_GROUP_COUNT: (groupId) => `/api/v1/groups/${groupId}/functions/count`,
    WITH_GROUP_STATUS: (groupId) => `/api/v1/functions/with-group-status/${groupId}`,
  },

  ROLE: {
    PATH: '/api/v1/roles',
    GROUPS: {
      BATCH_ADD: '/api/v1/roles/groups/functions/batch-add',
      BATCH_REMOVE: '/api/v1/roles/groups/functions/batch-remove',
      TOGGLE: '/api/v1/roles/groups/functions/toggle',
      ASSIGN: (groupId, functionCode) =>
        `/api/v1/roles/groups/${groupId}/functions/${functionCode}`,
      CHECK: (groupId, functionCode) =>
        `/api/v1/roles/groups/${groupId}/functions/${functionCode}/check`,
    },
    CLIENTS: {
      ASSIGN: '/api/v1/roles/clients/groups/assign',
      UNASSIGN: '/api/v1/roles/clients/groups/unassign',
      MAPPING: (clientId, groupId) => `/api/v1/roles/clients/${clientId}/groups/${groupId}`,
    },
  },

  DEVICE_METADATA: {
    ALL: '/api/v1/devices/all',
    BY_ROOM: (roomId) => `/api/v1/rooms/${roomId}/devices`,
  },

  LIGHT: {
    PATH: '/api/v1/lights',
    DETAIL: (id) => `/api/v1/lights/${id}`,
    BY_ROOM: (roomId) => `/api/v1/lights/room/${roomId}`,
    ALL_BY_ROOM: (roomId) => `/api/v1/lights/room/${roomId}/all`,
    CONTROL: (naturalId) => `/api/v1/lights/${naturalId}/control`,
  },

  FAN: {
    PATH: '/api/v1/fans',
    ALL: '/api/v1/fans/all',
    DETAIL: (id) => `/api/v1/fans/${id}`,
    BY_ROOM: (roomId) => `/api/v1/room/${roomId}/fans`,
    ALL_BY_ROOM: (roomId) => `/api/v1/room/${roomId}/fans/all`,
    BY_ROOM_NATURAL_ID: (roomId, naturalId) => `/api/v1/room/${roomId}/fans/${naturalId}`,
    CONTROL: (naturalId) => `/api/v1/fans/${naturalId}/control`,
  },

  AIR_CONDITION: {
    PATH: '/api/v1/air-conditions',
    DETAIL: (id) => `/api/v1/air-conditions/${id}`,
    BY_ROOM: (roomId) => `/api/v1/air-conditions/room/${roomId}`,
    ALL_BY_ROOM: (roomId) => `/api/v1/air-conditions/room/${roomId}/all`,
    CONTROL: (naturalId) => `/api/v1/air-conditions/${naturalId}/control`,
  },

  TELEMETRY: {
    PATH: '/api/v1/telemetries',
    BY_GATEWAY: (gatewayUsername) => `/api/v1/telemetries/gateway/${gatewayUsername}`,
    BY_ROOM: (roomCode) => `/api/v1/telemetries/room/${roomCode}`,
    TEMPERATURE: (naturalId) => `/api/v1/telemetries/temperature/${naturalId}`,
    POWER_CONSUMPTION: (naturalId) => `/api/v1/telemetries/power-consumption/${naturalId}`,
  },

  RULE: {
    PATH: '/api/v1/rules',
    ALL: '/api/v1/rules/all',
    DETAIL: (id) => `/api/v1/rules/${id}`,
    STATUS: (id) => `/api/v1/rules/${id}/status`,
    SCAN: '/api/v1/rules/scan',
    RELOAD: '/api/v1/rules/reload',
  },

  TEMPERATURE: {
    PATH: '/api/v1/temperatures',
    ALL: '/api/v1/temperatures/all',
    DETAIL: (id) => `/api/v1/temperatures/${id}`,
    BY_ROOM: (roomId) => `/api/v1/rooms/${roomId}/temperatures`,
    ALL_BY_ROOM: (roomId) => `/api/v1/rooms/${roomId}/temperatures/all`,
  },

  POWER_CONSUMPTION: {
    PATH: '/api/v1/power-consumptions',
    ALL: '/api/v1/power-consumptions/all',
    DETAIL: (id) => `/api/v1/power-consumptions/${id}`,
    BY_ROOM: (roomId) => `/api/v1/rooms/${roomId}/power-consumptions`,
    ALL_BY_ROOM: (roomId) => `/api/v1/rooms/${roomId}/power-consumptions/all`,
  },
};

const SMRC_API_V2 = {
  RULE: {
    PATH: '/api/v2/rules',
    ALL: '/api/v2/rules/all',
    DETAIL: (id) => `/api/v2/rules/${id}`,
    STATUS: (id) => `/api/v2/rules/${id}/status`,
    RELOAD: '/api/v2/rules/reload',
    EXECUTE: (id) => `/api/v2/rules/${id}/execute`,
  },
};

const SMRC_TYPES = {
  CLIENT_TYPE: {
    USER: 'USER',
    HARDWARE_GATEWAY: 'HARDWARE_GATEWAY',
  },
  HEALTH_STATUS: {
    GOOD: 'GOOD',
    WARNING: 'WARNING',
    CRITICAL: 'CRITICAL',
    UNKNOWN: 'UNKNOWN',
  },
};

const SMRC_DEVICES = {};

if (typeof window !== 'undefined') {
  window.SMRC_API_V1 = SMRC_API_V1;
  window.SMRC_API_V2 = SMRC_API_V2;
  window.SMRC_TYPES = SMRC_TYPES;
}
