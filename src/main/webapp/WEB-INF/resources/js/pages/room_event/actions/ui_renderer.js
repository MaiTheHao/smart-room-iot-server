import { TabulatorFull as Tabulator } from '../../../lib/tabulator_esm.min.js';
import { StateManager } from './state_manager.js';

const { i18n } = window.__ROOM_EVENT_ACTIONS_CONFIG__;

const CATEGORY_BADGES = {
  LIGHT: 'bg-warning text-dark',
  FAN: 'bg-info text-dark',
  AIR_CONDITION: 'bg-primary',
};

const formatParams = (params) => {
  if (!params) return '—';
  let obj = params;
  if (typeof params === 'string') {
    try {
      obj = JSON.parse(params);
    } catch {
      return params;
    }
  }

  const entries = Object.entries(obj).filter(([, v]) => v !== '' && v !== null && v !== undefined);
  if (entries.length === 0) return '—';

  return entries
    .map(([k, v]) => `<span class="badge bg-light text-dark border me-1 font-monospace">${k}: <strong>${v}</strong></span>`)
    .join('');
};

export const UiRenderer = (() => {
  let table = null;

  const init = (onEdit, onDelete, onSelectionChange) => {
    table = new Tabulator('#actionsTable', {
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
          <p>${i18n.noData || 'Chưa có hành động nào.'}</p>
        </div>`,
      columns: [
        {
          title: '#',
          field: 'executionOrder',
          width: 60,
          hozAlign: 'center',
          formatter: (cell) =>
            `<div class="d-flex align-items-center justify-content-center h-100">
               <span class="badge bg-light text-dark border">${cell.getValue()}</span>
             </div>`,
        },
        {
          title: i18n.colTargetDevice || 'Thiết bị mục tiêu',
          field: 'targetId',
          width: 180,
          formatter: (cell) => {
            const row = cell.getData();
            const id = row.targetId || row.targetDeviceId || '—';
            const cat = row.targetCategory || row.targetDeviceCategory || '';
            const cls = CATEGORY_BADGES[cat] || 'bg-secondary';
            return `<div class="d-flex align-items-center gap-2 h-100">
                      <span class="badge ${cls}">${cat || 'DEVICE'}</span>
                      <span class="font-monospace fw-bold">#${id}</span>
                    </div>`;
          },
        },
        {
          title: i18n.colParams || 'Tham số thực thi',
          field: 'params',
          formatter: (cell) => {
            const row = cell.getData();
            const raw = row.params || row.actionParams;
            return `<div class="d-flex align-items-center flex-wrap h-100">${formatParams(raw)}</div>`;
          },
        },
        {
          title: i18n.colActions || 'Thao tác',
          headerSort: false,
          width: 110,
          hozAlign: 'center',
          formatter: (cell) => {
            const localId = cell.getData()._localId;
            return `
              <div class="d-flex justify-content-center gap-1">
                <button type="button" class="btn btn-light btn-sm rounded-pill btn-act-edit" data-id="${localId}" title="Sửa">
                  <i data-lucide="pencil" class="lucide-sm text-primary"></i>
                </button>
                <button type="button" class="btn btn-light btn-sm rounded-pill btn-act-delete" data-id="${localId}" title="Xóa">
                  <i data-lucide="trash-2" class="lucide-sm text-danger"></i>
                </button>
              </div>`;
          },
        },
      ],
    });

    const tableEl = document.getElementById('actionsTable');
    if (tableEl && !tableEl.dataset.delegationBound) {
      tableEl.dataset.delegationBound = 'true';

      tableEl.addEventListener('click', (e) => {
        const btnEdit = e.target.closest('.btn-act-edit');
        if (btnEdit && btnEdit.dataset.id) {
          onEdit(btnEdit.dataset.id);
          return;
        }

        const btnDelete = e.target.closest('.btn-act-delete');
        if (btnDelete && btnDelete.dataset.id) {
          onDelete(btnDelete.dataset.id);
          return;
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

  const render = () => {
    if (!table) return;
    table.setData(StateManager.getActions());
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
