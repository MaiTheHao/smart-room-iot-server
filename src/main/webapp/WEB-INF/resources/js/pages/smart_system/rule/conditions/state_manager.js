import { mapConditionsForReplace } from '../../../../common/smart_system_util.js';

export const StateManager = (() => {

  let currentConditions = [];
  let isDirty = false;

  const generateLocalId = () => 'local_' + Math.random().toString(36).substr(2, 9);

  const reindex = () => {
    currentConditions.forEach((c, i) => { c.sortOrder = i; });
  };

  const init = (conditionsFromApi) => {
    currentConditions = (conditionsFromApi || [])
      .slice()
      .sort((a, b) => a.sortOrder - b.sortOrder)
      .map((c) => ({ ...c, _localId: generateLocalId() }));
    isDirty = false;
    triggerListeners();
  };

  const getConditions = () => [...currentConditions];

  const getCondition = (localId) => currentConditions.find((c) => c._localId === localId);

  const addCondition = (condition) => {
    condition._localId = generateLocalId();
    currentConditions.push(condition);
    currentConditions.sort((a, b) => a.sortOrder - b.sortOrder);
    reindex();
    isDirty = true;
    triggerListeners();
  };

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

  const deleteCondition = (localId) => {
    currentConditions = currentConditions.filter((c) => c._localId !== localId);
    reindex();
    isDirty = true;
    triggerListeners();
  };

  const buildPayload = () => mapConditionsForReplace(currentConditions);

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
