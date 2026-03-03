class SetupApiV1Service {
  static instance;

  constructor() {
    if (SetupApiV1Service.instance) return SetupApiV1Service.instance;
    SetupApiV1Service.instance = this;

    this.api = SMRC_API_V1.SETUP;
  }

  async triggerSetup(clientId) {
    try {
      return await window.http.post(this.api.TRIGGER(clientId), {});
    } catch (error) {
      this.#handleError(`trigger setup for client ${clientId}`, error);
    }
  }

  #handleError(action, error) {
    console.error(`[SetupApiV1Service] Failed to ${action}:`, error);
    throw error;
  }
}

if (typeof window !== 'undefined') {
  window.setupApiV1Service = new SetupApiV1Service();
}
