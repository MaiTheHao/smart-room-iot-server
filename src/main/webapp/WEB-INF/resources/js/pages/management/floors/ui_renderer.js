import { TabulatorFull as Tabulator } from '../../../lib/tabulator_esm.min.js';
import { getFloors } from '../../../api/floor.api.js';
import { StateManager } from './state_manager.js';

export const UiRenderer = (() => {
	let table = null;
	let isLoadingData = false;

	const init = (onEditRow, onSelectionChange) => {
		const i18n = StateManager.getI18n();

		table = new Tabulator('#floorsTable', {
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
					<p>${i18n.noData || ''}</p>
				</div>`,
			columns: [
				{
					title: i18n.colId,
					field: 'id',
					width: 80,
					hozAlign: 'center',
					formatter: (cell) => `<div class="d-flex align-items-center justify-content-center h-100"><span class="fw-medium text-muted">#${cell.getValue()}</span></div>`,
				},
				{
					title: i18n.colName,
					field: 'name',
					headerFilter: 'input',
					headerFilterPlaceholder: i18n.placeholderSearch,
					formatter: (cell) => `<div class="d-flex align-items-center h-100 py-1"><div class="fw-bold text-dark">${cell.getValue()}</div></div>`,
				},
				{
					title: i18n.colCode,
					field: 'code',
					headerFilter: 'input',
					formatter: (cell) => `<div class="d-flex align-items-center h-100"><span class="badge bg-light text-dark border">${cell.getValue()}</span></div>`,
				},
				{
					title: i18n.colLevel,
					field: 'level',
					hozAlign: 'center',
					width: 100,
					formatter: (cell) => `<div class="d-flex align-items-center justify-content-center h-100"><span class="fw-bold text-primary">${cell.getValue()}</span></div>`,
				},
				{
					title: i18n.colDescription,
					field: 'description',
					formatter: (cell) => `<div class="d-flex align-items-center h-100 text-muted small">${cell.getValue() || '--'}</div>`,
				},
				{
					title: i18n.colActions,
					hozAlign: 'center',
					headerSort: false,
					width: 100,
					formatter: (cell) => {
						const data = cell.getData();
						return `
							<div class="d-flex align-items-center justify-content-center h-100">
								<button class="btn btn-light btn-sm rounded-pill btn-edit" data-id="${data.id}" title="${i18n.editTitle}">
									<i data-lucide="edit-3" class="lucide-sm text-primary"></i>
								</button>
							</div>`;
					},
				},
			],
		});

		table.on('tableBuilt', refresh);
		table.on('rowSelectionChanged', (data) => onSelectionChange?.(data.length));
		table.on('renderComplete', () => window.renderIcons?.());

		document.addEventListener('click', (e) => {
			const btnEdit = e.target.closest('.btn-edit');
			if (btnEdit) {
				const row = table.getRow(btnEdit.dataset.id);
				if (row) onEditRow?.(row.getData());
			}
		});
	};

	const refresh = async () => {
		if (isLoadingData) return;
		const btnReload = document.getElementById('btnReload');
		const icon = btnReload?.querySelector('[data-lucide="refresh-cw"]');

		try {
			isLoadingData = true;
			if (btnReload) btnReload.disabled = true;
			if (icon) icon.classList.add('lucide-spin');

			const [err, res] = await getFloors(0, 1000);
			if (err) throw err;
			table.setData(res.data.content || []);
		} catch (err) {
			console.error('Refresh error:', err);
		} finally {
			isLoadingData = false;
			if (btnReload) btnReload.disabled = false;
			if (icon) icon.classList.remove('lucide-spin');
			window.renderIcons?.();
		}
	};

	const getSelectedData = () => table?.getSelectedData() || [];

	return {
		init,
		refresh,
		getSelectedData,
	};
})();
