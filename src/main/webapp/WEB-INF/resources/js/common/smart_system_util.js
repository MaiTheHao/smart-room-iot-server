import { UTCUtils } from './utc_util.js';
import {
  PROPERTY_LABEL_CONFIG,
  DEVICE_CAPABILITIES,
  ACTION_PARAM_SCHEMA,
  ACTION_FIELD_LABELS,
  SENSOR_CATEGORY_LABEL_KEYS,
  SENSOR_CATEGORY_FALLBACKS,
  DAY_OF_WEEK_MAP,
} from '../constants/smart_system.constants.js';

export const formatDayOfWeek = (val) => {
  const name = DAY_OF_WEEK_MAP[val];
  return name ? `${val} (${name})` : String(val ?? '');
};

export const formatSensorCategoryLabel = (category, i18n = {}) => {
  const key = SENSOR_CATEGORY_LABEL_KEYS[category];
  if (key && i18n && i18n[key] && !String(i18n[key]).startsWith('??')) {
    return i18n[key];
  }
  return SENSOR_CATEGORY_FALLBACKS[category] || category;
};

export const formatPropertyLabel = (propKey, i18n = {}) => {
  const cfg = PROPERTY_LABEL_CONFIG[propKey];
  if (!cfg) return propKey;
  const val = i18n ? i18n[cfg.i18nKey] : undefined;
  if (val && !String(val).startsWith('??')) return val;
  return cfg.fallback;
};

export const getAllowedActionParamKeys = (category, specificType) => {
  const caps = DEVICE_CAPABILITIES[category];
  if (!caps) return [];
  return specificType && caps[specificType] ? caps[specificType] : [];
};

const parseHourMinute = (value) => {
  if (value === null || value === undefined || value === '') {
    return { valid: false, hour: 0, minute: 0 };
  }
  if (typeof value === 'number') {
    const hour = Math.floor(value);
    const minute = Math.round((value - hour) * 60);
    return { valid: true, hour, minute };
  }
  const str = String(value).trim();
  const match = str.match(/^(\d{1,2})\s*[:hH]\s*(\d{1,2})$/);
  if (match) {
    return { valid: true, hour: parseInt(match[1], 10), minute: parseInt(match[2], 10) };
  }
  const num = parseFloat(str);
  if (!Number.isNaN(num)) {
    const hour = Math.floor(num);
    const minute = Math.round((num - hour) * 60);
    return { valid: true, hour, minute };
  }
  return { valid: false, hour: 0, minute: 0 };
};

const clamp = (val, min, max) => Math.min(Math.max(val, min), max);

export const conditionValueToUtc = (sourceCategory, property, localValue) => {
  if (sourceCategory !== 'SYSTEM' || property !== 'current_time') return localValue;
  const hm = parseHourMinute(localValue);
  if (!hm.valid) return localValue;
  const utc = UTCUtils.localToUTC(clamp(hm.hour, 0, 23), clamp(hm.minute, 0, 59), 0);
  const num = utc.hour + utc.minute / 60;
  return String(parseFloat(num.toFixed(2)));
};

export const conditionValueFromUtc = (sourceCategory, property, utcValue) => {
  if (sourceCategory !== 'SYSTEM' || property !== 'current_time') return utcValue;
  const num = parseFloat(utcValue);
  if (Number.isNaN(num)) return utcValue;
  const utcHour = Math.floor(num);
  const utcMinute = Math.round((num - utcHour) * 60);
  const local = UTCUtils.utcToLocal(utcHour, utcMinute, 0);
  return `${String(local.hour).padStart(2, '0')}:${String(local.minute).padStart(2, '0')}`;
};

export const tryParseJson = (value, fallback = {}) => {
  if (typeof value !== 'string') return value || fallback;
  try {
    return JSON.parse(value);
  } catch {
    return fallback;
  }
};

export const mapConditionsForReplace = (conditions = []) =>
  conditions.map((c, i) => ({
    id: c.id != null ? c.id : undefined,
    sourceCategory: c.sourceCategory,
    sourceTargetId: String(c.sourceTargetId != null ? c.sourceTargetId : ''),
    sourceTargetType: c.sourceTargetType ?? null,
    property: c.property,
    operator: c.operator,
    value: String(c.value),
    extraParams: c.extraParams || '',
    sortOrder: i,
    nextLogic: i < conditions.length - 1 ? c.nextLogic || 'AND' : '',
  }));

