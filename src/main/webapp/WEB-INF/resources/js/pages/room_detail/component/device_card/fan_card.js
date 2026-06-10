import { StateManager } from '../../state_manager.js';

export const FanCard = {
  render(device, isActive, renderCommonCard) {
    const controlPane = this.renderControlPane(device, isActive);
    return renderCommonCard(device, isActive, controlPane);
  },

  renderControlPane(fan, isActive) {
    const isIR = fan.type === 'IR';
    const i18n = StateManager.getI18n();
    return `
      <div class="bg-white rounded-3 p-3 border border-light">
          <div class="mb-3">
              <div class="d-flex justify-content-between align-items-center mb-2">
                  <small class="fw-bold text-muted tiny">${i18n.fanSpeed}</small>
                  <span class="badge bg-warning text-dark rounded-pill tiny">${fan.speed || 0}</span>
              </div>
              <input type="range" class="form-range fan-speed-range" min="0" max="3" value="${fan.speed || 0}" ${!isActive ? 'disabled' : ''}>
          </div>
          ${
            isIR
              ? `
              <div class="d-flex justify-content-between align-items-center mb-2 pt-2 border-top">
                  <small class="fw-bold text-muted tiny">${i18n.swing}</small>
                  <div class="form-check form-switch"><input class="form-check-input fan-swing-switch" type="checkbox" ${fan.swing === 'ON' ? 'checked' : ''} ${!isActive ? 'disabled' : ''}></div>
              </div>
              <div class="d-flex justify-content-between align-items-center">
                  <small class="fw-bold text-muted tiny">${i18n.light}</small>
                  <div class="form-check form-switch"><input class="form-check-input fan-light-switch" type="checkbox" ${fan.light === 'ON' ? 'checked' : ''} ${!isActive ? 'disabled' : ''}></div>
              </div>
          `
              : ''
          }
      </div>
    `;
  },

  updateStatus(card, device, isActive, i18n, commonUpdate) {
    commonUpdate(card, isActive, i18n);

    const speedBadge = card
      .querySelector('.fan-speed-range')
      ?.previousElementSibling?.querySelector('.badge');
    if (speedBadge) speedBadge.textContent = device.speed || 0;

    const swingSwitch = card.querySelector('.fan-swing-switch');
    if (swingSwitch) swingSwitch.checked = device.swing === 'ON';

    const lightSwitch = card.querySelector('.fan-light-switch');
    if (lightSwitch) lightSwitch.checked = device.light === 'ON';
  },
};
