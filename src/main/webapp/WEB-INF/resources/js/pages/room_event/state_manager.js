export const StateManager = (() => {
  let roomId = null;
  let availableCodes = [];
  let configsMap = {}; // eventCode -> configDto
  let selectedCode = null;

  let currentConfig = null; // configDto | null
  let initialConfigDraft = { isActive: true, cooldownSeconds: 60 };
  let currentConfigDraft = { isActive: true, cooldownSeconds: 60 };

  let initialConditions = [];
  let currentConditions = [];

  let initialActions = [];
  let currentActions = [];

  let isDirty = false;
  const listeners = [];

  const generateLocalId = () => 'local_' + Math.random().toString(36).substring(2, 11);

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

  const computeDirty = () => {
    // 1. Check config draft dirty
    const isConfigDirty =
      currentConfigDraft.isActive !== initialConfigDraft.isActive ||
      Number(currentConfigDraft.cooldownSeconds) !== Number(initialConfigDraft.cooldownSeconds);

    if (isConfigDirty) {
      isDirty = true;
      return;
    }

    // 2. Check conditions length or content
    if (currentConditions.length !== initialConditions.length) {
      isDirty = true;
      return;
    }

    // 3. Check actions length or content
    if (currentActions.length !== initialActions.length) {
      isDirty = true;
      return;
    }

    isDirty = false;
  };

  const updateConfigDraft = (changes) => {
    currentConfigDraft = { ...currentConfigDraft, ...changes };
    isDirty = true;
    notify();
  };

  // --- Conditions CRUD ---

  const addCondition = (cond) => {
    cond._localId = generateLocalId();
    currentConditions.push(cond);
    isDirty = true;
    notify();
  };

  const updateCondition = (localId, updated) => {
    const idx = currentConditions.findIndex((c) => c._localId === localId);
    if (idx !== -1) {
      currentConditions[idx] = { ...currentConditions[idx], ...updated };
      isDirty = true;
      notify();
    }
  };

  const deleteCondition = (localId) => {
    currentConditions = currentConditions.filter((c) => c._localId !== localId);
    isDirty = true;
    notify();
  };

  // --- Actions CRUD ---

  const addAction = (act) => {
    act._localId = generateLocalId();
    currentActions.push(act);
    isDirty = true;
    notify();
  };

  const updateAction = (localId, updated) => {
    const idx = currentActions.findIndex((a) => a._localId === localId);
    if (idx !== -1) {
      currentActions[idx] = { ...currentActions[idx], ...updated };
      isDirty = true;
      notify();
    }
  };

  const deleteAction = (localId) => {
    currentActions = currentActions.filter((a) => a._localId !== localId);
    isDirty = true;
    notify();
  };

  // --- Payload builders ---

  const buildConditionsPayload = (configId) => {
    return currentConditions.map((c, i) => ({
      id: c.id != null ? c.id : undefined,
      ownerCategory: 'ROOM_EVENT',
      ownerId: String(configId),
      sourceCategory: c.sourceCategory,
      sourceTargetId: String(c.sourceTargetId != null ? c.sourceTargetId : ''),
      sourceTargetType: c.sourceTargetType || null,
      property: c.property,
      operator: c.operator,
      value: String(c.value),
      extraParams: c.extraParams || null,
      sortOrder: i,
      nextLogic: i < currentConditions.length - 1 ? (c.nextLogic || 'AND') : null,
    }));
  };

  const buildActionsPayload = (configId) => {
    return currentActions.map((a, i) => ({
      id: a.id != null ? a.id : undefined,
      ownerCategory: 'ROOM_EVENT',
      ownerId: String(configId),
      targetCategory: a.targetCategory || a.targetDeviceCategory,
      targetId: String(a.targetId != null ? a.targetId : a.targetDeviceId),
      params: typeof a.params === 'string' ? JSON.parse(a.params) : (a.params || {}),
      executionOrder: i,
    }));
  };

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
