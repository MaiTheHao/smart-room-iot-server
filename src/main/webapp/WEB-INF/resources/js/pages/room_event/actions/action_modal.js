import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { getDevicesByRoom } from '../../../api/device.api.js';
import { Alert } from '../../../common/notification_util.js';
import { CreateActionDto } from '../../../types/rule.domain.js';
import { ACTION_PARAM_SCHEMA } from '../../../constants/smart_system.constants.js';
import {
  getAllowedActionParamKeys,
  renderActionParamFields,
  collectActionParamsFromContainer,
  validateActionParams,
  tryParseJson,
} from '../../../common/smart_system_util.js';

const { roomId, i18n } = window.__ROOM_EVENT_ACTIONS_CONFIG__;

export const ActionModal = (() => {
  let bootstrapModal = null;
  let cachedDevices = null;
  const el = {};

  const init = () => {
    el.modal = document.getElementById('actionModal');
    el.form = document.getElementById('actionForm');
    el.modalTitle = document.getElementById('modalTitle');
    el.localId = document.getElementById('actionLocalId');

    el.executionOrder = document.getElementById('executionOrder');
    el.category = document.getElementById('targetDeviceCategory');
    el.deviceId = document.getElementById('targetDeviceId');
    el.dynamicParamsContainer = document.getElementById('dynamicParamsContainer');

    if (el.modal && typeof bootstrap !== 'undefined') {
      bootstrapModal = new bootstrap.Modal(el.modal);
    }

    bindEvents();
  };

  const bindEvents = () => {
    el.category?.addEventListener('change', () => onCategoryChange());
    el.deviceId?.addEventListener('change', () => onDeviceChange());
  };

  const fetchDevices = async () => {
    if (!cachedDevices) {
      const [err, res] = await getDevicesByRoom(roomId);
      if (err) {
        Alert.error(i18n.error, i18n.errorLoading || 'Failed to load devices');
        return [];
      }
      cachedDevices = Array.isArray(res?.data) ? res.data : (res?.data?.content || []);
    }
    return cachedDevices;
  };

  const filterDevices = (devices, selectedCategory) => {
    if (!selectedCategory) return devices;
    return devices.filter((d) => (d.category || d.deviceCategory) === selectedCategory);
  };

  const populateDeviceDropdown = (devices, selectedCategory = '', selectedId = null) => {
    el.deviceId.disabled = true;

    const filtered = filterDevices(devices, selectedCategory);
    if (filtered.length === 0) {
      el.deviceId.innerHTML = `<option value="" disabled selected>${i18n.noDevicesFound || 'No devices found'}</option>`;
      return;
    }

    el.deviceId.innerHTML = `<option value="" disabled selected>${i18n.selectDevice || 'Select device'}</option>`;
    let hasSelected = false;
    filtered.forEach((d) => {
      const opt = document.createElement('option');
      opt.value = d.id;
      const cat = d.category || d.deviceCategory || '';
      opt.dataset.category = cat;
      opt.dataset.specificType = d.specificType || '';
      opt.textContent = `${d.name || d.code || '#' + d.id} (${cat})`;
      if (selectedId && String(d.id) === String(selectedId)) {
        opt.selected = true;
        hasSelected = true;
      }
      el.deviceId.appendChild(opt);
    });
    el.deviceId.disabled = false;

    if (hasSelected) {
      const selectedOpt = el.deviceId.options[el.deviceId.selectedIndex];
      const specificType = selectedOpt?.dataset?.specificType || null;
      const currentParams = getEnteredParams(selectedCategory);
      renderParams(selectedCategory, currentParams, specificType);
    }
  };

  const getEnteredParams = (category) => {
    const config = ACTION_PARAM_SCHEMA[category];
    if (!config) return {};
    const params = {};
    for (const [key, schema] of Object.entries(config)) {
      const inputEl = el.dynamicParamsContainer.querySelector(`[name="param_${key}"]`);
      if (inputEl) {
        const val = inputEl.value;
        if (val !== '' && val !== null && val !== undefined) {
          params[key] = val;
        }
      }
    }
    return params;
  };

  const onCategoryChange = async (preservedParams = {}) => {
    const selectedCategory = el.category.value;
    const devices = await fetchDevices();
    populateDeviceDropdown(devices, selectedCategory);
    renderParams(selectedCategory, preservedParams);
  };

  const onDeviceChange = () => {
    const opt = el.deviceId.selectedOptions[0];
    const specificType = opt?.dataset?.specificType || null;
    const currentParams = getEnteredParams(el.category.value);
    renderParams(el.category.value, currentParams, specificType);
  };

  const renderParams = (category, values = {}, specificType = null) => {
    if (!specificType) {
      const selectedOpt = el.deviceId.options[el.deviceId.selectedIndex];
      specificType = selectedOpt?.dataset?.specificType || null;
    }
    const allowedKeys = getAllowedActionParamKeys(category, specificType);
    renderActionParamFields({
      container: el.dynamicParamsContainer,
      category,
      values,
      allowedKeys,
      i18n,
    });
    window.renderIcons?.();
  };

  const collectParams = async (category) => {
    const params = collectActionParamsFromContainer(el.dynamicParamsContainer, category);
    const validation = validateActionParams({ category, params });
    if (!validation.isValid) {
      const firstErr = Object.values(validation.errors)[0] || 'Invalid parameter';
      await Alert.warning(firstErr, i18n.error || 'Error');
      return null;
    }
    return params;
  };

  const populateEditAction = (devices, existing) => {
    const category = existing.targetCategory || existing.targetDeviceCategory || '';
    el.category.value = category;
    populateDeviceDropdown(devices, category, String(existing.targetId || existing.targetDeviceId || ''));

    el.executionOrder.value = existing.executionOrder !== undefined ? existing.executionOrder : 0;
    const rawParams = tryParseJson(existing.params || existing.actionParams, {});
    renderParams(category, rawParams);
  };

  const populateNewAction = (devices, count) => {
    el.category.value = el.category.options[0]?.value || '';
    populateDeviceDropdown(devices, el.category.value);
    el.executionOrder.value = count;
    renderParams(el.category.value);
  };

  const open = async (localId = null) => {
    el.form.reset();
    el.localId.value = '';

    const isEdit = Boolean(localId);
    el.localId.value = localId || '';
    el.modalTitle.textContent = isEdit ? (i18n.editTitle || 'Chỉnh sửa hành động') : (i18n.addTitle || 'Thêm hành động');

    const devices = await fetchDevices();
    const existing = isEdit ? StateManager.getAction(localId) : null;
    if (isEdit && existing) {
      populateEditAction(devices, existing);
    } else {
      populateNewAction(devices, StateManager.getActions().length);
    }
    bootstrapModal?.show();
    window.renderIcons?.();
  };

  const submit = async (e) => {
    e.preventDefault();

    const localId = el.localId.value;
    const category = el.category.value;
    const targetId = el.deviceId.value;
    const orderVal = el.executionOrder.value;

    if (orderVal === '' || isNaN(orderVal) || parseInt(orderVal, 10) < 0) {
      await Alert.warning(i18n.valExecutionOrderInvalid || 'Execution order must be a valid number', i18n.error || 'Error');
      el.executionOrder?.focus();
      return;
    }

    const actionParams = await collectParams(category);
    if (actionParams === null) return;

    const builder = new CreateActionDto.Builder()
      .setTargetCategory(category)
      .setTargetId(targetId)
      .setParams(actionParams)
      .setExecutionOrder(orderVal);

    const result = builder.validate();
    if (!result.isValid) {
      const firstField = Object.keys(result.errors)[0];
      const msgKey = result.errors[firstField];
      const fieldLabel = ({ targetId: i18n.colTargetDevice, targetCategory: i18n.colType, executionOrder: i18n.colOrder, params: i18n.colParams })[firstField] || '';
      await Alert.warning((i18n[msgKey] || i18n.valRequired || 'Error').replace('{0}', fieldLabel), i18n.error || 'Error');
      const FIELD_ID_MAP = { targetId: el.deviceId, targetCategory: el.category, executionOrder: el.executionOrder };
      FIELD_ID_MAP[firstField]?.focus();
      return;
    }

    const selectedOption = el.deviceId.options[el.deviceId.selectedIndex];
    const targetName = selectedOption ? selectedOption.textContent : '';

    const actionData = {
      executionOrder: parseInt(orderVal, 10),
      targetCategory: category,
      targetId: String(targetId),
      params: actionParams,
      targetName,
    };

    if (localId) {
      StateManager.updateAction(localId, actionData);
    } else {
      StateManager.addAction(actionData);
    }

    UiRenderer.render();
    bootstrapModal?.hide();
  };

  return {
    init,
    open,
    submit,
  };
})();
