import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { getAllRooms } from '../../../../api/room.api.js';
import { getDevicesByRoom } from '../../../../api/device.api.js';
import { getAllTemperaturesByRoom, getAllPowerConsumptionsByRoom } from '../../../../api/sensor.api.js';
import { UTCUtils } from '../../../../common/utc_util.js';
import { Alert } from '../../../../common/notification_util.js';

const { i18n } = window.__CONDITIONS_CONFIG__;

const DATA_SOURCE_CONFIG = {
  SYSTEM: {
    needsRoom: false,
    needsTarget: false,
    properties: [
      { value: 'current_time', label: 'current_time' },
      { value: 'day_of_week',  label: 'day_of_week' },
      { value: 'day_of_month', label: 'day_of_month' },
    ],
  },
  ROOM: {
    needsRoom: true,
    needsTarget: false,
    properties: [
      { value: 'avg_temperature', label: 'avg_temperature' },
      { value: 'sum_watt',        label: 'sum_watt' },
    ],
  },
  DEVICE: {
    needsRoom: true,
    needsTarget: true,
    targetLabel: i18n.labelDevice,
    categories: {
      LIGHT:         [{ value: 'power', label: 'power' }, { value: 'level', label: 'level' }],
      AIR_CONDITION: [{ value: 'power', label: 'power' }, { value: 'temp', label: 'temp' },
                      { value: 'mode', label: 'mode' }, { value: 'fan_speed', label: 'fan_speed' },
                      { value: 'swing', label: 'swing' }],
      FAN:           [{ value: 'power', label: 'power' }, { value: 'speed', label: 'speed' },
                      { value: 'mode', label: 'mode' }, { value: 'swing', label: 'swing' },
                      { value: 'light', label: 'light' }],
    },
  },
  SENSOR: {
    needsRoom: true,
    needsTarget: true,
    targetLabel: i18n.labelSensor,
    categories: {
      TEMPERATURE:       [{ value: 'temperature', label: 'temperature' }],
      POWER_CONSUMPTION: [{ value: 'watt', label: 'watt' }],
    },
  },
};

