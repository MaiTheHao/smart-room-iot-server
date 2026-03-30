class RuleApiV2Service {
  static instance;

  constructor() {
    if (RuleApiV2Service.instance) return RuleApiV2Service.instance;
    RuleApiV2Service.instance = this;

    this.api = SMRC_API_V2.RULE;
  }

  async getAll() {
    try {
      return await window.http.get(this.api.ALL);
    } catch (error) {
      this.#handleError('fetch all active rules', error);
    }
  }

  async getList(page = 0, size = 10) {
    try {
      return await window.http.get(this.api.PATH, { page, size });
    } catch (error) {
      this.#handleError('fetch rules', error);
    }
  }

  async getById(id) {
    try {
      return await window.http.get(this.api.DETAIL(id));
    } catch (error) {
      this.#handleError(`fetch rule ${id}`, error);
    }
  }

  async create(dto) {
    try {
      return await window.http.post(this.api.PATH, dto);
    } catch (error) {
      this.#handleError('create rule', error);
    }
  }

  async update(id, dto) {
    try {
      return await window.http.patch(this.api.DETAIL(id), dto);
    } catch (error) {
      this.#handleError(`update rule ${id}`, error);
    }
  }

  async delete(id) {
    try {
      return await window.http.delete(this.api.DETAIL(id));
    } catch (error) {
      this.#handleError(`delete rule ${id}`, error);
    }
  }

  async patchStatus(id, isActive) {
    try {
      const url = this.api.STATUS(id);
      return await window.http.patch(url, { isActive });
    } catch (error) {
      this.#handleError(`patch status rule ${id}`, error);
    }
  }

  async reload() {
    try {
      return await window.http.post(this.api.RELOAD);
    } catch (error) {
      this.#handleError('reload all rules', error);
    }
  }

  async execute(id) {
    try {
      return await window.http.post(this.api.EXECUTE(id));
    } catch (error) {
      this.#handleError(`trigger rule ${id} immediately`, error);
    }
  }

  #handleError(action, error) {
    console.error(`[RuleApiV2Service] Failed to ${action}:`, error);
    throw error;
  }
}

if (typeof window == 'undefined')
  throw new Error('RuleApiV2Service can only be initialized in a browser environment');
window.ruleApiV2Service = new RuleApiV2Service();
