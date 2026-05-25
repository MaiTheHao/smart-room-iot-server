import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { getAllFloors } from '../../../../api/floor.api.js';
import { getAllRoomsByFloor } from '../../../../api/room.api.js';
import { getDevicesByRoom } from '../../../../api/device.api.js';

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
                    devices.forEach((device) => {
                        const opt = document.createElement('option');
                        opt.value = device.id;
                        opt.textContent = device.name;
                        if (selectedId && String(device.id) === String(selectedId)) {
                            opt.selected = true;
                        }
                        el.targetDeviceId.appendChild(opt);
                    });
                    el.targetDeviceId.disabled = false;
                }
            } else {
                el.targetDeviceId.innerHTML = `<option value="" disabled selected>${i18n.errorLoadingDevices}</option>`;
            }
        } catch (err) {
            console.error('Failed to load devices', err);
            el.targetDeviceId.innerHTML = `<option value="" disabled selected>${i18n.errorLoadingDevices}</option>`;
        }
    };

    const renderDynamicParams = (category, currentParams = {}) => {
        el.dynamicParamsContainer.innerHTML = '';
        const config = PARAMETER_CONFIG[category];
        if (!config) return;

        Object.entries(config).forEach(([key, schema]) => {
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

    const collectParams = (category) => {
        const config = PARAMETER_CONFIG[category];
        if (!config) return {};

        const params = {};
        let isValid = true;

        Object.entries(config).forEach(([key, schema]) => {
            const inputEl = el.dynamicParamsContainer.querySelector(`[name="param_${key}"]`);
            if (!inputEl) return;

            inputEl.classList.remove('is-invalid');
            const val = inputEl.value;

            if (val === '' || val === null || val === undefined) {

                return;
            }

            if (schema.type === 'int') {
                const num = parseInt(val, 10);
                if (isNaN(num) || num < schema.min || num > schema.max) {
                    inputEl.classList.add('is-invalid');
                    isValid = false;
                } else {
                    params[key] = num;
                }
            } else {
                params[key] = val;
            }
        });

        return isValid ? params : null;
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
                renderDynamicParams(data.targetDeviceCategory, params);

                el.targetDeviceId.innerHTML = '';
                const opt = document.createElement('option');
                opt.value = data.targetDeviceId;
                opt.textContent = data.targetDeviceName
                    || `Device #${data.targetDeviceId} (${i18n.keptAsIs})`;
                opt.selected = true;
                el.targetDeviceId.appendChild(opt);
                el.targetDeviceId.disabled = false;
            }
        } else {

            el.title.textContent = i18n.addTitle;
            el.executionOrder.value = StateManager.getActions().length;
            renderDynamicParams(el.targetDeviceCategory.value);
        }

        bootstrapModal?.show();
        window.renderIcons?.();
    };

    const submit = (e) => {
        e.preventDefault();

        if (!el.targetDeviceId.value) {
            el.targetDeviceId.classList.add('is-invalid');
            return;
        }
        el.targetDeviceId.classList.remove('is-invalid');

        const category = el.targetDeviceCategory.value;
        const actionParams = collectParams(category);

        if (actionParams === null) return;

        const selectedOption = el.targetDeviceId.options[el.targetDeviceId.selectedIndex];
        const targetDeviceName = selectedOption ? selectedOption.textContent : '';

        const data = {
            executionOrder:      parseInt(el.executionOrder.value, 10),
            targetDeviceId:      parseInt(el.targetDeviceId.value, 10),
            targetDeviceCategory: category,
            actionParams:        actionParams,
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
