import { getGroups, createGroup, updateGroup, deleteGroup, getClientsCountByGroup, getAllClientsByGroupId } from '../../api/group.api.js';
import { unassignGroupsFromClient } from '../../api/role.api.js';
import { TabulatorFull as Tabulator } from '../../lib/tabulator_esm.min.js';
import { Validator } from '../../common/validator.js';

document.addEventListener('DOMContentLoaded', () => {
	const tableContainer = document.querySelector('#groupsTable');
	if (!tableContainer) return;

	const config = window.__GROUPS_CONFIG__ || { i18n: {} };
	const { i18n } = config;

	const MainForm = (() => {
		const elements = {
			modal: document.getElementById('groupModal'),
			form: document.getElementById('groupForm'),
			title: document.getElementById('modalTitle'),
			groupId: document.getElementById('groupId'),
			name: document.getElementById('name'),
			groupCode: document.getElementById('groupCode'),
			description: document.getElementById('description'),
			submitBtn: document.getElementById('btnSubmitGroup'),
		};

		const bootstrapModal = elements.modal && typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
		const inputs = elements.form.querySelectorAll('.form-control');
		const feedbacks = elements.form.querySelectorAll('.invalid-feedback');

		const clearValidation = () => {
			inputs.forEach((el) => el.classList.remove('is-invalid'));
			feedbacks.forEach((el) => (el.textContent = ''));
		};

		const reset = () => {
			elements.form.reset();
			elements.groupId.value = '';
			clearValidation();
		};

		const open = (data = null) => {
			reset();
			const isEdit = !!data;

			elements.title.textContent = isEdit ? i18n.editTitle : i18n.addTitle;
			elements.groupCode.readOnly = isEdit;
			elements.groupCode.parentElement?.classList.toggle('opacity-75', isEdit);

			if (isEdit) {
				Object.entries(data).forEach(([key, value]) => {
					const input = elements.form.elements[key];
					if (input) input.value = value ?? '';
				});
				elements.groupId.value = data.id || '';
			}

			bootstrapModal?.show();
			window.renderIcons?.();
		};

		const validate = () => {
			const formData = new FormData(elements.form);
			const data = Object.fromEntries(formData.entries());
			let isValid = true;

			clearValidation();

			const setError = (field, msg) => {
				const input = elements.form.querySelector(`#${field}`);
				const feedback = elements.form.querySelector(`#val-${field}`);
				if (input) input.classList.add('is-invalid');
				if (feedback) feedback.textContent = msg;
				isValid = false;
			};

			// Validate Name
			if (!Validator.name.isBlank(data.name)) {
				setError('name', i18n.valRequired.replace('{0}', i18n.colName));
			} else if (!Validator.name.isLowerMin(data.name) || !Validator.name.isHigherMax(data.name)) {
				setError('name', i18n.valNameLen);
			}

			// Validate Code
			if (!data.id) {
				// Only validate code on create
				if (!Validator.groupCode.isBlank(data.groupCode)) {
					setError('groupCode', i18n.valRequired.replace('{0}', i18n.colCode));
				} else if (!Validator.groupCode.isHigherMax(data.groupCode)) {
					setError('groupCode', i18n.valCodeLen);
				} else if (!Validator.groupCode.isValidFormat(data.groupCode)) {
					setError('groupCode', i18n.valGroupCodeFormat);
				}
				data.groupCode = data.groupCode.toUpperCase();
			}

			// Validate Description
			if (data.description && !Validator.description.isHigherMax(data.description)) {
				setError('description', i18n.valDescriptionLen);
			}

			if (isValid) {
				return data;
			}
			return null;
		};

		return {
			open,
			close: () => bootstrapModal?.hide(),
			validate,
			elements,
		};
	})();

	const MappingModule = (() => {
		const elements = {
			modal: document.getElementById('manageMembersModal'),
			title: document.getElementById('mappingModalTitle'),
			list: document.getElementById('membersList'),
			loader: document.getElementById('membersListLoader'),
			search: document.getElementById('memberSearch'),
		};

		const bootstrapModal = elements.modal && typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
		let currentGroupId = null;
		let currentGroupName = '';

		const open = async (group) => {
			currentGroupId = group.id;
			currentGroupName = group.name;
			elements.title.textContent = i18n.mappingTitle.replace('{0}', group.name);
			elements.list.style.display = 'none';
			elements.loader.classList.remove('d-none');
			elements.search.value = '';

			bootstrapModal?.show();
			window.renderIcons?.();

			try {
				const [err, res] = await getAllClientsByGroupId(currentGroupId);
				if (err) throw err;

				const members = res.data || [];
				renderList(members);
			} catch (err) {
				console.error('Failed to load members:', err);
				elements.list.innerHTML = `<div class="alert alert-danger">${i18n.error}</div>`;
			} finally {
				elements.loader.classList.add('d-none');
				elements.list.style.display = 'block';
				window.renderIcons?.();
			}
		};

		const renderList = (members) => {
			if (members.length === 0) {
				elements.list.innerHTML = `<div class="text-center py-4 text-muted">${i18n.noData}</div>`;
				return;
			}

			elements.list.innerHTML = members
				.map(
					(user) => `
				<div class="selection-list-item d-flex align-items-center member-item" data-search="${user.username.toLowerCase()} ${user.clientType.toLowerCase()}">
					<div class="flex-shrink-0">
						<img src="${user.avatarUrl || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(user.username)}" 
							 class="rounded-circle border shadow-sm user-avatar" 
							 onerror="this.src='https://ui-avatars.com/api/?name=${encodeURIComponent(user.username)}'">
					</div>
					<div class="ms-3 flex-grow-1">
						<div class="fw-bold text-dark">${user.username}</div>
						<div class="small text-muted">${user.clientType}</div>
					</div>
					<button class="btn btn-light btn-sm rounded-pill btn-remove-member" 
						data-id="${user.id}" data-username="${user.username}" title="Remove from group">
						<i data-lucide="user-minus" class="lucide-sm text-danger"></i>
					</button>
				</div>
			`,
				)
				.join('');
		};

		const handleSearch = (query) => {
			const q = query.toLowerCase();
			const items = elements.list.querySelectorAll('.member-item');
			items.forEach((item) => {
				const text = item.dataset.search;
				item.style.display = text.includes(q) ? 'flex' : 'none';
			});
		};

		elements.search?.addEventListener('input', (e) => handleSearch(e.target.value));

		return {
			open,
			getCurrentGroupId: () => currentGroupId,
			getCurrentGroupName: () => currentGroupName,
		};
	})();

	const Datatable = (() => {
		let table = null;
		let isLoadingData = false;

		const init = (onEditRow, onManageMembers, onSelectionChange) => {
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
						<p>${i18n.noData}</p>
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
						field: 'groupCode',
						width: 150,
						headerFilter: 'input',
						formatter: (cell) => `<div class="d-flex align-items-center h-100"><span class="badge bg-light text-dark border badge-code">${cell.getValue()}</span></div>`,
					},
					{
						title: i18n.colMembers,
						field: 'id',
						width: 120,
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
						formatter: (cell) => {
							const data = cell.getData();
							return `
								<div class="d-flex align-items-center justify-content-center h-100 gap-1">
									<button class="btn btn-light btn-sm rounded-pill btn-members" data-id="${data.id}" title="${i18n.colMembers}">
										<i data-lucide="users" class="lucide-sm text-info"></i>
									</button>
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

				const groups = res.data.content || [];
				table.setData(groups);
			} catch (err) {
				console.error('Refresh error:', err);
			} finally {
				isLoadingData = false;
				if (btnReload) btnReload.disabled = false;
				if (icon) icon.classList.remove('lucide-spin');
				window.renderIcons?.();
			}
		};

		return {
			init,
			refresh,
			getSelectedData: () => table?.getSelectedData() || [],
		};
	})();

	const Controller = {
		init() {
			Datatable.init(
				(data) => MainForm.open(data),
				(data) => MappingModule.open(data),
				(count) => {
					const btnDelete = document.getElementById('btnDeleteSelected');
					if (btnDelete) btnDelete.disabled = count === 0;
				},
			);
			this.bindEvents();
		},

		bindEvents() {
			document.getElementById('btnReload')?.addEventListener('click', () => Datatable.refresh());
			document.getElementById('btnAdd')?.addEventListener('click', () => MainForm.open());
			document.getElementById('btnDeleteSelected')?.addEventListener('click', () => this.handleBatchDelete());
			MainForm.elements.form?.addEventListener('submit', (e) => this.handleFormSubmit(e));

			// Removal of members triggered from MappingModule
			document.addEventListener('click', (e) => {
				const btnRemove = e.target.closest('.btn-remove-member');
				if (btnRemove) {
					this.handleRemoveMember(btnRemove.dataset.id, btnRemove.dataset.username);
				}
			});
		},

		async handleFormSubmit(e) {
			e.preventDefault();
			const data = MainForm.validate();
			if (!data) return;

			const { submitBtn } = MainForm.elements;
			const originalHtml = submitBtn.innerHTML;

			submitBtn.disabled = true;
			submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing}`;

			try {
				const isUpdate = !!data.id;
				if (isUpdate) delete data.groupCode;

				const [err, res] = isUpdate ? await updateGroup(data.id, data) : await createGroup(data);

				if (err) {
					Swal.fire(i18n.error, err.message || i18n.error, 'error');
				} else {
					Swal.fire(i18n.success, isUpdate ? i18n.updatedSuccess : i18n.createdSuccess, 'success');
					MainForm.close();
					Datatable.refresh();
				}
			} catch (error) {
				console.error('Submit error:', error);
				Swal.fire(i18n.error, i18n.error, 'error');
			} finally {
				submitBtn.disabled = false;
				submitBtn.innerHTML = originalHtml;
			}
		},

		async handleBatchDelete() {
			const selected = Datatable.getSelectedData();
			if (selected.length === 0) return;

			const result = await Swal.fire({
				title: i18n.confirmDelete,
				text: selected.length > 1 ? i18n.confirmDeleteTextBatch.replace('{0}', selected.length) : i18n.confirmDeleteTextSingle.replace('{0}', selected[0].groupCode),
				icon: 'warning',
				showCancelButton: true,
				confirmButtonColor: '#d33',
				cancelButtonColor: '#3085d6',
				confirmButtonText: i18n.yesDelete,
				cancelButtonText: i18n.cancel,
			});

			if (result.isConfirmed) {
				try {
					let errorCount = 0;
					let lastErrorMessage = '';

					for (const row of selected) {
						const [err] = await deleteGroup(row.id);
						if (err) {
							errorCount++;
							lastErrorMessage = err.message;
						}
					}

					if (errorCount > 0) {
						if (errorCount === selected.length && lastErrorMessage.toLowerCase().includes('client')) {
							const match = lastErrorMessage.match(/(\d+)/);
							const count = match ? match[0] : '?';
							Swal.fire(i18n.error, i18n.deleteErrorHasClients.replace('{0}', count), 'error');
						} else {
							Swal.fire(i18n.error, `Failed to delete ${errorCount} items. ${lastErrorMessage}`, 'error');
						}
					} else {
						await Swal.fire(i18n.success, i18n.success, 'success');
					}
					Datatable.refresh();
				} catch (err) {
					Swal.fire(i18n.error, err.message, 'error');
				}
			}
		},

		async handleRemoveMember(clientId, username) {
			const result = await Swal.fire({
				title: i18n.confirmDelete,
				text: `Remove ${username} from this group?`,
				icon: 'warning',
				showCancelButton: true,
				confirmButtonColor: '#d33',
				cancelButtonColor: '#3085d6',
				confirmButtonText: i18n.yesDelete,
				cancelButtonText: i18n.cancel,
			});

			if (result.isConfirmed) {
				try {
					const groupId = MappingModule.getCurrentGroupId();
					const [err] = await unassignGroupsFromClient({ clientId, groupIds: [groupId] });
					if (err) throw err;

					Swal.fire(i18n.success, '', 'success');
					// Refresh MappingModule
					MappingModule.open({ id: groupId, name: MappingModule.getCurrentGroupName() });
					// Refresh Main Table
					Datatable.refresh();
				} catch (err) {
					Swal.fire(i18n.error, err.message || i18n.error, 'error');
				}
			}
		},
	};

	Controller.init();
});
