import { TabulatorFull as Tabulator } from '../../../../lib/tabulator_esm.min.js';
import { StateManager } from './state_manager.js';

const { i18n } = window.__RULE_CONFIG__;

export const UiRenderer = (() => {
  let table = null;

  const init = (onEdit, onExecute, onToggleStatus, onSelectionChange) => {
    table = new Tabulator('#rulesTable', {
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
      pagination: true,
      paginationMode: 'local',
      paginationSize: 10,
      paginationSizeSelector: [10, 25, 50, 100],
      paginationCounter: 'rows',
      columns: [
        {
          title: i18n.colId,
          field: 'id',
          minWidth: 60,
          hozAlign: 'center',
          formatter: (cell) => `<div class="d-flex align-items-center justify-content-center h-100"><span class="fw-medium text-muted">#${cell.getValue()}</span></div>`,
        },
        {
          title: i18n.colName,
          field: 'name',
          headerFilter: 'input',
          minWidth: 120,
          formatter: (cell) => {
            return `<div class="d-flex flex-column justify-content-center h-100 py-1">
                      <div class="fw-bold text-dark small">${cell.getValue()}</div>
                    </div>`;
          },
        },
        {
          title: i18n.colPriority,
          field: 'priority',
          minWidth: 100,
          hozAlign: 'center',
          formatter: (cell) => `<div class="d-flex align-items-center justify-content-center h-100"><span class="badge bg-secondary rounded-pill">${cell.getValue() !== undefined ? cell.getValue() : 1}</span></div>`,
        },
        {
          title: i18n.colInterval,
          field: 'intervalSeconds',
          minWidth: 100,
          hozAlign: 'center',
          formatter: (cell) => `<div class="d-flex align-items-center justify-content-center h-100"><span class="fw-semibold text-primary">${cell.getValue() || 60}s</span></div>`,
        },
        {
          title: i18n.colStatus,
          field: 'isActive',
          width: 150,
          minWidth: 100,
          hozAlign: 'center',
          formatter: (cell) => {
            const isActive = cell.getValue();
            return `<div class="d-flex align-items-center justify-content-center h-100">
                      <div class="form-check form-switch switch-ios m-0">
                        <input class="form-check-input btn-toggle-status" type="checkbox" role="switch" data-id="${cell.getData().id}" ${isActive ? 'checked' : ''} title="Toggle Active">
                      </div>
                    </div>`;
          },
        },
        {
          title: i18n.colActions,
          hozAlign: 'center',
          headerSort: false,
          width: 250,
          responsive: 0,
          formatter: (cell) => {
            const data = cell.getData();
            return `
              <div class="d-flex align-items-center justify-content-center h-100 gap-1">
                <a href="/management/smart-system/rules/${data.id}/conditions" class="btn btn-light btn-sm rounded-pill" title="${i18n.manageConditions}">
                  <i data-lucide="filter" class="lucide-sm text-warning"></i>
                </a>
                <a href="/management/smart-system/rules/${data.id}/actions" class="btn btn-light btn-sm rounded-pill" title="${i18n.manageActions}">
                  <i data-lucide="settings-2" class="lucide-sm text-info"></i>
                </a>
                <button class="btn btn-light btn-sm rounded-pill btn-execute" data-id="${data.id}" title="Run Now">
                  <i data-lucide="play" class="lucide-sm text-success"></i>
                </button>
                <button class="btn btn-light btn-sm rounded-pill btn-edit" data-id="${data.id}" title="${i18n.editTitle}">
                  <i data-lucide="edit-3" class="lucide-sm text-primary"></i>
                </button>
              </div>`;
          },
        },
      ],
    });

    table.on('rowSelectionChanged', (data) => onSelectionChange?.(data.length));
    table.on('renderComplete', () => window.renderIcons?.());

    document.addEventListener('click', (e) => {
      const btnEdit = e.target.closest('.btn-edit');
      const btnExecute = e.target.closest('.btn-execute');

      if (btnEdit) {
        const id = btnEdit.dataset.id;
        const rule = StateManager.getRule(id);
        if (rule) onEdit?.(rule);
      } else if (btnExecute) {
        const id = btnExecute.dataset.id;
        onExecute?.(id, btnExecute);
      }
    });

    document.addEventListener('change', (e) => {
      const toggle = e.target.closest('.btn-toggle-status');
      if (toggle) {
        onToggleStatus?.(toggle.dataset.id, toggle.checked, toggle);
      }
    });
  };

  const render = () => {
    if (table) {
      table.setData(StateManager.getRules());
    }
  };

  const getSelectedData = () => table?.getSelectedData() || [];

  return {
    init,
    render,
    getSelectedData,
  };
})();
