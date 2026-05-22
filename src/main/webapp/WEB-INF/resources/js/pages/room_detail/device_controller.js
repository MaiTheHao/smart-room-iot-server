import { getDevicesByRoom, controlAc, controlFan, controlLight } from '../../api/device.api.js';
import { StateManager } from './state_manager.js';
import { DeviceRenderer, DeviceChart } from './ui_renderer.js';

export const DeviceController = {
  bindEvents() {
    const container = document.querySelector('#controls-container');
    if (!container) return;

    container.addEventListener('change', (e) => this.handleChange(e));
    container.addEventListener('click', (e) => this.handleClick(e));

    container.addEventListener('shown.bs.tab', (e) => {
      const tabBtn = e.target.closest('.device-analytics-tab');
      if (tabBtn) {
        const item = tabBtn.closest('.device-item');
        if (item) DeviceChart.init(item.dataset.naturalId);
      }
    });

    container.addEventListener(
      'mousedown',
      (e) => e.target.type === 'range' && StateManager.setIsInteracting(true),
    );
    container.addEventListener(
      'mouseup',
      (e) => e.target.type === 'range' && StateManager.setIsInteracting(false),
    );
    container.addEventListener(
      'touchstart',
      (e) => e.target.type === 'range' && StateManager.setIsInteracting(true),
    );
    container.addEventListener(
      'touchend',
      (e) => e.target.type === 'range' && StateManager.setIsInteracting(false),
    );
  },

  async syncDevices() {
    const roomId = StateManager.getRoomId();
    if (!roomId) return;

    const [err, res] = await getDevicesByRoom(roomId);
    if (err || !res.data || StateManager.getIsInteracting()) return;

    const newDevices = res.data;
    DeviceRenderer.renderOrUpdateAll(newDevices);
    StateManager.setDevices(newDevices);
  },

  startPolling() {
    this.stopPolling();
    const interval = setInterval(() => {
      if (!StateManager.getIsInteracting()) {
        this.syncDevices();
      }
    }, 5000);
    StateManager.setPollingInterval(interval);
  },

  stopPolling() {
    const interval = StateManager.getPollingInterval();
    if (interval) {
      clearInterval(interval);
      StateManager.setPollingInterval(null);
    }
  },

  async handleChange(e) {
    const target = e.target;
    const item = target.closest('.device-item');
    if (!item) return;

    const { naturalId, category } = item.dataset;
    let payload = {};

    if (target.classList.contains('master-switch')) {
      payload.power = target.checked ? 'ON' : 'OFF';
    } else if (target.classList.contains('ac-fanspeed-range')) {
      payload.fanSpeed = parseInt(target.value);
    } else if (target.classList.contains('ac-swing-switch')) {
      payload.swing = target.checked ? 'ON' : 'OFF';
    } else if (target.classList.contains('fan-speed-range')) {
      payload.speed = parseInt(target.value);
    } else if (target.classList.contains('fan-swing-switch')) {
      payload.swing = target.checked ? 'ON' : 'OFF';
    } else if (target.classList.contains('fan-light-switch')) {
      payload.light = target.checked ? 'ON' : 'OFF';
    } else if (target.classList.contains('light-level-range')) {
      payload.level = parseInt(target.value);
    }

    if (Object.keys(payload).length > 0) {
      await this.handleApiCall(category, naturalId, payload);
    }
  },

  async handleClick(e) {
    const target = e.target;

    const acBtn = target.closest('.ac-temp-btn, .ac-mode-btn');
    if (acBtn) {
      const item = acBtn.closest('.device-item');
      const { naturalId } = item.dataset;
      if (acBtn.classList.contains('ac-temp-btn')) {
        const valEl = item.querySelector('.ac-temp-value');
        const newVal = parseInt(valEl.textContent) + parseInt(acBtn.dataset.delta);
        if (newVal >= 16 && newVal <= 32) {
          await this.handleApiCall('AC', naturalId, { temperature: newVal });
        }
      } else {
        await this.handleApiCall('AC', naturalId, { mode: acBtn.dataset.mode });
      }
      return;
    }

    const typeBtn = target.closest('.chart-type-group button');
    if (typeBtn) {
      const item = typeBtn.closest('.device-item');
      typeBtn.parentElement.querySelectorAll('button').forEach((b) => b.classList.remove('active'));
      typeBtn.classList.add('active');
      DeviceChart.switchType(item.dataset.naturalId, typeBtn.dataset.type);
    }
  },

  async handleApiCall(category, naturalId, payload) {
    StateManager.setIsInteracting(true);
    let err, res;
    const type = category === 'AIR_CONDITION' ? 'AC' : category;

    if (type === 'AC') [err, res] = await controlAc(naturalId, payload);
    else if (type === 'FAN') [err, res] = await controlFan(naturalId, payload);
    else if (type === 'LIGHT') [err, res] = await controlLight(naturalId, payload);

    StateManager.setIsInteracting(false);
    const i18n = StateManager.getI18n();

    if (err) {
      window.Swal?.fire({
        title: i18n.errorTitle || 'Error',
        text: i18n.errorControl || 'Failed to control device',
        icon: 'error',
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 3000,
      });
    } else if (res?.data) {
      const controlData = res.data?.data ?? res.data;
      const successCount = Number(controlData?.successCount ?? 0);
      const totalCount = Number(controlData?.totalCount ?? 0);
      const deviceName =
        document.querySelector(`.device-item[data-natural-id="${naturalId}"] h6`)?.textContent ||
        'Device';

      let icon = 'success';
      let title = '';
      let msg = '';

      if (successCount === 0) {
        icon = 'error';
        title = i18n.errorTitle || 'Error';
        msg = i18n.errorControl || 'Failed to control device';
      } else if (successCount >= totalCount && totalCount > 0) {
        icon = 'success';
        msg = i18n.successControl || 'Device updated successfully';
      } else {
        icon = 'warning';
        msg = (i18n.controlStatus || '{0}: {1}/{2} successful')
          .replace('{0}', deviceName)
          .replace('{1}', successCount)
          .replace('{2}', totalCount);
      }

      window.Swal?.fire({
        title: title || undefined,
        text: msg,
        icon,
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 3000,
      });
    }
    await this.syncDevices();
  },
};
