import { StateManager } from './state_manager.js';
import { getSensorsByRoom } from '../../api/sensor-metadata.api.js';
import { Alert } from '../../common/notification_util.js';
import { SENSOR_PROPERTIES, SYSTEM_PROPERTIES } from '../../constants/smart_system.constants.js';
import { conditionValueToUtc, conditionValueFromUtc } from '../../common/smart_system_util.js';

const PROPERTY_LABELS_VI = {
  temperature: 'Nhiệt độ (°C)',
  watt: 'Công suất (W)',
  humidity: 'Độ ẩm (%)',
  lux: 'Cường độ sáng (Lux)',
  co2: 'Nồng độ CO₂ (ppm)',
  current_time: 'Thời gian trong ngày (HH:mm)',
  day_of_week: 'Ngày trong tuần (1-7)',
  day_of_month: 'Ngày trong tháng (1-31)',
  power: 'Nguồn',
  level: 'Độ sáng',
  temp: 'Nhiệt độ',
  mode: 'Chế độ',
  fan_speed: 'Tốc độ quạt',
  swing: 'Đảo gió',
  speed: 'Tốc độ',
  light: 'Đèn quạt',
};

const labelOf = (key) => PROPERTY_LABELS_VI[key] || key;

export const ConditionModal = (() => {
  let modalInstance = null;
  let cachedSensors = null;
  let editingLocalId = null;

  const getEl = (id) => document.getElementById(id);

  const init = () => {
    const modalEl = getEl('roomEventConditionModal');
    if (!modalEl) return;
    modalInstance = new bootstrap.Modal(modalEl);

    const form = getEl('conditionForm');
    form?.addEventListener('submit', (e) => {
      e.preventDefault();
      handleSubmit();
    });

    getEl('condSourceCategory')?.addEventListener('change', (e) => {
      handleSourceCategoryChange(e.target.value);
    });

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

  const populatePropertyOptions = (keys, selected = '') => {
    const propSelect = getEl('condProperty');
    if (!propSelect) return;
    propSelect.innerHTML = '<option value="" disabled selected>-- Chọn thuộc tính --</option>';
    (keys || []).forEach((key) => {
      const opt = document.createElement('option');
      opt.value = key;
      opt.textContent = labelOf(key);
      if (selected && selected === key) opt.selected = true;
      propSelect.appendChild(opt);
    });
  };

  const handleSourceCategoryChange = (cat) => {
    const sensorGroup = getEl('condSensorGroup');

    if (cat === 'SYSTEM') {
      sensorGroup?.classList.add('d-none');
      populatePropertyOptions(SYSTEM_PROPERTIES);
    } else {
      sensorGroup?.classList.remove('d-none');
      populateSensorSelect();

      populatePropertyOptions([]);
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
    const sensor = (cachedSensors || []).find((s) => String(s.id) === String(sensorId));
    const keys = sensor ? SENSOR_PROPERTIES[sensor.category] : [];
    populatePropertyOptions(keys || []);
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
        const sourceCategory = cond.sourceCategory || 'SENSOR';
        if (getEl('condSourceCategory')) getEl('condSourceCategory').value = sourceCategory;

        if (sourceCategory === 'SENSOR') {
          handleSourceCategoryChange('SENSOR');
          if (getEl('condSensorId')) getEl('condSensorId').value = cond.sourceTargetId || '';
          handleSensorChange(cond.sourceTargetId);
        } else {
          handleSourceCategoryChange('SYSTEM');
        }

        if (getEl('condProperty')) getEl('condProperty').value = cond.property || '';
        if (getEl('condOperator')) getEl('condOperator').value = cond.operator || '<';
        if (getEl('condValue')) {
          getEl('condValue').value = conditionValueFromUtc(sourceCategory, cond.property, cond.value ?? '');
        }
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

  const handleSubmit = async () => {
    const sourceCategory = getEl('condSourceCategory')?.value;
    const sensorSelect = getEl('condSensorId');
    const sensorId = sensorSelect?.value;
    const property = getEl('condProperty')?.value;
    const operator = getEl('condOperator')?.value;
    const rawValue = getEl('condValue')?.value?.trim();
    const nextLogic = getEl('condNextLogic')?.value || 'AND';

    if (!sourceCategory || !property || !operator || rawValue === '' || rawValue === undefined) {
      await Alert.warning('Vui lòng điền đầy đủ thông tin điều kiện!', 'Thiếu thông tin');
      return;
    }

    let sourceTargetType = null;
    let sourceTargetId = '';
    let targetDisplay = '';

    if (sourceCategory === 'SENSOR') {
      if (!sensorId) {
        await Alert.warning('Vui lòng chọn cảm biến trong phòng!', 'Thiếu thông tin');
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

    const value = conditionValueToUtc(sourceCategory, property, rawValue);

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
