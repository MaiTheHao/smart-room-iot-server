const SMRC_API_V1 = {
	AUTOMATION: {
		PATH: 'automations',
		DETAIL: (id) => `automations/${id}`,
		STATUS: (id) => `automations/${id}/status`,
		EXECUTE: (id) => `automations/${id}/execute`,
		ACTIVE: 'automations/active',
		RELOAD: 'automations/reload-job',
		ACTIONS: {
			ROOT: (automationId) => `automations/${automationId}/actions`,
			DETAIL: (actionId) => `automations/actions/${actionId}`,
		},
	},

	CLIENT: {
		PATH: 'clients',
		DETAIL: (id) => `clients/${id}`,
		BY_ROOM: (roomId) => `clients/room/${roomId}`,
		HEALTH: (id) => `clients/${id}/health`,
		HEALTH_SCORE: (id) => `clients/${id}/health-score`,
		HEALTH_IP: 'clients/health',
		GROUPS: {
			ROOT: (id) => `clients/${id}/groups`,
			ALL: (id) => `clients/${id}/groups/all`,
			COUNT: (id) => `clients/${id}/groups/count`,
		},
	},

	FLOOR: {
		PATH: 'floors',
		DETAIL: (id) => `floors/${id}`,
		ROOMS: (id) => `floors/${id}/rooms`,
	},

	ROOM: {
		PATH: 'rooms',
		DETAIL: (id) => `rooms/${id}`,
		HEALTH: (id) => `rooms/${id}/health`,
		HEALTH_SCORE: (id) => `rooms/${id}/health-score`,
		HEALTH_CODE: 'rooms/health',
		CLIENTS: (id) => `clients/room/${id}`,
	},

	GROUP: {
		PATH: 'groups',
		ALL: 'groups/all',
		COUNT: 'groups/count',
		DETAIL: (id) => `groups/${id}`,
		BY_CODE: (code) => `groups/code/${code}`,
		WITH_CLIENT_STATUS: (clientId) => `groups/with-client-status/${clientId}`,
		FUNCTIONS: {
			ROOT: (id) => `groups/${id}/functions`,
			ALL: (id) => `groups/${id}/functions/all`,
			COUNT: (id) => `groups/${id}/functions/count`,
		},
		CLIENTS: {
			ROOT: (id) => `groups/${id}/clients`,
			ALL: (id) => `groups/${id}/clients/all`,
			COUNT: (id) => `groups/${id}/clients/count`,
		},
	},

	FUNCTION: {
		PATH: 'functions',
		ALL: 'functions/all',
		COUNT: 'functions/count',
		DETAIL: (id) => `functions/${id}`,
		BY_CODE: (code) => `functions/code/${code}`,
		WITH_GROUP_STATUS: (groupId) => `functions/with-group-status/${groupId}`,
	},

	ROLE: {
		PATH: 'roles',
		GROUPS: {
			BATCH_ADD: 'roles/groups/functions/batch-add',
			BATCH_REMOVE: 'roles/groups/functions/batch-remove',
			TOGGLE: 'roles/groups/functions/toggle',
			ASSIGN: (groupId, functionCode) => `roles/groups/${groupId}/functions/${functionCode}`,
			CHECK: (groupId, functionCode) => `roles/groups/${groupId}/functions/${functionCode}/check`,
		},
		CLIENTS: {
			ASSIGN: 'roles/clients/groups/assign',
			UNASSIGN: 'roles/clients/groups/unassign',
			MAPPING: (clientId, groupId) => `roles/clients/${clientId}/groups/${groupId}`,
		},
	},
	LIGHT: {
		BASE: 'lights',
		DETAIL: (id) => `lights/${id}`,
		BY_ROOM: (roomId) => `lights/room/${roomId}`,
		TOGGLE: (id) => `lights/${id}/toggle-state`,
		LEVEL: (id, newLevel) => `lights/${id}/level/${newLevel}`,
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

if (typeof window !== 'undefined') {
	window.SMRC_API_V1 = SMRC_API_V1;
	window.SMRC_TYPES = SMRC_TYPES;
}
