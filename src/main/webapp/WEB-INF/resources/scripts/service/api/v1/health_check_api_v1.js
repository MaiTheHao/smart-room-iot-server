class HealthCheckApiV1Service {
	static instance;

	constructor() {
		if (HealthCheckApiV1Service.instance) return HealthCheckApiV1Service.instance;
		HealthCheckApiV1Service.instance = this;
	}

	async getRoomHealthScore(roomId) {
		try {
			const response = await window.http.get(SMRC_API_V1.ROOM.HEALTH_SCORE(roomId));
			return response.data;
		} catch (error) {
			this.#handleError(`get room score for ID: ${roomId}`, error);
		}
	}

	async getRoomHealthDetails(roomId) {
		try {
			const response = await window.http.get(SMRC_API_V1.ROOM.HEALTH(roomId));
			return response.data;
		} catch (error) {
			this.#handleError(`get room health details for ID: ${roomId}`, error);
		}
	}

	async getClientHealthScore(clientId) {
		try {
			const response = await window.http.get(SMRC_API_V1.CLIENT.HEALTH_SCORE(clientId));
			return response.data;
		} catch (error) {
			this.#handleError(`get client health score for ID: ${clientId}`, error);
		}
	}

	async getClientHealth(clientId) {
		try {
			const response = await window.http.get(SMRC_API_V1.CLIENT.HEALTH(clientId));
			return response.data;
		} catch (error) {
			this.#handleError(`get client health for ID: ${clientId}`, error);
		}
	}

	static evaluateStatus(score) {
		const types = SMRC_TYPES.HEALTH_STATUS;
		if (score === null || score === undefined || isNaN(score)) {
			return {
				level: types.UNKNOWN,
				className: 'badge-secondary',
				label: 'N/A',
				color: '#6c757d',
				icon: 'fa-question-circle',
			};
		}

		if (score >= 80) {
			return {
				level: types.GOOD,
				className: 'badge-success',
				label: 'Healthy',
				color: '#28a745',
				icon: 'fa-check-circle',
			};
		} else if (score >= 50) {
			return {
				level: types.WARNING,
				className: 'badge-warning',
				label: 'Unstable',
				color: '#ffc107',
				icon: 'fa-exclamation-triangle',
			};
		} else {
			return {
				level: types.CRITICAL,
				className: 'badge-danger',
				label: 'Critical',
				color: '#dc3545',
				icon: 'fa-times-circle',
			};
		}
	}

	async getRoomHealthUiConfig(roomId) {
		try {
			const score = await this.getRoomHealthScore(roomId);
			const statusConfig = HealthCheckApiV1Service.evaluateStatus(score);
			return { score, ...statusConfig };
		} catch (e) {
			return { score: 0, ...HealthCheckApiV1Service.evaluateStatus(null) };
		}
	}

	#handleError(action, error) {
		console.error(`[HealthCheckApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

if (typeof window == 'undefined') throw new Error('HealthCheckApiV1Service can only be initialized in a browser environment');
window.healthCheckApiV1Service = new HealthCheckApiV1Service();
