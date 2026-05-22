import { StateManager } from './state_manager.js';
import { RoomAnalytics, DeviceRenderer } from './ui_renderer.js';
import { DeviceController } from './device_controller.js';

const RoomDetailPage = {
  async init() {
    const config = window.__ROOM_CONFIG__;
    if (!config) return;

    StateManager.init(config);

    RoomAnalytics.init();
    DeviceRenderer.init();
    DeviceController.bindEvents();

    this.bindGlobalPicker();

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
            RoomAnalytics.updateMainCharts(dates[0].toISOString(), dates[1].toISOString());
          }
        },
      });
    }
  },
};

document.addEventListener('DOMContentLoaded', () => RoomDetailPage.init());
