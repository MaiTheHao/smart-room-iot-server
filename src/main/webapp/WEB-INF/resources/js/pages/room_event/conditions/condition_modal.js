import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { getDevicesByRoom } from '../../../api/device.api.js';
import { getSensorsByRoom } from '../../../api/sensor-metadata.api.js';
import { Alert } from '../../../common/notification_util.js';
import { Validator } from '../../../common/validator.js';
import { CreateConditionDto } from '../../../types/rule.domain.js';
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
  SENSOR_CATEGORY_LABEL_KEYS,
  SENSOR_CATEGORY_FALLBACKS,
  CONDITION_PARAMETER_CONFIG,
  DAY_OF_WEEK_OPTIONS,
} from '../../../constants/smart_system.constants.js';

const { roomId, i18n } = window.__ROOM_EVENT_CONDITIONS_CONFIG__;

const DEFAULT_PAGE = 0;
const TARGET_FETCH_SIZE = 100;

const toPropOptions = (keys) => keys.map((value) => ({ value }));
const toCategoryMap = (map) =>
  Object.fromEntries(Object.entries(map).map(([cat, keys]) => [cat, toPropOptions(keys)]));

const DATA_SOURCE_CONFIG = {
  SYSTEM: {
    needsTarget: false,
    properties: toPropOptions(SYSTEM_PROPERTIES),
  },
  ROOM: {
    needsTarget: false,
    properties: toPropOptions(ROOM_PROPERTIES),
  },
  DEVICE: {
    needsTarget: true,
    targetLabel: i18n.labelDevice,
    categories: toCategoryMap(DEVICE_PROPERTIES),
  },
  SENSOR: {
    needsTarget: true,
    targetLabel: i18n.labelSensor,
    categories: toCategoryMap(SENSOR_PROPERTIES),
  },
};

