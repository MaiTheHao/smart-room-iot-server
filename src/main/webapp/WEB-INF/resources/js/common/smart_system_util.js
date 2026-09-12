import { UTCUtils } from './utc_util.js';
import { PROPERTY_LABEL_CONFIG, DEVICE_CAPABILITIES, ACTION_PARAM_SCHEMA, ACTION_FIELD_LABELS } from '../constants/smart_system.constants.js';

export const formatPropertyLabel = (propKey, i18n = {}) => {
  const cfg = PROPERTY_LABEL_CONFIG[propKey];
  if (!cfg) return propKey;
  const val = i18n ? i18n[cfg.i18nKey] : undefined;
  if (val && !String(val).startsWith('??')) return val;
  return cfg.fallback;
};

export const getAllowedActionParamKeys = (category, specificType) => {
  const caps = DEVICE_CAPABILITIES[category];
  if (!caps) return null;
  return specificType && caps[specificType] ? caps[specificType] : null;
};

const parseHourMinute = (value) => {
  if (value === null || value === undefined || value === '') return null;

  if (typeof value === 'number') {
    const hour = Math.floor(value);
    const minute = Math.round((value - hour) * 60);
    return { hour, minute };
  }

  const str = String(value).trim();
  const match = str.match(/^(\d{1,2})\s*[:hH]\s*(\d{1,2})$/);
  if (match) {
    return { hour: parseInt(match[1], 10), minute: parseInt(match[2], 10) };
  }

  const num = parseFloat(str);
  if (!Number.isNaN(num)) {
    const hour = Math.floor(num);
    const minute = Math.round((num - hour) * 60);
    return { hour, minute };
  }
  return null;
};

const clamp = (val, min, max) => Math.min(Math.max(val, min), max);

export const conditionValueToUtc = (sourceCategory, property, localValue) => {
  if (sourceCategory !== 'SYSTEM' || property !== 'current_time') return localValue;

  const hm = parseHourMinute(localValue);
  if (!hm) return localValue;

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

export const mapConditionsForReplace = (conditions = []) =>
  conditions.map((c, i) => ({
    id: c.id != null ? c.id : undefined,
    sourceCategory: c.sourceCategory,
    sourceTargetId: String(c.sourceTargetId != null ? c.sourceTargetId : ''),
    sourceTargetType: c.sourceTargetType || null,
    property: c.property,
    operator: c.operator,
    value: String(c.value),
    extraParams: c.extraParams || null,
    sortOrder: i,
    nextLogic: i < conditions.length - 1 ? c.nextLogic || 'AND' : null,
  }));

export const mapActionsForReplace = (actions = []) =>
  actions.map((a, i) => ({
    id: a.id != null ? a.id : undefined,
    targetCategory: a.targetCategory || a.targetDeviceCategory,
    targetId: String(a.targetId != null ? a.targetId : a.targetDeviceId),
    params: typeof (a.params || a.actionParams) === 'string' ? JSON.parse(a.params || a.actionParams) : a.params || a.actionParams || {},
    executionOrder: i,
  }));

export const renderActionParamFields = ({ container, category, values = {}, allowedKeys = null, i18n = {}, requireAll = false }) => {
  if (!container) return;
  container.innerHTML = '';
  const schemaMap = ACTION_PARAM_SCHEMA[category];
  if (!schemaMap) {
    container.innerHTML = '<div class="col-12 text-muted small py-2">Không có tham số mở rộng.</div>';
    return;
  }

  Object.entries(schemaMap).forEach(([key, schema]) => {
    if (allowedKeys && !allowedKeys.includes(key)) return;

    const col = document.createElement('div');
    col.className = 'col-12 col-md-6 mb-2';

    const label = document.createElement('label');
    label.className = 'form-label fw-semibold small text-muted text-uppercase mb-1';
    label.textContent = (i18n && i18n[schema.labelKey]) || ACTION_FIELD_LABELS[key] || schema.labelKey;
    col.appendChild(label);

    const currentVal = values && values[key] !== undefined ? values[key] : '';

    if (schema.type === 'enum') {
      const select = document.createElement('select');
      select.className = 'form-select bg-light border-0';
      select.name = `param_${key}`;
      select.dataset.paramKey = key;

      if (!requireAll) {
        const defaultOpt = document.createElement('option');
        defaultOpt.value = '';
        defaultOpt.textContent = `— ${i18n.unchanged || 'Mặc định'} —`;
        select.appendChild(defaultOpt);
      }

      (schema.options || []).forEach((optVal) => {
        const opt = document.createElement('option');
        opt.value = optVal;
        opt.textContent = optVal;
        if (String(currentVal) === String(optVal)) opt.selected = true;
        select.appendChild(opt);
      });
      col.appendChild(select);
    } else {
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

      col.appendChild(input);
      col.appendChild(feedback);
    }

    container.appendChild(col);
  });
};

export const collectActionParamsFromContainer = (container, category) => {
  if (!container) return {};
  const schemaMap = ACTION_PARAM_SCHEMA[category] || {};
  const params = {};

  Object.entries(schemaMap).forEach(([key, schema]) => {
    const el = container.querySelector(`[data-param-key="${key}"]`);
    if (!el) return;
    const val = el.value.trim();
    if (val === '') return;

    if (schema.type === 'int') {
      const parsed = parseInt(val, 10);
      if (!Number.isNaN(parsed)) params[key] = parsed;
    } else if (schema.type === 'float') {
      const parsed = parseFloat(val);
      if (!Number.isNaN(parsed)) params[key] = parsed;
    } else {
      params[key] = val;
    }
  });

  return params;
};

export const validateActionParams = ({ category, params = {} }) => {
  const schemaMap = ACTION_PARAM_SCHEMA[category];
  if (!schemaMap) return { isValid: true, errors: {} };

  const errors = {};
  Object.entries(schemaMap).forEach(([key, schema]) => {
    const val = params[key];
    if (val === undefined || val === null || val === '') return;

    if (schema.type === 'enum' && !schema.options.includes(val)) {
      errors[key] = `Giá trị ${key} không hợp lệ.`;
    } else if (schema.type === 'int' || schema.type === 'float') {
      const num = Number(val);
      if (Number.isNaN(num)) {
        errors[key] = `${key} phải là số.`;
      } else if (schema.min !== undefined && num < schema.min) {
        errors[key] = `${key} phải >= ${schema.min}.`;
      } else if (schema.max !== undefined && num > schema.max) {
        errors[key] = `${key} phải <= ${schema.max}.`;
      }
    }
  });

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
};
