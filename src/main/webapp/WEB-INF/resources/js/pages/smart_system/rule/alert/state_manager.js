export const StateManager = (() => {
  /** @type {object[]} */
  let currentAlerts = [];
  let isDirty = false;

  const generateLocalId = () => 'local_' + Math.random().toString(36).substr(2, 9);

  /**
   * Initialize from API response (rule.data.alertConfigs[])
   * @param {object[]} alertsFromApi
   */
  const init = (alertsFromApi) => {
    currentAlerts = (alertsFromApi || [])
      .slice()
      .map((a) => ({ ...a, _localId: generateLocalId() }));
    isDirty = false;
    triggerListeners();
  };

  /** @returns {object[]} copy */
  const getAlerts = () => [...currentAlerts];

  /** @returns {object|undefined} */
  const getAlert = (localId) => currentAlerts.find((a) => a._localId === localId);

  /** @param {object} alertConfig — must NOT have _localId yet */
  const addAlert = (alertConfig) => {
    alertConfig._localId = generateLocalId();
    currentAlerts.push(alertConfig);
    isDirty = true;
    triggerListeners();
  };

  /**
   * @param {string} localId
   * @param {object} updatedData
   */
  const updateAlert = (localId, updatedData) => {
    const index = currentAlerts.findIndex((a) => a._localId === localId);
    if (index > -1) {
      currentAlerts[index] = { ...currentAlerts[index], ...updatedData };
      isDirty = true;
      triggerListeners();
    }
  };

  /** @param {string} localId */
  const deleteAlert = (localId) => {
    currentAlerts = currentAlerts.filter((a) => a._localId !== localId);
    isDirty = true;
    triggerListeners();
  };

  /**
   * Build the clean payload for PATCH /api/v1/rules/{id} → { alertConfigs: [...] }
   * - Strips internal _localId
   * @returns {object[]}
   */
  const buildPayload = () => {
    return currentAlerts.map((a) => ({
      id: typeof a.id === 'string' || typeof a.id === 'number' ? a.id : null,
      alertName: a.alertName,
      severity: a.severity,
      recipientGroups: a.recipientGroups,
      channels: a.channels,
      messageTemplate: a.messageTemplate,
      cooldownMinutes: a.cooldownMinutes,
      autoResolve: a.autoResolve
    }));
  };

  const getIsDirty = () => isDirty;

  const listeners = [];
  const subscribe = (fn) => listeners.push(fn);
  const triggerListeners = () => listeners.forEach((fn) => fn(isDirty));

  return {
    init,
    getAlerts,
    getAlert,
    addAlert,
    updateAlert,
    deleteAlert,
    buildPayload,
    getIsDirty,
    subscribe,
  };
})();
