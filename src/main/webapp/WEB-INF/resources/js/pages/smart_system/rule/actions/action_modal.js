import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { getAllFloors } from '../../../../api/floor.api.js';
import { getAllRoomsByFloor, getRoomById } from '../../../../api/room.api.js';
import { getDevicesByRoom, getDeviceById } from '../../../../api/device.api.js';
import { Alert } from '../../../../common/notification_util.js';
import { Validator } from '../../../../common/validator.js';

const { i18n } = window.__ACTIONS_CONFIG__;

const PARAMETER_CONFIG = {
    LIGHT: {
        power: {
            type: 'enum',
            labelKey: 'power',
            options: ['ON', 'OFF'],
        },
        level: {
            type: 'int',
            labelKey: 'brightnessLevel',
            min: 0,
            max: 100,
            placeholder: '0 – 100',
        },
    },
    AIR_CONDITION: {
        power: {
            type: 'enum',
            labelKey: 'power',
            options: ['ON', 'OFF'],
        },
        temperature: {
            type: 'int',
            labelKey: 'temperature',
            min: 16,
            max: 32,
            placeholder: '16 – 32 °C',
        },
        mode: {
            type: 'enum',
            labelKey: 'mode',
            options: ['COOL', 'HEAT', 'DRY', 'FAN', 'AUTO'],
        },
        fanSpeed: {
            type: 'int',
            labelKey: 'fanSpeed',
            min: 0,
            max: 5,
            placeholder: '0 – 5',
        },
        swing: {
            type: 'enum',
            labelKey: 'swing',
            options: ['ON', 'OFF'],
        },
    },
    FAN: {
        power: {
            type: 'enum',
            labelKey: 'power',
            options: ['ON', 'OFF'],
        },
        mode: {
            type: 'enum',
            labelKey: 'mode',
            options: ['NATURAL', 'SLEEP', 'NORMAL'],
        },
        speed: {
            type: 'int',
            labelKey: 'speed',
            min: 1,
            max: 3,
            placeholder: '1 – 3',
        },
        swing: {
            type: 'enum',
            labelKey: 'swing',
            options: ['ON', 'OFF'],
        },
        light: {
            type: 'enum',
            labelKey: 'fanLight',
            options: ['ON', 'OFF'],
        },
    },
};

const DEVICE_CAPABILITIES = {
    FAN: {
        GPIO: ['power', 'speed'],
        IRSEND: ['power', 'speed', 'mode', 'swing'],
        IR_CTL: ['power', 'speed', 'mode', 'swing']
    },
    LIGHT: {
        GPIO: ['power', 'level'],
        IRSEND: ['power', 'level'],
        IR_CTL: ['power', 'level']
    },
    AIR_CONDITION: {
        GPIO: ['power'],
        IRSEND: ['power', 'temperature', 'mode', 'fanSpeed', 'swing'],
        IR_CTL: ['power', 'temperature', 'mode', 'fanSpeed', 'swing']
    }
};

