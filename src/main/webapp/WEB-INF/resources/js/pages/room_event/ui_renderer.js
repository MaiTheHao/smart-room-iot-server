/**
 * UI Renderer cho Trang Room Event
 * Quản lý Bảng Tabulator duy nhất và Modal Cài đặt sự kiện
 * Tuân thủ Clean Code & phong cách smart_system/rule
 */

import { TabulatorFull as Tabulator } from '../../../../lib/tabulator_esm.min.js';

export const UiRenderer = (() => {
  let table = null;
  let configModalInstance = null;
  let onSaveConfigCallback = null;

  const getEl = (id) => document.getElementById(id);

  const init = (callbacks = {}) => {
    initConfigModal(callbacks.onSaveConfig);
    initMainTable(callbacks);
  };

  const initConfigModal = (onSave) => {
    onSaveConfigCallback = onSave;
    const modalEl = getEl('roomEventConfigModal');
    if (!modalEl) return;
    configModalInstance = new bootstrap.Modal(modalEl);

    getEl('configForm')?.addEventListener('submit', (e) => {
      e.preventDefault();
      handleSubmitConfigForm();
    });
  };

  const initMainTable = (callbacks) => {
    const tableContainer = getEl('roomEventsTable');
    if (!tableContainer) return;

    table = new Tabulator('#roomEventsTable', {
      height: 'auto',
      layout: 'fitColumns',
      responsiveLayout: 'collapse',
      selectableRows: true,
      rowHeader: {
        formatter: 'rowSelection',
        titleFormatter: 'rowSelection',
        headerSort: false,
        resizable: false,
        frozen: true,
        headerHozAlign: 'center',
        hozAlign: 'center',
        width: 40,
      },
      placeholder: `
        <div class="text-center py-5 text-muted">
          <i data-lucide="inbox" class="mb-2" style="width: 48px; height: 48px"></i>
          <p>Chưa có cấu hình sự kiện nào cho phòng này.</p>
        </div>`,
      columns: [
        {
          title: 'Mã sự kiện',
          field: 'eventCode',
          minWidth: 160,
          formatter: (cell) => {
            const val = cell.getValue();
            return `
              <div class="d-flex align-items-center h-100 py-1">
                <span class="badge bg-light text-dark border font-monospace px-3 py-2 fs-6 fw-bold">${val}</span>
              </div>`;
          },
        },
        {
          title: 'Cooldown (s)',
          field: 'cooldownSeconds',
          width: 140,
          hozAlign: 'center',
          formatter: (cell) => {
            const val = cell.getValue() ?? 60;
            return `<div class="d-flex align-items-center justify-content-center h-100"><span class="fw-bold font-monospace text-primary">${val}s</span></div>`;
          },
        },
        {
          title: 'Trạng thái',
          field: 'isActive',
          width: 140,
          hozAlign: 'center',
          formatter: (cell) => {
            const isActive = cell.getValue() !== false;
            const id = cell.getData().id;
            return `
              <div class="d-flex align-items-center justify-content-center h-100">
                <div class="form-check form-switch switch-ios m-0">
                  <input class="form-check-input btn-toggle-active" type="checkbox" role="switch" data-id="${id}" ${isActive ? 'checked' : ''} title="Bật/Tắt sự kiện">
                </div>
              </div>`;
          },
        },
        {
          title: 'Thao tác',
          hozAlign: 'center',
          headerSort: false,
          width: 220,
          formatter: (cell) => {
            const data = cell.getData();
            return `
              <div class="d-flex align-items-center justify-content-center h-100 gap-1">
                <button type="button" class="btn btn-light btn-sm rounded-pill btn-manage-cond" data-id="${data.id}" data-code="${data.eventCode}" title="Quản lý Điều kiện">
                  <i data-lucide="filter" class="lucide-sm text-warning"></i>
                </button>
                <button type="button" class="btn btn-light btn-sm rounded-pill btn-manage-act" data-id="${data.id}" data-code="${data.eventCode}" title="Quản lý Hành động">
                  <i data-lucide="settings-2" class="lucide-sm text-info"></i>
                </button>
                <button type="button" class="btn btn-light btn-sm rounded-pill btn-edit-cfg" data-id="${data.id}" data-code="${data.eventCode}" title="Cài đặt sự kiện">
                  <i data-lucide="edit-3" class="lucide-sm text-primary"></i>
                </button>
                <button type="button" class="btn btn-light btn-sm rounded-pill btn-del-cfg" data-id="${data.id}" data-code="${data.eventCode}" title="Xóa cấu hình">
                  <i data-lucide="trash-2" class="lucide-sm text-danger"></i>
                </button>
              </div>`;
          },
        },
      ],
    });

    table.on('renderComplete', () => {
      if (window.lucide) window.lucide.createIcons();
    });

    document.addEventListener('click', (e) => {
      const btnCond = e.target.closest('.btn-manage-cond');
      const btnAct = e.target.closest('.btn-manage-act');
      const btnEdit = e.target.closest('.btn-edit-cfg');
      const btnDel = e.target.closest('.btn-del-cfg');

      if (btnCond) callbacks.onOpenConditions?.(btnCond.dataset.id, btnCond.dataset.code);
      else if (btnAct) callbacks.onOpenActions?.(btnAct.dataset.id, btnAct.dataset.code);
      else if (btnEdit) callbacks.onOpenEditConfig?.(btnEdit.dataset.id);
      else if (btnDel) callbacks.onDeleteConfig?.(btnDel.dataset.id, btnDel.dataset.code);
    });

    document.addEventListener('change', (e) => {
      const toggle = e.target.closest('.btn-toggle-active');
      if (toggle) {
        callbacks.onToggleStatus?.(toggle.dataset.id, toggle.checked);
      }
    });
  };

  const renderTable = (configs) => {
    if (table) {
      table.setData(configs);
    }
  };

  const openCreateConfigModal = (unconfiguredCodes = []) => {
    getEl('configForm')?.reset();
    getEl('cfgConfigId').value = '';
    getEl('configModalTitle').textContent = 'Thêm cấu hình sự kiện mới';

    const codeGroup = getEl('cfgEventCodeGroup');
    codeGroup.classList.remove('d-none');
    const select = getEl('cfgEventCode');
    select.innerHTML = '<option value="" disabled selected>-- Chọn loại sự kiện --</option>';

    if (unconfiguredCodes.length === 0) {
      select.innerHTML = '<option value="" disabled selected>-- Tất cả sự kiện đã được cấu hình --</option>';
      select.disabled = true;
    } else {
      select.disabled = false;
      unconfiguredCodes.forEach((c) => {
        const opt = document.createElement('option');
        opt.value = c.code;
        opt.textContent = `${c.code}${c.description ? ` - ${c.description}` : ''}`;
        select.appendChild(opt);
      });
    }

    getEl('cfgCooldown').value = '60';
    getEl('cfgIsActive').checked = true;

    configModalInstance?.show();
    if (window.lucide) window.lucide.createIcons();
  };

  const openEditConfigModal = (config) => {
    if (!config) return;
    getEl('configForm')?.reset();
    getEl('cfgConfigId').value = config.id;
    getEl('configModalTitle').textContent = `Cài đặt sự kiện: ${config.eventCode}`;

    const codeGroup = getEl('cfgEventCodeGroup');
    codeGroup.classList.add('d-none');

    getEl('cfgCooldown').value = config.cooldownSeconds ?? 60;
    getEl('cfgIsActive').checked = config.isActive !== false;

    configModalInstance?.show();
    if (window.lucide) window.lucide.createIcons();
  };

  const handleSubmitConfigForm = () => {
    const configId = getEl('cfgConfigId').value;
    const cooldownSeconds = parseInt(getEl('cfgCooldown').value || '0', 10);
    const isActive = getEl('cfgIsActive').checked;

    if (configId) {
      // EDIT MODE
      onSaveConfigCallback?.({
        isEdit: true,
        configId,
        data: { cooldownSeconds, isActive },
      });
    } else {
      // CREATE MODE
      const eventCode = getEl('cfgEventCode').value;
      if (!eventCode) return;
      onSaveConfigCallback?.({
        isEdit: false,
        data: { eventCode, cooldownSeconds, isActive },
      });
    }

    configModalInstance?.hide();
  };

  return {
    init,
    renderTable,
    openCreateConfigModal,
    openEditConfigModal,
  };
})();
