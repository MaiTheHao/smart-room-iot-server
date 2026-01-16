class RoomApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
	}

	async getHealthScore(roomId) {
		try {
			const response = await this.client.get(`rooms/${roomId}/health-score`);
			return response.data;
		} catch (error) {
			console.error(`[RoomService] Failed to get room health score ${roomId}`, error);
			throw error;
		}
	}

	async getHealth(roomId) {
		try {
			return await this.client.get(`rooms/${roomId}/health`);
		} catch (error) {
			console.error(`[RoomService] Failed to get room health ${roomId}`, error);
			throw error;
		}
	}

	async getHealthUiConfig(roomId) {
		try {
			const score = await this.getHealthScore(roomId);
			const statusConfig = this.evaluateStatus(score);
			return {
				score: score,
				...statusConfig,
			};
		} catch (error) {
			console.error(`[RoomService] Failed to get room health UI config ${roomId}`, error);
			return {
				score: 0,
				...this.evaluateStatus(null),
			};
		}
	}

	evaluateStatus(score) {
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

	async toggleLight(lightId) {
		try {
			return await this.client.put(`lights/${lightId}/toggle-state`);
		} catch (error) {
			console.error(`[RoomService] Failed to toggle light ${lightId}`, error);
			throw error;
		}
	}

	async toggleLightAndRefreshHealth(lightId, roomId) {
		try {
			const lightResponse = await this.toggleLight(lightId);
			const health = await this.getHealthScore(roomId);
			return {
				light: lightResponse.data,
				health: health,
			};
		} catch (error) {
			console.error(`[RoomService] Failed to toggle light with health update ${lightId}`, error);
			throw error;
		}
	}
}

window.RoomApiV1Service = RoomApiV1Service;
