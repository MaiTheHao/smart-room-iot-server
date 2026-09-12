/**
 * State Manager cho Quản lý Sự kiện Phòng (Room Event)
 * Tuân thủ Clean Code: CQS, Single Responsibility, Guard Clauses
 */

import {
  mapConditionsForReplace,
  mapActionsForReplace,
} from '../../common/smart_system_util.js';
import {
  RoomEventCodeDto,
  RoomEventConfigDto,
} from '../../types/room_event.domain.js';

export const StateManager = (() => {
  let roomId = null;
  let availableCodes = [];
  let configs = [];

  let activeConfigId = null;
  let activeEventCode = null;
  let currentConditions = [];
  let currentActions = [];

  const listeners = [];
  const generateLocalId = () => `local_${Math.random().toString(36).substring(2, 11)}`;

  const init = (pRoomId, pAvailableCodes = [], pConfigs = []) => {
    roomId = pRoomId;
    availableCodes = (pAvailableCodes || []).map((c) => RoomEventCodeDto.fromApi(c));
    configs = (pConfigs || []).map((c) => RoomEventConfigDto.fromApi(c));
    notify();
  };

  const getUnconfiguredCodes = () => {
    const configuredCodeSet = new Set(configs.map((c) => c.eventCode));
    return availableCodes.filter((ac) => !configuredCodeSet.has(ac.code));
  };

  const addConfig = (raw) => {
    const dto = RoomEventConfigDto.fromApi(raw);
    configs = [...configs, dto];
    notify();
    return dto;
  };

  const updateConfig = (raw) => {
    const dto = RoomEventConfigDto.fromApi(raw);
    configs = configs.map((c) => (c.id === dto.id ? dto : c));
    notify();
    return dto;
  };

  const removeConfig = (configId) => {
    configs = configs.filter((c) => c.id !== Number(configId));
    notify();
  };

  // Sub-resources for Active Config Modal
  const setActiveConfig = (configId, eventCode, conditions = [], actions = []) => {
    activeConfigId = configId;
    activeEventCode = eventCode;

    currentConditions = (conditions || []).map((c) => ({
      ...c,
      _localId: generateLocalId(),
    }));

    currentActions = (actions || []).map((a) => ({
      ...a,
      _localId: generateLocalId(),
      params: typeof (a.params || a.actionParams) === 'string'
        ? JSON.parse(a.params || a.actionParams)
        : (a.params || a.actionParams || {}),
    }));
  };

  const addCondition = (cond) => {
    const newCond = { ...cond, _localId: generateLocalId() };
    currentConditions = [...currentConditions, newCond];
    return newCond;
  };

  const updateCondition = (localId, updated) => {
    currentConditions = currentConditions.map((c) =>
      c._localId === localId ? { ...c, ...updated } : c
    );
  };

  const deleteCondition = (localId) => {
    currentConditions = currentConditions.filter((c) => c._localId !== localId);
  };

  const addAction = (act) => {
    const newAct = { ...act, _localId: generateLocalId() };
    currentActions = [...currentActions, newAct];
    return newAct;
  };

  const updateAction = (localId, updated) => {
    currentActions = currentActions.map((a) =>
      a._localId === localId ? { ...a, ...updated } : a
    );
  };

  const deleteAction = (localId) => {
    currentActions = currentActions.filter((a) => a._localId !== localId);
  };

  const buildConditionsPayload = () => mapConditionsForReplace(currentConditions);
  const buildActionsPayload = () => mapActionsForReplace(currentActions);

  const subscribe = (fn) => listeners.push(fn);
  const notify = () => listeners.forEach((fn) => fn({
    roomId,
    availableCodes,
    configs: [...configs],
  }));

  return {
    init,
    getRoomId: () => roomId,
    getConfigs: () => [...configs],
    getConfigById: (id) => configs.find((c) => c.id === Number(id)) || null,
    getAvailableCodes: () => [...availableCodes],
    getUnconfiguredCodes,
    addConfig,
    updateConfig,
    removeConfig,
    setActiveConfig,
    getActiveConfigId: () => activeConfigId,
    getActiveEventCode: () => activeEventCode,
    getConditions: () => [...currentConditions],
    addCondition,
    updateCondition,
    deleteCondition,
    getActions: () => [...currentActions],
    addAction,
    updateAction,
    deleteAction,
    buildConditionsPayload,
    buildActionsPayload,
    subscribe,
  };
})();