export const mapActionsForReplace = (actions = []) =>
  actions.map((a, i) => ({
    id: a.id != null ? a.id : undefined,
    targetCategory: a.targetCategory || a.targetDeviceCategory,
    targetId: String(a.targetId != null ? a.targetId : a.targetDeviceId),
    params: tryParseJson(a.params || a.actionParams, {}),
    executionOrder: i,
  }));

const createEnumControl = (key, schema, currentVal, defaultLabel) => {
  const select = document.createElement('select');
  select.className = 'form-select bg-light border-0';
  select.name = `param_${key}`;
  select.dataset.paramKey = key;

  if (defaultLabel) {
    const defaultOpt = document.createElement('option');
    defaultOpt.value = '';
    defaultOpt.textContent = `— ${defaultLabel} —`;
    select.appendChild(defaultOpt);
  }

  (schema.options || []).forEach((optVal) => {
    const opt = document.createElement('option');
    opt.value = optVal;
    opt.textContent = optVal;
    if (String(currentVal) === String(optVal)) opt.selected = true;
    select.appendChild(opt);
  });
  return select;
};

const createNumericControl = (key, schema, currentVal) => {
  const wrap = document.createDocumentFragment();
  const input = document.createElement('input');
  input.type = 'number';
  input.className = 'form-control bg-light border-0 font-monospace';
  input.name = `param_${key}`;
  input.dataset.paramKey = key;
  input.placeholder = schema.placeholder || '';
  if (schema.min !== undefined) input.min = schema.min;
  if (schema.max !== undefined) input.max = schema.max;
  if (currentVal !== '' && currentVal !== undefined && currentVal !== null) {
    input.value = currentVal;
  }
  const feedback = document.createElement('div');
  feedback.className = 'invalid-feedback';
  feedback.textContent = `Giá trị phải trong khoảng ${schema.min} – ${schema.max}`;
  wrap.appendChild(input);
  wrap.appendChild(feedback);
  return wrap;
};

const createParamField = ({ key, schema, values, i18n, isOptional }) => {
  const col = document.createElement('div');
  col.className = 'col-12 col-md-6 mb-2';
  const label = document.createElement('label');
  label.className = 'form-label fw-semibold small text-muted text-uppercase mb-1';
  label.textContent = (i18n && i18n[schema.labelKey]) || ACTION_FIELD_LABELS[key] || schema.labelKey;
  col.appendChild(label);

  const currentVal = values && values[key] !== undefined ? values[key] : '';
  if (schema.type === 'enum') {
    const defaultLabel = isOptional ? (i18n.unchanged || 'Mặc định') : '';
    col.appendChild(createEnumControl(key, schema, currentVal, defaultLabel));
  } else {
    col.appendChild(createNumericControl(key, schema, currentVal));
  }
  return col;
};

const renderParamFieldsInternal = ({ container, category, values = {}, allowedKeys = [], i18n = {}, isOptional }) => {
  if (!container) return;
  container.innerHTML = '';
  const schemaMap = ACTION_PARAM_SCHEMA[category];
  if (!schemaMap) {
    container.innerHTML = `<div class="col-12 text-muted small py-2">${i18n.noExtraParams || 'Không có tham số mở rộng.'}</div>`;
    return;
  }
  const allowed = Array.isArray(allowedKeys) && allowedKeys.length > 0 ? allowedKeys : [];
  Object.entries(schemaMap).forEach(([key, schema]) => {
    if (allowed.length > 0 && !allowed.includes(key)) return;
    container.appendChild(createParamField({ key, schema, values, i18n, isOptional }));
  });
};

export const renderActionParamFields = (options = {}) => {
  renderParamFieldsInternal({ ...options, isOptional: true });
};

export const renderRequiredActionParamFields = (options = {}) => {
  renderParamFieldsInternal({ ...options, isOptional: false });
};

const extractFieldValue = (el, schema) => {
  const val = el.value.trim();
  if (val === '') return undefined;
  if (schema.type === 'int') {
    const num = parseInt(val, 10);
    return Number.isNaN(num) ? undefined : num;
  }
  if (schema.type === 'float') {
    const num = parseFloat(val);
    return Number.isNaN(num) ? undefined : num;
  }
  return val;
};

