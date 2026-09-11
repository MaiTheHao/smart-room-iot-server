import { StateManager } from './state_manager.js';
import { getDevicesByRoom } from '../../api/device.api.js';
import { Alert } from '../../common/notification_util.js';
import { ACTION_PARAM_SCHEMA } from '../../constants/smart_system.constants.js';
import { getAllowedActionParamKeys } from '../../common/smart_system_util.js';

const FIELD_LABELS = {
  power: 'Nguồn (Power)',
  level: 'Độ sáng (Level: 0 - 100)',
  temperature: 'Nhiệt độ (°C: 16 - 32)',
  mode: 'Chế độ (Mode)',
  fanSpeed: 'Tốc độ quạt (0 - 5)',
  speed: 'Tốc độ gió (Speed: 1 - 3)',
  swing: 'Đảo gió (Swing)',
};

const FIELD_DEFAULTS = {
  LIGHT: { power: 'ON', level: 80 },
  FAN: { power: 'ON', speed: 1, mode: 'NORMAL', swing: 'OFF' },
  AIR_CONDITION: { power: 'ON', temperature: 24, mode: 'COOL', fanSpeed: 2, swing: 'OFF' },
};

export const ActionModal = (() => {
  let modalInstance = null;
  let cachedDevices = null;
  let editingLocalId = null;

  const getEl = (id) => document.getElementById(id);

  const init = () => {
    const modalEl = getEl('roomEventActionModal');
    if (!modalEl) return;
    modalInstance = new bootstrap.Modal(modalEl);

    const form = getEl('actionForm');
    form?.addEventListener('submit', (e) => {
      e.preventDefault();
      handleSubmit();
    });

    getEl('actDeviceId')?.addEventListener('change', (e) => {
      handleDeviceSelect(e.target.value);
    });
  };

  const loadRoomDevices = async (roomId) => {
    if (cachedDevices) return cachedDevices;
    try {
      const [err, res] = await getDevicesByRoom(roomId);
      if (!err && res?.data) {
        cachedDevices = res.data;
      } else {
        cachedDevices = [];
      }
    } catch (e) {
      cachedDevices = [];
    }
    return cachedDevices;
  };

  const populateDeviceSelect = (selectedDeviceId = null) => {
    const select = getEl('actDeviceId');
    if (!select) return;
    select.innerHTML = '<option value="" disabled selected>-- Chọn thiết bị trong phòng --</option>';

    (cachedDevices || []).forEach((dev) => {
      const opt = document.createElement('option');
      opt.value = dev.id;
      opt.textContent = `${dev.name || 'Device #' + dev.id} (${dev.category || 'Unknown'})`;
      opt.dataset.category = dev.category;
      opt.dataset.specificType = dev.specificType || '';
      if (selectedDeviceId && String(dev.id) === String(selectedDeviceId)) {
        opt.selected = true;
      }
      select.appendChild(opt);
    });
  };

  const renderParamsUi = (category, currentParams = {}, specificType = null) => {
    const container = getEl('actParamsContainer');
    if (!container) return;
    container.innerHTML = '';
    if (!category) return;

    const schema = ACTION_PARAM_SCHEMA[category];
    if (!schema) {
      container.innerHTML = `<div class="text-muted small">Loại thiết bị ${category} chưa hỗ trợ cấu hình tham số.</div>`;
      return;
    }

    const allowedKeys = getAllowedActionParamKeys(category, specificType);
    const defaults = FIELD_DEFAULTS[category] || {};
    const row = document.createElement('div');
    row.className = 'row g-3';

    Object.entries(schema).forEach(([key, field]) => {
      if (allowedKeys && !allowedKeys.includes(key)) return;

      const current = (currentParams[key] !== undefined && currentParams[key] !== null)
        ? currentParams[key]
        : defaults[key];

      const col = document.createElement('div');
      col.className = 'col-md-6';

      const label = document.createElement('label');
      label.className = 'form-label fw-semibold small text-muted text-uppercase mb-1';
      label.textContent = FIELD_LABELS[key] || key;
      col.appendChild(label);

      if (field.type === 'enum') {
        const select = document.createElement('select');
        select.className = 'form-select bg-light border-0';
        select.id = `param_${key}`;
        select.name = `param_${key}`;
        (field.options || []).forEach((optVal) => {
          const opt = document.createElement('option');
          opt.value = optVal;
          opt.textContent = optVal;
          if (current !== undefined && String(current) === optVal) opt.selected = true;
          select.appendChild(opt);
        });
        col.appendChild(select);
      } else {
        const input = document.createElement('input');
        input.type = 'number';
        input.className = 'form-control bg-light border-0 font-monospace';
        input.id = `param_${key}`;
        input.name = `param_${key}`;
        if (field.min != null) input.min = field.min;
        if (field.max != null) input.max = field.max;
        input.placeholder = field.placeholder || '';
        if (current !== undefined && current !== null && current !== '') input.value = current;
        col.appendChild(input);
      }

      row.appendChild(col);
    });

    container.appendChild(row);
  };

  const handleDeviceSelect = (deviceId) => {
    const dev = (cachedDevices || []).find((d) => String(d.id) === String(deviceId));
    if (dev) {
      renderParamsUi(dev.category, {}, dev.specificType || null);
    }
  };

  const open = async (localId = null) => {
    editingLocalId = localId;
    const form = getEl('actionForm');
    form?.reset();

    const titleEl = getEl('actionModalTitle');
    if (titleEl) {
      titleEl.textContent = localId ? 'Chỉnh sửa hành động điều khiển' : 'Thêm hành động điều khiển';
    }

    const currentRoomId = StateManager.getCurrentConfig()?.roomId;
    if (currentRoomId) {
      await loadRoomDevices(currentRoomId);
    }

    if (localId) {
      const act = StateManager.getActions().find((a) => a._localId === localId);
      if (act) {
        const targetId = act.targetId || act.targetDeviceId;
        populateDeviceSelect(targetId);
        const dev = (cachedDevices || []).find((d) => String(d.id) === String(targetId));
        const category = act.targetCategory || act.targetDeviceCategory;
        renderParamsUi(category, act.params || {}, dev?.specificType || null);
      }
    } else {
      populateDeviceSelect();
      const container = getEl('actParamsContainer');
      if (container) container.innerHTML = '<div class="text-muted small">Chọn thiết bị để thiết lập trạng thái mong muốn.</div>';
    }

    modalInstance?.show();
  };

  const handleSubmit = async () => {
    const deviceSelect = getEl('actDeviceId');
    const deviceId = deviceSelect?.value;
    if (!deviceId) {
      await Alert.warning('Vui lòng chọn thiết bị điều khiển!', 'Thiếu thông tin');
      return;
    }

    const selectedOpt = deviceSelect.options[deviceSelect.selectedIndex];
    const targetCategory = selectedOpt?.dataset?.category;
    const targetDisplay = selectedOpt?.textContent || `Device #${deviceId}`;
    const schema = ACTION_PARAM_SCHEMA[targetCategory] || {};

    const params = {};
    for (const [key, field] of Object.entries(schema)) {
      const input = getEl(`param_${key}`);
      if (!input) continue;
      const val = input.value;
      if (val === '' || val === null || val === undefined) continue;

      if (field.type === 'int') {
        const num = parseInt(val, 10);
        if (Number.isNaN(num) || (field.min != null && num < field.min) || (field.max != null && num > field.max)) {
          await Alert.warning(
            `${FIELD_LABELS[key] || key}: giá trị phải trong khoảng ${field.min} – ${field.max}`,
            'Giá trị không hợp lệ',
          );
          input.focus();
          return;
        }
        params[key] = num;
      } else {
        params[key] = val;
      }
    }

    const payload = {
      targetCategory,
      targetId: String(deviceId),
      targetDisplay,
      params,
    };

    if (editingLocalId) {
      StateManager.updateAction(editingLocalId, payload);
    } else {
      StateManager.addAction(payload);
    }

    modalInstance?.hide();
  };

  return {
    init,
    open,
  };
})();