export const ConditionModal = (() => {
  let bootstrapModal = null;
  let isRoomsLoaded = false;

  const el = {};

  const init = () => {
    el.modal        = document.getElementById('conditionModal');
    el.form         = document.getElementById('conditionForm');
    el.title        = document.getElementById('conditionModalTitle');
    el.localId      = document.getElementById('conditionLocalId');

    el.dataSource   = document.getElementById('condDataSource');
    el.categoryWrap = document.getElementById('condCategoryWrap');
    el.category     = document.getElementById('condCategory');

    el.roomWrap     = document.getElementById('condRoomWrap');
    el.room         = document.getElementById('condRoom');

    el.targetWrap   = document.getElementById('condTargetWrap');
    el.targetLabel  = document.getElementById('condTargetLabel');
    el.target       = document.getElementById('condTarget');

    el.propertyWrap = document.getElementById('condPropertyWrap');
    el.property     = document.getElementById('condProperty');

    el.operator     = document.getElementById('condOperator');
    el.value        = document.getElementById('condValue');
    el.valueSelect  = document.getElementById('condValueSelect');
    el.valueHour    = document.getElementById('condValueHour');
    el.valueMinute  = document.getElementById('condValueMinute');
    el.valueTimeGroup = document.getElementById('condValueTimeGroup');
    el.valueHelp    = document.getElementById('condValueHelp');
    el.nextLogicWrap= document.getElementById('condNextLogicWrap');
    el.nextLogic    = document.getElementById('condNextLogic');
    el.sortOrder    = document.getElementById('condSortOrder');

    el.valTarget    = document.getElementById('val-condTarget');
    el.valValue     = document.getElementById('val-condValue');

    if (el.modal) {
      bootstrapModal = typeof bootstrap !== 'undefined'
        ? new bootstrap.Modal(el.modal) : null;
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

    el.dataSource?.addEventListener('change', async () => {
      await onDataSourceChange();
      updateValueInput();
    });
    el.category?.addEventListener('change', async () => {
      await onCategoryChange();
      updateValueInput();
    });
    el.room?.addEventListener('change', () => onRoomChange());
    el.property?.addEventListener('change', () => updateValueInput());
  };

  const onDataSourceChange = async () => {
    const ds = el.dataSource.value;
    const cfg = DATA_SOURCE_CONFIG[ds];
    if (!cfg) return;

    const hasCat = !!cfg.categories;
    el.categoryWrap.classList.toggle('d-none', !hasCat);
    if (hasCat) {
      const cats = Object.keys(cfg.categories);
      el.category.innerHTML = cats.map((k) => `<option value="${k}">${k}</option>`).join('');
    }

    el.roomWrap.classList.toggle('d-none', !cfg.needsRoom);
    if (cfg.needsRoom) await loadRooms();

    el.targetWrap.classList.add('d-none');
    el.target.innerHTML = '';

    if (cfg.properties) {
      populateProperties(cfg.properties);
      el.propertyWrap.classList.remove('d-none');
    } else {
      el.property.innerHTML = '';
      el.propertyWrap.classList.add('d-none');
    }

    if (hasCat) {
      await onCategoryChange();
    }
  };

  const onCategoryChange = async () => {
    const ds  = el.dataSource.value;
    const cat = el.category.value;
    const cfg = DATA_SOURCE_CONFIG[ds];
    const roomId = el.room.value;

    const props = cfg?.categories?.[cat];
    if (props) {
      populateProperties(props);
      el.propertyWrap.classList.remove('d-none');
    }

    if (cfg?.needsTarget && roomId) {
      await loadTargets(ds, cat, roomId);
    }
  };

  const onRoomChange = async () => {
    const ds  = el.dataSource.value;
    const cat = el.category.value;
    const cfg = DATA_SOURCE_CONFIG[ds];

    if (cfg?.needsTarget) {
      await loadTargets(ds, cat, el.room.value);
    }
  };

  const loadRooms = async () => {
    if (isRoomsLoaded) return;
    try {
      const [err, res] = await getAllRooms();
      if (!err && res?.data) {
        el.room.innerHTML = `<option value="" disabled selected>${i18n.selectRoom}</option>`;
        res.data.forEach((r) => {
          const opt = document.createElement('option');
          opt.value = r.id;
          opt.textContent = r.name;
          el.room.appendChild(opt);
        });
        isRoomsLoaded = true;
      }
    } catch (e) { console.error('loadRooms', e); }
  };

  const loadTargets = async (ds, category, roomId, selectedId = null) => {
    if (!roomId || !category) return;

    el.target.disabled = true;
    el.target.innerHTML = `<option value="" disabled selected>${i18n.loading}</option>`;
    el.targetWrap.classList.remove('d-none');

    const cfg = DATA_SOURCE_CONFIG[ds];
    if (el.targetLabel) el.targetLabel.textContent = cfg?.targetLabel || 'Target';

    try {
      let items = [];
      if (ds === 'DEVICE') {
        const [err, res] = await getDevicesByRoom(roomId, category);
        if (!err && res?.data) items = res.data;
      } else if (ds === 'SENSOR') {
        let err, res;
        if (category === 'TEMPERATURE') {
          [err, res] = await getAllTemperaturesByRoom(roomId);
        } else if (category === 'POWER_CONSUMPTION') {
          [err, res] = await getAllPowerConsumptionsByRoom(roomId);
        }
        if (!err && res?.data) items = res.data;
      }

      if (items.length === 0) {
        el.target.innerHTML = `<option value="" disabled selected>${i18n.noTargets}</option>`;
      } else {
        el.target.innerHTML = `<option value="" disabled selected>${i18n.selectTarget}</option>`;
        items.forEach((item) => {
          const opt = document.createElement('option');
          opt.value = item.id;
          opt.textContent = item.name;
          if (selectedId && String(item.id) === String(selectedId)) opt.selected = true;
          el.target.appendChild(opt);
        });
        el.target.disabled = false;
      }
    } catch (e) {
      el.target.innerHTML = `<option value="" disabled selected>${i18n.errorLoading}</option>`;
      console.error('loadTargets', e);
    }
  };

  const populateProperties = (props) => {
    el.property.innerHTML = props.map((p) => `<option value="${p.value}">${p.label}</option>`).join('');
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

  const setValue = (val) => {
    el.value.value = val;
    el.valueSelect.value = val;

    const ds = el.dataSource.value;
    const prop = el.property.value;
    if (ds === 'SYSTEM' && prop === 'current_time') {
      const localNum = parseFloat(val);
      if (!isNaN(localNum)) {
        const hour = Math.floor(localNum);
        const minute = Math.round((localNum - hour) * 60);
        el.valueHour.value = String(hour).padStart(2, '0');
        el.valueMinute.value = String(minute).padStart(2, '0');
      } else {
        el.valueHour.value = '';
        el.valueMinute.value = '';
      }
    }
  };

  const updateValueInput = () => {
    const ds = el.dataSource.value;
    const prop = el.property.value;

    el.valueHelp.classList.add('d-none');
    el.valueHelp.textContent = '';

    el.value.classList.add('d-none');
    el.valueSelect.classList.add('d-none');
    el.valueTimeGroup.classList.add('d-none');
    el.value.type = 'text';
    el.value.removeAttribute('min');
    el.value.removeAttribute('max');
    el.value.removeAttribute('step');
    el.value.placeholder = i18n.placeholderValue || 'Enter value';

    if (ds === 'SYSTEM') {
      if (prop === 'current_time') {
        el.valueTimeGroup.classList.remove('d-none');
        el.valueHelp.textContent = 'Enter time in Local Time Zone';
        el.valueHelp.classList.remove('d-none');
      } else if (prop === 'day_of_week') {
        el.valueSelect.classList.remove('d-none');
        el.valueSelect.innerHTML = `
          <option value="1">1 - Monday</option>
          <option value="2">2 - Tuesday</option>
          <option value="3">3 - Wednesday</option>
          <option value="4">4 - Thursday</option>
          <option value="5">5 - Friday</option>
          <option value="6">6 - Saturday</option>
          <option value="7">7 - Sunday</option>
        `;
      } else if (prop === 'day_of_month') {
        el.value.classList.remove('d-none');
        el.value.type = 'number';
        el.value.min = '1';
        el.value.max = '31';
        el.value.step = '1';
        el.value.placeholder = '1 - 31';
        el.valueHelp.textContent = 'Day of month (1 → 31)';
        el.valueHelp.classList.remove('d-none');
      }
    } else {
      el.value.classList.remove('d-none');
    }
  };

  const open = async (localId = null) => {
    el.form.reset();
    el.localId.value   = '';
    isRoomsLoaded      = false;

    el.categoryWrap.classList.add('d-none');
    el.roomWrap.classList.add('d-none');
    el.targetWrap.classList.add('d-none');
    el.propertyWrap.classList.add('d-none');
    el.target.innerHTML = '';

    el.nextLogicWrap.classList.remove('d-none');

    if (localId) {
      const data = StateManager.getCondition(localId);
      if (data) {
        el.title.textContent = i18n.editTitle;
        el.localId.value = data._localId;

        el.sortOrder.value = data.sortOrder !== undefined ? data.sortOrder : 0;

        el.dataSource.value = data.dataSource;
        await onDataSourceChange();

        if (data.dataSource === 'DEVICE' || data.dataSource === 'SENSOR') {
          el.category.value = data.resourceParam?.category || '';
          await onCategoryChange();

          const roomId = data.resourceParam?.roomId || data.resourceParam?.roomId;
          if (roomId) {
            el.room.value = roomId;
            const idKey = data.dataSource === 'DEVICE' ? data.resourceParam?.deviceId : data.resourceParam?.sensorId;
            await loadTargets(data.dataSource, el.category.value, roomId, idKey);
            if (idKey) el.target.value = idKey;
          }
        } else if (data.dataSource === 'ROOM') {
          if (data.resourceParam?.roomId) el.room.value = data.resourceParam.roomId;
        }

        if (data.resourceParam?.property) {
          el.property.value = data.resourceParam.property;
        }
        updateValueInput();
        el.operator.value = data.operator;

        let displayVal = data.value;
        if (data.dataSource === 'SYSTEM' && data.resourceParam?.property === 'current_time') {
          const utcNum = parseFloat(data.value);
          if (!isNaN(utcNum)) {
            const utcHour = Math.floor(utcNum);
            const utcMin = Math.round((utcNum - utcHour) * 60);
            const local = UTCUtils.utcToLocal(utcHour, utcMin, 0);
            displayVal = (local.hour + local.minute / 60.0).toFixed(2);
            displayVal = parseFloat(displayVal).toString();
          }
        }
        setValue(displayVal);

        const nl = data.nextLogic || 'AND';
        const radio = document.querySelector(`input[name="condNextLogicRadio"][value="${nl}"]`);
        if (radio) radio.checked = true;
      }
    } else {
      el.title.textContent = i18n.addTitle;
      el.dataSource.value = 'SYSTEM';
      await onDataSourceChange();
      updateValueInput();

      el.sortOrder.value = StateManager.getConditions().length;

      const radio = document.querySelector('input[name="condNextLogicRadio"][value="AND"]');
      if (radio) radio.checked = true;
    }

    bootstrapModal?.show();
    window.renderIcons?.();
  };

  const submit = async (e) => {
    e.preventDefault();

    const val = getValue().trim();

    if (!val) {
      await Alert.warning(i18n.valRequired || 'Value is required', i18n.error || 'Error');
      el.value?.focus();
      return;
    }

    const ds  = el.dataSource.value;
    const cfg = DATA_SOURCE_CONFIG[ds];
    const prop = el.property.value;

    if (cfg?.needsTarget && !el.target.value) {
      await Alert.warning(i18n.valTargetRequired || 'Target is required', i18n.error || 'Error');
      el.target?.focus();
      return;
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
      const h = parseInt(el.valueHour.value, 10);
      const m = parseInt(el.valueMinute.value, 10);
      const utc = UTCUtils.localToUTC(h, m, 0);
      finalValue = (utc.hour + utc.minute / 60.0).toFixed(2);
      finalValue = parseFloat(finalValue).toString();
    }

    let orderVal = parseInt(el.sortOrder.value, 10);
    if (isNaN(orderVal) || orderVal < 0) {
      await Alert.warning('Order must be a positive integer', i18n.error || 'Error');
      el.sortOrder?.focus();
      return;
    }

    const resourceParam = buildResourceParam(ds, cfg);

    const nextLogic = document.querySelector('input[name="condNextLogicRadio"]:checked')?.value || 'AND';

    const data = {
      sortOrder:  orderVal,
      dataSource: ds,
      resourceParam,
      operator:   el.operator.value,
      value:      finalValue,
      nextLogic:  nextLogic,
    };

    const localId = el.localId.value;
    if (localId) {
      StateManager.updateCondition(localId, data);
    } else {
      StateManager.addCondition(data);
    }

    UiRenderer.render();
    bootstrapModal?.hide();
  };

  const buildResourceParam = (ds, cfg) => {
    const property = el.property.value;
    if (ds === 'SYSTEM') {
      return { property };
    }
    if (ds === 'ROOM') {
      return { roomId: Number(el.room.value), property };
    }
    const category = el.category.value;
    const targetId = Number(el.target.value);
    if (ds === 'DEVICE') {
      return { category, deviceId: targetId, property };
    }
    if (ds === 'SENSOR') {
      return { category, sensorId: targetId, property };
    }
    return {};
  };

  return { init, open, submit };
})();
