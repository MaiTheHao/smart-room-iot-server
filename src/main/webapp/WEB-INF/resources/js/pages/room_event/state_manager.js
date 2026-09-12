/**
 * State Manager cho Quản lý Danh sách Sự kiện Phòng (Room Event List Page)
 */

import {
  RoomEventCodeDto,
  RoomEventConfigDto,
} from '../../types/room_event.domain.js';

export const StateManager = (() => {
  let roomId = null;
  let availableCodes = [];
  let configs = [];

  const listeners = [];

  const init = (pRoomId, pAvailableCodes = [], pConfigs = []) => {
    roomId = pRoomId;
    availableCodes = (pAvailableCodes || []).map((c) => RoomEventCodeDto.fromApi(c));
    configs = (pConfigs || []).map((c) => RoomEventConfigDto.fromApi(c));
    notify();
  };

  const getRoomId = () => roomId;
  const getConfigs = () => [...configs];
  const getConfig = (id) => configs.find((c) => c.id === Number(id));
  const getAvailableCodes = () => [...availableCodes];

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

  const subscribe = (fn) => {
    listeners.push(fn);
    return () => {
      const idx = listeners.indexOf(fn);
      if (idx > -1) listeners.splice(idx, 1);
    };
  };

  const notify = () => {
    listeners.forEach((fn) => fn(configs));
  };

  return {
    init,
    getRoomId,
    getConfigs,
    getConfig,
    getAvailableCodes,
    getUnconfiguredCodes,
    addConfig,
    updateConfig,
    removeConfig,
    subscribe,
  };
})();
