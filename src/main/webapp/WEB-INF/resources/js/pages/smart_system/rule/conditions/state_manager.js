export const StateManager = (() => {
  /** @type {object[]} */
  let currentConditions = [];
  let isDirty = false;

  const generateLocalId = () => 'local_' + Math.random().toString(36).substr(2, 9);

  /**
   * Re-assign sortOrder sequentially (0, 1, 2...) after add/delete
   */
  const reindex = () => {
    currentConditions.forEach((c, i) => { c.sortOrder = i; });
  };

  /**
   * Initialize from API response (rule.data.conditions[])
   * @param {object[]} conditionsFromApi
   */
  const init = (conditionsFromApi) => {
    currentConditions = (conditionsFromApi || [])
      .slice()
      .sort((a, b) => a.sortOrder - b.sortOrder)
      .map((c) => ({ ...c, _localId: generateLocalId() }));
    isDirty = false;
    triggerListeners();
  };

  /** @returns {object[]} sorted copy */
  const getConditions = () => [...currentConditions];

  /** @returns {object|undefined} */
  const getCondition = (localId) => currentConditions.find((c) => c._localId === localId);

  /** @param {object} condition — must NOT have _localId yet */
  const addCondition = (condition) => {
    condition._localId = generateLocalId();
    currentConditions.push(condition);
    currentConditions.sort((a, b) => a.sortOrder - b.sortOrder);
    reindex();
    isDirty = true;
    triggerListeners();
  };

  /**
   * @param {string} localId
   * @param {object} updatedData
   */
  const updateCondition = (localId, updatedData) => {
    const index = currentConditions.findIndex((c) => c._localId === localId);
    if (index > -1) {
      currentConditions[index] = { ...currentConditions[index], ...updatedData };
      currentConditions.sort((a, b) => a.sortOrder - b.sortOrder);
      reindex();
      isDirty = true;
      triggerListeners();
    }
  };

  /** @param {string} localId */
  const deleteCondition = (localId) => {
    currentConditions = currentConditions.filter((c) => c._localId !== localId);
    reindex();
    isDirty = true;
    triggerListeners();
  };

  /**
   * Build the clean payload for PATCH /api/v1/rules/{id} → { conditions: [...] }
   * - Strips internal _localId
   * - Re-assigns sortOrder by position
   * - Sets nextLogic=null on the last item
   * @returns {object[]}
   */
  const buildPayload = () => {
    return currentConditions.map((c, i) => ({
      sortOrder: i,
      dataSource: c.dataSource,
      resourceParam: c.resourceParam,
      operator: c.operator,
      value: String(c.value),
      nextLogic: i < currentConditions.length - 1 ? (c.nextLogic || 'AND') : null,
    }));
  };

  const getIsDirty = () => isDirty;

  const listeners = [];
  const subscribe = (fn) => listeners.push(fn);
  const triggerListeners = () => listeners.forEach((fn) => fn(isDirty));

  return {
    init,
    getConditions,
    getCondition,
    addCondition,
    updateCondition,
    deleteCondition,
    buildPayload,
    getIsDirty,
    subscribe,
  };
})();
