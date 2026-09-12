import { StateManager } from './state_manager.js';
import { createConfig, updateConfig } from '../../api/room_event.api.js';
import { Alert } from '../../common/notification_util.js';
import {
  CreateRoomEventConfigDto,
  UpdateRoomEventConfigDto,
} from '../../types/room_event.domain.js';

const pageConfig = window.__ROOM_EVENT_PAGE_CONFIG__ || {};
const i18n = pageConfig.i18n || {};

export const ConfigModal = (() => {
  let bootstrapModal = null;
  const el = {};

  const init = () => {
    el.modal = document.getElementById('roomEventConfigModal');
    el.form = document.getElementById('configForm');
    el.modalTitle = document.getElementById('configModalTitle');
    el.configId = document.getElementById('cfgConfigId');
    el.eventCode = document.getElementById('cfgEventCode');
    el.cooldown = document.getElementById('cfgCooldown');
    el.isActive = document.getElementById('cfgIsActive');

    if (el.modal && typeof bootstrap !== 'undefined') {
      bootstrapModal = new bootstrap.Modal(el.modal);
    }

    el.form?.addEventListener('submit', (e) => submit(e));
  };

  const clearValidation = () => {
    [el.eventCode, el.cooldown].forEach((input) => {
      if (input) input.classList.remove('is-invalid');
    });
  };

  const populateEditConfig = (config) => {
    el.eventCode.innerHTML = `<option value="${config.eventCode}" selected>${config.eventCode}</option>`;
    el.eventCode.disabled = true;
    el.cooldown.value = config.cooldownSeconds !== undefined ? config.cooldownSeconds : 60;
    el.isActive.checked = Boolean(config.isActive);
  };

  const populateNewConfig = () => {
    el.eventCode.disabled = false;
    const unconfigured = StateManager.getUnconfiguredCodes();
    el.eventCode.innerHTML = `<option value="" disabled selected>${i18n.selectEventCode || '-- Chọn mã sự kiện --'}</option>`;
    unconfigured.forEach((item) => {
      const opt = document.createElement('option');
      opt.value = item.code;
      opt.textContent = item.description ? `${item.code} (${item.description})` : item.code;
      el.eventCode.appendChild(opt);
    });
    el.cooldown.value = 60;
    el.isActive.checked = true;
  };

  const open = (configId = '') => {
    el.form.reset();
    clearValidation();
    const isEdit = Boolean(configId);
    el.configId.value = configId || '';
    el.modalTitle.textContent = isEdit ? (i18n.editTitle || 'Cập nhật cấu hình') : (i18n.addTitle || 'Thêm cấu hình');
    if (isEdit) {
      const config = StateManager.getConfig(configId);
      if (config) populateEditConfig(config);
    } else {
      populateNewConfig();
    }
    bootstrapModal?.show();
  };

  const getValidationResult = () => {
    const isEdit = Boolean(el.configId.value);
    const errors = {};
    if (!isEdit && !el.eventCode.value) {
      errors.eventCode = true;
    }
    const cd = parseInt(el.cooldown.value, 10);
    if (Number.isNaN(cd) || cd < 0) {
      errors.cooldown = true;
    }
    return { isValid: Object.keys(errors).length === 0, errors };
  };

  const applyValidationErrors = (errors = {}) => {
    clearValidation();
    if (errors.eventCode) el.eventCode.classList.add('is-invalid');
    if (errors.cooldown) el.cooldown.classList.add('is-invalid');
  };

  const submitUpdateConfig = async (roomId, { isActive, cooldownSeconds }) => {
    const configId = el.configId.value;
    const dto = UpdateRoomEventConfigDto.fromForm({ isActive, cooldownSeconds });
    const [err, res] = await updateConfig(roomId, configId, dto.toApi());
    if (err) {
      Alert.error(i18n.error || 'Lỗi', err.message || i18n.updateError || 'Cập nhật thất bại');
      return false;
    }
    StateManager.updateConfig(res?.data);
    Alert.success(i18n.success || 'Thành công', i18n.updateSuccess || 'Đã cập nhật cấu hình');
    return true;
  };

  const submitCreateConfig = async (roomId, { isActive, cooldownSeconds }) => {
    const eventCode = el.eventCode.value;
    const dto = CreateRoomEventConfigDto.fromForm({ eventCode, isActive, cooldownSeconds });
    const [err, res] = await createConfig(roomId, dto.toApi());
    if (err) {
      Alert.error(i18n.error || 'Lỗi', err.message || i18n.createError || 'Tạo mới thất bại');
      return false;
    }
    StateManager.addConfig(res?.data);
    Alert.success(i18n.success || 'Thành công', i18n.createSuccess || 'Đã tạo cấu hình mới');
    return true;
  };

  const submit = async (e) => {
    e.preventDefault();
    const validation = getValidationResult();
    applyValidationErrors(validation.errors);
    if (!validation.isValid) return;

    const roomId = StateManager.getRoomId();
    const isEdit = Boolean(el.configId.value);
    const cooldownSeconds = parseInt(el.cooldown.value, 10) || 0;
    const isActive = el.isActive.checked;

    const success = isEdit
      ? await submitUpdateConfig(roomId, { isActive, cooldownSeconds })
      : await submitCreateConfig(roomId, { isActive, cooldownSeconds });
    if (success) bootstrapModal?.hide();
  };

  return {
    init,
    open,
  };
})();
