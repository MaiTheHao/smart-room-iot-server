import { TabulatorFull as Tabulator } from '../../lib/tabulator_esm.min.js';
import { StateManager } from './state_manager.js';

const { roomId, i18n } = window.__ROOM_EVENT_PAGE_CONFIG__;

export const UiRenderer = (() => {
  let table = null;

  const init = (onEdit, onDelete, onToggleStatus, onSelectionChange) => {
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
          <p>${i18n.noData || 'Chưa có cấu hình sự kiện nào.'}</p>
        </div>`,
      columns: [
        {
          title: i18n.colCode || 'Sự kiện',
          field: 'eventCode',
          formatter: (cell) => {
            const data = cell.getData();
            return `
              <div class="d-flex align-items-center gap-2 h-100">
                <span class="badge bg-light text-primary border font-monospace px-2 py-1 fw-bold">${data.eventCode}</span>
                <span class="text-muted small font-monospace">#${data.id}</span>
              </div>`;
          },
        },
        {
          title: i18n.colCooldown || 'Cooldown (s)',
          field: 'cooldownSeconds',
          width: 150,
          hozAlign: 'center',
          formatter: (cell) =>
            `<div class="d-flex align-items-center justify-content-center h-100">
               <span class="font-monospace fw-semibold">${cell.getValue()}s</span>
             </div>`,
        },
        {
          title: i18n.colStatus || 'Trạng thái',
          field: 'isActive',
          width: 130,
          hozAlign: 'center',
          formatter: (cell) => {
            const checked = cell.getValue() ? 'checked' : '';
            const id = cell.getData().id;
            return `
              <div class="d-flex align-items-center justify-content-center h-100">
                <div class="form-check form-switch m-0">
                  <input class="form-check-input btn-status-toggle" type="checkbox" role="switch" data-id="${id}" ${checked}>
                </div>
              </div>`;
          },
        },
        {
          title: i18n.colActions || 'Thao tác',
          headerSort: false,
          width: 180,
          hozAlign: 'center',
          formatter: (cell) => {
            const data = cell.getData();
            return `
              <div class="d-flex justify-content-center gap-1">
                <a href="/rooms/${roomId}/events/${data.id}/conditions" class="btn btn-light btn-sm rounded-pill" title="${i18n.titleConditions || 'Điều kiện'}">
                  <i data-lucide="filter" class="lucide-sm text-warning"></i>
                </a>
                <a href="/rooms/${roomId}/events/${data.id}/actions" class="btn btn-light btn-sm rounded-pill" title="${i18n.titleActions || 'Hành động'}">
                  <i data-lucide="settings-2" class="lucide-sm text-info"></i>
                </a>
                <button type="button" class="btn btn-light btn-sm rounded-pill btn-edit" data-id="${data.id}" title="${i18n.titleSettings || 'Cài đặt'}">
                  <i data-lucide="pencil" class="lucide-sm text-primary"></i>
                </button>
                <button type="button" class="btn btn-light btn-sm rounded-pill btn-delete" data-id="${data.id}" title="${i18n.titleDelete || 'Xóa'}">
                  <i data-lucide="trash-2" class="lucide-sm text-danger"></i>
                </button>
              </div>`;
          },
        },
      ],
    });

    // Event delegation on table container
    const tableEl = document.getElementById('roomEventsTable');
    if (tableEl && !tableEl.dataset.delegationBound) {
      tableEl.dataset.delegationBound = 'true';

      tableEl.addEventListener('click', (e) => {
        const btnEdit = e.target.closest('.btn-edit');
        if (btnEdit && btnEdit.dataset.id) {
          onEdit(btnEdit.dataset.id);
          return;
        }

        const btnDelete = e.target.closest('.btn-delete');
        if (btnDelete && btnDelete.dataset.id) {
          onDelete(btnDelete.dataset.id);
          return;
        }
      });

      tableEl.addEventListener('change', (e) => {
        const switchEl = e.target.closest('.btn-status-toggle');
        if (switchEl && switchEl.dataset.id) {
          onToggleStatus(switchEl.dataset.id, switchEl.checked);
        }
      });
    }

    table.on('rowSelectionChanged', (_data, rows) => {
      onSelectionChange(rows.length);
    });

    table.on('renderComplete', () => {
      if (typeof lucide !== 'undefined') lucide.createIcons();
    });
  };

  const render = (data = null) => {
    if (!table) return;
    table.setData(data || StateManager.getConfigs());
  };

  const getSelectedData = () => {
    return table ? table.getSelectedData() : [];
  };

  return {
    init,
    render,
    getSelectedData,
  };
})();
