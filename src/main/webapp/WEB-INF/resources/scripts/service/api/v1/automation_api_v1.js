class AutomationApiV1Service {
	static instance;

	constructor() {
		if (AutomationApiV1Service.instance) return AutomationApiV1Service.instance;
		AutomationApiV1Service.instance = this;

		this.client = new HttpClient('/api/v1');
		this.api = SMRC_API_V1.AUTOMATION;
	}

	async getAll(page = 0, size = 20) {
		try {
			return await this.client.get(this.api.PATH, { page, size });
		} catch (error) {
			this.#handleError('fetch automations', error);
		}
	}

	async getById(id) {
		try {
			return await this.client.get(this.api.DETAIL(id));
		} catch (error) {
			this.#handleError(`fetch automation ${id}`, error);
		}
	}

	async create(dto) {
		try {
			return await this.client.post(this.api.PATH, dto);
		} catch (error) {
			this.#handleError('create automation', error);
		}
	}

	async update(id, dto) {
		try {
			return await this.client.put(this.api.DETAIL(id), dto);
		} catch (error) {
			this.#handleError(`update automation ${id}`, error);
		}
	}

	async delete(id) {
		try {
			return await this.client.delete(this.api.DETAIL(id));
		} catch (error) {
			this.#handleError(`delete automation ${id}`, error);
		}
	}

	async getActive() {
		try {
			return await this.client.get(this.api.ACTIVE);
		} catch (error) {
			this.#handleError('fetch active automations', error);
		}
	}

	async getActions(automationId) {
		try {
			return await this.client.get(SMRC_API_V1.AUTOMATION_ACTION.BY_AUTOMATION(automationId));
		} catch (error) {
			this.#handleError(`fetch actions for automation ${automationId}`, error);
		}
	}

	async addAction(automationId, dto) {
		try {
			return await this.client.post(SMRC_API_V1.AUTOMATION_ACTION.BY_AUTOMATION(automationId), dto);
		} catch (error) {
			this.#handleError(`add action to automation ${automationId}`, error);
		}
	}

	async updateAction(actionId, dto) {
		try {
			return await this.client.put(SMRC_API_V1.AUTOMATION_ACTION.DETAIL(actionId), dto);
		} catch (error) {
			this.#handleError(`update action ${actionId}`, error);
		}
	}

	async removeAction(actionId) {
		try {
			return await this.client.delete(SMRC_API_V1.AUTOMATION_ACTION.DETAIL(actionId));
		} catch (error) {
			this.#handleError(`remove action ${actionId}`, error);
		}
	}

	async patchStatus(id, isActive) {
		try {
			const url = this.api.STATUS(id);
			if (this.client.patch) {
				return await this.client.patch(url, null, { params: { isActive } });
			} else {
				return await this.client.request('PATCH', `${url}?isActive=${isActive}`);
			}
		} catch (error) {
			this.#handleError(`patch status ${id}`, error);
		}
	}

	async execute(id) {
		try {
			return await this.client.post(this.api.EXECUTE(id));
		} catch (error) {
			this.#handleError(`execute automation ${id}`, error);
		}
	}

	async reloadJob() {
		try {
			return await this.client.post(this.api.RELOAD);
		} catch (error) {
			this.#handleError('reload system jobs', error);
		}
	}

	#handleError(action, error) {
		console.error(`[AutomationApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

if (typeof window == 'undefined') throw new Error('AutomationApiV1Service can only be initialized in a browser environment');
window.automationApiV1Service = new AutomationApiV1Service();
