import { TabulatorFull as Tabulator } from '../../../../lib/tabulator_esm.min.js';
import { StateManager } from './state_manager.js';

export const UiRenderer = (() => {
    let table = null;

    const init = (onEdit, onDelete) => {
        table = new Tabulator('#conditionsTable', {
            height: 'auto',
            layout: 'fitColumns',
            responsiveLayout: 'collapse',
            placeholder: `
                <div class="text-center py-5 text-muted">
                    <i data-lucide="inbox" class="mb-2" style="width: 48px; height: 48px"></i>
                    <p>No conditions added yet.</p>
                </div>`,
            columns: [
                {
                    title: 'Sensor ID',
                    field: 'sensorId',
                    width: 120,
                    hozAlign: 'center',
                    formatter: (cell) => `<div class="d-flex align-items-center justify-content-center h-100 fw-bold">${cell.getValue()}</div>`
                },
                {
                    title: 'Metric',
                    field: 'metricType',
                    width: 150,
                    formatter: (cell) => `<div class="d-flex align-items-center h-100"><span class="badge bg-secondary">${cell.getValue()}</span></div>`
                },
                {
                    title: 'Operator',
                    field: 'operator',
                    hozAlign: 'center',
                    width: 100,
                    formatter: (cell) => {
                        const val = cell.getValue();
                        const op = val === 'GREATER_THAN' ? '>' : val === 'LESS_THAN' ? '<' : '==';
                        return `<div class="d-flex align-items-center justify-content-center h-100"><span class="badge bg-warning text-dark border">${op}</span></div>`;
                    }
                },
                {
                    title: 'Threshold',
                    field: 'threshold',
                    width: 120,
                    hozAlign: 'center',
                    formatter: (cell) => `<div class="d-flex align-items-center justify-content-center h-100 font-monospace">${cell.getValue()}</div>`
                },
                {
                    title: 'Actions',
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
        if(table) table.setData(StateManager.getConditions());
    };

    return { init, render };
})();
