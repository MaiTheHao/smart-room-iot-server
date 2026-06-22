import { getAllGroups } from '../../../../api/group.api.js';
import { createConfig, updateConfig } from '../../../../api/alert.api.js';
import { Toast } from '../../../../common/notification_util.js';
import { getAllActiveRules } from '../../../../api/rule.api.js';

const { i18n } = window.__ALERT_MANAGE_CONFIG__;

export const AlertConfigModal = (() => {
  let bootstrapModal = null;
  let saveCallback = null;

  const el = {};

  const init = (onSave) => {
    saveCallback = onSave;
    el.modal = document.getElementById('alertConfigModal');
    el.form = document.getElementById('alertConfigForm');
    el.title = document.getElementById('alertConfigModalTitle');
    el.id = document.getElementById('editConfigId');

    el.alertName = document.getElementById('cfgAlertName');
    el.alertCode = document.getElementById('cfgAlertCode');
    el.namespace = document.getElementById('cfgNamespace');
    el.sourceId = document.getElementById('cfgSourceId');
    el.sourceIdSelect = document.getElementById('cfgSourceIdSelect');
    el.severity = document.getElementById('cfgSeverity');
    el.cooldownMinutes = document.getElementById('cfgCooldownMinutes');
    el.recipientGroupsContainer = document.getElementById('cfgRecipientGroupsContainer');
    el.autoResolve = document.getElementById('cfgAutoResolve');
    el.messageTemplate = document.getElementById('cfgMessageTemplate');

    if (el.namespace) {
      el.namespace.addEventListener('change', (e) => {
        toggleSourceIdInput(e.target.value);
      });
    }

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
    inputs.forEach((input) => input.classList.remove('is-invalid'));
    el.sourceIdSelect?.classList.remove('is-invalid');
  };

  const loadActiveRulesDropdown = async (selectedValue = '') => {
    const select = el.sourceIdSelect;
    if (!select) return;

    select.replaceChildren();
    const defaultOpt = document.createElement('option');
    defaultOpt.value = '';
    defaultOpt.disabled = true;
    defaultOpt.selected = !selectedValue;
    defaultOpt.textContent = 'Select Rule...';
    select.appendChild(defaultOpt);

    const [err, res] = await getAllActiveRules();
    if (err) {
      Toast.error('Failed to load active rules.');
      return;
    }

    const rules = res.data || [];
    rules.forEach((rule) => {
      const opt = document.createElement('option');
      opt.value = rule.id.toString();
      opt.textContent = `${rule.id} - ${rule.name}`;
      if (rule.id.toString() === selectedValue.toString()) {
        opt.selected = true;
      }
      select.appendChild(opt);
    });
  };

  const toggleSourceIdInput = async (namespace, sourceIdValue = '') => {
    if (namespace === 'RULE') {
      el.sourceId.classList.add('d-none');
      el.sourceId.disabled = true;
      el.sourceIdSelect.classList.remove('d-none');
      el.sourceIdSelect.disabled = false;
      await loadActiveRulesDropdown(sourceIdValue);
    } else {
      el.sourceIdSelect.classList.add('d-none');
      el.sourceIdSelect.disabled = true;
      el.sourceId.classList.remove('d-none');
      el.sourceId.disabled = false;
      el.sourceId.value = sourceIdValue;
    }
  };

  const open = async (id = null, data = null) => {
    el.form.reset();
    el.id.value = '';
    clearValidation();

    let selectedGroups = [];
    let selectedChannels = ['PUSH', 'EMAIL'];

    const ns = data ? (data.namespace || 'RULE') : 'RULE';
    const sId = data ? (data.sourceId || '') : '';

    if (id && data) {
      el.title.textContent = i18n.editTitle || 'Edit Alert Configuration';
      el.id.value = data.id || '';

      el.alertName.value = data.alertName || '';
      el.alertCode.value = data.alertCode || '';
      el.namespace.value = ns;
      el.severity.value = data.severity || 'INFO';
      el.cooldownMinutes.value = data.cooldownMinutes !== undefined ? data.cooldownMinutes : 10;
      el.autoResolve.checked = !!data.autoResolve;
      el.messageTemplate.value = data.messageTemplate || '';

      selectedGroups = data.recipientGroupCodes || [];
      selectedChannels = data.channels || [];

      // AlertCode and Namespace might not be editable depending on rules, but let's keep them editable unless designed otherwise.
      // In typical CRUD, we can edit them or read-only them. Let's make alertCode read-only in edit mode for safety.
      el.alertCode.disabled = true;
    } else {
      el.title.textContent = i18n.addTitle || 'Add Alert Configuration';
      el.alertName.value = '';
      el.alertCode.value = '';
      el.namespace.value = ns;
      el.severity.value = 'WARNING';
      el.cooldownMinutes.value = 10;
      el.autoResolve.checked = true;
      el.messageTemplate.value = '';
      el.alertCode.disabled = false;
    }

    await toggleSourceIdInput(ns, sId);

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
    const alertCode = el.alertCode.value.trim();
    const namespace = el.namespace.value;
    const isRule = namespace === 'RULE';
    const sourceId = isRule ? el.sourceIdSelect.value : el.sourceId.value.trim();
    const severity = el.severity.value;
    const cooldownMinutes = el.cooldownMinutes.value;
    const messageTemplate = el.messageTemplate.value.trim();
    const autoResolve = !!el.autoResolve.checked;

    const recipientGroupCodes = [];
    const groupCheckboxes = el.recipientGroupsContainer.querySelectorAll('input[name="recipientGroups"]:checked');
    groupCheckboxes.forEach((cb) => {
      recipientGroupCodes.push(cb.value);
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

    if (!alertCode) {
      el.alertCode.classList.add('is-invalid');
      isValid = false;
    }

    if (!sourceId) {
      if (isRule) {
        el.sourceIdSelect.classList.add('is-invalid');
      } else {
        el.sourceId.classList.add('is-invalid');
      }
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

    if (recipientGroupCodes.length === 0) {
      Toast.warning(i18n.valSelectGroup || 'Please select at least one recipient group.');
      isValid = false;
    }

    if (channels.length === 0) {
      Toast.warning(i18n.valSelectChannel || 'Please select at least one notification channel.');
      isValid = false;
    }

    if (!isValid) return;

    const dto = {
      alertName,
      alertCode,
      namespace,
      sourceId,
      severity,
      recipientGroupCodes,
      channels,
      messageTemplate,
      cooldownMinutes: parseInt(cooldownMinutes, 10),
      autoResolve,
    };

    const id = el.id.value;
    let err = null;
    let res = null;

    if (id) {
      [err, res] = await updateConfig(Number(id), dto);
    } else {
      [err, res] = await createConfig(dto);
    }

    if (err) {
      Swal.fire(i18n.error, err.message || 'An error occurred', 'error');
      return;
    }

    Toast.success(i18n.success);
    bootstrapModal?.hide();
    if (saveCallback) {
      await saveCallback();
    }
  };

  return { init, open, submit };
})();
