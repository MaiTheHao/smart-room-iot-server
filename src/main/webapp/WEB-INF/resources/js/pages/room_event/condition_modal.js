/**
 * Condition Modal cho Quản lý Sự kiện Phòng (Room Event)
 * Tuân thủ Clean Code: Tái sử dụng constants & utils, không hardcode labels
 */

import { TabulatorFull as Tabulator } from '../../../../lib/tabulator_esm.min.js';
import { StateManager } from './state_manager.js';
import { getSensorsByRoom } from '../../api/sensor-metadata.api.js';
import { getDevicesByRoom } from '../../api/device.api.js';
import {
  SENSOR_PROPERTIES,
  DEVICE_PROPERTIES,
  SYSTEM_PROPERTIES,
  ROOM_PROPERTIES,
} from '../../constants/smart_system.constants.js';
import {
  formatPropertyLabel,
  conditionValueToUtc,
  conditionValueFromUtc,
} from '../../common/smart_system_util.js';
import { Toast } from '../../common/notification_util.js';

export const ConditionModal = (() => {
  let modalInstance = null;
  let table = null;
  let cachedSensors = null;
  let cachedDevices = null;
  let editingLocalId = null;
  let onSaveCallback = null;

  const getEl = (id) => document.getElementById(id);

  const init = (onSaveConditions) => {
    onSaveCallback = onSaveConditions;
    const modalEl = getEl('roomEventConditionModal');
    if (!modalEl) return;
    modalInstance = new bootstrap.Modal(modalEl);

    bindEvents();
    initTable();
  };

  const bindEvents = () => {
    getEl('conditionForm')?.addEventListener('submit', (e) => {
      e.preventDefault();
      handleSubmitForm();
    });

    getEl('condDataSource')?.addEventListener('change', (e) => {
      handleDataSourceChange(e.target.value);
    });

    getEl('condCategory')?.addEventListener('change', () => {
      handleCategoryChange();
    });

    getEl('btnAddNewCond')?.addEventListener('click', () => {
      resetForm();
    });

    getEl('btnCancelEditCond')?.addEventListener('click', () => {
      resetForm();
    });

    getEl('btnSaveConditionsBatch')?.addEventListener('click', () => {
      handleSaveBatch();
    });
  };

  const initTable = () => {
    const tableContainer = getEl('conditionsTableInModal');
    if (!tableContainer) return;

    table = new Tabulator('#conditionsTableInModal', {
      height: '180px',
      layout: 'fitColumns',
      placeholder: '<div class="text-center py-3 text-muted small">Chưa có điều kiện nào. Hành động sẽ luôn thực thi khi có sự kiện.</div>',
      columns: [
        {
          title: 'STT',
          field: 'sortOrder',
          width: 60,
          hozAlign: 'center',
          formatter: (cell) => `<span class="badge bg-light text-dark border">#${(cell.getValue() ?? cell.getRow().getPosition()) + 1}</span>`,
        },
        {
          title: 'Nguồn',
          field: 'sourceCategory',
          width: 90,
          hozAlign: 'center',
          formatter: (cell) => `<span class="badge bg-secondary-subtle text-secondary border font-monospace">${cell.getValue()}</span>`,
        },
        {
          title: 'Thuộc tính',
          field: 'property',
          minWidth: 120,
          formatter: (cell) => `<strong>${formatPropertyLabel(cell.getValue())}</strong>`,
        },
        {
          title: 'Toán tử',
          field: 'operator',
          width: 70,
          hozAlign: 'center',
          formatter: (cell) => `<span class="font-monospace fw-bold">${cell.getValue()}</span>`,
        },
        {
          title: 'Giá trị',
          field: 'value',
          minWidth: 80,
          formatter: (cell) => `<span class="badge bg-light text-dark border font-monospace">${cell.getValue()}</span>`,
        },
        {
          title: 'Liên kết',
          field: 'nextLogic',
          width: 80,
          hozAlign: 'center',
          formatter: (cell) => {
            const val = cell.getValue();
            if (!val) return '—';
            return `<span class="badge ${val === 'AND' ? 'bg-primary-subtle text-primary border' : 'bg-warning-subtle text-warning border'} fw-bold">${val}</span>`;
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
                <button type="button" class="btn btn-sm btn-light rounded-circle p-1 text-primary btn-edit-cond" data-id="${localId}" title="Sửa">
                  <i data-lucide="edit-3" class="lucide-sm"></i>
                </button>
                <button type="button" class="btn btn-sm btn-light rounded-circle p-1 text-danger btn-del-cond" data-id="${localId}" title="Xóa">
                  <i data-lucide="trash-2" class="lucide-sm"></i>
                </button>
              </div>`;
          },
        },
      ],
    });

    table.on('renderComplete', () => {
      tableContainer.querySelectorAll('.btn-edit-cond').forEach((btn) => {
        btn.addEventListener('click', () => handleEditRow(btn.dataset.id));
      });
      tableContainer.querySelectorAll('.btn-del-cond').forEach((btn) => {
        btn.addEventListener('click', () => handleDeleteRow(btn.dataset.id));
      });
      if (window.lucide) window.lucide.createIcons();
    });
  };

  const open = async (configId, eventCode) => {
    editingLocalId = null;
    getEl('conditionModalTitle').textContent = `Quản lý điều kiện sự kiện: ${eventCode}`;
    resetForm();

    await loadSensorsAndDevices();
    refreshTable();
    handleDataSourceChange('SENSOR');

    modalInstance?.show();
    if (window.lucide) window.lucide.createIcons();
  };

  const loadSensorsAndDevices = async () => {
    const roomId = StateManager.getRoomId();
    if (!cachedSensors) {
      const [err, res] = await getSensorsByRoom(roomId, undefined, 0, 100);
      cachedSensors = (!err && res?.data?.content) ? res.data.content : [];
    }
    if (!cachedDevices) {
      const [err, res] = await getDevicesByRoom(roomId);
      cachedDevices = (!err && res?.data) ? res.data : [];
    }
  };

  const refreshTable = () => {
    if (table) {
      table.setData(StateManager.getConditions());
    }
  };

  const handleDataSourceChange = (source) => {
    const catWrap = getEl('condCategoryWrap');
    const targetWrap = getEl('condTargetWrap');
    const catSelect = getEl('condCategory');

    if (source === 'SENSOR' || source === 'DEVICE') {
      catWrap.classList.remove('d-none');
      targetWrap.classList.remove('d-none');
      catSelect.innerHTML = '';

      const keys = source === 'SENSOR'
        ? Object.keys(SENSOR_PROPERTIES)
        : Object.keys(DEVICE_PROPERTIES);

      keys.forEach((cat) => {
        const opt = document.createElement('option');
        opt.value = cat;
        opt.textContent = cat;
        catSelect.appendChild(opt);
      });
      handleCategoryChange();
    } else {
      catWrap.classList.add('d-none');
      targetWrap.classList.add('d-none');
      populateProperties(source);
    }
  };

  const handleCategoryChange = () => {
    const source = getEl('condDataSource').value;
    const category = getEl('condCategory').value;
    populateTargets(source, category);
    populateProperties(source, category);
  };

  const populateTargets = (source, category) => {
    const targetSelect = getEl('condTarget');
    targetSelect.innerHTML = '';

    const list = source === 'SENSOR' ? cachedSensors : cachedDevices;
    const filtered = (list || []).filter((item) => !category || item.category === category || item.sensorType === category);

    if (filtered.length === 0) {
      targetSelect.innerHTML = '<option value="" disabled selected>-- Không tìm thấy thiết bị nào --</option>';
      return;
    }

    filtered.forEach((item) => {
      const opt = document.createElement('option');
      opt.value = item.id;
      opt.textContent = `${item.name} (#${item.id})`;
      targetSelect.appendChild(opt);
    });
  };

  const populateProperties = (source, category) => {
    const propSelect = getEl('condProperty');
    propSelect.innerHTML = '';

    let props = [];
    if (source === 'SYSTEM') props = [...SYSTEM_PROPERTIES];
    else if (source === 'ROOM') props = [...ROOM_PROPERTIES];
    else if (source === 'SENSOR') props = SENSOR_PROPERTIES[category] || [];
    else if (source === 'DEVICE') props = DEVICE_PROPERTIES[category] || [];

    props.forEach((prop) => {
      const opt = document.createElement('option');
      opt.value = prop;
      opt.textContent = `${formatPropertyLabel(prop)} (${prop})`;
      propSelect.appendChild(opt);
    });
  };

  const handleEditRow = (localId) => {
    const cond = StateManager.getConditions().find((c) => c._localId === localId);
    if (!cond) return;

    editingLocalId = localId;
    getEl('conditionLocalId').value = localId;
    getEl('condFormHeading').textContent = `✏️ Chỉnh sửa điều kiện #${(cond.sortOrder ?? 0) + 1}`;
    getEl('btnCancelEditCond').classList.remove('d-none');
    getEl('btnSubmitCond').innerHTML = '<i data-lucide="check" class="lucide-sm me-1"></i> Cập nhật điều kiện';

    getEl('condSortOrder').value = cond.sortOrder ?? 0;
    getEl('condDataSource').value = cond.sourceCategory || 'SENSOR';
    handleDataSourceChange(cond.sourceCategory || 'SENSOR');

    if (cond.sourceTargetType) getEl('condCategory').value = cond.sourceTargetType;
    handleCategoryChange();

    if (cond.sourceTargetId) getEl('condTarget').value = cond.sourceTargetId;
    if (cond.property) getEl('condProperty').value = cond.property;
    getEl('condOperator').value = cond.operator || '>=';
    getEl('condValue').value = conditionValueFromUtc(cond.sourceCategory, cond.property, cond.value);

    if (cond.nextLogic === 'OR') {
      getEl('condNextLogicOr').checked = true;
    } else {
      getEl('condNextLogicAnd').checked = true;
    }

    if (window.lucide) window.lucide.createIcons();
  };

  const handleDeleteRow = (localId) => {
    StateManager.deleteCondition(localId);
    refreshTable();
    if (editingLocalId === localId) resetForm();
  };

  const resetForm = () => {
    editingLocalId = null;
    getEl('conditionForm')?.reset();
    getEl('conditionLocalId').value = '';
    getEl('condSortOrder').value = StateManager.getConditions().length;
    getEl('condFormHeading').textContent = '+ Thêm điều kiện';
    getEl('btnCancelEditCond').classList.add('d-none');
    getEl('btnSubmitCond').innerHTML = '<i data-lucide="check" class="lucide-sm me-1"></i> Lưu điều kiện vào danh sách';
    handleDataSourceChange('SENSOR');
    if (window.lucide) window.lucide.createIcons();
  };

  const handleSubmitForm = () => {
    const source = getEl('condDataSource').value;
    const property = getEl('condProperty').value;
    const rawVal = getEl('condValue').value.trim();

    if (!property || rawVal === '') {
      Toast.warning('Vui lòng chọn thuộc tính và nhập giá trị.');
      return;
    }

    const payload = {
      sortOrder: parseInt(getEl('condSortOrder').value || '0', 10),
      sourceCategory: source,
      sourceTargetType: (source === 'SENSOR' || source === 'DEVICE') ? getEl('condCategory').value : null,
      sourceTargetId: (source === 'SENSOR' || source === 'DEVICE') ? getEl('condTarget').value : null,
      property,
      operator: getEl('condOperator').value,
      value: conditionValueToUtc(source, property, rawVal),
      nextLogic: getEl('condNextLogicOr').checked ? 'OR' : 'AND',
    };

    if (editingLocalId) {
      StateManager.updateCondition(editingLocalId, payload);
    } else {
      StateManager.addCondition(payload);
    }

    refreshTable();
    resetForm();
  };

  const handleSaveBatch = async () => {
    const configId = StateManager.getActiveConfigId();
    if (!configId) return;

    const payload = StateManager.buildConditionsPayload();
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
