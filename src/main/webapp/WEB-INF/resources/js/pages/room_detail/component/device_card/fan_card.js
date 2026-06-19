import { StateManager } from '../../state_manager.js';

export const FanCard = {
  render(device, isActive, renderCommonCard) {
    const controlPane = this.renderControlPane(device, isActive);
    return renderCommonCard(device, isActive, controlPane);
  },

  renderControlPane(fan, isActive) {
    const i18n = StateManager.getI18n();
    const hasSpeed = true;
    const hasSwing = fan.specificType === 'IRSEND' || fan.specificType === 'IR_CTL';
    const hasMode = fan.specificType === 'IRSEND' || fan.specificType === 'IR_CTL';

    let speedHtml = '';
    if (hasSpeed) {
      speedHtml = `
          <div class="mb-3">
              <div class="d-flex justify-content-between align-items-center mb-2">
                  <small class="fw-bold text-muted tiny">${i18n.fanSpeed}</small>
                  <span class="badge bg-warning text-dark rounded-pill tiny">${fan.speed || 1}</span>
              </div>
              <input type="range" class="form-range fan-speed-range" min="1" max="3" value="${fan.speed || 1}" ${!isActive ? 'disabled' : ''}>
          </div>
      `;
    }

    let swingHtml = '';
    if (hasSwing) {
      const borderClass = hasSpeed ? 'pt-2 border-top' : '';
      swingHtml = `
          <div class="d-flex justify-content-between align-items-center mb-2 ${borderClass}">
              <small class="fw-bold text-muted tiny">${i18n.swing}</small>
              <div class="form-check form-switch"><input class="form-check-input fan-swing-switch" type="checkbox" ${fan.swing === 'ON' ? 'checked' : ''} ${!isActive ? 'disabled' : ''}></div>
          </div>
      `;
    }

    let modesHtml = '';
    if (hasMode) {
      const modes = ['NORMAL', 'SLEEP', 'NATURAL'];
      const icons = { NORMAL: 'wind', SLEEP: 'moon', NATURAL: 'leaf' };
      modesHtml = `
        <div class="mb-4 pt-3 border-top border-white text-center">
            <div class="d-flex justify-content-center flex-wrap gap-2">
                ${modes
                  .map(
                    (m) => `
                    <button class="btn btn-sm rounded-pill fan-mode-btn shadow-none ${fan.mode === m ? 'btn-primary' : 'btn-outline-secondary'}" data-mode="${m}" ${!isActive ? 'disabled' : ''}>
                        <i data-lucide="${icons[m] || 'circle'}" class="me-1" style="width:14px"></i>${m}
                    </button>
                `,
                  )
                  .join('')}
            </div>
        </div>
      `;
    }

    if (!speedHtml && !swingHtml && !modesHtml) {
      return '';
    }

    return `
      ${modesHtml}
      <div class="bg-white rounded-3 p-3 border border-light">
          ${speedHtml}
          ${swingHtml}
      </div>
    `;
  },

  updateStatus(card, device, isActive, i18n, commonUpdate) {
    commonUpdate(card, isActive, i18n);

    const hasSpeed = true;
    const hasSwing = device.specificType === 'IRSEND' || device.specificType === 'IR_CTL';
    const hasMode = device.specificType === 'IRSEND' || device.specificType === 'IR_CTL';

    if (hasMode) {
      const modeBtns = card.querySelectorAll('.fan-mode-btn');
      modeBtns.forEach((btn) => {
        const isCurrentMode = btn.dataset.mode === device.mode;
        btn.className = `btn btn-sm rounded-pill fan-mode-btn shadow-none ${isCurrentMode ? 'btn-primary' : 'btn-outline-secondary'}`;
        btn.disabled = !isActive;
      });
    }

    if (hasSpeed) {
      const speedBadge = card.querySelector('.fan-speed-range')?.previousElementSibling?.querySelector('.badge');
      if (speedBadge) speedBadge.textContent = device.speed || 1;
    }

    if (hasSwing) {
      const swingSwitch = card.querySelector('.fan-swing-switch');
      if (swingSwitch) swingSwitch.checked = device.swing === 'ON';
    }
  },
};
