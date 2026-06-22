import { TabulatorFull as Tabulator } from '../../../../lib/tabulator_esm.min.js';

const { i18n } = window.__ALERT_MANAGE_CONFIG__;

const SEVERITY_BADGE = {
  INFO:     'bg-info text-dark',
  WARNING:  'bg-warning text-dark',
  CRITICAL: 'bg-danger text-white',
};

const NS_BADGE = {
  RULE:    'bg-primary-subtle text-primary',
  GATEWAY: 'bg-purple-subtle text-purple',
  SYSTEM:  'bg-secondary-subtle text-secondary',
};

export const UiRenderer = (() => {
  let table = null;

  const init = (onEdit, onDelete, onSelectionChange) => {
    table = new Tabulator('#alertConfigsTable', {
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
          <i data-lucide="inbox" class="mb-2" style="width:48px;height:48px"></i>
          <p>${i18n.noData || 'No alert configs found.'}</p>
        </div>`,
      columns: [
        {
          title: i18n.colAlertName || 'Alert Name',
          field: 'alertName',
          minWidth: 130,
          formatter: (cell) => `<div class="d-flex align-items-center h-100 fw-bold text-dark">${cell.getValue()}</div>`,
        },
        {
          title: i18n.colAlertCode || 'Alert Code',
          field: 'alertCode',
          minWidth: 130,
          formatter: (cell) => `<div class="d-flex align-items-center h-100 font-monospace text-muted small">${cell.getValue() || '—'}</div>`,
        },
        {
          title: i18n.colNamespace || 'Namespace',
          field: 'namespace',
          width: 110,
          hozAlign: 'center',
          formatter: (cell) => {
            const ns = cell.getValue();
            const cls = NS_BADGE[ns] || 'bg-light text-dark';
            return `<div class="d-flex align-items-center justify-content-center h-100">
                      <span class="badge ${cls}">${ns || '—'}</span>
                    </div>`;
          },
        },
        {
          title: i18n.colSourceId || 'Source ID',
          field: 'sourceId',
          minWidth: 100,
          formatter: (cell) => `<div class="d-flex align-items-center h-100 font-monospace text-muted small">${cell.getValue() || '—'}</div>`,
        },
        {
          title: i18n.colSeverity || 'Severity',
          field: 'severity',
          width: 110,
          hozAlign: 'center',
          formatter: (cell) => {
            const val = cell.getValue();
            return `<div class="d-flex align-items-center justify-content-center h-100">
                      <span class="badge ${SEVERITY_BADGE[val] || 'bg-secondary'}">${val}</span>
                    </div>`;
          },
        },
        {
          title: i18n.colCooldown || 'Cooldown',
          field: 'cooldownMinutes',
          width: 120,
          hozAlign: 'center',
          formatter: (cell) => `<div class="d-flex align-items-center justify-content-center h-100 text-muted">${cell.getValue()} ${i18n.colCooldownUnit || 'mins'}</div>`,
        },
        {
          title: i18n.colChannels || 'Channels',
          field: 'channels',
          minWidth: 120,
          formatter: (cell) => {
            const val = cell.getValue() || [];
            return `<div class="d-flex flex-wrap gap-1 align-items-center h-100">
              ${val.map(c => `<span class="badge bg-light text-dark border">${c}</span>`).join('')}
            </div>`;
          },
        },
        {
          title: i18n.colActions || 'Actions',
          hozAlign: 'center',
          headerSort: false,
          width: 100,
          responsive: 0,
          formatter: (cell) => {
            const id = cell.getData().id;
            return `
              <div class="d-flex align-items-center justify-content-center h-100 gap-1">
                <button class="btn btn-light btn-sm rounded-pill btn-cfg-edit" data-id="${id}" title="${i18n.edit || 'Edit'}">
                  <i data-lucide="edit-3" class="lucide-sm text-primary"></i>
                </button>
                <button class="btn btn-light btn-sm rounded-pill btn-cfg-delete" data-id="${id}" title="${i18n.delete || 'Delete'}">
                  <i data-lucide="trash-2" class="lucide-sm text-danger"></i>
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
      const btnEdit   = e.target.closest('.btn-cfg-edit');
      const btnDelete = e.target.closest('.btn-cfg-delete');
      if (btnEdit)   onEdit(btnEdit.dataset.id);
      if (btnDelete) onDelete(btnDelete.dataset.id);
    });
  };

  const setData = (rows) => table?.setData(rows);

  return {
    init,
    setData,
    getSelectedData: () => table?.getSelectedData() || [],
  };
})();
