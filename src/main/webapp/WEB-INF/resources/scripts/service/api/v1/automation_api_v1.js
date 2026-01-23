class AutomationApiV1Service {
	constructor(httpClient) {
		this.client = httpClient || new HttpClient();
		this.api = SMRC_API_V1.AUTOMATION;
	}

	async getAll(page = 0, size = 20) {
		try {
			return await this.client.get(this.api.PATH, { page, size });
		} catch (error) {
			this._handleError('fetch automations', error);
		}
	}

	async getById(id) {
		try {
			return await this.client.get(this.api.DETAIL(id));
		} catch (error) {
			this._handleError(`fetch automation ${id}`, error);
		}
	}

	async create(dto) {
		try {
			return await this.client.post(this.api.PATH, dto);
		} catch (error) {
			this._handleError('create automation', error);
		}
	}

	async update(id, dto) {
		try {
			return await this.client.put(this.api.DETAIL(id), dto);
		} catch (error) {
			this._handleError(`update automation ${id}`, error);
		}
	}

	async delete(id) {
		try {
			return await this.client.delete(this.api.DETAIL(id));
		} catch (error) {
			this._handleError(`delete automation ${id}`, error);
		}
	}

	async getActive() {
		try {
			return await this.client.get(this.api.ACTIVE);
		} catch (error) {
			this._handleError('fetch active automations', error);
		}
	}

	async getActions(automationId) {
		try {
			return await this.client.get(this.api.ACTIONS.ROOT(automationId));
		} catch (error) {
			this._handleError(`fetch actions for automation ${automationId}`, error);
		}
	}

	async addAction(automationId, dto) {
		try {
			return await this.client.post(this.api.ACTIONS.ROOT(automationId), dto);
		} catch (error) {
			this._handleError(`add action to automation ${automationId}`, error);
		}
	}

	async updateAction(actionId, dto) {
		try {
			return await this.client.put(this.api.ACTIONS.DETAIL(actionId), dto);
		} catch (error) {
			this._handleError(`update action ${actionId}`, error);
		}
	}

	async removeAction(actionId) {
		try {
			return await this.client.delete(this.api.ACTIONS.DETAIL(actionId));
		} catch (error) {
			this._handleError(`remove action ${actionId}`, error);
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
			this._handleError(`patch status ${id}`, error);
		}
	}

	async execute(id) {
		try {
			return await this.client.post(this.api.EXECUTE(id));
		} catch (error) {
			this._handleError(`execute automation ${id}`, error);
		}
	}

	async reloadJob() {
		try {
			return await this.client.post(this.api.RELOAD);
		} catch (error) {
			this._handleError('reload system jobs', error);
		}
	}

	_handleError(action, error) {
		console.error(`[AutomationApiV1Service] Failed to ${action}:`, error);
		throw error;
	}
}

window.AutomationApiV1Service = AutomationApiV1Service;
