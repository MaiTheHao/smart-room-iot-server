class HealthCheckApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
	}

	async getRoomHealthScore(roomId) {
		try {
			const response = await this.client.get(`rooms/${roomId}/health-score`);
			return response.data;
		} catch (error) {
			console.error(`[HealthService] Failed to get room score for ID: ${roomId}`, error);
			throw error;
		}
	}

	async getRoomHealthDetails(roomId) {
		const response = await this.client.get(`rooms/${roomId}/health`);
		return response.data;
	}

	async getClientHealthScore(clientId) {
		const response = await this.client.get(`clients/${clientId}/health-score`);
		return response.data;
	}

	async getClientHealth(clientId) {
		const response = await this.client.get(`clients/${clientId}/health`);
		return response.data;
	}

	static evaluateStatus(score) {
		if (score === null || score === undefined || isNaN(score)) {
			return {
				level: 'UNKNOWN',
				className: 'badge-secondary',
				label: 'N/A',
				color: '#6c757d',
				icon: 'fa-question-circle',
			};
		}

		if (score >= 80) {
			return {
				level: 'GOOD',
				className: 'badge-success',
				label: 'Healthy',
				color: '#28a745',
				icon: 'fa-check-circle',
			};
		} else if (score >= 50) {
			return {
				level: 'WARNING',
				className: 'badge-warning',
				label: 'Unstable',
				color: '#ffc107',
				icon: 'fa-exclamation-triangle',
			};
		} else {
			return {
				level: 'CRITICAL',
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
			const statusConfig = HealthCheckService.evaluateStatus(score);
			return {
				score: score,
				...statusConfig,
			};
		} catch (e) {
			return {
				score: 0,
				...HealthCheckService.evaluateStatus(null),
			};
		}
	}
}

window.HealthCheckApiV1Service = HealthCheckApiV1Service;
