import { TabulatorFull as Tabulator } from '../../../lib/tabulator_esm.min.js';
import { StateManager } from './state_manager.js';
import { UTCUtils } from '../../../common/utc_util.js';
import { formatPropertyLabel, formatDayOfWeek } from '../../../common/smart_system_util.js';

const { i18n } = window.__ROOM_EVENT_CONDITIONS_CONFIG__;

const DS_BADGE = {
  SYSTEM: 'bg-info text-dark',
  ROOM: 'bg-primary',
  DEVICE: 'bg-success',
  SENSOR: 'bg-warning text-dark',
};

const OPERATOR_MAP = { '=': '=', '!=': '≠', '>': '>', '<': '<', '>=': '≥', '<=': '≤' };

const formatOperator = (op) => OPERATOR_MAP[op] ?? op;

const formatResourceParam = (row) => {
  const ds = row.sourceCategory || row.dataSource;
  const prop = row.property || row.resourceParam?.property;
  const propLabel = formatPropertyLabel(prop, i18n);
  const targetId = row.sourceTargetId || row.resourceParam?.deviceId || row.resourceParam?.sensorId || row.resourceParam?.roomId;
  const targetType = row.sourceTargetType || row.resourceParam?.category;

  switch (ds) {
    case 'SYSTEM':
      return `<span class="badge bg-light text-dark border me-1">SYSTEM</span> ${propLabel}`;
    case 'ROOM':
      return `<span class="badge bg-light text-dark border me-1">ROOM</span> ${propLabel}`;
    case 'DEVICE':
      return `<span class="badge bg-light text-dark border me-1">${targetType || 'DEVICE'} #${targetId}</span> ${propLabel}`;
    case 'SENSOR':
      return `<span class="badge bg-light text-dark border me-1">${targetType || 'SENSOR'} #${targetId}</span> ${propLabel}`;
    default:
      return propLabel || '—';
  }
};

const formatTimeValue = (val) => {
  const num = parseFloat(val);
  if (isNaN(num) || num < 0 || num >= 24) return val;
  const utcHour = Math.floor(num);
  const utcMin = Math.round((num - utcHour) * 60);
  const local = UTCUtils.utcToLocal(utcHour, utcMin, 0);
  const hh = String(local.hour).padStart(2, '0');
  const mm = String(local.minute).padStart(2, '0');
  return `${hh}:${mm}`;
};

const formatValue = (row) => {
  const ds = row.sourceCategory || row.dataSource;
  const prop = row.property || row.resourceParam?.property;
  if (ds === 'SYSTEM' && prop === 'current_time') return formatTimeValue(row.value);
  if (ds === 'SYSTEM' && prop === 'day_of_week') return formatDayOfWeek(row.value);
  return row.value;
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
          <p>${i18n.noData || 'Chưa có điều kiện nào.'}</p>
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
          title: i18n.colDataSource || 'Nguồn dữ liệu',
          field: 'sourceCategory',
          width: 140,
          formatter: (cell) => {
            const row = cell.getData();
            const val = row.sourceCategory || row.dataSource || '—';
            const cls = DS_BADGE[val] || 'bg-secondary';
            return `<div class="d-flex align-items-center h-100">
                      <span class="badge ${cls}">${val}</span>
                    </div>`;
          },
        },
        {
          title: i18n.colProperty || 'Thuộc tính',
          field: 'property',
          formatter: (cell) => {
            const row = cell.getData();
            return `<div class="d-flex align-items-center h-100">${formatResourceParam(row)}</div>`;
          },
        },
        {
          title: i18n.colOperator || 'Toán tử',
          field: 'operator',
          width: 90,
          hozAlign: 'center',
          formatter: (cell) =>
            `<div class="d-flex align-items-center justify-content-center h-100">
               <span class="font-monospace fw-bold">${formatOperator(cell.getValue())}</span>
             </div>`,
        },
        {
          title: i18n.colValue || 'Giá trị',
          field: 'value',
          formatter: (cell) => {
            const row = cell.getData();
            return `<div class="d-flex align-items-center h-100 font-monospace">${formatValue(row)}</div>`;
          },
        },
        {
          title: i18n.colLogic || 'Logic tiếp theo',
          field: 'nextLogic',
          width: 110,
          hozAlign: 'center',
          formatter: (cell) => {
            const val = cell.getValue();
            if (!val) return '<span class="text-muted small">—</span>';
            const cls = val === 'AND' ? 'bg-primary-subtle text-primary' : 'bg-warning-subtle text-warning';
            return `<span class="badge ${cls} border">${val}</span>`;
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
                <button type="button" class="btn btn-light btn-sm rounded-pill btn-cond-edit" data-id="${localId}" title="Sửa">
                  <i data-lucide="pencil" class="lucide-sm text-primary"></i>
                </button>
                <button type="button" class="btn btn-light btn-sm rounded-pill btn-cond-delete" data-id="${localId}" title="Xóa">
                  <i data-lucide="trash-2" class="lucide-sm text-danger"></i>
                </button>
              </div>`;
          },
        },
      ],
    });

    const tableEl = document.getElementById('conditionsTable');
    if (tableEl && !tableEl.dataset.delegationBound) {
      tableEl.dataset.delegationBound = 'true';

      tableEl.addEventListener('click', (e) => {
        const btnEdit = e.target.closest('.btn-cond-edit');
        if (btnEdit && btnEdit.dataset.id) {
          onEdit(btnEdit.dataset.id);
          return;
        }

        const btnDelete = e.target.closest('.btn-cond-delete');
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
    table.setData(StateManager.getConditions());
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
