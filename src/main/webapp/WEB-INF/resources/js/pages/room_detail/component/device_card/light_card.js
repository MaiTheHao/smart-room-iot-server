import { StateManager } from '../../state_manager.js';

export const LightCard = {
  render(device, isActive, renderCommonCard) {
    const controlPane = this.renderControlPane(device, isActive);
    return renderCommonCard(device, isActive, controlPane);
  },

  renderControlPane(light, isActive) {
    if (
      light.specificType === 'GPIO' ||
      light.specificType === 'IRSEND' ||
      light.specificType === 'IR_CTL'
    ) {
      return '';
    }

    const i18n = StateManager.getI18n();
    return `
      <div class="bg-white rounded-3 p-3 border border-light">
          <div class="d-flex justify-content-between align-items-center mb-2">
              <small class="fw-bold text-muted tiny">${i18n.brightness}</small>
              <span class="badge bg-success rounded-pill tiny">${light.level || 0}%</span>
          </div>
          <input type="range" class="form-range light-level-range" min="0" max="100" value="${light.level || 0}" ${!isActive ? 'disabled' : ''}>
      </div>
    `;
  },

  updateStatus(card, device, isActive, i18n, commonUpdate) {
    commonUpdate(card, isActive, i18n);

    if (
      device.specificType === 'GPIO' ||
      device.specificType === 'IRSEND' ||
      device.specificType === 'IR_CTL'
    ) {
      return;
    }

    const levelBadge = card
      .querySelector('.light-level-range')
      ?.previousElementSibling?.querySelector('.badge');
    if (levelBadge) levelBadge.textContent = `${device.level || 0}%`;
  },
};
