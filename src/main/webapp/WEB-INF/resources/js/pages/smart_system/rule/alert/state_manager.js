export const StateManager = (() => {
  /** @type {object[]} */
  let initialAlerts = [];
  /** @type {object[]} */
  let currentAlerts = [];
  let isDirty = false;

  const generateLocalId = () => 'local_' + Math.random().toString(36).substr(2, 9);

  /**
   * Initialize from API response (alerts list)
   * @param {object[]} alertsFromApi
   */
  const init = (alertsFromApi) => {
    initialAlerts = JSON.parse(JSON.stringify(alertsFromApi || []));
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
   * Calculates diff between initial and current state
   * @returns {{ toCreate: object[], toUpdate: object[], toDelete: (number|string)[] }}
   */
  const getDiff = () => {
    const currentIdSet = new Set(currentAlerts.filter((a) => a.id).map((a) => a.id));
    const toCreate = currentAlerts.filter((a) => !a.id);
    const toUpdate = currentAlerts.filter((a) => a.id);
    const toDelete = initialAlerts.filter((a) => a.id && !currentIdSet.has(a.id)).map((a) => a.id);

    return { toCreate, toUpdate, toDelete };
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
    getDiff,
    getIsDirty,
    subscribe,
  };
})();
