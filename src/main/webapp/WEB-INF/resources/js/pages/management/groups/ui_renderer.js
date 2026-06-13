import { TabulatorFull as Tabulator } from '../../../lib/tabulator_esm.min.js';
import { getGroups, getClientsCountByGroup } from '../../../api/group.api.js';
import { StateManager } from './state_manager.js';

export const UiRenderer = (() => {
	let table = null;
	let isLoadingData = false;

	const init = (onEditRow, onManageMembers, onSelectionChange) => {
		const i18n = StateManager.getI18n();

		table = new Tabulator('#groupsTable', {
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
			pagination: true,
			paginationMode: 'local',
			paginationSize: 10,
			paginationSizeSelector: [10, 25, 50, 100],
			paginationCounter: 'rows',
			columns: [
				{
					title: i18n.colId,
					field: 'id',
					width: 80,
					minWidth: 60,
					hozAlign: 'center',
					formatter: (cell) => `<div class="d-flex align-items-center justify-content-center h-100"><span class="fw-medium text-muted">#${cell.getValue()}</span></div>`,
				},
				{
					title: i18n.colName,
					field: 'name',
					headerFilter: 'input',
					headerFilterPlaceholder: i18n.placeholderSearch,
					minWidth: 150,
					formatter: (cell) => `<div class="d-flex align-items-center h-100 py-1"><div class="fw-bold text-dark">${cell.getValue()}</div></div>`,
				},
				{
					title: i18n.colCode,
					field: 'groupCode',
					width: 150,
					minWidth: 120,
					headerFilter: 'input',
					formatter: (cell) => `<div class="d-flex align-items-center h-100"><span class="badge bg-light text-dark border badge-code">${cell.getValue()}</span></div>`,
				},
				{
					title: i18n.colMembers,
					field: 'id',
					width: 120,
					minWidth: 100,
					hozAlign: 'center',
					headerSort: false,
					formatter: (cell) => {
						const id = cell.getValue();
						const container = document.createElement('div');
						container.className = 'd-flex align-items-center justify-content-center h-100';

						const badge = document.createElement('span');
						badge.className = 'badge rounded-pill bg-primary-subtle text-primary border border-primary-subtle px-3';
						badge.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

						getClientsCountByGroup(id).then(([err, res]) => {
							badge.textContent = !err ? res.data || 0 : '-';
						});

						container.appendChild(badge);
						return container;
					},
				},
				{
					title: i18n.colActions,
					hozAlign: 'center',
					headerSort: false,
					width: 150,
					responsive: 0,
					formatter: (cell) => {
						return `
							<div class="d-flex align-items-center justify-content-center h-100 gap-1">
								<button class="btn btn-light btn-sm rounded-pill btn-members" data-id="${cell.getData().id}" title="${i18n.colMembers}">
									<i data-lucide="users" class="lucide-sm text-info"></i>
								</button>
								<button class="btn btn-light btn-sm rounded-pill btn-edit" data-id="${cell.getData().id}" title="${i18n.editTitle}">
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
			const btnMembers = e.target.closest('.btn-members');

			if (btnEdit) {
				const row = table.getRow(btnEdit.dataset.id);
				if (row) onEditRow?.(row.getData());
			} else if (btnMembers) {
				const row = table.getRow(btnMembers.dataset.id);
				if (row) onManageMembers?.(row.getData());
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

			const [err, res] = await getGroups(0, 1000);
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
