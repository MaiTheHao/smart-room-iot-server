import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { getDevicesByRoom } from '../../../api/device.api.js';
import { Alert } from '../../../common/notification_util.js';
import {
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
    el.valDeviceId = document.getElementById('val-targetDeviceId');

    if (el.modal && typeof bootstrap !== 'undefined') {
      bootstrapModal = new bootstrap.Modal(el.modal);
    }

    bindEvents();
  };

  const bindEvents = () => {
    el.form?.addEventListener('submit', (e) => submit(e));
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

  const renderDeviceOptions = (filtered) => {
    filtered.forEach((d) => {
      const opt = document.createElement('option');
      opt.value = d.id;
      const cat = d.category || d.deviceCategory || '';
      opt.dataset.category = cat;
      opt.textContent = `${d.name || d.code || '#' + d.id} (${cat})`;
      el.deviceId.appendChild(opt);
    });
  };

  const populateDeviceDropdown = (devices, selectedCategory = '') => {
    el.deviceId.innerHTML = `<option value="" disabled selected>${i18n.selectDevice || 'Select device'}</option>`;
    const filtered = filterDevices(devices, selectedCategory);
    if (filtered.length === 0) {
      el.deviceId.innerHTML = `<option value="" disabled selected>${i18n.noDevicesFound || 'No devices found'}</option>`;
      return;
    }
    renderDeviceOptions(filtered);
  };

  const onCategoryChange = async (preservedParams = {}) => {
    const selectedCategory = el.category.value;
    const devices = await fetchDevices();
    populateDeviceDropdown(devices, selectedCategory);

    const selectedOpt = el.deviceId.selectedOptions[0];
    if (selectedCategory && selectedOpt?.dataset?.category !== selectedCategory) {
      el.deviceId.value = '';
    }
    renderParams(selectedCategory, preservedParams);
  };

  const onDeviceChange = () => {
    clearValidation();
    const opt = el.deviceId.selectedOptions[0];
    if (opt?.dataset?.category && el.category.value !== opt.dataset.category) {
      el.category.value = opt.dataset.category;
      renderParams(opt.dataset.category);
    }
  };

  const renderParams = (category, values = {}) => {
    renderActionParamFields({
      container: el.dynamicParamsContainer,
      category,
      values,
      i18n,
    });
  };

  const clearValidation = () => {
    el.deviceId?.classList.remove('is-invalid');
  };

  const getValidationResult = () => {
    const errors = {};
    if (!el.deviceId.value) {
      errors.deviceId = true;
    }
    const opt = el.deviceId.selectedOptions[0];
    const cat = el.category.value || opt?.dataset?.category || '';
    const params = collectActionParamsFromContainer(el.dynamicParamsContainer, cat);
    const validation = validateActionParams({ category: cat, params });
    if (!validation.isValid) {
      errors.params = Object.values(validation.errors)[0] || 'Tham số hành động không hợp lệ.';
    }
    return { isValid: Object.keys(errors).length === 0, errors, params, targetCat: cat };
  };

  const applyValidationErrors = (errors = {}) => {
    clearValidation();
    if (errors.deviceId) el.deviceId.classList.add('is-invalid');
    if (errors.params) Alert.error(i18n.error || 'Lỗi', errors.params);
  };

  const populateEditAction = (devices, existing) => {
    const category = existing.targetCategory || existing.targetDeviceCategory || '';
    el.category.value = category;
    populateDeviceDropdown(devices, category);

    el.deviceId.value = String(existing.targetId || existing.targetDeviceId || '');
    el.executionOrder.value = existing.executionOrder !== undefined ? existing.executionOrder : 0;
    const rawParams = tryParseJson(existing.params || existing.actionParams, {});
    renderParams(category, rawParams);
  };

  const populateNewAction = (devices, count) => {
    el.category.value = '';
    populateDeviceDropdown(devices, '');
    el.executionOrder.value = count;
    renderParams('');
  };

  const open = async (localId = null) => {
    el.form.reset();
    clearValidation();

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
  };

  const buildActionPayload = (targetCat, params) => ({
    targetCategory: targetCat,
    targetId: el.deviceId.value,
    params,
    executionOrder: parseInt(el.executionOrder.value, 10) || 0,
  });

  const submit = (e) => {
    e.preventDefault();
    const validation = getValidationResult();
    applyValidationErrors(validation.errors);
    if (!validation.isValid) return;

    const localId = el.localId.value;
    const actionData = buildActionPayload(validation.targetCat, validation.params);
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
