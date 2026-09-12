/**
 * Action Modal cho Quản lý Sự kiện Phòng (Room Event)
 * Tuân thủ Clean Code: Tái sử dụng constants & utils, không hardcode labels/defaults
 */

import { TabulatorFull as Tabulator } from '../../../../lib/tabulator_esm.min.js';
import { StateManager } from './state_manager.js';
import { getDevicesByRoom } from '../../api/device.api.js';
import {
  ACTION_FIELD_DEFAULTS,
} from '../../constants/smart_system.constants.js';
import {
  renderActionParamFields,
  collectActionParamsFromContainer,
  validateActionParams,
} from '../../common/smart_system_util.js';
import { Toast } from '../../common/notification_util.js';

export const ActionModal = (() => {
  let modalInstance = null;
  let table = null;
  let cachedDevices = null;
  let editingLocalId = null;
  let onSaveCallback = null;

  const getEl = (id) => document.getElementById(id);

  const init = (onSaveActions) => {
    onSaveCallback = onSaveActions;
    const modalEl = getEl('roomEventActionModal');
    if (!modalEl) return;
    modalInstance = new bootstrap.Modal(modalEl);

    bindEvents();
    initTable();
  };

  const bindEvents = () => {
    getEl('actionForm')?.addEventListener('submit', (e) => {
      e.preventDefault();
      handleSubmitForm();
    });

    getEl('targetDeviceCategory')?.addEventListener('change', (e) => {
      handleCategoryChange(e.target.value);
    });

    getEl('actDeviceId')?.addEventListener('change', (e) => {
      handleDeviceSelect(e.target.value);
    });

    getEl('btnAddNewAct')?.addEventListener('click', () => {
      resetForm();
    });

    getEl('btnCancelEditAct')?.addEventListener('click', () => {
      resetForm();
    });

    getEl('btnSaveActionsBatch')?.addEventListener('click', () => {
      handleSaveBatch();
    });
  };

  const initTable = () => {
    const tableContainer = getEl('actionsTableInModal');
    if (!tableContainer) return;

    table = new Tabulator('#actionsTableInModal', {
      height: '180px',
      layout: 'fitColumns',
      placeholder: '<div class="text-center py-3 text-muted small">Chưa có hành động nào. Khuyến nghị thêm ít nhất 1 lệnh điều khiển thiết bị.</div>',
      columns: [
        {
          title: 'Thứ tự',
          field: 'executionOrder',
          width: 70,
          hozAlign: 'center',
          formatter: (cell) => `<span class="badge bg-light text-dark border">#${(cell.getValue() ?? cell.getRow().getPosition()) + 1}</span>`,
        },
        {
          title: 'Loại',
          field: 'targetCategory',
          width: 90,
          hozAlign: 'center',
          formatter: (cell) => {
            const cat = cell.getValue() || cell.getData().targetDeviceCategory;
            let cls = 'bg-secondary';
            if (cat === 'LIGHT') cls = 'bg-warning text-dark';
            else if (cat === 'FAN') cls = 'bg-info text-dark';
            else if (cat === 'AIR_CONDITION') cls = 'bg-primary';
            return `<span class="badge ${cls} font-monospace">${cat}</span>`;
          },
        },
        {
          title: 'Thiết bị mục tiêu',
          field: 'targetDisplay',
          minWidth: 140,
          formatter: (cell) => {
            const row = cell.getData();
            return `<strong>${cell.getValue() || `Thiết bị #${row.targetId || row.targetDeviceId}`}</strong>`;
          },
        },
        {
          title: 'Tham số lệnh',
          field: 'params',
          minWidth: 140,
          formatter: (cell) => {
            const p = cell.getValue() || {};
            const parts = [];
            if (p.power) parts.push(`Nguồn: ${p.power}`);
            if (p.level !== undefined) parts.push(`Sáng: ${p.level}%`);
            if (p.temperature !== undefined) parts.push(`Nhiệt: ${p.temperature}°C`);
            if (p.speed !== undefined) parts.push(`Tốc độ: ${p.speed}`);
            if (p.mode) parts.push(`Chế độ: ${p.mode}`);
            return `<span class="badge bg-light text-dark border font-monospace">${parts.join(' | ') || '—'}</span>`;
          },
        },
        {
          title: 'Thao tác',
          width: 90,
          hozAlign: 'center',
          headerSort: false,
          formatter: (cell) => {
            const localId = cell.getData()._localId;
            return `
              <div class="d-flex justify-content-center gap-1">
                <button type="button" class="btn btn-sm btn-light rounded-circle p-1 text-primary btn-edit-act" data-id="${localId}" title="Sửa">
                  <i data-lucide="edit-3" class="lucide-sm"></i>
                </button>
                <button type="button" class="btn btn-sm btn-light rounded-circle p-1 text-danger btn-del-act" data-id="${localId}" title="Xóa">
                  <i data-lucide="trash-2" class="lucide-sm"></i>
                </button>
              </div>`;
          },
        },
      ],
    });

    table.on('renderComplete', () => {
      tableContainer.querySelectorAll('.btn-edit-act').forEach((btn) => {
        btn.addEventListener('click', () => handleEditRow(btn.dataset.id));
      });
      tableContainer.querySelectorAll('.btn-del-act').forEach((btn) => {
        btn.addEventListener('click', () => handleDeleteRow(btn.dataset.id));
      });
      if (window.lucide) window.lucide.createIcons();
    });
  };

  const open = async (configId, eventCode) => {
    editingLocalId = null;
    getEl('actionModalTitle').textContent = `Quản lý hành động sự kiện: ${eventCode}`;
    resetForm();

    await loadRoomDevices();
    refreshTable();
    handleCategoryChange('LIGHT');

    modalInstance?.show();
    if (window.lucide) window.lucide.createIcons();
  };

  const loadRoomDevices = async () => {
    const roomId = StateManager.getRoomId();
    if (!cachedDevices) {
      const [err, res] = await getDevicesByRoom(roomId);
      cachedDevices = (!err && res?.data) ? res.data : [];
    }
  };

  const refreshTable = () => {
    if (table) {
      table.setData(StateManager.getActions());
    }
  };

  const handleCategoryChange = (category, currentValues = null) => {
    populateDevices(category);
    const container = getEl('actParamsContainer');
    const defaultVals = currentValues || ACTION_FIELD_DEFAULTS[category] || { power: 'ON' };

    renderActionParamFields({
      container,
      category,
      values: defaultVals,
      requireAll: true,
    });
  };

  const populateDevices = (category) => {
    const select = getEl('actDeviceId');
    select.innerHTML = '';

    const filtered = (cachedDevices || []).filter((d) => d.category === category);
    if (filtered.length === 0) {
      select.innerHTML = '<option value="" disabled selected>-- Không có thiết bị thuộc loại này trong phòng --</option>';
      return;
    }

    filtered.forEach((d) => {
      const opt = document.createElement('option');
      opt.value = d.id;
      opt.textContent = `${d.name} (#${d.id})`;
      select.appendChild(opt);
    });
  };

  const handleDeviceSelect = (deviceId) => {
    const device = (cachedDevices || []).find((d) => String(d.id) === String(deviceId));
    if (device && device.category) {
      const cat = device.category;
      if (getEl('targetDeviceCategory').value !== cat) {
        getEl('targetDeviceCategory').value = cat;
        handleCategoryChange(cat);
      }
    }
  };

  const handleEditRow = (localId) => {
    const act = StateManager.getActions().find((a) => a._localId === localId);
    if (!act) return;

    editingLocalId = localId;
    getEl('actionLocalId').value = localId;
    getEl('actFormHeading').textContent = `✏️ Chỉnh sửa hành động #${(act.executionOrder ?? 0) + 1}`;
    getEl('btnCancelEditAct').classList.remove('d-none');
    getEl('btnSubmitAct').innerHTML = '<i data-lucide="check" class="lucide-sm me-1"></i> Cập nhật hành động';

    getEl('executionOrder').value = act.executionOrder ?? 0;
    const cat = act.targetCategory || act.targetDeviceCategory || 'LIGHT';
    getEl('targetDeviceCategory').value = cat;

    handleCategoryChange(cat, act.params || {});
    if (act.targetId || act.targetDeviceId) {
      getEl('actDeviceId').value = act.targetId || act.targetDeviceId;
    }

    if (window.lucide) window.lucide.createIcons();
  };

  const handleDeleteRow = (localId) => {
    StateManager.deleteAction(localId);
    refreshTable();
    if (editingLocalId === localId) resetForm();
  };

  const resetForm = () => {
    editingLocalId = null;
    getEl('actionForm')?.reset();
    getEl('actionLocalId').value = '';
    getEl('executionOrder').value = StateManager.getActions().length;
    getEl('actFormHeading').textContent = '+ Thêm hành động';
    getEl('btnCancelEditAct').classList.add('d-none');
    getEl('btnSubmitAct').innerHTML = '<i data-lucide="check" class="lucide-sm me-1"></i> Lưu hành động vào danh sách';
    handleCategoryChange('LIGHT');
    if (window.lucide) window.lucide.createIcons();
  };

  const handleSubmitForm = () => {
    const deviceId = getEl('actDeviceId').value;
    const category = getEl('targetDeviceCategory').value;

    if (!deviceId) {
      Toast.warning('Vui lòng chọn thiết bị điều khiển.');
      return;
    }

    const container = getEl('actParamsContainer');
    const params = collectActionParamsFromContainer(container, category);
    const validation = validateActionParams({ category, params });

    if (!validation.isValid) {
      const firstError = Object.values(validation.errors)[0];
      Toast.warning(firstError || 'Tham số hành động không hợp lệ.');
      return;
    }

    const selectedDevice = (cachedDevices || []).find((d) => String(d.id) === String(deviceId));
    const payload = {
      executionOrder: parseInt(getEl('executionOrder').value || '0', 10),
      targetCategory: category,
      targetId: deviceId,
      targetDisplay: selectedDevice ? selectedDevice.name : `Thiết bị #${deviceId}`,
      params,
    };

    if (editingLocalId) {
      StateManager.updateAction(editingLocalId, payload);
    } else {
      StateManager.addAction(payload);
    }

    refreshTable();
    resetForm();
  };

  const handleSaveBatch = async () => {
    const configId = StateManager.getActiveConfigId();
    if (!configId) return;

    const payload = StateManager.buildActionsPayload();
    if (onSaveCallback) {
      await onSaveCallback(configId, payload);
      modalInstance?.hide();
    }
  };

  return {
    init,
    open,
  };
})();
