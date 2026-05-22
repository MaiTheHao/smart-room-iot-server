import { TabulatorFull as Tabulator } from '../../../lib/tabulator_esm.min.js';
import { getAll as getAllClients } from '../../../api/user.api.js';
import { StateManager } from './state_manager.js';

export const UiRenderer = (() => {
	let table = null;
	let isLoadingData = false;

	const init = (onEditRow, onPasswordRow, onManageGroups, onSetup, onClearConfig, onSelectionChange) => {
		const i18n = StateManager.getI18n();
		const constants = StateManager.getConstants();

		table = new Tabulator('#clientsTable', {
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
					title: i18n.colUsername,
					field: 'username',
					headerFilter: 'input',
					headerFilterPlaceholder: i18n.placeholderSearch,
					formatter: (cell) => {
						const client = cell.getData();
						const avatarUrl = client.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(client.username)}&background=random`;
						return `
							<div class="d-flex align-items-center h-100 py-1">
								<img src="${avatarUrl}" class="rounded-circle border shadow-sm me-3" style="width: 32px; height: 32px; object-fit: cover;" alt="${client.username}">
								<div class="fw-bold text-dark">${client.username}</div>
							</div>`;
					},
				},
				{
					title: i18n.colType,
					field: 'clientType',
					headerFilter: 'list',
					headerFilterParams: {
						values: {
							'': i18n.allTypes || 'All Types',
							USER: i18n.typeUser || 'User',
							HARDWARE_GATEWAY: i18n.typeGateway || 'Gateway',
						},
					},
					formatter: (cell) => {
						const value = cell.getValue();
						const isUser = value === constants.CLIENT_TYPE?.USER;
						const icon = isUser ? 'user' : 'cpu';
						const cls = isUser ? 'bg-primary text-primary' : 'bg-purple text-purple';
						const customStyle = !isUser ? 'style="--bs-purple: #6610f2; color: #6610f2 !important; background-color: rgba(102, 16, 242, 0.1) !important; border-color: rgba(102, 16, 242, 0.25) !important;"' : '';

						return `
							<div class="d-flex align-items-center h-100">
								<span class="badge rounded-pill bg-opacity-10 border border-opacity-25 px-3 py-2 ${cls}" ${customStyle}>
									<i data-lucide="${icon}" class="lucide-sm me-1"></i>${isUser ? (i18n.typeUser || 'User') : (i18n.typeGateway || 'Gateway')}
								</span>
							</div>`;
					},
				},
				{
					title: i18n.colIp,
					field: 'ipAddress',
					headerFilter: 'input',
					formatter: (cell) => `<div class="d-flex align-items-center h-100"><span class="font-monospace small text-muted">${cell.getValue() || '--'}</span></div>`,
				},
				{
					title: i18n.colMac,
					field: 'macAddress',
					headerFilter: 'input',
					formatter: (cell) => `<div class="d-flex align-items-center h-100"><span class="font-monospace small text-muted">${cell.getValue() || '--'}</span></div>`,
				},
				{
					title: i18n.colLastLogin,
					field: 'lastLoginAt',
					formatter: (cell) => {
						const val = cell.getValue();
						return `<div class="d-flex align-items-center h-100">${val ? new Date(val).toLocaleString() : `<span class="text-muted small">${i18n.never || 'Never'}</span>`}</div>`;
					},
				},
				{
					title: i18n.colActions,
					hozAlign: 'center',
					headerSort: false,
					width: 200,
					formatter: (cell) => {
						const data = cell.getData();
						const isGateway = data.clientType === constants.CLIENT_TYPE?.HARDWARE_GATEWAY;
						const setupBtn = isGateway
							? `
							<button class="btn btn-light btn-sm rounded-pill btn-setup me-1" data-id="${data.id}" data-username="${data.username}" title="${i18n.setupTitle || ''}">
								<i data-lucide="file-cog" class="lucide-sm text-success"></i>
							</button>
							<button class="btn btn-light btn-sm rounded-pill btn-clear-config me-1" data-id="${data.id}" data-username="${data.username}" title="${i18n.clearConfigTitle || ''}">
								<i data-lucide="file-x" class="lucide-sm text-danger"></i>
							</button>`
							: '';
						return `
							<div class="d-flex align-items-center justify-content-center h-100">
								${setupBtn}
								<button class="btn btn-light btn-sm rounded-pill btn-groups me-1" data-id="${data.id}" title="${i18n.mappingTitle || 'Manage Groups'}">
									<i data-lucide="shield" class="lucide-sm text-info"></i>
								</button>
								<button class="btn btn-light btn-sm rounded-pill btn-password me-1" data-id="${data.id}" title="${i18n.pwdChangeTitle || 'Change Password'}">
									<i data-lucide="key" class="lucide-sm text-warning"></i>
								</button>
								<button class="btn btn-light btn-sm rounded-pill btn-edit" data-id="${data.id}" title="${i18n.commonEdit || 'Edit'}">
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
			const btnPassword = e.target.closest('.btn-password');
			const btnGroups = e.target.closest('.btn-groups');
			const btnSetup = e.target.closest('.btn-setup');
			const btnClear = e.target.closest('.btn-clear-config');

			if (btnEdit) {
				const row = table.getRow(btnEdit.dataset.id);
				if (row) onEditRow?.(row.getData());
			} else if (btnPassword) {
				const row = table.getRow(btnPassword.dataset.id);
				if (row) onPasswordRow?.(row.getData());
			} else if (btnGroups) {
				const row = table.getRow(btnGroups.dataset.id);
				if (row) onManageGroups?.(row.getData());
			} else if (btnSetup) {
				onSetup?.(btnSetup.dataset.id, btnSetup.dataset.username);
			} else if (btnClear) {
				onClearConfig?.(btnClear.dataset.id, btnClear.dataset.username);
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

			const [err, res] = await getAllClients({ page: 0, size: 1000 });
			if (err) {
				const errorMsg = err.message || i18n.error || 'Error loading data';
				table.setPlaceholder(`
					<div class="text-center py-5 text-danger">
						<i data-lucide="alert-circle" class="mb-2" style="width: 48px; height: 48px"></i>
						<p class="fw-bold">${errorMsg}</p>
					</div>`);
				table.setData([]);
				return;
			}
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
