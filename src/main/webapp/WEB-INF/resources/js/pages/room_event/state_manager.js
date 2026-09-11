import {
  mapConditionsForReplace,
  mapActionsForReplace,
} from '../../common/smart_system_util.js';

export const StateManager = (() => {
  let roomId = null;
  let availableCodes = [];
  let configsMap = {};
  let selectedCode = null;

  let currentConfig = null;
  let initialConfigDraft = { isActive: true, cooldownSeconds: 60 };
  let currentConfigDraft = { isActive: true, cooldownSeconds: 60 };

  let initialConditions = [];
  let currentConditions = [];

  let initialActions = [];
  let currentActions = [];

  let isDirty = false;
  const listeners = [];

  const generateLocalId = () => 'local_' + Math.random().toString(36).substring(2, 11);

  const stripForCompare = (list) =>
    list.map(({ _localId, targetDisplay, ...rest }) => rest);

  const init = (pRoomId, pAvailableCodes, pConfigs) => {
    roomId = pRoomId;
    availableCodes = pAvailableCodes || [];
    configsMap = {};
    (pConfigs || []).forEach((cfg) => {
      if (cfg.eventCode) {
        configsMap[cfg.eventCode] = cfg;
      }
    });

    if (availableCodes.length > 0) {
      selectEventCode(availableCodes[0].code);
    } else {
      selectedCode = null;
      currentConfig = null;
      currentConditions = [];
      currentActions = [];
      isDirty = false;
      notify();
    }
  };

  const selectEventCode = (code) => {
    selectedCode = code;
    currentConfig = configsMap[code] || null;

    if (currentConfig) {
      currentConfigDraft = {
        isActive: currentConfig.isActive !== false,
        cooldownSeconds: currentConfig.cooldownSeconds ?? 60,
      };
      initialConfigDraft = { ...currentConfigDraft };
    } else {
      currentConfigDraft = { isActive: true, cooldownSeconds: 60 };
      initialConfigDraft = { ...currentConfigDraft };
      currentConditions = [];
      initialConditions = [];
      currentActions = [];
      initialActions = [];
    }

    isDirty = false;
    notify();
  };

  const setLoadedSubResources = (conditionsFromApi, actionsFromApi) => {
    initialConditions = JSON.parse(JSON.stringify(conditionsFromApi || []));
    currentConditions = (conditionsFromApi || []).map((c) => ({
      ...c,
      _localId: generateLocalId(),
    }));

    initialActions = JSON.parse(JSON.stringify(actionsFromApi || []));
    currentActions = (actionsFromApi || []).map((a) => ({
      ...a,
      _localId: generateLocalId(),
      params: typeof (a.params || a.actionParams) === 'string'
        ? JSON.parse(a.params || a.actionParams)
        : (a.params || a.actionParams || {}),
    }));

    isDirty = false;
    notify();
  };

  const recomputeDirty = () => {
    const configDirty =
      currentConfigDraft.isActive !== initialConfigDraft.isActive ||
      Number(currentConfigDraft.cooldownSeconds) !== Number(initialConfigDraft.cooldownSeconds);

    const conditionsDirty =
      JSON.stringify(stripForCompare(currentConditions)) !==
      JSON.stringify(stripForCompare(initialConditions));

    const actionsDirty =
      JSON.stringify(stripForCompare(currentActions)) !==
      JSON.stringify(stripForCompare(initialActions));

    isDirty = configDirty || conditionsDirty || actionsDirty;
  };

  const updateConfigDraft = (changes) => {
    currentConfigDraft = { ...currentConfigDraft, ...changes };
    recomputeDirty();
    notify();
  };

  const addCondition = (cond) => {
    cond._localId = generateLocalId();
    currentConditions.push(cond);
    recomputeDirty();
    notify();
  };

  const updateCondition = (localId, updated) => {
    const idx = currentConditions.findIndex((c) => c._localId === localId);
    if (idx !== -1) {
      currentConditions[idx] = { ...currentConditions[idx], ...updated };
      recomputeDirty();
      notify();
    }
  };

  const deleteCondition = (localId) => {
    currentConditions = currentConditions.filter((c) => c._localId !== localId);
    recomputeDirty();
    notify();
  };

  const addAction = (act) => {
    act._localId = generateLocalId();
    currentActions.push(act);
    recomputeDirty();
    notify();
  };

  const updateAction = (localId, updated) => {
    const idx = currentActions.findIndex((a) => a._localId === localId);
    if (idx !== -1) {
      currentActions[idx] = { ...currentActions[idx], ...updated };
      recomputeDirty();
      notify();
    }
  };

  const deleteAction = (localId) => {
    currentActions = currentActions.filter((a) => a._localId !== localId);
    recomputeDirty();
    notify();
  };

  const buildConditionsPayload = () => mapConditionsForReplace(currentConditions);

  const buildActionsPayload = () => mapActionsForReplace(currentActions);

  const onSavedSuccess = (savedConfig) => {
    if (savedConfig && savedConfig.eventCode) {
      configsMap[savedConfig.eventCode] = savedConfig;
      currentConfig = savedConfig;
      currentConfigDraft = {
        isActive: savedConfig.isActive !== false,
        cooldownSeconds: savedConfig.cooldownSeconds ?? 60,
      };
      initialConfigDraft = { ...currentConfigDraft };
    }
    initialConditions = JSON.parse(JSON.stringify(currentConditions));
    initialActions = JSON.parse(JSON.stringify(currentActions));
    isDirty = false;
    notify();
  };

  const onDeletedSuccess = (eventCode) => {
    delete configsMap[eventCode];
    if (selectedCode === eventCode) {
      currentConfig = null;
      currentConditions = [];
      initialConditions = [];
      currentActions = [];
      initialActions = [];
      currentConfigDraft = { isActive: true, cooldownSeconds: 60 };
      initialConfigDraft = { ...currentConfigDraft };
    }
    isDirty = false;
    notify();
  };

  const subscribe = (fn) => listeners.push(fn);
  const notify = () => listeners.forEach((fn) => fn({
    roomId,
    availableCodes,
    selectedCode,
    currentConfig,
    currentConfigDraft,
    conditions: [...currentConditions],
    actions: [...currentActions],
    isDirty,
  }));

  return {
    init,
    selectEventCode,
    setLoadedSubResources,
    updateConfigDraft,
    getAvailableCodes: () => [...availableCodes],
    getSelectedCode: () => selectedCode,
    getCurrentConfig: () => currentConfig,
    getConfigDraft: () => ({ ...currentConfigDraft }),
    getConditions: () => [...currentConditions],
    getActions: () => [...currentActions],
    addCondition,
    updateCondition,
    deleteCondition,
    addAction,
    updateAction,
    deleteAction,
    buildConditionsPayload,
    buildActionsPayload,
    getIsDirty: () => isDirty,
    onSavedSuccess,
    onDeletedSuccess,
    subscribe,
  };
})();
