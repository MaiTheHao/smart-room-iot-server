import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { getAllGroups } from '../../../../api/group.api.js';
import { TemplateEditor } from '../../alert/manage/editor/editor.js';
import { RuleStrategy } from '../../alert/manage/strategies/rule_strategy.js';

const { i18n } = window.__ALERT_PAGE_CONFIG__;

export const AlertConfigModal = (() => {
  let bootstrapModal = null;
  const el = {};

  const bindDOMElements = () => {
    const ID_MAP = {
      modal: 'alertModal', form: 'alertForm', title: 'alertModalTitle',
      localId: 'alertLocalId', id: 'alertConfigId', alertCode: 'alertCode',
      alertName: 'alertName', severity: 'alertSeverity', cooldownMinutes: 'alertCooldownMinutes',
      recipientGroupsContainer: 'alertRecipientGroupsContainer', messageTemplate: 'alertMessageTemplate',
      variablesContainer: 'alertTemplateVariables', editorContainer: 'alertMessageTemplateEditor',
      previewContainer: 'alertMessageTemplatePreview',
    };
    Object.entries(ID_MAP).forEach(([k, id]) => { el[k] = document.getElementById(id); });
  };

  const init = () => {
    bindDOMElements();
    el.templateEditor = new TemplateEditor({
      editorEl: el.editorContainer,
      textareaEl: el.messageTemplate,
      previewEl: el.previewContainer,
      variablesContainerEl: el.variablesContainer,
    });
    el.ruleStrategy = new RuleStrategy();
    if (el.modal && typeof bootstrap !== 'undefined') {
      bootstrapModal = new bootstrap.Modal(el.modal);
    }
  };

  const createGroupCheckbox = (group, isChecked) => {
    const wrapper = document.createElement('div');
    wrapper.className = 'form-check mb-1';
    wrapper.innerHTML = `
      <input class="form-check-input" type="checkbox" name="recipientGroups" value="${group.groupCode}" id="group_${group.id}" ${isChecked ? 'checked' : ''}>
      <label class="form-check-label small text-dark" for="group_${group.id}">${group.name} (${group.groupCode})</label>
    `;
    return wrapper;
  };

  const loadRecipientGroups = async (selectedGroups = []) => {
    const container = el.recipientGroupsContainer;
    if (!container) return;
    container.replaceChildren();

    const [err, res] = await getAllGroups();
    const groups = res?.data || [];
    if (err || groups.length === 0) {
      const msg = err ? (i18n.errLoadGroups || 'Failed to load groups.') : (i18n.noGroups || 'No groups available.');
      const p = document.createElement('p');
      p.className = `${err ? 'text-danger' : 'text-muted'} small mb-0`;
      p.textContent = msg;
      container.appendChild(p);
      return;
    }

    groups.forEach((group) => {
      container.appendChild(createGroupCheckbox(group, selectedGroups.includes(group.groupCode)));
    });
  };

  const clearValidation = () => {
    el.form.querySelectorAll('.form-control, .form-select').forEach((i) => i.classList.remove('is-invalid'));
    el.editorContainer?.classList.remove('is-invalid');
  };

  const populateEditMode = (data) => {
    el.title.textContent = i18n.editTitle || 'Edit Alert Configuration';
    el.localId.value = data._localId || '';
    el.id.value = data.id || '';
    if (el.alertCode) {
      el.alertCode.value = data.alertCode || '';
      el.alertCode.disabled = !!data.id;
    }
    el.alertName.value = data.alertName || '';
    el.severity.value = data.severity || 'INFO';
    el.cooldownMinutes.value = data.cooldownMinutes ?? 10;
    el.messageTemplate.value = data.messageTemplate || '';

    return {
      selectedGroups: data.recipientGroupCodes || data.recipientGroups || [],
      selectedChannels: data.channels || [],
      textValue: data.messageTemplate || '',
    };
  };

  const resetAddMode = () => {
    el.title.textContent = i18n.addTitle || 'Add Alert Configuration';
    if (el.alertCode) {
      el.alertCode.value = '';
      el.alertCode.disabled = false;
    }
    el.alertName.value = '';
    el.severity.value = 'WARNING';
    el.cooldownMinutes.value = 10;
    el.messageTemplate.value = '';

    return {
      selectedGroups: [],
      selectedChannels: ['PUSH', 'EMAIL'],
      textValue: '',
    };
  };

  const loadRuleTokens = async (ruleId, initialText) => {
    const contextData = await el.ruleStrategy.fetchData(ruleId);
    const tokens = el.ruleStrategy.getTokens(contextData);
    el.templateEditor.load(initialText, tokens);
  };

  const syncChannels = (selectedChannels) => {
    el.form.querySelectorAll('input[name="channels"]').forEach((cb) => {
      cb.checked = selectedChannels.includes(cb.value);
    });
  };

  const open = async (localId = null) => {
    el.form.reset();
    el.localId.value = '';
    el.id.value = '';
    clearValidation();

    const editData = localId ? StateManager.getAlert(localId) : null;
    const modeState = editData ? populateEditMode(editData) : resetAddMode();

    const ruleId = document.getElementById('ruleId')?.value;
    await loadRuleTokens(ruleId, modeState.textValue);
    await loadRecipientGroups(modeState.selectedGroups);
    syncChannels(modeState.selectedChannels);

    bootstrapModal?.show();
    window.renderIcons?.();
  };

  const collectFormData = () => {
    const groups = Array.from(el.recipientGroupsContainer.querySelectorAll('input[name="recipientGroups"]:checked')).map((c) => c.value);
    const channels = Array.from(el.form.querySelectorAll('input[name="channels"]:checked')).map((c) => c.value);

    return {
      id: el.id.value ? Number(el.id.value) : null,
      alertCode: el.alertCode ? el.alertCode.value.trim() : '',
      alertName: el.alertName.value.trim(),
      severity: el.severity.value,
      cooldownMinutes: parseInt(el.cooldownMinutes.value, 10),
      messageTemplate: el.messageTemplate.value.trim(),
      recipientGroupCodes: groups,
      channels,
    };
  };

  const getInvalidFields = (data) => {
    const invalid = [];
    if (el.alertCode && !data.alertCode) invalid.push('alertCode');
    if (!data.alertName) invalid.push('alertName');
    if (isNaN(data.cooldownMinutes) || data.cooldownMinutes < 0) invalid.push('cooldownMinutes');
    if (!data.messageTemplate) invalid.push('messageTemplate');
    return invalid;
  };

  const applyValidationErrors = (invalidFields) => {
    const FIELD_MAP = {
      alertCode: el.alertCode,
      alertName: el.alertName,
      cooldownMinutes: el.cooldownMinutes,
      messageTemplate: el.editorContainer,
    };
    invalidFields.forEach((key) => FIELD_MAP[key]?.classList.add('is-invalid'));
    if (invalidFields.includes('messageTemplate')) {
      el.messageTemplate.classList.add('is-invalid');
    }
  };

  const submit = async (e) => {
    e.preventDefault();
    clearValidation();
    el.templateEditor.syncToTextarea();

    const data = collectFormData();
    const invalidFields = getInvalidFields(data);
    if (invalidFields.length > 0) {
      applyValidationErrors(invalidFields);
      return;
    }

    const localId = el.localId.value;
    if (localId) StateManager.updateAlert(localId, data);
    else StateManager.addAlert(data);

    UiRenderer.render();
    bootstrapModal?.hide();
  };

  return { init, open, submit };
})();
