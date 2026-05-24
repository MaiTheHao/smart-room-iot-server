import { TabulatorFull as Tabulator } from '../../../../lib/tabulator_esm.min.js';
import { StateManager } from './state_manager.js';

const { i18n } = window.__ACTIONS_CONFIG__;

export const UiRenderer = (() => {
    let table = null;

    const init = (onEdit, onDelete) => {
        table = new Tabulator('#actionsTable', {
            height: 'auto',
            layout: 'fitColumns',
            responsiveLayout: 'collapse',
            placeholder: `
                <div class="text-center py-5 text-muted">
                    <i data-lucide="inbox" class="mb-2" style="width: 48px; height: 48px"></i>
                    <p>${i18n.noData}</p>
                </div>`,
            columns: [
                {
                    title: i18n.colOrder,
                    field: 'executionOrder',
                    width: 80,
                    hozAlign: 'center',
                    formatter: (cell) => `<div class="d-flex align-items-center justify-content-center h-100"><span class="badge bg-light text-dark border">${cell.getValue()}</span></div>`
                },
                {
                    title: i18n.colTargetDevice,
                    field: 'targetId',
                    formatter: (cell) => `<div class="d-flex align-items-center h-100 fw-bold">${cell.getValue()}</div>`
                },
                {
                    title: i18n.colType,
                    field: 'targetType',
                    width: 120,
                    formatter: (cell) => `<div class="d-flex align-items-center h-100"><span class="badge bg-secondary">${cell.getValue()}</span></div>`
                },
                {
                    title: i18n.colAction,
                    field: 'actionType',
                    width: 120,
                    formatter: (cell) => {
                        const val = cell.getValue();
                        const colorClass = val === 'ON' ? 'bg-success' : val === 'OFF' ? 'bg-danger' : 'bg-primary';
                        return `<div class="d-flex align-items-center h-100"><span class="badge ${colorClass}">${val}</span></div>`;
                    }
                },
                {
                    title: i18n.colParams,
                    field: 'parameterValue',
                    formatter: (cell) => `<div class="d-flex align-items-center h-100 text-muted">${cell.getValue() || '-'}</div>`
                },
                {
                    title: i18n.colActions,
                    hozAlign: 'center',
                    headerSort: false,
                    width: 100,
                    formatter: (cell) => {
                        const id = cell.getData()._localId;
                        return `
                            <div class="d-flex align-items-center justify-content-center h-100 gap-1">
                                <button class="btn btn-light btn-sm rounded-pill btn-edit" data-id="${id}">
                                    <i data-lucide="edit-3" class="lucide-sm text-primary"></i>
                                </button>
                                <button class="btn btn-light btn-sm rounded-pill btn-delete" data-id="${id}">
                                    <i data-lucide="trash-2" class="lucide-sm text-danger"></i>
                                </button>
                             </div>`;
                    }
                }
            ]
        });

        table.on('renderComplete', () => window.renderIcons?.());

        document.addEventListener('click', (e) => {
            const btnEdit = e.target.closest('.btn-edit');
            const btnDelete = e.target.closest('.btn-delete');
            if (btnEdit) onEdit(btnEdit.dataset.id);
            if (btnDelete) onDelete(btnDelete.dataset.id);
        });
    };

    const render = () => {
        if(table) table.setData(StateManager.getActions());
    };

    return { init, render };
})();
