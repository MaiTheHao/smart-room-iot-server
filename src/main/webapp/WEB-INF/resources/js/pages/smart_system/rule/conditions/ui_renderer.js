import { TabulatorFull as Tabulator } from '../../../../lib/tabulator_esm.min.js';
import { StateManager } from './state_manager.js';
import { UTCUtils } from '../../../../common/utc_util.js';

const { i18n } = window.__CONDITIONS_CONFIG__;

const DS_BADGE = {
  SYSTEM: 'bg-info text-dark',
  ROOM: 'bg-primary',
  DEVICE: 'bg-success',
  SENSOR: 'bg-warning text-dark',
};

const OPERATOR_MAP = { '=': '=', '!=': '≠', '>': '>', '<': '<', '>=': '≥', '<=': '≤' };

const formatOperator = (op) => {
  return OPERATOR_MAP[op] ?? op;
};

const formatResourceParam = (ds, param) => {
  if (!param) return '—';
  const p = typeof param === 'string' ? JSON.parse(param) : param;
  switch (ds) {
    case 'SYSTEM':
      return p.property || '—';
    case 'ROOM':
      return `Room #${p.roomId} · ${p.property}`;
    case 'DEVICE':
      return `[${p.category}] Device #${p.deviceId} · ${p.property}`;
    case 'SENSOR':
      return `[${p.category}] Sensor #${p.sensorId} · ${p.property}`;
    default:
      return JSON.stringify(p);
  }
};

const formatValue = (row) => {
  const val = row.value;
  if (row.dataSource === 'SYSTEM') {
    const param = typeof row.resourceParam === 'string' ? JSON.parse(row.resourceParam) : row.resourceParam;
    const prop = param?.property;
    if (prop === 'current_time') {
      const num = parseFloat(val);
      if (!isNaN(num) && num >= 0 && num < 24) {
        const utcHour = Math.floor(num);
        const utcMin = Math.round((num - utcHour) * 60);
        const local = UTCUtils.utcToLocal(utcHour, utcMin, 0);
        const hh = String(local.hour).padStart(2, '0');
        const mm = String(local.minute).padStart(2, '0');
        return `${hh}:${mm}`;
      }
    } else if (prop === 'day_of_week') {
      const map = {
        1: 'Monday',
        2: 'Tuesday',
        3: 'Wednesday',
        4: 'Thursday',
        5: 'Friday',
        6: 'Saturday',
        7: 'Sunday',
      };
      return map[val] ? `${val} (${map[val]})` : val;
    }
  }
  return val;
};

export const UiRenderer = (() => {
  let table = null;

  const init = (onEdit, onDelete, onSelectionChange) => {
    table = new Tabulator('#conditionsTable', {
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
          <p>${i18n.noData}</p>
        </div>`,
      columns: [
        {
          title: '#',
          field: 'sortOrder',
          width: 60,
          hozAlign: 'center',
          formatter: (cell) =>
            `<div class="d-flex align-items-center justify-content-center h-100">
               <span class="badge bg-light text-dark border">${cell.getValue()}</span>
             </div>`,
        },
        {
          title: i18n.colDataSource,
          field: 'dataSource',
          width: 140,
          formatter: (cell) => {
            const val = cell.getValue();
            const cls = DS_BADGE[val] || 'bg-secondary';
            return `<div class="d-flex align-items-center h-100">
                      <span class="badge ${cls}">${val}</span>
                    </div>`;
          },
        },
        {
          title: i18n.colResource,
          minWidth: 150,
          formatter: (cell) => {
            const row = cell.getData();
            const text = formatResourceParam(row.dataSource, row.resourceParam);
            return `<div class="d-flex align-items-center h-100 text-truncate">${text}</div>`;
          },
        },
        {
          title: i18n.colOperator,
          field: 'operator',
          width: 150,
          hozAlign: 'center',
          formatter: (cell) =>
            `<div class="d-flex align-items-center justify-content-center h-100">
               <span class="badge bg-warning text-dark border font-monospace fs-6">${formatOperator(cell.getValue())}</span>
             </div>`,
        },
        {
          title: i18n.colValue,
          field: 'value',
          width: 150,
          hozAlign: 'center',
          formatter: (cell) => {
            const text = formatValue(cell.getData());
            return `<div class="d-flex align-items-center justify-content-center h-100 font-monospace fw-bold">${text}</div>`;
          },
        },
        {
          title: i18n.colLogic,
          field: 'nextLogic',
          width: 150,
          hozAlign: 'center',
          formatter: (cell) => {
            const val = cell.getValue();
            if (!val) return `<div class="d-flex align-items-center justify-content-center h-100 text-muted">—</div>`;
            const cls = val === 'AND' ? 'bg-primary' : 'bg-orange';
            return `<div class="d-flex align-items-center justify-content-center h-100">
                      <span class="badge ${cls}">${val}</span>
                    </div>`;
          },
        },
        {
          title: i18n.colActions,
          hozAlign: 'center',
          headerSort: false,
          width: 250,
          formatter: (cell) => {
            const id = cell.getData()._localId;
            return `
              <div class="d-flex align-items-center justify-content-center h-100 gap-1">
                <button class="btn btn-light btn-sm rounded-pill btn-cond-edit" data-id="${id}">
                  <i data-lucide="edit-3" class="lucide-sm text-primary"></i>
                </button>
              </div>`;
          },
        },
      ],
    });

    table.on('renderComplete', () => window.renderIcons?.());
    table.on('tableBuilt', () => window.renderIcons?.());
    table.on('rowSelectionChanged', (data) => onSelectionChange?.(data.length));

    document.addEventListener('click', (e) => {
      const btnEdit = e.target.closest('.btn-cond-edit');
      const btnDelete = e.target.closest('.btn-cond-delete');
      if (btnEdit) onEdit(btnEdit.dataset.id);
      if (btnDelete) onDelete(btnDelete.dataset.id);
    });
  };

  const render = () => {
    if (table) table.setData(StateManager.getConditions());
  };

  return { init, render, getSelectedData: () => table?.getSelectedData() || [] };
})();
