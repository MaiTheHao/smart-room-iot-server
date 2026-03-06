class RuleApiV1Service {
  static instance;

  constructor() {
    if (RuleApiV1Service.instance) return RuleApiV1Service.instance;
    RuleApiV1Service.instance = this;

    this.api = SMRC_API_V1.RULE;
  }

  async getAll() {
    try {
      return await window.http.get(this.api.ALL);
    } catch (error) {
      this.#handleError('fetch all rules', error);
    }
  }

  async getList(page = 0, size = 20) {
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
      return await window.http.put(this.api.DETAIL(id), dto);
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
      return await window.http.patch(url, null, { params: { isActive } });
    } catch (error) {
      this.#handleError(`patch status rule ${id}`, error);
    }
  }

  async scan() {
    try {
      return await window.http.post(this.api.SCAN);
    } catch (error) {
      this.#handleError('execute rule scan', error);
    }
  }

  async reload() {
    try {
      return await window.http.post(this.api.RELOAD);
    } catch (error) {
      this.#handleError('reload rules', error);
    }
  }

  #handleError(action, error) {
    console.error(`[RuleApiV1Service] Failed to ${action}:`, error);
    throw error;
  }
}

if (typeof window == 'undefined')
  throw new Error('RuleApiV1Service can only be initialized in a browser environment');
window.ruleApiV1Service = new RuleApiV1Service();
