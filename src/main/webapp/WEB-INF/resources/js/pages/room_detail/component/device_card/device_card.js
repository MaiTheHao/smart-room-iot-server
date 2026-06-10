import { StateManager } from '../../state_manager.js';
import { AcCard } from './ac_card.js';
import { FanCard } from './fan_card.js';
import { LightCard } from './light_card.js';

export const DeviceCard = {
  render(device) {
    const isActive = device.power === 'ON';

    if (device.category === 'AIR_CONDITION') {
      return AcCard.render(device, isActive, this.renderCommonCard.bind(this));
    }
    if (device.category === 'FAN') {
      return FanCard.render(device, isActive, this.renderCommonCard.bind(this));
    }
    if (device.category === 'LIGHT') {
      return LightCard.render(device, isActive, this.renderCommonCard.bind(this));
    }
    return '';
  },

  update(device) {
    const card = document.querySelector(`.device-item[data-natural-id="${device.naturalId}"]`);
    if (!card) return;

    const isActive = device.power === 'ON';
    const i18n = StateManager.getI18n();

    if (device.category === 'AIR_CONDITION') {
      AcCard.updateStatus(card, device, isActive, i18n, this.commonUpdateStatus.bind(this, device));
    } else if (device.category === 'FAN') {
      FanCard.updateStatus(card, device, isActive, i18n, this.commonUpdateStatus.bind(this, device));
    } else if (device.category === 'LIGHT') {
      LightCard.updateStatus(card, device, isActive, i18n, this.commonUpdateStatus.bind(this, device));
    }
  },

  renderCommonCard(device, isActive, controlPaneHtml) {
    const i18n = StateManager.getI18n();
    console.log(device);

    return `
      <div class="px-2 py-3 device-item" data-id="${device.id}" data-natural-id="${device.naturalId}" data-category="${device.category}">
          <div class="d-flex justify-content-between align-items-center">
              <div class="d-flex align-items-center flex-grow-1 overflow-hidden">
                  <div class="overflow-hidden">
                      <div class="d-flex align-items-center flex-wrap gap-2 mb-1">
                          <h6 class="mb-0 fw-bold text-dark small text-truncate">${device.name}</h6>
                          <span class="badge rounded-pill bg-secondary bg-opacity-10 text-secondary border border-secondary border-opacity-25 fw-medium" style="font-size: 0.65rem; padding: 0.15rem 0.4rem">
                              <i data-lucide="fingerprint" style="width: 10px; height: 10px" class="me-1"></i>${device.naturalId}
                          </span>
                          <span class="text-muted fw-normal" style="font-size: 0.65rem">#${device.id}</span>
                      </div>
                      <small class="text-muted tiny text-truncate d-block">${device.description || '--'}</small>
                  </div>
              </div>
              <div class="d-flex align-items-center gap-3 ms-3">
                  <div class="position-relative">
                      <input type="checkbox" class="btn-check master-switch" id="switch${device.naturalId}" ${isActive ? 'checked' : ''} autocomplete="off">
                      <label class="btn ${isActive ? 'btn-success border-success' : 'btn-outline-secondary'} rounded-pill px-3 py-1 d-flex align-items-center gap-2 shadow-sm" for="switch${device.naturalId}" style="font-size: 0.75rem; border-width: 1px">
                          <i data-lucide="power" style="width: 14px; height: 14px"></i>
                          <span class="fw-bold">${isActive ? i18n.on : i18n.off}</span>
                      </label>
                  </div>
                  <button class="btn btn-link text-muted p-0 border-0" data-bs-toggle="collapse" data-bs-target="#deviceContent${device.naturalId}">
                      <i data-lucide="chevron-down" style="width: 18px"></i>
                  </button>
              </div>
          </div>
          <div class="collapse mt-3" id="deviceContent${device.naturalId}">
              <div class="card border-0 bg-light rounded-2 overflow-hidden">
                  <div class="card-header bg-transparent border-0 p-2">
                      <ul class="nav nav-pills nav-fill gap-2 p-1 bg-white bg-opacity-50 rounded-4" role="tablist">
                          <li class="nav-item">
                               <button class="nav-link active py-1 tiny rounded-4 shadow-none" data-bs-toggle="tab" data-bs-target="#control${device.naturalId}">
                                  <i data-lucide="sliders" class="me-1" style="width:12px"></i>Control
                              </button>
                          </li>
                          <li class="nav-item">
                              <button class="nav-link py-1 tiny rounded-4 device-analytics-tab shadow-none" data-bs-toggle="tab" data-bs-target="#analytics${device.naturalId}">
                                  <i data-lucide="bar-chart-2" class="me-1" style="width:12px"></i>Analytics
                              </button>
                          </li>
                      </ul>
                  </div>
                  <div class="tab-content">
                      <div class="tab-pane fade show active p-3" id="control${device.naturalId}">
                          ${controlPaneHtml}
                      </div>
                      <div class="tab-pane fade p-3" id="analytics${device.naturalId}">
                          ${this.renderAnalyticsPane(device)}
                      </div>
                  </div>
              </div>
          </div>
      </div>
    `;
  },

  renderAnalyticsPane(device) {
    const i18n = StateManager.getI18n();
    return `
      <div class="device-chart-container" data-id="${device.id}" data-natural-id="${device.naturalId}" data-category="${device.category}">
          <div class="d-flex justify-content-between align-items-center mb-3">
              <div class="btn-group btn-group-sm chart-type-group shadow-none" role="group">
                  <button type="button" class="btn btn-outline-primary active tiny" data-type="power" title="${i18n.metricPower}">W</button>
                  <button type="button" class="btn btn-outline-primary tiny" data-type="voltage" title="${i18n.metricVoltage}">V</button>
                  <button type="button" class="btn btn-outline-primary tiny" data-type="current" title="${i18n.metricCurrent}">A</button>
                  <button type="button" class="btn btn-outline-primary tiny" data-type="energy" title="${i18n.metricEnergy}">kWh</button>
              </div>
              <div class="input-group input-group-sm shadow-none" style="width: 280px">
                  <input type="text" class="form-control border-0 bg-white device-chart-range tiny" placeholder="Range" readonly>
              </div>
          </div>
          <div class="device-chart-el" style="min-height: 200px"></div>
      </div>
    `;
  },

  commonUpdateStatus(device, card, isActive, i18n) {
    const masterSwitch = card.querySelector('.master-switch');
    if (masterSwitch && masterSwitch.checked !== isActive) {
      masterSwitch.checked = isActive;
    }

    const label = card.querySelector(`label[for="switch${device.naturalId}"]`);
    const statusText = label?.querySelector('span');

    if (label) {
      label.className = `btn ${isActive ? 'btn-success border-success' : 'btn-outline-secondary'} rounded-pill px-3 py-1 d-flex align-items-center gap-2 shadow-sm`;
      if (statusText) statusText.textContent = isActive ? i18n.on : i18n.off;
    }

    const inputs = card.querySelectorAll('input:not(.master-switch):not(.device-chart-range), button:not([data-bs-toggle]):not(.chart-type-group button)');
    inputs.forEach((input) => (input.disabled = !isActive));
  },

  getCategoryIcon(cat) {
    if (cat === 'AIR_CONDITION') return 'air-vent';
    if (cat === 'FAN') return 'fan';
    return 'lightbulb';
  },

  getCategoryColor(cat, active) {
    if (!active) return 'bg-light text-muted';
    if (cat === 'AIR_CONDITION') return 'bg-primary bg-opacity-10 text-primary';
    if (cat === 'FAN') return 'bg-warning bg-opacity-10 text-warning';
    return 'bg-success bg-opacity-10 text-success';
  },
};
