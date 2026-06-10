import { StateManager } from '../../state_manager.js';

export const AcCard = {
  render(device, isActive, renderCommonCard) {
    const controlPane = this.renderControlPane(device, isActive);
    return renderCommonCard(device, isActive, controlPane);
  },

  renderControlPane(ac, isActive) {
    const modes = ['COOL', 'HEAT', 'DRY', 'FAN', 'AUTO'];
    const i18n = StateManager.getI18n();
    return `
      <div class="text-center mb-4">
          <div class="d-flex justify-content-center align-items-center">
              <button class="btn btn-outline-secondary rounded-circle ac-temp-btn shadow-none" data-delta="-1" ${!isActive ? 'disabled' : ''}><i data-lucide="minus"></i></button>
              <div class="mx-4"><span class="fw-bold h2 m-0 ac-temp-value">${ac.temperature || 25}</span><span class="ms-1 text-muted">°C</span></div>
              <button class="btn btn-outline-secondary rounded-circle ac-temp-btn shadow-none" data-delta="1" ${!isActive ? 'disabled' : ''}><i data-lucide="plus"></i></button>
          </div>
      </div>
      <div class="mb-4 pt-3 border-top border-white text-center">
          <div class="d-flex justify-content-center flex-wrap gap-2">
              ${modes
                .map(
                  (m) => `
                  <button class="btn btn-sm rounded-pill ac-mode-btn shadow-none ${ac.mode === m ? 'btn-primary' : 'btn-outline-secondary'}" data-mode="${m}" ${!isActive ? 'disabled' : ''}>
                      <i data-lucide="${this.getAcIcon(m)}" class="me-1" style="width:14px"></i>${m}
                  </button>
              `,
                )
                .join('')}
          </div>
      </div>
      <div class="bg-white rounded-3 p-3 border border-light">
          <div class="d-flex justify-content-between align-items-center mb-2">
              <small class="fw-bold text-muted tiny">${i18n.fanSpeed}</small>
              <span class="badge bg-primary rounded-pill tiny">${ac.fanSpeed || 0}</span>
          </div>
          <input type="range" class="form-range ac-fanspeed-range" min="0" max="5" value="${ac.fanSpeed || 0}" ${!isActive ? 'disabled' : ''}>
          <div class="d-flex justify-content-between align-items-center pt-2 mt-2 border-top">
              <small class="fw-bold text-muted tiny">${i18n.swing}</small>
              <div class="form-check form-switch"><input class="form-check-input ac-swing-switch" type="checkbox" ${ac.swing === 'ON' ? 'checked' : ''} ${!isActive ? 'disabled' : ''}></div>
          </div>
      </div>
    `;
  },

  updateStatus(card, device, isActive, i18n, commonUpdate) {
    commonUpdate(card, isActive, i18n);

    const tempVal = card.querySelector('.ac-temp-value');
    if (tempVal) tempVal.textContent = device.temperature || 25;

    const modeBtns = card.querySelectorAll('.ac-mode-btn');
    modeBtns.forEach((btn) => {
      const isCurrentMode = btn.dataset.mode === device.mode;
      btn.className = `btn btn-sm rounded-pill ac-mode-btn shadow-none ${isCurrentMode ? 'btn-primary' : 'btn-outline-secondary'}`;
      btn.disabled = !isActive;
    });

    const fanBadge = card
      .querySelector('.ac-fanspeed-range')
      ?.previousElementSibling?.querySelector('.badge');
    if (fanBadge) fanBadge.textContent = device.fanSpeed || 0;

    const swingSwitch = card.querySelector('.ac-swing-switch');
    if (swingSwitch) swingSwitch.checked = device.swing === 'ON';
  },

  getAcIcon(mode) {
    const map = { COOL: 'snowflake', HEAT: 'flame', DRY: 'droplets', FAN: 'fan', AUTO: 'sparkles' };
    return map[mode] || 'circle';
  },
};
