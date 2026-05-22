export const StateManager = (() => {
  let config = null;
  let roomId = null;
  let i18n = {};
  let devices = [];
  const deviceCharts = {}; // { naturalId: { chart, currentType, category, targetId, data, range } }
  let isInteracting = false;
  let pollingInterval = null;

  const init = (cfg) => {
    config = cfg;
    roomId = cfg.roomId;
    i18n = cfg.i18n;
  };

  const getConfig = () => config;
  const getRoomId = () => roomId;
  const getI18n = () => i18n;
  
  const getDevices = () => devices;
  const setDevices = (newDevices) => {
    devices = newDevices;
  };

  const getDeviceCharts = () => deviceCharts;
  const getDeviceChart = (naturalId) => deviceCharts[naturalId];
  const setDeviceChart = (naturalId, chartData) => {
    deviceCharts[naturalId] = chartData;
  };
  const deleteDeviceChart = (naturalId) => {
    if (deviceCharts[naturalId]) {
      deviceCharts[naturalId].chart?.destroy();
      delete deviceCharts[naturalId];
    }
  };

  const getIsInteracting = () => isInteracting;
  const setIsInteracting = (val) => {
    isInteracting = val;
  };

  const getPollingInterval = () => pollingInterval;
  const setPollingInterval = (val) => {
    pollingInterval = val;
  };

  return {
    init,
    getConfig,
    getRoomId,
    getI18n,
    getDevices,
    setDevices,
    getDeviceCharts,
    getDeviceChart,
    setDeviceChart,
    deleteDeviceChart,
    getIsInteracting,
    setIsInteracting,
    getPollingInterval,
    setPollingInterval
  };
})();
