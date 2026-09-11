import { StateManager } from './state_manager.js';
import { getSensorsByRoom } from '../../api/sensor-metadata.api.js';

const SENSOR_PROPERTIES = {
  TEMPERATURE: [{ value: 'temperature', label: 'Nhiệt độ (°C)' }],
  HUMIDITY: [{ value: 'humidity', label: 'Độ ẩm (%)' }],
  POWER_CONSUMPTION: [
    { value: 'power', label: 'Công suất (W)' },
    { value: 'voltage', label: 'Điện áp (V)' },
    { value: 'current', label: 'Dòng điện (A)' },
  ],
  SENSOR_LUX: [{ value: 'lux', label: 'Cường độ sáng (Lux)' }],
  SENSOR_CO2: [{ value: 'co2', label: 'Nồng độ CO₂ (ppm)' }],
};

const SYSTEM_PROPERTIES = [
  { value: 'current_time', label: 'Thời gian trong ngày (HH:mm)' },
  { value: 'day_of_week', label: 'Ngày trong tuần (1-7)' },
];

export const ConditionModal = (() => {
  let modalInstance = null;
  let cachedSensors = null;
  let editingLocalId = null;

  const getEl = (id) => document.getElementById(id);

  const init = () => {
    const modalEl = getEl('roomEventConditionModal');
    if (!modalEl) return;
    modalInstance = new bootstrap.Modal(modalEl);

    // Form submission
    const form = getEl('conditionForm');
    form?.addEventListener('submit', (e) => {
      e.preventDefault();
      handleSubmit();
    });

    // Source category change (SENSOR vs SYSTEM)
    getEl('condSourceCategory')?.addEventListener('change', (e) => {
      handleSourceCategoryChange(e.target.value);
    });

    // Sensor selection change
    getEl('condSensorId')?.addEventListener('change', (e) => {
      handleSensorChange(e.target.value);
    });
  };

  const loadRoomSensors = async (roomId) => {
    if (cachedSensors) return cachedSensors;
    try {
      const [err, res] = await getSensorsByRoom(roomId, undefined, 0, 100);
      if (!err && res?.data?.content) {
        cachedSensors = res.data.content;
      } else {
        cachedSensors = [];
      }
    } catch (e) {
      cachedSensors = [];
    }
    return cachedSensors;
  };

  const handleSourceCategoryChange = (cat) => {
    const sensorGroup = getEl('condSensorGroup');
    const propSelect = getEl('condProperty');
    if (!propSelect) return;
    propSelect.innerHTML = '<option value="" disabled selected>-- Chọn thuộc tính --</option>';

    if (cat === 'SYSTEM') {
      sensorGroup?.classList.add('d-none');
      SYSTEM_PROPERTIES.forEach((p) => {
        const opt = document.createElement('option');
        opt.value = p.value;
        opt.textContent = p.label;
        propSelect.appendChild(opt);
      });
    } else {
      sensorGroup?.classList.remove('d-none');
      // Populate sensor options if loaded
      populateSensorSelect();
    }
  };

  const populateSensorSelect = () => {
    const select = getEl('condSensorId');
    if (!select) return;
    select.innerHTML = '<option value="" disabled selected>-- Chọn cảm biến trong phòng --</option>';
    (cachedSensors || []).forEach((s) => {
      const opt = document.createElement('option');
      opt.value = s.id;
      opt.textContent = `${s.name || s.naturalId || 'Sensor #' + s.id} (${s.category})`;
      opt.dataset.category = s.category;
      select.appendChild(opt);
    });
  };

  const handleSensorChange = (sensorId) => {
    const propSelect = getEl('condProperty');
    if (!propSelect) return;
    propSelect.innerHTML = '<option value="" disabled selected>-- Chọn thuộc tính --</option>';

    const sensor = (cachedSensors || []).find((s) => String(s.id) === String(sensorId));
    if (sensor && SENSOR_PROPERTIES[sensor.category]) {
      SENSOR_PROPERTIES[sensor.category].forEach((p) => {
        const opt = document.createElement('option');
        opt.value = p.value;
        opt.textContent = p.label;
        propSelect.appendChild(opt);
      });
    }
  };

  const open = async (localId = null) => {
    editingLocalId = localId;
    const form = getEl('conditionForm');
    form?.reset();

    const titleEl = getEl('conditionModalTitle');
    if (titleEl) {
      titleEl.textContent = localId ? 'Chỉnh sửa điều kiện cảm biến' : 'Thêm điều kiện cảm biến';
    }

    const currentRoomId = StateManager.getCurrentConfig()?.roomId;
    if (currentRoomId) {
      await loadRoomSensors(currentRoomId);
    }

    if (localId) {
      const cond = StateManager.getConditions().find((c) => c._localId === localId);
      if (cond) {
        if (getEl('condSourceCategory')) getEl('condSourceCategory').value = cond.sourceCategory || 'SENSOR';
        handleSourceCategoryChange(cond.sourceCategory || 'SENSOR');

        if (cond.sourceCategory === 'SENSOR') {
          if (getEl('condSensorId')) getEl('condSensorId').value = cond.sourceTargetId || '';
          handleSensorChange(cond.sourceTargetId);
        }

        if (getEl('condProperty')) getEl('condProperty').value = cond.property || '';
        if (getEl('condOperator')) getEl('condOperator').value = cond.operator || '<';
        if (getEl('condValue')) getEl('condValue').value = cond.value ?? '';
        if (getEl('condNextLogic')) getEl('condNextLogic').value = cond.nextLogic || 'AND';
      }
    } else {
      if (getEl('condSourceCategory')) getEl('condSourceCategory').value = 'SENSOR';
      handleSourceCategoryChange('SENSOR');
      if (getEl('condOperator')) getEl('condOperator').value = '<';
      if (getEl('condNextLogic')) getEl('condNextLogic').value = 'AND';
    }

    modalInstance?.show();
  };

  const handleSubmit = () => {
    const sourceCategory = getEl('condSourceCategory')?.value;
    const sensorSelect = getEl('condSensorId');
    const sensorId = sensorSelect?.value;
    const property = getEl('condProperty')?.value;
    const operator = getEl('condOperator')?.value;
    const value = getEl('condValue')?.value?.trim();
    const nextLogic = getEl('condNextLogic')?.value || 'AND';

    if (!sourceCategory || !property || !operator || value === '' || value === undefined) {
      alert('Vui lòng điền đầy đủ thông tin điều kiện!');
      return;
    }

    let sourceTargetType = null;
    let sourceTargetId = '';
    let targetDisplay = '';

    if (sourceCategory === 'SENSOR') {
      if (!sensorId) {
        alert('Vui lòng chọn cảm biến trong phòng!');
        return;
      }
      const selectedOpt = sensorSelect.options[sensorSelect.selectedIndex];
      sourceTargetType = selectedOpt?.dataset?.category || null;
      sourceTargetId = String(sensorId);
      targetDisplay = selectedOpt?.textContent || `Sensor #${sensorId}`;
    } else {
      sourceTargetId = 'SYSTEM';
      targetDisplay = 'Hệ thống';
    }

    const payload = {
      sourceCategory,
      sourceTargetId,
      sourceTargetType,
      targetDisplay,
      property,
      operator,
      value,
      nextLogic,
    };

    if (editingLocalId) {
      StateManager.updateCondition(editingLocalId, payload);
    } else {
      StateManager.addCondition(payload);
    }

    modalInstance?.hide();
  };

  return {
    init,
    open,
  };
})();
