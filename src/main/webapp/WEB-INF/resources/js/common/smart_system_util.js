import { UTCUtils } from './utc_util.js';
import {
  PROPERTY_LABEL_CONFIG,
  DEVICE_CAPABILITIES,
} from '../constants/smart_system.constants.js';

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
    nextLogic: i < conditions.length - 1 ? (c.nextLogic || 'AND') : null,
  }));

export const mapActionsForReplace = (actions = []) =>
  actions.map((a, i) => ({
    id: a.id != null ? a.id : undefined,
    targetCategory: a.targetCategory || a.targetDeviceCategory,
    targetId: String(a.targetId != null ? a.targetId : a.targetDeviceId),
    params: typeof (a.params || a.actionParams) === 'string'
      ? JSON.parse(a.params || a.actionParams)
      : (a.params || a.actionParams || {}),
    executionOrder: i,
  }));