export const collectActionParamsFromContainer = (container, category) => {
  if (!container) return {};
  const schemaMap = ACTION_PARAM_SCHEMA[category] || {};
  const params = {};
  Object.entries(schemaMap).forEach(([key, schema]) => {
    const el = container.querySelector(`[data-param-key="${key}"]`);
    if (!el) return;
    const parsed = extractFieldValue(el, schema);
    if (parsed !== undefined) params[key] = parsed;
  });
  return params;
};

const validateFieldParam = (key, schema, val) => {
  if (val === undefined || val === null || val === '') return '';
  if (schema.type === 'enum' && !schema.options.includes(val)) {
    return `Giá trị ${key} không hợp lệ.`;
  }
  if (schema.type === 'int' || schema.type === 'float') {
    const num = Number(val);
    if (Number.isNaN(num)) return `${key} phải là số.`;
    if (schema.min !== undefined && num < schema.min) return `${key} phải >= ${schema.min}.`;
    if (schema.max !== undefined && num > schema.max) return `${key} phải <= ${schema.max}.`;
  }
  return '';
};

export const validateActionParams = ({ category, params = {} }) => {
  const schemaMap = ACTION_PARAM_SCHEMA[category];
  if (!schemaMap) return { isValid: true, errors: {} };
  const errors = {};
  Object.entries(schemaMap).forEach(([key, schema]) => {
    const err = validateFieldParam(key, schema, params[key]);
    if (err) errors[key] = err;
  });
  return { isValid: Object.keys(errors).length === 0, errors };
};

export const createOrderedItemStateManager = ({ orderKey = 'sortOrder', mapPayload, itemAlias = 'Item' }) => {
  let currentItems = [];
  let isDirty = false;
  const listeners = [];

  const generateLocalId = () => 'local_' + Math.random().toString(36).substring(2, 11);

  const reindex = () => {
    currentItems.forEach((item, idx) => {
      item[orderKey] = idx;
    });
  };

  const triggerListeners = () => listeners.forEach((fn) => fn(isDirty));

  const init = (itemsFromApi) => {
    currentItems = (itemsFromApi || [])
      .slice()
      .sort((a, b) => (a[orderKey] ?? 0) - (b[orderKey] ?? 0))
      .map((item) => ({ ...item, _localId: generateLocalId() }));
    isDirty = false;
    triggerListeners();
  };

  const getItems = () => [...currentItems];
  const getItem = (localId) => currentItems.find((item) => item._localId === localId);

  const addItem = (item) => {
    const newItem = { ...item, _localId: generateLocalId() };
    currentItems.push(newItem);
    currentItems.sort((a, b) => (a[orderKey] ?? 0) - (b[orderKey] ?? 0));
    reindex();
    isDirty = true;
    triggerListeners();
    return newItem;
  };

  const updateItem = (localId, updatedData) => {
    const idx = currentItems.findIndex((item) => item._localId === localId);
    if (idx > -1) {
      currentItems[idx] = { ...currentItems[idx], ...updatedData };
      currentItems.sort((a, b) => (a[orderKey] ?? 0) - (b[orderKey] ?? 0));
      reindex();
      isDirty = true;
      triggerListeners();
    }
  };

  const deleteItem = (localId) => {
    currentItems = currentItems.filter((item) => item._localId !== localId);
    reindex();
    isDirty = true;
    triggerListeners();
  };

  const buildPayload = () => (typeof mapPayload === 'function' ? mapPayload(currentItems) : currentItems);
  const getIsDirty = () => isDirty;
  const subscribe = (fn) => {
    listeners.push(fn);
    return () => {
      const i = listeners.indexOf(fn);
      if (i > -1) listeners.splice(i, 1);
    };
  };

  const manager = {
    init,
    getItems,
    getItem,
    addItem,
    updateItem,
    deleteItem,
    buildPayload,
    getIsDirty,
    subscribe,
  };

  if (itemAlias) {
    manager[`get${itemAlias}s`] = getItems;
    manager[`get${itemAlias}`] = getItem;
    manager[`add${itemAlias}`] = addItem;
    manager[`update${itemAlias}`] = updateItem;
    manager[`delete${itemAlias}`] = deleteItem;
  }

  return manager;
};
