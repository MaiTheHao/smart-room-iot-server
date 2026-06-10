import { StateManager } from './state_manager.js';
import { RoomTempChart } from './component/room_temp_chart/room_temp_chart.js';
import { RoomPowerChart } from './component/room_power_chart/room_power_chart.js';
import { DeviceList } from './component/device_list/device_list.js';
import { DeviceController } from './device_controller.js';

const RoomDetailPage = {
  async init() {
    const config = window.__ROOM_CONFIG__;
    if (!config) return;

    StateManager.init(config);

    RoomTempChart.init();
    RoomPowerChart.init();

    DeviceList.init();
    DeviceController.bindEvents();

    this.bindGlobalPicker();

    const now = new Date();
    const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000);
    RoomTempChart.update(yesterday.toISOString(), now.toISOString());
    RoomPowerChart.update(yesterday.toISOString(), now.toISOString());

    await DeviceController.syncDevices();
    DeviceController.startPolling();
  },

  bindGlobalPicker() {
    if (window.flatpickr) {
      flatpickr('#analyticsRange', {
        mode: 'range',
        enableTime: true,
        time_24hr: true,
        altInput: true,
        altFormat: 'd/m/Y H:i',
        dateFormat: 'Z',
        defaultDate: [new Date(Date.now() - 24 * 60 * 60 * 1000), new Date()],
        onClose: (dates) => {
          if (dates.length === 2) {
            RoomTempChart.update(dates[0].toISOString(), dates[1].toISOString());
            RoomPowerChart.update(dates[0].toISOString(), dates[1].toISOString());
          }
        },
      });
    }
  },
};

document.addEventListener('DOMContentLoaded', () => RoomDetailPage.init());
