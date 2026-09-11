import { StateManager } from './state_manager.js';
import { getDevicesByRoom } from '../../api/device.api.js';

export const ActionModal = (() => {
  let modalInstance = null;
  let cachedDevices = null;
  let editingLocalId = null;

  const getEl = (id) => document.getElementById(id);

  const init = () => {
    const modalEl = getEl('roomEventActionModal');
    if (!modalEl) return;
    modalInstance = new bootstrap.Modal(modalEl);

    // Form submission
    const form = getEl('actionForm');
    form?.addEventListener('submit', (e) => {
      e.preventDefault();
      handleSubmit();
    });

    // Device selection change -> render appropriate control params
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
      if (selectedDeviceId && String(dev.id) === String(selectedDeviceId)) {
        opt.selected = true;
      }
      select.appendChild(opt);
    });
  };

  const renderParamsUi = (category, currentParams = {}) => {
    const container = getEl('actParamsContainer');
    if (!container) return;
    container.innerHTML = '';

    if (!category) return;

    if (category === 'LIGHT') {
      container.innerHTML = `
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label small fw-bold">Nguồn (Power)</label>
            <select class="form-select" id="param_power" required>
              <option value="ON" ${currentParams.power !== 'OFF' ? 'selected' : ''}>BẬT (ON)</option>
              <option value="OFF" ${currentParams.power === 'OFF' ? 'selected' : ''}>TẮT (OFF)</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-bold">Độ sáng (Level: 0 - 100)</label>
            <input type="number" class="form-control" id="param_level" min="0" max="100" value="${currentParams.level ?? 80}" required>
          </div>
        </div>
      `;
    } else if (category === 'FAN') {
      container.innerHTML = `
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label small fw-bold">Nguồn (Power)</label>
            <select class="form-select" id="param_power" required>
              <option value="ON" ${currentParams.power !== 'OFF' ? 'selected' : ''}>BẬT (ON)</option>
              <option value="OFF" ${currentParams.power === 'OFF' ? 'selected' : ''}>TẮT (OFF)</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-bold">Tốc độ gió (Speed: 1 - 3)</label>
            <input type="number" class="form-control" id="param_speed" min="1" max="3" value="${currentParams.speed ?? 1}" required>
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-bold">Chế độ (Mode)</label>
            <select class="form-select" id="param_mode">
              <option value="NORMAL" ${currentParams.mode === 'NORMAL' ? 'selected' : ''}>NORMAL</option>
              <option value="NATURAL" ${currentParams.mode === 'NATURAL' ? 'selected' : ''}>NATURAL</option>
              <option value="SLEEP" ${currentParams.mode === 'SLEEP' ? 'selected' : ''}>SLEEP</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-bold">Đảo gió (Swing)</label>
            <select class="form-select" id="param_swing">
              <option value="OFF" ${currentParams.swing !== 'ON' ? 'selected' : ''}>TẮT (OFF)</option>
              <option value="ON" ${currentParams.swing === 'ON' ? 'selected' : ''}>BẬT (ON)</option>
            </select>
          </div>
        </div>
      `;
    } else if (category === 'AIR_CONDITION') {
      container.innerHTML = `
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label small fw-bold">Nguồn (Power)</label>
            <select class="form-select" id="param_power" required>
              <option value="ON" ${currentParams.power !== 'OFF' ? 'selected' : ''}>BẬT (ON)</option>
              <option value="OFF" ${currentParams.power === 'OFF' ? 'selected' : ''}>TẮT (OFF)</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label small fw-bold">Nhiệt độ (°C: 16 - 32)</label>
            <input type="number" class="form-control" id="param_temperature" min="16" max="32" value="${currentParams.temperature ?? 24}" required>
          </div>
          <div class="col-md-4">
            <label class="form-label small fw-bold">Chế độ (Mode)</label>
            <select class="form-select" id="param_mode">
              <option value="COOL" ${currentParams.mode === 'COOL' || !currentParams.mode ? 'selected' : ''}>COOL</option>
              <option value="HEAT" ${currentParams.mode === 'HEAT' ? 'selected' : ''}>HEAT</option>
              <option value="DRY" ${currentParams.mode === 'DRY' ? 'selected' : ''}>DRY</option>
              <option value="FAN" ${currentParams.mode === 'FAN' ? 'selected' : ''}>FAN</option>
              <option value="AUTO" ${currentParams.mode === 'AUTO' ? 'selected' : ''}>AUTO</option>
            </select>
          </div>
          <div class="col-md-4">
            <label class="form-label small fw-bold">Tốc độ quạt (0 - 5)</label>
            <input type="number" class="form-control" id="param_fanSpeed" min="0" max="5" value="${currentParams.fanSpeed ?? 2}">
          </div>
          <div class="col-md-4">
            <label class="form-label small fw-bold">Đảo gió (Swing)</label>
            <select class="form-select" id="param_swing">
              <option value="OFF" ${currentParams.swing !== 'ON' ? 'selected' : ''}>TẮT (OFF)</option>
              <option value="ON" ${currentParams.swing === 'ON' ? 'selected' : ''}>BẬT (ON)</option>
            </select>
          </div>
        </div>
      `;
    } else {
      container.innerHTML = `<div class="text-muted small">Loại thiết bị ${category} chưa hỗ trợ cấu hình tham số.</div>`;
    }
  };

  const handleDeviceSelect = (deviceId) => {
    const dev = (cachedDevices || []).find((d) => String(d.id) === String(deviceId));
    if (dev) {
      renderParamsUi(dev.category);
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
        populateDeviceSelect(act.targetId || act.targetDeviceId);
        const category = act.targetCategory || act.targetDeviceCategory;
        renderParamsUi(category, act.params || {});
      }
    } else {
      populateDeviceSelect();
      const container = getEl('actParamsContainer');
      if (container) container.innerHTML = '<div class="text-muted small">Chọn thiết bị để thiết lập trạng thái mong muốn.</div>';
    }

    modalInstance?.show();
  };

  const handleSubmit = () => {
    const deviceSelect = getEl('actDeviceId');
    const deviceId = deviceSelect?.value;
    if (!deviceId) {
      alert('Vui lòng chọn thiết bị điều khiển!');
      return;
    }

    const selectedOpt = deviceSelect.options[deviceSelect.selectedIndex];
    const targetCategory = selectedOpt?.dataset?.category;
    const targetDisplay = selectedOpt?.textContent || `Device #${deviceId}`;

    const params = {};
    const power = getEl('param_power')?.value;
    if (power) params.power = power;

    if (targetCategory === 'LIGHT') {
      const level = getEl('param_level')?.value;
      if (level !== undefined && level !== '') params.level = parseInt(level, 10);
    } else if (targetCategory === 'FAN') {
      const speed = getEl('param_speed')?.value;
      if (speed !== undefined && speed !== '') params.speed = parseInt(speed, 10);
      const mode = getEl('param_mode')?.value;
      if (mode) params.mode = mode;
      const swing = getEl('param_swing')?.value;
      if (swing) params.swing = swing;
    } else if (targetCategory === 'AIR_CONDITION') {
      const temp = getEl('param_temperature')?.value;
      if (temp !== undefined && temp !== '') params.temperature = parseInt(temp, 10);
      const mode = getEl('param_mode')?.value;
      if (mode) params.mode = mode;
      const fanSpeed = getEl('param_fanSpeed')?.value;
      if (fanSpeed !== undefined && fanSpeed !== '') params.fanSpeed = parseInt(fanSpeed, 10);
      const swing = getEl('param_swing')?.value;
      if (swing) params.swing = swing;
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
