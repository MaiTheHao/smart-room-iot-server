import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { getDevicesByRoom } from '../../../api/device.api.js';
import { getSensorsByRoom } from '../../../api/sensor-metadata.api.js';
import { Alert } from '../../../common/notification_util.js';
import {
  formatPropertyLabel,
  conditionValueToUtc,
  conditionValueFromUtc,
} from '../../../common/smart_system_util.js';
import {
  SYSTEM_PROPERTIES,
  ROOM_PROPERTIES,
  DEVICE_PROPERTIES,
  SENSOR_PROPERTIES,
  CONDITION_PARAMETER_CONFIG,
  DAY_OF_WEEK_OPTIONS,
} from '../../../constants/smart_system.constants.js';

const { roomId, i18n } = window.__ROOM_EVENT_CONDITIONS_CONFIG__;

export const ConditionModal = (() => {
  let bootstrapModal = null;
  let cachedDevices = null;
  let cachedSensors = null;
  const el = {};

  const init = () => {
    el.modal = document.getElementById('conditionModal');
    el.form = document.getElementById('conditionForm');
    el.title = document.getElementById('conditionModalTitle');
    el.localId = document.getElementById('conditionLocalId');
    el.targetType = document.getElementById('condTargetType');

    el.dataSource = document.getElementById('condDataSource');
    el.categoryWrap = document.getElementById('condCategoryWrap');
    el.category = document.getElementById('condCategory');

    el.targetWrap = document.getElementById('condTargetWrap');
    el.targetLabel = document.getElementById('condTargetLabel');
    el.target = document.getElementById('condTarget');

    el.propertyWrap = document.getElementById('condPropertyWrap');
    el.property = document.getElementById('condProperty');

    el.operator = document.getElementById('condOperator');
    el.value = document.getElementById('condValue');
    el.valueSelect = document.getElementById('condValueSelect');
    el.valueHour = document.getElementById('condValueHour');
    el.valueMinute = document.getElementById('condValueMinute');
    el.valueTimeGroup = document.getElementById('condValueTimeGroup');
    el.valueHelp = document.getElementById('condValueHelp');
    el.nextLogic = document.getElementById('condNextLogic');
    el.sortOrder = document.getElementById('condSortOrder');

    if (el.modal && typeof bootstrap !== 'undefined') {
      bootstrapModal = new bootstrap.Modal(el.modal);
    }

    bindEvents();
  };

  const bindEvents = () => {
    el.form?.addEventListener('submit', (e) => submit(e));
    el.dataSource?.addEventListener('change', () => onDataSourceChange());
    el.target?.addEventListener('change', () => onTargetChange());
    el.property?.addEventListener('change', () => onPropertyChange());
  };

  const extractItems = (res) => {
    if (Array.isArray(res?.data?.content)) return res.data.content;
    if (Array.isArray(res?.data)) return res.data;
    return [];
  };

  const fetchDevices = async () => {
    if (!cachedDevices) {
      const [err, res] = await getDevicesByRoom(roomId);
      if (err) {
        Alert.error(i18n.error, i18n.errorLoading || 'Failed to load devices');
        return [];
      }
      cachedDevices = extractItems(res);
    }
    return cachedDevices;
  };

  const fetchSensors = async () => {
    if (!cachedSensors) {
      const [err, res] = await getSensorsByRoom(roomId, undefined, 0, 100);
      if (err) {
        Alert.error(i18n.error, i18n.errorLoading || 'Failed to load sensors');
        return [];
      }
      cachedSensors = extractItems(res);
    }
    return cachedSensors;
  };

  const fetchTargets = async (ds) => {
    if (ds === 'DEVICE') return fetchDevices();
    if (ds === 'SENSOR') return fetchSensors();
    return [];
  };

  const setupFixedSource = (ds, properties, preservedData) => {
    el.categoryWrap.classList.add('d-none');
    el.targetWrap.classList.add('d-none');
    el.targetType.value = ds;
    renderProperties(properties);
    if (preservedData?.property) {
      el.property.value = preservedData.property;
    }
    onPropertyChange(preservedData?.value);
  };

  const setupDeviceOrSensorSource = async (ds, preservedData) => {
    el.targetWrap.classList.remove('d-none');
    el.targetLabel.textContent = ds === 'DEVICE' ? i18n.labelDevice : i18n.labelSensor;
    el.target.innerHTML = `<option value="">${i18n.loading || 'Loading...'}</option>`;

    const targets = await fetchTargets(ds);
    populateTargetDropdown(targets, ds);

    if (preservedData?.sourceTargetId) {
      el.target.value = String(preservedData.sourceTargetId);
    }
    onTargetChange(preservedData);
  };

  const onDataSourceChange = async (preservedData = null) => {
    const ds = el.dataSource.value;
    clearValidation();
    if (ds === 'SYSTEM') return setupFixedSource('SYSTEM', SYSTEM_PROPERTIES, preservedData);
    if (ds === 'ROOM') return setupFixedSource('ROOM', ROOM_PROPERTIES, preservedData);
    await setupDeviceOrSensorSource(ds, preservedData);
  };

  const populateTargetDropdown = (targets, ds) => {
    el.target.innerHTML = `<option value="">${i18n.selectTarget}</option>`;
    const items = Array.isArray(targets) ? targets : [];
    items.forEach((item) => {
      const opt = document.createElement('option');
      opt.value = item.id;
      const category = item.category || (ds === 'DEVICE' ? item.deviceCategory : item.sensorCategory) || '';
      opt.dataset.category = category;
      opt.textContent = `${item.name || item.naturalId || item.code || '#' + item.id} (${category || ds})`;
      el.target.appendChild(opt);
    });
  };

  const onTargetChange = (preservedData = null) => {
    const ds = el.dataSource.value;
    const selectedOpt = el.target.options[el.target.selectedIndex];
    const category = selectedOpt?.dataset?.category || '';
    el.targetType.value = category || ds;

    const props = ds === 'DEVICE' ? (DEVICE_PROPERTIES[category] || []) : (SENSOR_PROPERTIES[category] || []);
    renderProperties(props);

    if (preservedData?.property) {
      el.property.value = preservedData.property;
    }
    onPropertyChange(preservedData?.value);
  };

  const renderProperties = (props = []) => {
    el.property.innerHTML = `<option value="">${i18n.placeholderValue || 'Select property'}</option>`;
    (props || []).forEach((prop) => {
      const opt = document.createElement('option');
      opt.value = prop;
      opt.textContent = formatPropertyLabel(prop, i18n);
      el.property.appendChild(opt);
    });
    if (props && props.length > 0) {
      el.property.value = props[0];
    }
  };

  const resetValueControls = () => {
    el.value.classList.add('d-none');
    el.valueSelect.classList.add('d-none');
    el.valueTimeGroup.classList.add('d-none');
    el.valueHelp.textContent = '';
  };

  const onPropertyChange = (preservedValue = null) => {
    const prop = el.property.value;
    const ds = el.dataSource.value;
    const cat = el.targetType.value;
    resetValueControls();

    if (ds === 'SYSTEM') return handleSystemProperty(prop, preservedValue);
    if (ds === 'ROOM') return handleRoomProperty(preservedValue);
    handleDeviceOrSensorProperty(ds, cat, prop, preservedValue);
  };

  const handleSystemProperty = (prop, preservedValue) => {
    if (prop === 'current_time') {
      el.valueTimeGroup.classList.remove('d-none');
      if (preservedValue !== null && preservedValue !== undefined && preservedValue !== '') {
        const hhmm = conditionValueFromUtc('SYSTEM', prop, preservedValue);
        const [h, m] = hhmm.split(':');
        if (h !== undefined && m !== undefined) {
          el.valueHour.value = String(parseInt(h, 10));
          el.valueMinute.value = String(parseInt(m, 10));
        }
      }
      el.valueHelp.textContent = i18n.valTimeRange || 'Giờ hệ thống (Local time sẽ chuyển thành UTC)';
      return;
    }
    if (prop === 'day_of_week') {
      el.valueSelect.classList.remove('d-none');
      el.valueSelect.innerHTML = DAY_OF_WEEK_OPTIONS.map((opt) => `<option value="${opt.value}">${opt.label}</option>`).join('');
      if (preservedValue) el.valueSelect.value = String(preservedValue);
      return;
    }
    if (prop === 'day_of_month') {
      el.value.classList.remove('d-none');
      el.value.type = 'number';
      el.value.min = '1';
      el.value.max = '31';
      el.value.step = '1';
      el.value.placeholder = '1 - 31';
      el.value.value = preservedValue !== null && preservedValue !== undefined ? preservedValue : '1';
      el.valueHelp.textContent = 'Ngày trong tháng (1 - 31)';
      return;
    }
    el.value.classList.remove('d-none');
    el.value.type = 'text';
    el.value.value = preservedValue !== null && preservedValue !== undefined ? preservedValue : '';
  };

  const handleRoomProperty = (preservedValue) => {
    el.value.classList.remove('d-none');
    el.value.type = 'number';
    el.value.step = 'any';
    el.value.value = preservedValue !== null && preservedValue !== undefined ? preservedValue : '';
  };

  const renderEnumProperty = (options, preservedValue) => {
    el.valueSelect.classList.remove('d-none');
    el.valueSelect.innerHTML = options.map((opt) => `<option value="${opt}">${opt}</option>`).join('');
    if (preservedValue) el.valueSelect.value = String(preservedValue);
  };

  const renderNumericProperty = (config, preservedValue) => {
    el.value.classList.remove('d-none');
    el.value.type = 'number';
    el.value.step = config?.type === 'int' ? '1' : 'any';
    if (config?.min !== undefined) el.value.min = config.min;
    if (config?.max !== undefined) el.value.max = config.max;
    if (config?.placeholder) el.value.placeholder = config.placeholder;
    el.value.value = preservedValue !== null && preservedValue !== undefined ? preservedValue : '';
  };

  const handleDeviceOrSensorProperty = (ds, cat, prop, preservedValue) => {
    const config = CONDITION_PARAMETER_CONFIG[ds]?.[cat]?.[prop];
    if (config?.type === 'enum' && Array.isArray(config.options)) {
      renderEnumProperty(config.options, preservedValue);
      return;
    }
    renderNumericProperty(config, preservedValue);
  };

  const getComputedValue = () => {
    const ds = el.dataSource.value;
    const prop = el.property.value;
    if (ds === 'SYSTEM' && prop === 'current_time') {
      const hh = el.valueHour.value;
      const mm = el.valueMinute.value;
      return conditionValueToUtc(ds, prop, `${hh}:${mm}`);
    }
    if (!el.valueSelect.classList.contains('d-none')) {
      return el.valueSelect.value;
    }
    return el.value.value.trim();
  };

  const clearValidation = () => {
    [el.target, el.property, el.value, el.operator, el.sortOrder].forEach((input) => {
      if (input) input.classList.remove('is-invalid');
    });
  };

  const getValidationResult = () => {
    const ds = el.dataSource.value;
    const errors = {};
    if ((ds === 'DEVICE' || ds === 'SENSOR') && !el.target.value) {
      errors.target = true;
    }
    if (!el.property.value) {
      errors.property = true;
    }
    const val = getComputedValue();
    if (val === '' || val === undefined) {
      errors.value = true;
    }
    return { isValid: Object.keys(errors).length === 0, errors };
  };

  const applyValidationErrors = (errors = {}) => {
    clearValidation();
    if (errors.target) el.target.classList.add('is-invalid');
    if (errors.property) el.property.classList.add('is-invalid');
    if (errors.value) el.value.classList.add('is-invalid');
  };

  const populateEditCondition = async (existing) => {
    el.dataSource.value = existing.sourceCategory || existing.dataSource || 'SENSOR';
    el.operator.value = existing.operator || '=';
    el.sortOrder.value = existing.sortOrder !== undefined ? existing.sortOrder : 0;
    el.nextLogic.value = existing.nextLogic || 'AND';
    await onDataSourceChange(existing);
  };

  const populateNewCondition = async (order) => {
    el.dataSource.value = 'SENSOR';
    el.operator.value = '=';
    el.sortOrder.value = order;
    el.nextLogic.value = 'AND';
    await onDataSourceChange();
  };

  const open = async (localId = null) => {
    el.form.reset();
    clearValidation();
    const isEdit = Boolean(localId);
    el.localId.value = localId || '';
    el.title.textContent = isEdit ? (i18n.editTitle || 'Chỉnh sửa điều kiện') : (i18n.addTitle || 'Thêm điều kiện');
    const existing = isEdit ? StateManager.getCondition(localId) : null;
    if (isEdit && existing) {
      await populateEditCondition(existing);
    } else {
      await populateNewCondition(StateManager.getConditions().length);
    }
    bootstrapModal?.show();
  };

  const resolveTargetId = (ds) => {
    if (ds === 'ROOM') return String(roomId);
    if (ds === 'SYSTEM') return '0';
    return el.target.value;
  };

  const buildConditionPayload = () => ({
    sourceCategory: el.dataSource.value,
    sourceTargetId: resolveTargetId(el.dataSource.value),
    sourceTargetType: el.targetType.value || el.dataSource.value,
    property: el.property.value,
    operator: el.operator.value,
    value: getComputedValue(),
    nextLogic: el.nextLogic.value || 'AND',
    sortOrder: parseInt(el.sortOrder.value, 10) || 0,
  });

  const submit = (e) => {
    e.preventDefault();
    const validation = getValidationResult();
    applyValidationErrors(validation.errors);
    if (!validation.isValid) return;

    const localId = el.localId.value;
    const payload = buildConditionPayload();
    if (localId) {
      StateManager.updateCondition(localId, payload);
    } else {
      StateManager.addCondition(payload);
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
