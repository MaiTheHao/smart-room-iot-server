import { getRuleAlertConfig, saveRuleAlertConfig, deleteRuleAlertConfig } from '../../../../api/rule.api.js';
import { getAllGroups } from '../../../../api/group.api.js';
import { Toast, Alert } from '../../../../common/notification_util.js';

const { ruleName, i18n } = window.__ALERT_PAGE_CONFIG__;

const Controller = (() => {
  const elements = {
    form: null,
    ruleIdInput: null,
    alertNameInput: null,
    severitySelect: null,
    cooldownMinutesInput: null,
    recipientGroupsContainer: null,
    messageTemplateTextarea: null,
    autoResolveSwitch: null,
    submitBtn: null,
    deleteBtn: null,
  };

  let ruleId = null;

  const init = async () => {
    elements.form = document.getElementById('alertConfigForm');
    if (!elements.form) return;

    elements.ruleIdInput = document.getElementById('ruleId');
    elements.alertNameInput = document.getElementById('alertName');
    elements.severitySelect = document.getElementById('alertSeverity');
    elements.cooldownMinutesInput = document.getElementById('alertCooldownMinutes');
    elements.recipientGroupsContainer = document.getElementById('alertRecipientGroupsContainer');
    elements.messageTemplateTextarea = document.getElementById('alertMessageTemplate');
    elements.autoResolveSwitch = document.getElementById('alertAutoResolve');
    elements.submitBtn = document.getElementById('btnSubmitAlertConfig');
    elements.deleteBtn = document.getElementById('btnDeleteAlertConfig');

    ruleId = elements.ruleIdInput?.value;
    if (!ruleId) return;

    elements.form.addEventListener('submit', handleSubmit);
    elements.deleteBtn.addEventListener('click', handleDelete);

    await loadConfig();
  };

  const clearValidation = () => {
    const inputs = elements.form.querySelectorAll('.form-control, .form-select');
    inputs.forEach((el) => el.classList.remove('is-invalid'));
  };

  const resetForm = () => {
    if (elements.form) {
      elements.form.reset();
    }
    elements.alertNameInput.value = `Alert for ${ruleName}`;
    elements.severitySelect.value = 'WARNING';
    elements.cooldownMinutesInput.value = '10';
    elements.messageTemplateTextarea.value = `Rule [${ruleName}] triggered with severity [WARNING]`;
    elements.autoResolveSwitch.checked = true;

    const channelCheckboxes = elements.form.querySelectorAll('input[name="channels"]');
    channelCheckboxes.forEach((checkbox) => {
      checkbox.checked = ['PUSH', 'EMAIL'].includes(checkbox.value);
    });

    elements.deleteBtn.classList.add('d-none');
    clearValidation();
  };

  const loadRecipientGroups = async (selectedGroups = []) => {
    const container = elements.recipientGroupsContainer;
    if (!container) return;
    container.replaceChildren();

    const [err, res] = await getAllGroups();
    if (err) {
      const errorMsg = document.createElement('p');
      errorMsg.className = 'text-danger small mb-0';
      errorMsg.textContent = 'Failed to load recipient groups.';
      container.appendChild(errorMsg);
      return;
    }

    const groups = res.data || [];
    if (groups.length === 0) {
      const emptyMsg = document.createElement('p');
      emptyMsg.className = 'text-muted small mb-0';
      emptyMsg.textContent = 'No groups available.';
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

  const loadConfig = async () => {
    if (elements.recipientGroupsContainer) {
      elements.recipientGroupsContainer.replaceChildren();
      const loadingMsg = document.createElement('p');
      loadingMsg.className = 'text-muted small mb-0';
      loadingMsg.textContent = 'Loading recipient groups...';
      elements.recipientGroupsContainer.appendChild(loadingMsg);
    }

    try {
      const [err, res] = await getRuleAlertConfig(ruleId);

      let selectedGroups = [];
      let selectedChannels = [];

      if (!err && res && res.data) {
        const config = res.data;
        elements.alertNameInput.value = config.alertName || '';
        elements.severitySelect.value = config.severity || 'INFO';
        elements.cooldownMinutesInput.value = config.cooldownMinutes !== undefined ? config.cooldownMinutes : '10';
        elements.messageTemplateTextarea.value = config.messageTemplate || '';
        elements.autoResolveSwitch.checked = !!config.autoResolve;

        selectedGroups = config.recipientGroups || [];
        selectedChannels = config.channels || [];

        elements.deleteBtn.classList.remove('d-none');
      } else {
        elements.alertNameInput.value = `Alert for ${ruleName}`;
        elements.severitySelect.value = 'WARNING';
        elements.cooldownMinutesInput.value = '10';
        elements.messageTemplateTextarea.value = `Rule [${ruleName}] triggered with severity [WARNING]`;
        elements.autoResolveSwitch.checked = true;

        selectedChannels = ['PUSH', 'EMAIL'];
        elements.deleteBtn.classList.add('d-none');
      }

      await loadRecipientGroups(selectedGroups);

      const channelCheckboxes = elements.form.querySelectorAll('input[name="channels"]');
      channelCheckboxes.forEach((checkbox) => {
        checkbox.checked = selectedChannels.includes(checkbox.value);
      });
      window.renderIcons?.();
    } catch (e) {
      console.error('Error loading alert config:', e);
      Toast.error('Failed to load alert configuration');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    clearValidation();

    const alertName = elements.alertNameInput.value.trim();
    const severity = elements.severitySelect.value;
    const cooldownMinutes = elements.cooldownMinutesInput.value;
    const messageTemplate = elements.messageTemplateTextarea.value.trim();
    const autoResolve = !!elements.autoResolveSwitch.checked;

    const recipientGroups = [];
    const groupCheckboxes = elements.recipientGroupsContainer.querySelectorAll('input[name="recipientGroups"]:checked');
    groupCheckboxes.forEach((cb) => {
      recipientGroups.push(cb.value);
    });

    const channels = [];
    const channelCheckboxes = elements.form.querySelectorAll('input[name="channels"]:checked');
    channelCheckboxes.forEach((cb) => {
      channels.push(cb.value);
    });

    let isValid = true;
    if (!alertName) {
      elements.alertNameInput.classList.add('is-invalid');
      isValid = false;
    }

    if (!cooldownMinutes || isNaN(cooldownMinutes) || parseInt(cooldownMinutes, 10) < 0) {
      elements.cooldownMinutesInput.classList.add('is-invalid');
      isValid = false;
    }

    if (!messageTemplate) {
      elements.messageTemplateTextarea.classList.add('is-invalid');
      isValid = false;
    }

    if (recipientGroups.length === 0) {
      Toast.warning('Please select at least one recipient group.');
      isValid = false;
    }

    if (channels.length === 0) {
      Toast.warning('Please select at least one notification channel.');
      isValid = false;
    }

    if (!isValid) return;

    const payload = {
      ruleId: parseInt(ruleId, 10),
      alertName,
      severity,
      recipientGroups,
      channels,
      messageTemplate,
      cooldownMinutes: parseInt(cooldownMinutes, 10),
      autoResolve
    };

    const originalHtml = elements.submitBtn.innerHTML;
    elements.submitBtn.disabled = true;
    elements.submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> Saving...`;

    try {
      const [err] = await saveRuleAlertConfig(ruleId, payload);
      if (err) {
        Toast.error(err.message || 'Failed to save alert configuration');
      } else {
        Toast.success(i18n.saveSuccess);
        await loadConfig();
      }
    } catch (error) {
      console.error('Submit alert config error:', error);
      Toast.error('An unexpected error occurred');
    } finally {
      elements.submitBtn.disabled = false;
      elements.submitBtn.innerHTML = originalHtml;
    }
  };

  const handleDelete = async () => {
    const confirmRes = await Alert.confirm({
      title: i18n.confirmDelete,
      text: '',
      icon: 'warning',
      confirmText: i18n.yesDelete,
      cancelText: i18n.cancel
    });

    if (!confirmRes.isConfirmed) return;

    const originalHtml = elements.deleteBtn.innerHTML;
    elements.deleteBtn.disabled = true;
    elements.deleteBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> Deleting...`;

    try {
      const [err] = await deleteRuleAlertConfig(ruleId);
      if (err) {
        Toast.error(err.message || 'Failed to delete alert configuration');
      } else {
        Toast.success(i18n.deleteSuccess);
        resetForm();
        await loadConfig();
      }
    } catch (error) {
      console.error('Delete alert config error:', error);
      Toast.error('An unexpected error occurred');
    } finally {
      elements.deleteBtn.disabled = false;
      elements.deleteBtn.innerHTML = originalHtml;
    }
  };

  return { init };
})();

document.addEventListener('DOMContentLoaded', () => {
  Controller.init();
});
