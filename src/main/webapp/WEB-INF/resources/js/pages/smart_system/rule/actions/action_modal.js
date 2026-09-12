import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { getAllFloors } from '../../../../api/floor.api.js';
import { getAllRoomsByFloor, getRoomById } from '../../../../api/room.api.js';
import { getDevicesByRoom, getDeviceById } from '../../../../api/device.api.js';
import { Alert } from '../../../../common/notification_util.js';
import { Validator } from '../../../../common/validator.js';
import { CreateActionDto } from '../../../../types/rule.domain.js';
import { ACTION_PARAM_SCHEMA } from '../../../../constants/smart_system.constants.js';
import {
    getAllowedActionParamKeys,
    renderActionParamFields,
    collectActionParamsFromContainer,
    validateActionParams,
} from '../../../../common/smart_system_util.js';

const { i18n } = window.__ACTIONS_CONFIG__;

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
        const config = ACTION_PARAM_SCHEMA[category];
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
        if (!specificType) {
            const selectedOpt = el.targetDeviceId.options[el.targetDeviceId.selectedIndex];
            specificType = selectedOpt?.dataset?.specificType || null;
        }
        const allowedKeys = getAllowedActionParamKeys(category, specificType);
        renderActionParamFields({
            container: el.dynamicParamsContainer,
            category,
            values: currentParams,
            allowedKeys,
            i18n,
        });
        window.renderIcons?.();
    };

    const collectParams = async (category) => {
        const params = collectActionParamsFromContainer(el.dynamicParamsContainer, category);
        const validation = validateActionParams({ category, params });
        if (!validation.isValid) {
            const firstErr = Object.values(validation.errors)[0] || 'Invalid parameter';
            await Alert.warning(firstErr, i18n.error || 'Error');
            return null;
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
                const category = data.targetCategory || data.targetDeviceCategory;
                const targetId = data.targetId || data.targetDeviceId;
                el.targetDeviceCategory.value = category;

                const params = typeof (data.params || data.actionParams) === 'string'
                    ? JSON.parse(data.params || data.actionParams)
                    : (data.params || data.actionParams || {});

                const [devErr, devRes] = await getDeviceById(targetId, category);
                if (!devErr && devRes?.data) {
                    const device = devRes.data;
                    const [roomErr, roomRes] = await getRoomById(device.roomId);
                    if (!roomErr && roomRes?.data) {
                        const room = roomRes.data;
                        el.floorId.value = room.floorId;
                        await loadRooms(room.floorId);
                        el.roomId.value = device.roomId;
                        await loadDevices(device.roomId, category, targetId);
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
        const category = data.targetCategory || data.targetDeviceCategory;
        const targetId = data.targetId || data.targetDeviceId;
        renderDynamicParams(category, null, params);
        el.targetDeviceId.innerHTML = '';
        const opt = document.createElement('option');
        opt.value = targetId;
        opt.textContent = data.targetName || data.targetDeviceName
            || `Device #${targetId} (${i18n.keptAsIs})`;
        opt.selected = true;
        el.targetDeviceId.appendChild(opt);
        el.targetDeviceId.disabled = false;
    };

    const submit = async (e) => {
        e.preventDefault();

        const localId = el.localId.value;
        const category = el.targetDeviceCategory.value;
        const targetId = el.targetDeviceId.value;
        const orderVal = el.executionOrder.value;

        if (orderVal === '' || isNaN(orderVal) || parseInt(orderVal, 10) < 0) {
            await Alert.warning(i18n.valExecutionOrderInvalid || 'Execution order must be a valid number', i18n.error || 'Error');
            el.executionOrder?.focus();
            return;
        }

        const actionParams = await collectParams(category);
        if (actionParams === null) return;

        const builder = new CreateActionDto.Builder()
            .setTargetCategory(category)
            .setTargetId(targetId)
            .setParams(actionParams)
            .setExecutionOrder(orderVal);

        const result = builder.validate();
        if (!result.isValid) {
            const firstField = Object.keys(result.errors)[0];
            const msgKey = result.errors[firstField];
            const fieldLabel = ({ targetId: i18n.colTargetDevice, targetCategory: i18n.colType, executionOrder: i18n.colOrder, params: i18n.colParams })[firstField] || '';
            await Alert.warning((i18n[msgKey] || i18n.valRequired || 'Error').replace('{0}', fieldLabel), i18n.error || 'Error');
            const FIELD_ID_MAP = { targetId: el.targetDeviceId, targetCategory: el.targetDeviceCategory, executionOrder: el.executionOrder };
            FIELD_ID_MAP[firstField]?.focus();
            return;
        }

        const selectedOption = el.targetDeviceId.options[el.targetDeviceId.selectedIndex];
        const targetDeviceName = selectedOption ? selectedOption.textContent : '';

        const data = {
            executionOrder: parseInt(orderVal, 10),
            targetCategory: category,
            targetId:       String(targetId),
            params:         actionParams,
            targetName:     targetDeviceName,
        };

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
