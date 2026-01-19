class AutomationApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
	}

	async getAll(page = 0, size = 20) {
		try {
			return await this.client.get('automations', { page, size });
		} catch (error) {
			console.error('[AutomationService] Failed to fetch automations', error);
			throw error;
		}
	}

	async getById(id) {
		try {
			return await this.client.get(`automations/${id}`);
		} catch (error) {
			console.error(`[AutomationService] Failed to fetch automation ${id}`, error);
			throw error;
		}
	}

	async create(automationData) {
		try {
			return await this.client.post('automations', automationData);
		} catch (error) {
			console.error('[AutomationService] Failed to create automation', error);
			throw error;
		}
	}

	async update(id, updateData) {
		try {
			return await this.client.put(`automations/${id}`, updateData);
		} catch (error) {
			console.error(`[AutomationService] Failed to update automation ${id}`, error);
			throw error;
		}
	}

	async delete(id) {
		try {
			return await this.client.delete(`automations/${id}`);
		} catch (error) {
			console.error(`[AutomationService] Failed to delete automation ${id}`, error);
			throw error;
		}
	}

	async getActive() {
		try {
			return await this.client.get('automations/active');
		} catch (error) {
			console.error('[AutomationService] Failed to fetch active automations', error);
			throw error;
		}
	}

	async toggleStatus(id, isActive) {
		try {
			return await this.client.request('PATCH', `automations/${id}/toggle`, { isActive });
		} catch (error) {
			console.error(`[AutomationService] Failed to toggle status for automation ${id}`, error);
			throw error;
		}
	}

	async execute(id) {
		try {
			return await this.client.post(`automations/${id}/execute`);
		} catch (error) {
			console.error(`[AutomationService] Failed to execute automation ${id}`, error);
			throw error;
		}
	}

	async reload() {
		try {
			return await this.client.post('automations/reload');
		} catch (error) {
			console.error('[AutomationService] Failed to reload automations', error);
			throw error;
		}
	}
}

window.AutomationApiV1Service = AutomationApiV1Service;
