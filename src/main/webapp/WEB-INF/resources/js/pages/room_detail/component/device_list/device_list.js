import { StateManager } from '../../state_manager.js';
import { DeviceCard } from '../device_card/device_card.js';
import { DeviceChart } from '../device_chart/device_chart.js';

export const DeviceList = {
  init() {
    const container = document.querySelector('#controls-container');
    if (!container) return;

    const i18n = StateManager.getI18n();

    container.innerHTML = `
      <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 mt-5">
          <h5 class="m-0 fw-bold text-dark d-flex align-items-center">
              <span class="d-inline-flex align-items-center justify-content-center bg-success bg-opacity-10 text-success rounded-2 me-2" style="width: 32px; height: 32px;">
                  <i data-lucide="gamepad-directional" style="width: 18px; height: 18px;"></i>
              </span>
              <span>${i18n.controls}</span>
          </h5>
      </div>
      
      <div class="card border-1 rounded-2 overflow-hidden shadow-sm mb-4">
          <div class="card-header bg-white border-0">
              <h6 class="m-0 text-dark d-flex align-items-center">
                  <i data-lucide="air-vent" class="text-primary me-2"></i>
                  <span>${i18n.ac}</span>
              </h6>
          </div>
          <div class="card-body border-top" id="ac-pane"></div>
      </div>

      <div class="card border-1 rounded-2 overflow-hidden shadow-sm mb-4">
          <div class="card-header bg-white border-0">
              <h6 class="m-0 text-dark d-flex align-items-center">
                  <i data-lucide="fan" class="text-warning me-2"></i>
                  <span>${i18n.fan}</span>
              </h6>
          </div>
          <div class="card-body border-top" id="fan-pane"></div>
      </div>

      <div class="card border-1 rounded-2 overflow-hidden shadow-sm mb-4">
          <div class="card-header bg-white border-0">
              <h6 class="m-0 text-dark d-flex align-items-center">
                  <i data-lucide="lightbulb" class="text-success me-2"></i>
                  <span>${i18n.light}</span>
              </h6>
          </div>
          <div class="card-body border-top" id="light-pane"></div>
      </div>
    `;

    if (window.lucide) lucide.createIcons();
  },

  renderOrUpdateAll(newDevices) {
    const i18n = StateManager.getI18n();
    const currentDevices = StateManager.getDevices();

    const groups = [
      { id: '#ac-pane', category: 'AIR_CONDITION', empty: i18n.noAc },
      { id: '#fan-pane', category: 'FAN', empty: i18n.noFan },
      { id: '#light-pane', category: 'LIGHT', empty: i18n.noLight },
    ];

    groups.forEach((g) => {
      const pane = document.querySelector(g.id);
      if (!pane) return;

      const newItems = newDevices.filter((d) => d.category === g.category);
      const currentItems = currentDevices.filter((d) => d.category === g.category);

      const newIds = newItems
        .map((d) => d.id)
        .sort()
        .join(',');
      const currentIds = currentItems
        .map((d) => d.id)
        .sort()
        .join(',');

      if (newIds !== currentIds || pane.innerHTML.includes('text-center py-4')) {
        this.renderGroupFull(pane, newItems, g.empty);
      } else {
        newItems.forEach((device) => DeviceCard.update(device));
      }
    });
  },

  renderGroupFull(pane, items, emptyMsg) {
    const openIds = Array.from(pane.querySelectorAll('.collapse.show')).map((c) => c.id);
    const activeTabIds = Array.from(pane.querySelectorAll('.nav-link.active')).map((t) => t.dataset.bsTarget);

    pane.querySelectorAll('.device-chart-container').forEach((c) => {
      const nid = c.dataset.naturalId;
      StateManager.deleteDeviceChart(nid);
    });

    pane.innerHTML = items.map((d) => DeviceCard.render(d)).join('') || `<p class="text-muted text-center py-4">${emptyMsg}</p>`;

    openIds.forEach((id) => pane.querySelector(`#${id}`)?.classList.add('show'));
    activeTabIds.forEach((tid) => {
      const tabBtn = pane.querySelector(`[data-bs-target="${tid}"]`);
      if (tabBtn) {
        const tabList = tabBtn.closest('ul');
        if (tabList) tabList.querySelectorAll('.nav-link').forEach((nl) => nl.classList.remove('active'));
        tabBtn.classList.add('active');
      }
      const tabPane = pane.querySelector(tid);
      if (tabPane) {
        const tabContent = tabPane.closest('.tab-content');
        if (tabContent) tabContent.querySelectorAll('.tab-pane').forEach((tp) => tp.classList.remove('show', 'active'));
        tabPane.classList.add('show', 'active');
      }
    });

    activeTabIds.forEach((tid) => {
      if (tid?.startsWith('#analytics')) {
        const aPane = pane.querySelector(tid);
        if (aPane) {
          const cc = aPane.querySelector('.device-chart-container');
          if (cc) DeviceChart.init(cc.dataset.naturalId);
        }
      }
    });

    if (window.lucide) lucide.createIcons();
  },
};