export const ActionModal = (() => {
    let bootstrapModal = null;
    let isFloorsLoaded = false;

    const el = {
        modal: null,
        form: null,
        title: null,
        localId: null,
        executionOrder: null,
        targetDeviceCategory: null,
        floorId: null,
        roomId: null,
        targetDeviceId: null,
        dynamicParamsContainer: null,
    };

    const loadFloors = async () => {
        if (isFloorsLoaded) return;
        try {
            const [err, res] = await getAllFloors();
            if (!err && res?.data) {
                el.floorId.innerHTML = `<option value="" disabled selected>${i18n.selectFloor}</option>`;
                res.data.forEach((floor) => {
                    const opt = document.createElement('option');
                    opt.value = floor.id;
                    opt.textContent = floor.name;
                    el.floorId.appendChild(opt);
                });
                isFloorsLoaded = true;
            }
        } catch (err) {
            console.error('Failed to load floors', err);
        }
    };

    const loadRooms = async (floorId) => {
        el.roomId.disabled = true;
        el.roomId.innerHTML = `<option value="" disabled selected>${i18n.selectRoom}</option>`;

        el.targetDeviceId.disabled = true;
        el.targetDeviceId.innerHTML = `<option value="" disabled selected>${i18n.selectRoomAndCategory}</option>`;

        if (!floorId) return;

        try {
            const [err, res] = await getAllRoomsByFloor(floorId);
            if (!err && res?.data) {
                el.roomId.innerHTML = `<option value="" disabled selected>${i18n.selectRoom}</option>`;
                res.data.forEach((room) => {
                    const opt = document.createElement('option');
                    opt.value = room.id;
                    opt.textContent = room.name;
                    el.roomId.appendChild(opt);
                });
                el.roomId.disabled = false;
            }
        } catch (err) {
            console.error('Failed to load rooms', err);
        }
    };

    const loadDevices = async (roomId, category, selectedId = null) => {
        el.targetDeviceId.disabled = true;
        el.targetDeviceId.innerHTML = `<option value="" disabled selected>${i18n.loadingDevices}</option>`;

        if (!roomId || !category) {
            el.targetDeviceId.innerHTML = `<option value="" disabled selected>${i18n.selectRoomAndCategory}</option>`;
            return;
        }

        try {
            const [err, res] = await getDevicesByRoom(roomId, category);
            if (!err && res?.data) {
                const devices = res.data;
                if (devices.length === 0) {
                    el.targetDeviceId.innerHTML = `<option value="" disabled selected>${i18n.noDevicesFound}</option>`;
                } else {
                    el.targetDeviceId.innerHTML = `<option value="" disabled selected>${i18n.selectDevice}</option>`;
                    let hasSelected = false;
                    devices.forEach((device) => {
                        const opt = document.createElement('option');
                        opt.value = device.id;
                        opt.textContent = device.name;
                        opt.dataset.specificType = device.specificType;
                        if (selectedId && String(device.id) === String(selectedId)) {
                            opt.selected = true;
                            hasSelected = true;
                        }
                        el.targetDeviceId.appendChild(opt);
                    });
                    el.targetDeviceId.disabled = false;

                    if (hasSelected) {
                        const selectedOpt = el.targetDeviceId.options[el.targetDeviceId.selectedIndex];
                        const specificType = selectedOpt?.dataset?.specificType || null;
                        const currentParams = getEnteredParams(category);
                        renderDynamicParams(category, specificType, currentParams);
                    }
                }
            } else {
                el.targetDeviceId.innerHTML = `<option value="" disabled selected>${i18n.errorLoadingDevices}</option>`;
            }
        } catch (err) {
            console.error('Failed to load devices', err);
            el.targetDeviceId.innerHTML = `<option value="" disabled selected>${i18n.errorLoadingDevices}</option>`;
        }
    };

    const getEnteredParams = (category) => {
        const config = PARAMETER_CONFIG[category];
        if (!config) return {};
        const params = {};
        for (const [key, schema] of Object.entries(config)) {
            const inputEl = el.dynamicParamsContainer.querySelector(`[name="param_${key}"]`);
            if (inputEl) {
                const val = inputEl.value;
                if (val !== '' && val !== null && val !== undefined) {
                    params[key] = val;
                }
            }
        }
        return params;
    };

    const renderDynamicParams = (category, specificType = null, currentParams = {}) => {
        el.dynamicParamsContainer.innerHTML = '';
        const config = PARAMETER_CONFIG[category];
        if (!config) return;

        if (!specificType) {
            const selectedOpt = el.targetDeviceId.options[el.targetDeviceId.selectedIndex];
            specificType = selectedOpt?.dataset?.specificType || null;
        }

        const allowedKeys = (specificType && DEVICE_CAPABILITIES[category]?.[specificType])
            ? DEVICE_CAPABILITIES[category][specificType]
            : null;

        Object.entries(config).forEach(([key, schema]) => {
            if (allowedKeys && !allowedKeys.includes(key)) {
                return;
            }
            const col = document.createElement('div');
            col.className = 'col-12 col-md-6';

            const label = document.createElement('label');
            label.className = 'form-label fw-semibold small text-muted text-uppercase mb-1';
            label.textContent = i18n[schema.labelKey] || schema.labelKey;
            col.appendChild(label);

            const currentVal = (currentParams && currentParams[key] !== undefined)
                ? String(currentParams[key])
                : '';

            if (schema.type === 'enum') {
                const select = document.createElement('select');
                select.className = 'form-select bg-light border-0';
                select.name = `param_${key}`;

                const defaultOpt = document.createElement('option');
                defaultOpt.value = '';
                defaultOpt.textContent = `— ${i18n.unchanged} —`;
                select.appendChild(defaultOpt);

                schema.options.forEach((val) => {
                    const opt = document.createElement('option');
                    opt.value = val;
                    opt.textContent = val;
                    if (currentVal === val) opt.selected = true;
                    select.appendChild(opt);
                });

                col.appendChild(select);
            } else {
                const input = document.createElement('input');
                input.type = 'number';
                input.className = 'form-control bg-light border-0';
                input.name = `param_${key}`;
                input.placeholder = schema.placeholder || '';
                input.min = schema.min;
                input.max = schema.max;
                if (currentVal !== '') input.value = currentVal;

                const feedback = document.createElement('div');
                feedback.className = 'invalid-feedback';
                feedback.textContent = `Must be between ${schema.min} and ${schema.max}`;

                col.appendChild(input);
                col.appendChild(feedback);
            }

            el.dynamicParamsContainer.appendChild(col);
        });

        window.renderIcons?.();
    };

    const collectParams = async (category) => {
        const config = PARAMETER_CONFIG[category];
        if (!config) return {};

        const params = {};

        for (const [key, schema] of Object.entries(config)) {
            const inputEl = el.dynamicParamsContainer.querySelector(`[name="param_${key}"]`);
            if (!inputEl) continue;

            const val = inputEl.value;

            if (val === '' || val === null || val === undefined) {
                continue;
            }

            const categoryValidators = Validator[category];
            const validatorKey = key === 'temperature' ? 'temp' : (key === 'fanSpeed' ? 'fan_speed' : key);
            const validator = categoryValidators ? categoryValidators[validatorKey] : null;

            if (validator && !validator.isValidFormat(val)) {
                if (schema.type === 'int') {
                    await Alert.warning(`${i18n[schema.labelKey] || schema.labelKey}: Must be between ${schema.min} and ${schema.max}`, i18n.error || 'Error');
                } else {
                    await Alert.warning(`${i18n[schema.labelKey] || schema.labelKey}: Invalid value`, i18n.error || 'Error');
                }
                inputEl.focus();
                return null;
            }

            if (schema.type === 'int') {
                const num = parseInt(val, 10);
                if (!validator && (isNaN(num) || num < schema.min || num > schema.max)) {
                    await Alert.warning(`${i18n[schema.labelKey] || schema.labelKey}: Must be between ${schema.min} and ${schema.max}`, i18n.error || 'Error');
                    inputEl.focus();
                    return null;
                }
                params[key] = num;
            } else {
                params[key] = val;
            }
        }

        return params;
    };

    const init = () => {
        el.modal                  = document.getElementById('actionModal');
        el.form                   = document.getElementById('actionForm');
        el.title                  = document.getElementById('modalTitle');
        el.localId                = document.getElementById('actionLocalId');
        el.executionOrder         = document.getElementById('executionOrder');
        el.targetDeviceCategory   = document.getElementById('targetDeviceCategory');
        el.floorId                = document.getElementById('floorId');
        el.roomId                 = document.getElementById('roomId');
        el.targetDeviceId         = document.getElementById('targetDeviceId');
        el.dynamicParamsContainer = document.getElementById('dynamicParamsContainer');

        if (el.modal) {
            bootstrapModal = typeof bootstrap !== 'undefined'
                ? new bootstrap.Modal(el.modal)
                : null;
        }

        el.targetDeviceCategory?.addEventListener('change', () => {
            renderDynamicParams(el.targetDeviceCategory.value);
            const roomId = el.roomId.value;
            if (roomId) loadDevices(roomId, el.targetDeviceCategory.value);
        });

        el.floorId?.addEventListener('change', () => {
            loadRooms(el.floorId.value);
        });

        el.roomId?.addEventListener('change', () => {
            loadDevices(el.roomId.value, el.targetDeviceCategory.value);
        });

        el.targetDeviceId?.addEventListener('change', () => {
            const selectedOpt = el.targetDeviceId.options[el.targetDeviceId.selectedIndex];
            const specificType = selectedOpt?.dataset?.specificType || null;
            const currentParams = getEnteredParams(el.targetDeviceCategory.value);
            renderDynamicParams(el.targetDeviceCategory.value, specificType, currentParams);
        });
    };

    const open = async (localId = null) => {
        el.form.reset();
        el.localId.value = '';
        isFloorsLoaded = false;

        el.roomId.disabled = true;
        el.roomId.innerHTML = `<option value="" disabled selected>${i18n.selectRoom}</option>`;
        el.targetDeviceId.disabled = true;
        el.targetDeviceId.innerHTML = `<option value="" disabled selected>${i18n.selectRoomAndCategory}</option>`;

        await loadFloors();

        if (localId) {
            const data = StateManager.getAction(localId);
            if (data) {
                el.title.textContent = i18n.editTitle;
                el.localId.value = data._localId;
                el.executionOrder.value = data.executionOrder;
                el.targetDeviceCategory.value = data.targetDeviceCategory;

                const params = typeof data.actionParams === 'string'
                    ? JSON.parse(data.actionParams)
                    : (data.actionParams || {});

                const [devErr, devRes] = await getDeviceById(data.targetDeviceId, data.targetDeviceCategory);
                if (!devErr && devRes?.data) {
                    const device = devRes.data;
                    const [roomErr, roomRes] = await getRoomById(device.roomId);
                    if (!roomErr && roomRes?.data) {
                        const room = roomRes.data;
                        el.floorId.value = room.floorId;
                        await loadRooms(room.floorId);
                        el.roomId.value = device.roomId;
                        await loadDevices(device.roomId, data.targetDeviceCategory, data.targetDeviceId);
                    } else {
                        fallbackLoad(data, params);
                    }
                } else {
                    fallbackLoad(data, params);
                }
            }
        } else {
            el.title.textContent = i18n.addTitle;
            el.executionOrder.value = StateManager.getActions().length;
            renderDynamicParams(el.targetDeviceCategory.value);
        }

        bootstrapModal?.show();
        window.renderIcons?.();
    };

    const fallbackLoad = (data, params) => {
        renderDynamicParams(data.targetDeviceCategory, null, params);
        el.targetDeviceId.innerHTML = '';
        const opt = document.createElement('option');
        opt.value = data.targetDeviceId;
        opt.textContent = data.targetName || data.targetDeviceName
            || `Device #${data.targetDeviceId} (${i18n.keptAsIs})`;
        opt.selected = true;
        el.targetDeviceId.appendChild(opt);
        el.targetDeviceId.disabled = false;
    };

    const submit = async (e) => {
        e.preventDefault();

        if (!Validator.id.isBlank(el.targetDeviceId.value)) {
            await Alert.warning(i18n.selectDevice || 'Please select a device', i18n.error || 'Error');
            el.targetDeviceId?.focus();
            return;
        }

        const orderVal = el.executionOrder.value;
        if (orderVal === '' || isNaN(orderVal) || parseInt(orderVal, 10) < 0) {
            await Alert.warning('Execution order must be a valid non-negative integer', i18n.error || 'Error');
            el.executionOrder?.focus();
            return;
        }

        const category = el.targetDeviceCategory.value;
        const actionParams = await collectParams(category);

        if (actionParams === null) return;

        const selectedOption = el.targetDeviceId.options[el.targetDeviceId.selectedIndex];
        const targetDeviceName = selectedOption ? selectedOption.textContent : '';

        const data = {
            executionOrder:      parseInt(el.executionOrder.value, 10),
            targetDeviceId:      parseInt(el.targetDeviceId.value, 10),
            targetDeviceCategory: category,
            actionParams:        actionParams,
            targetName:          targetDeviceName,
            targetDeviceName:    targetDeviceName,
        };

        const localId = el.localId.value;
        if (localId) {
            StateManager.updateAction(localId, data);
        } else {
            StateManager.addAction(data);
        }

        UiRenderer.render();
        bootstrapModal?.hide();
    };

    return { init, open, submit };
})();