export const ConditionModal = (() => {
  let bootstrapModal = null;
  const el = {};

  const init = () => {
    el.modal = document.getElementById('conditionModal');
    el.form = document.getElementById('conditionForm');
    el.title = document.getElementById('conditionModalTitle');
    el.localId = document.getElementById('conditionLocalId');

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
    el.nextLogicWrap = document.getElementById('condNextLogicWrap');
    el.sortOrder = document.getElementById('condSortOrder');

    if (el.modal && typeof bootstrap !== 'undefined') {
      bootstrapModal = new bootstrap.Modal(el.modal);
    }

    el.sortOrder?.addEventListener('blur', () => {
      let v = parseInt(el.sortOrder.value, 10);
      if (isNaN(v) || v < 0) el.sortOrder.value = 0;
    });

    const handleInput = (input) => {
      input.value = input.value.replace(/[^0-9]/g, '');
      if (input.value.length > 2) {
        input.value = input.value.slice(0, 2);
      }
    };
    const padAndClamp = (input, min, max) => {
      let val = parseInt(input.value, 10);
      if (isNaN(val)) {
        input.value = '00';
      } else {
        val = Math.min(Math.max(val, min), max);
        input.value = val.toString().padStart(2, '0');
      }
    };

    el.valueHour?.addEventListener('input', () => handleInput(el.valueHour));
    el.valueMinute?.addEventListener('input', () => handleInput(el.valueMinute));
    el.valueHour?.addEventListener('blur', () => padAndClamp(el.valueHour, 0, 23));
    el.valueMinute?.addEventListener('blur', () => padAndClamp(el.valueMinute, 0, 59));

    bindEvents();
  };

  const bindEvents = () => {
    el.form?.addEventListener('submit', (e) => submit(e));
    el.dataSource?.addEventListener('change', () => onDataSourceChange());
    el.category?.addEventListener('change', () => onCategoryChange());
    el.property?.addEventListener('change', () => onPropertyChange());
  };

  const getCatLabel = (k) => {
    const val = i18n[SENSOR_CATEGORY_LABEL_KEYS[k]];
    if (val && !val.startsWith('??')) return val;
    return SENSOR_CATEGORY_FALLBACKS[k] || k;
  };

  const onDataSourceChange = async (preservedData = null) => {
    const ds = el.dataSource.value;
    const cfg = DATA_SOURCE_CONFIG[ds];
    if (!cfg) return;

    const hasCat = !!cfg.categories;
    el.categoryWrap.classList.toggle('d-none', !hasCat);
    if (hasCat) {
      const cats = Object.keys(cfg.categories);
      el.category.innerHTML = cats.map((k) => `<option value="${k}">${getCatLabel(k)}</option>`).join('');
      if (preservedData?.sourceTargetType) {
        el.category.value = preservedData.sourceTargetType;
      }
    }

    el.targetWrap.classList.toggle('d-none', !cfg.needsTarget);
    el.target.disabled = true;
    el.target.innerHTML = '';

    if (cfg.properties) {
      populateProperties(cfg.properties);
      el.propertyWrap.classList.remove('d-none');
      if (preservedData?.property) {
        el.property.value = preservedData.property;
      }
      onPropertyChange(preservedData?.value);
    } else {
      el.property.innerHTML = '';
      el.propertyWrap.classList.add('d-none');
    }

    if (hasCat) {
      await onCategoryChange(preservedData);
    }
  };

  const onCategoryChange = async (preservedData = null) => {
    const ds = el.dataSource.value;
    const cat = el.category.value;
    const cfg = DATA_SOURCE_CONFIG[ds];

    const props = cfg?.categories?.[cat];
    if (props) {
      populateProperties(props);
      el.propertyWrap.classList.remove('d-none');
      if (preservedData?.property) {
        el.property.value = preservedData.property;
      }
      onPropertyChange(preservedData?.value);
    }

    if (cfg?.needsTarget) {
      await loadTargets(ds, cat, preservedData?.sourceTargetId);
    }
  };

  const loadTargets = async (ds, category, selectedId = null) => {
    if (!category) return;

    el.target.disabled = true;
    el.target.innerHTML = `<option value="" disabled selected>${i18n.loading || 'Loading...'}</option>`;
    el.targetWrap.classList.remove('d-none');

    if (el.targetLabel) {
      el.targetLabel.textContent = DATA_SOURCE_CONFIG[ds]?.targetLabel || 'Target';
    }

    let items = [];
    if (ds === 'DEVICE') {
      const [err, res] = await getDevicesByRoom(roomId, category);
      if (err) {
        Alert.error(i18n.error, i18n.errorLoading || 'Failed to load devices');
      } else {
        items = extractItems(res);
      }
    } else if (ds === 'SENSOR') {
      const [err, res] = await getSensorsByRoom(roomId, category, DEFAULT_PAGE, TARGET_FETCH_SIZE);
      if (err) {
        Alert.error(i18n.error, i18n.errorLoading || 'Failed to load sensors');
      } else {
        items = extractItems(res);
      }
    }

    if (items.length === 0) {
      el.target.innerHTML = `<option value="" disabled selected>${i18n.noTargets || 'No targets found'}</option>`;
      return;
    }

    el.target.innerHTML = `<option value="" disabled selected>${i18n.selectTarget}</option>`;
    items.forEach((item) => {
      const opt = document.createElement('option');
      opt.value = item.id;
      opt.textContent = `${item.name || item.naturalId || item.code || '#' + item.id} (${category})`;
      if (selectedId && String(item.id) === String(selectedId)) opt.selected = true;
      el.target.appendChild(opt);
    });
    el.target.disabled = false;
  };

  const extractItems = (res) => {
    if (Array.isArray(res?.data?.content)) return res.data.content;
    if (Array.isArray(res?.data)) return res.data;
    return [];
  };

  const populateProperties = (props = []) => {
    el.property.innerHTML = (props || [])
      .map((p) => `<option value="${p.value ?? p}">${formatPropertyLabel(p.value ?? p, i18n)}</option>`)
      .join('');
  };

  const resetValueControls = () => {
    el.value.classList.add('d-none');
    el.valueSelect.classList.add('d-none');
    el.valueTimeGroup.classList.add('d-none');
    el.valueHelp.classList.add('d-none');
    el.valueHelp.textContent = '';
  };

  const onPropertyChange = (preservedValue = null) => {
    const prop = el.property.value;
    const ds = el.dataSource.value;
    const cat = el.category.value;
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
        const [h, m] = hhmm.split(':').map(Number);
        if (!Number.isNaN(h) && !Number.isNaN(m)) {
          el.valueHour.value = String(h).padStart(2, '0');
          el.valueMinute.value = String(m).padStart(2, '0');
        }
      }
      el.valueHelp.textContent = i18n.valTimeRange || 'Giờ hệ thống (Local time sẽ chuyển thành UTC)';
      el.valueHelp.classList.remove('d-none');
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
      el.valueHelp.classList.remove('d-none');
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

  const getValue = () => {
    const ds = el.dataSource.value;
    const prop = el.property.value;
    if (ds === 'SYSTEM' && prop === 'current_time') {
      const h = parseInt(el.valueHour.value, 10);
      const m = parseInt(el.valueMinute.value, 10);
      if (isNaN(h) || isNaN(m)) return '';
      const localVal = h + m / 60.0;
      return localVal.toFixed(2);
    }
    if (!el.valueSelect.classList.contains('d-none')) {
      return el.valueSelect.value;
    }
    return el.value.value;
  };

  const populateEditCondition = async (existing) => {
    el.dataSource.value = existing.sourceCategory || existing.dataSource || 'SYSTEM';
    el.operator.value = existing.operator || '=';
    el.sortOrder.value = existing.sortOrder !== undefined ? existing.sortOrder : 0;
    const nl = existing.nextLogic || 'AND';
    const radio = document.querySelector(`input[name="condNextLogicRadio"][value="${nl}"]`);
    if (radio) radio.checked = true;
    await onDataSourceChange(existing);
  };

  const populateNewCondition = async (order) => {
    el.dataSource.value = 'SYSTEM';
    el.operator.value = '=';
    el.sortOrder.value = order;
    const radio = document.querySelector('input[name="condNextLogicRadio"][value="AND"]');
    if (radio) radio.checked = true;
    await onDataSourceChange();
  };

  const open = async (localId = null) => {
    el.form.reset();
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
    window.renderIcons?.();
  };

  const submit = async (e) => {
    e.preventDefault();

    const localId = el.localId.value;
    const ds = el.dataSource.value;
    const prop = el.property.value;
    const cat = (ds === 'DEVICE' || ds === 'SENSOR') ? el.category.value : ds;

    let sourceTargetId = '0';
    if (ds === 'DEVICE' || ds === 'SENSOR') {
      sourceTargetId = String(el.target.value || '');
    } else if (ds === 'ROOM') {
      sourceTargetId = String(roomId);
    }

    const val = getValue().trim();

    const builder = new CreateConditionDto.Builder()
      .setSourceCategory(ds)
      .setSourceTargetId(sourceTargetId)
      .setSourceTargetType(cat)
      .setProperty(prop)
      .setOperator(el.operator.value)
      .setValue(val)
      .setSortOrder(el.sortOrder.value);

    const result = builder.validate();
    if (!result.isValid) {
      const firstField = Object.keys(result.errors)[0];
      const msgKey = result.errors[firstField];
      const fieldLabel = ({
        sourceCategory: i18n.colDataSource,
        sourceTargetId: (ds === 'DEVICE' || ds === 'SENSOR') ? (el.targetLabel.textContent || 'Target') : i18n.colDataSource,
        property: i18n.colProperty,
        operator: i18n.colOperator,
        value: i18n.colValue,
        sortOrder: i18n.colOrder,
      })[firstField] || '';
      await Alert.warning((i18n[msgKey] || i18n.valRequired || 'Validation failed').replace('{0}', fieldLabel), i18n.error || 'Error');
      const FIELD_ID_MAP = {
        sourceCategory: el.dataSource,
        sourceTargetId: el.target,
        property: el.property,
        operator: el.operator,
        value: el.value,
        sortOrder: el.sortOrder,
      };
      FIELD_ID_MAP[firstField]?.focus();
      return;
    }

    if ((ds === 'DEVICE' || ds === 'SENSOR') && !el.target.value) {
      await Alert.warning(i18n.valTargetRequired || 'Target is required', i18n.error || 'Error');
      el.target?.focus();
      return;
    }

    if (ds === 'DEVICE' || ds === 'SENSOR') {
      const config = CONDITION_PARAMETER_CONFIG[ds]?.[cat]?.[prop];
      if (config) {
        const propLabel = formatPropertyLabel(prop, i18n);

        if (config.type === 'enum') {
          const categoryValidators = Validator[cat];
          const validator = categoryValidators ? categoryValidators[prop] : null;
          if (validator && !validator.isValidFormat(val)) {
            await Alert.warning(`Invalid value for ${propLabel}`, i18n.error || 'Error');
            return;
          }
        } else if (config.type === 'int') {
          const categoryValidators = Validator[cat];
          const validator = categoryValidators ? categoryValidators[prop] : null;
          const isValid = validator
            ? validator.isValidFormat(val)
            : (!isNaN(parseInt(val, 10)) && parseInt(val, 10) >= config.min && parseInt(val, 10) <= config.max);

          if (!isValid) {
            await Alert.warning(`${propLabel}: Must be between ${config.min} and ${config.max}`, i18n.error || 'Error');
            el.value?.focus();
            return;
          }
        } else if (config.type === 'float') {
          const num = parseFloat(val);
          if (isNaN(num)) {
            await Alert.warning(`${propLabel}: Must be a valid float number`, i18n.error || 'Error');
            el.value?.focus();
            return;
          }
        }
      }
    }

    if (ds === 'SYSTEM') {
      if (prop === 'current_time') {
        const h = parseInt(el.valueHour.value, 10);
        const m = parseInt(el.valueMinute.value, 10);
        if (isNaN(h) || h < 0 || h > 23 || isNaN(m) || m < 0 || m > 59) {
          await Alert.warning(i18n.valTimeRange || 'Hour must be 0-23 and Minute must be 0-59', i18n.error || 'Error');
          el.valueHour?.focus();
          return;
        }
      } else if (prop === 'day_of_month') {
        const num = parseInt(val, 10);
        if (isNaN(num) || num < 1 || num > 31 || String(num) !== val) {
          await Alert.warning(i18n.valDayMonthRange || 'Must be an integer between 1 and 31', i18n.error || 'Error');
          el.value?.focus();
          return;
        }
      }
    }

    let finalValue = val;
    if (ds === 'SYSTEM' && prop === 'current_time') {
      const h = el.valueHour.value;
      const m = el.valueMinute.value;
      finalValue = conditionValueToUtc(ds, prop, `${h}:${m}`);
    }

    let orderVal = parseInt(el.sortOrder.value, 10);
    if (isNaN(orderVal) || orderVal < 0) {
      await Alert.warning('Order must be a positive integer', i18n.error || 'Error');
      el.sortOrder?.focus();
      return;
    }

    const nextLogic = document.querySelector('input[name="condNextLogicRadio"]:checked')?.value || 'AND';

    const data = {
      sourceCategory:   ds,
      sourceTargetId:   sourceTargetId,
      sourceTargetType: cat,
      property:         prop,
      operator:         el.operator.value,
      value:            finalValue,
      sortOrder:        orderVal,
      nextLogic:        nextLogic,
    };

    if (localId) {
      StateManager.updateCondition(localId, data);
    } else {
      StateManager.addCondition(data);
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
