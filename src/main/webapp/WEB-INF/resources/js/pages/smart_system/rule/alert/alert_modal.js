import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { getAllGroups } from '../../../../api/group.api.js';
import { Toast } from '../../../../common/notification_util.js';

const { i18n } = window.__ALERT_PAGE_CONFIG__;

export const AlertConfigModal = (() => {
  let bootstrapModal = null;

  const el = {};

  const init = () => {
    el.modal = document.getElementById('alertModal');
    el.form = document.getElementById('alertForm');
    el.title = document.getElementById('alertModalTitle');
    el.localId = document.getElementById('alertLocalId');
    el.id = document.getElementById('alertConfigId');

    el.alertName = document.getElementById('alertName');
    el.severity = document.getElementById('alertSeverity');
    el.cooldownMinutes = document.getElementById('alertCooldownMinutes');
    el.recipientGroupsContainer = document.getElementById('alertRecipientGroupsContainer');
    el.autoResolve = document.getElementById('alertAutoResolve');
    el.messageTemplate = document.getElementById('alertMessageTemplate');

    if (el.modal) {
      bootstrapModal = typeof bootstrap !== 'undefined' ? new bootstrap.Modal(el.modal) : null;
    }
  };

  const loadRecipientGroups = async (selectedGroups = []) => {
    const container = el.recipientGroupsContainer;
    if (!container) return;
    container.replaceChildren();

    const [err, res] = await getAllGroups();
    if (err) {
      const errorMsg = document.createElement('p');
      errorMsg.className = 'text-danger small mb-0';
      errorMsg.textContent = i18n.errLoadGroups || 'Failed to load recipient groups.';
      container.appendChild(errorMsg);
      return;
    }

    const groups = res.data || [];
    if (groups.length === 0) {
      const emptyMsg = document.createElement('p');
      emptyMsg.className = 'text-muted small mb-0';
      emptyMsg.textContent = i18n.noGroups || 'No groups available.';
      container.appendChild(emptyMsg);
      return;
    }

    groups.forEach((group) => {
      const wrapper = document.createElement('div');
      wrapper.className = 'form-check mb-1';

      const input = document.createElement('input');
      input.className = 'form-check-input';
      input.type = 'checkbox';
      input.name = 'recipientGroups';
      input.value = group.groupCode;
      input.id = `group_${group.id}`;
      if (selectedGroups.includes(group.groupCode)) {
        input.checked = true;
      }

      const label = document.createElement('label');
      label.className = 'form-check-label small text-dark';
      label.setAttribute('for', `group_${group.id}`);
      label.textContent = `${group.name} (${group.groupCode})`;

      wrapper.appendChild(input);
      wrapper.appendChild(label);
      container.appendChild(wrapper);
    });
  };

  const clearValidation = () => {
    const inputs = el.form.querySelectorAll('.form-control, .form-select');
    inputs.forEach((el) => el.classList.remove('is-invalid'));
  };

  const open = async (localId = null) => {
    el.form.reset();
    el.localId.value = '';
    el.id.value = '';
    clearValidation();

    let selectedGroups = [];
    let selectedChannels = ['PUSH', 'EMAIL'];

    if (localId) {
      const data = StateManager.getAlert(localId);
      if (data) {
        el.title.textContent = i18n.editTitle || 'Edit Alert Configuration';
        el.localId.value = data._localId;
        el.id.value = data.id || '';

        el.alertName.value = data.alertName || '';
        el.severity.value = data.severity || 'INFO';
        el.cooldownMinutes.value = data.cooldownMinutes !== undefined ? data.cooldownMinutes : 10;
        el.autoResolve.checked = !!data.autoResolve;
        el.messageTemplate.value = data.messageTemplate || '';

        selectedGroups = data.recipientGroups || [];
        selectedChannels = data.channels || [];
      }
    } else {
      el.title.textContent = i18n.addTitle || 'Add Alert Configuration';
      el.alertName.value = '';
      el.severity.value = 'WARNING';
      el.cooldownMinutes.value = 10;
      el.autoResolve.checked = true;
      el.messageTemplate.value = '';
    }

    await loadRecipientGroups(selectedGroups);

    const channelCheckboxes = el.form.querySelectorAll('input[name="channels"]');
    channelCheckboxes.forEach((checkbox) => {
      checkbox.checked = selectedChannels.includes(checkbox.value);
    });

    bootstrapModal?.show();
    window.renderIcons?.();
  };

  const submit = async (e) => {
    e.preventDefault();
    clearValidation();

    const alertName = el.alertName.value.trim();
    const severity = el.severity.value;
    const cooldownMinutes = el.cooldownMinutes.value;
    const messageTemplate = el.messageTemplate.value.trim();
    const autoResolve = !!el.autoResolve.checked;

    const recipientGroups = [];
    const groupCheckboxes = el.recipientGroupsContainer.querySelectorAll('input[name="recipientGroups"]:checked');
    groupCheckboxes.forEach((cb) => {
      recipientGroups.push(cb.value);
    });

    const channels = [];
    const channelCheckboxes = el.form.querySelectorAll('input[name="channels"]:checked');
    channelCheckboxes.forEach((cb) => {
      channels.push(cb.value);
    });

    let isValid = true;
    if (!alertName) {
      el.alertName.classList.add('is-invalid');
      isValid = false;
    }

    if (!cooldownMinutes || isNaN(cooldownMinutes) || parseInt(cooldownMinutes, 10) < 0) {
      el.cooldownMinutes.classList.add('is-invalid');
      isValid = false;
    }

    if (!messageTemplate) {
      el.messageTemplate.classList.add('is-invalid');
      isValid = false;
    }

    if (recipientGroups.length === 0) {
      Toast.warning(i18n.valSelectGroup || 'Please select at least one recipient group.');
      isValid = false;
    }

    if (channels.length === 0) {
      Toast.warning(i18n.valSelectChannel || 'Please select at least one notification channel.');
      isValid = false;
    }

    if (!isValid) return;

    const data = {
      id: el.id.value ? Number(el.id.value) : null,
      alertName,
      severity,
      recipientGroups,
      channels,
      messageTemplate,
      cooldownMinutes: parseInt(cooldownMinutes, 10),
      autoResolve,
    };

    const localId = el.localId.value;
    if (localId) {
      StateManager.updateAlert(localId, data);
    } else {
      StateManager.addAlert(data);
    }

    UiRenderer.render();
    bootstrapModal?.hide();
  };

  return { init, open, submit };
})();
