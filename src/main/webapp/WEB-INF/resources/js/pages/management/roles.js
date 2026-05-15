import { getGroups, createGroup, updateGroup, deleteGroup } from '../../api/group.api.js';
import { getFunctionsWithGroupStatus } from '../../api/function.api.js';
import { toggleGroupFunctions } from '../../api/role.api.js';
import { TabulatorFull as Tabulator } from '../../lib/tabulator_esm.min.js';
import { Validator } from '../../common/validator.js';

document.addEventListener('DOMContentLoaded', () => {
	const tableContainer = document.querySelector('#rolesTable');
	if (!tableContainer) return;

	const config = window.__ROLES_CONFIG__ || { i18n: {} };
	const { i18n } = config;

	const MainForm = (() => {
		const elements = {
			modal: document.getElementById('roleModal'),
			form: document.getElementById('roleForm'),
			title: document.getElementById('modalTitle'),
			roleId: document.getElementById('roleId'),
			name: document.getElementById('name'),
			groupCode: document.getElementById('groupCode'),
			description: document.getElementById('description'),
			submitBtn: document.getElementById('btnSubmitRole'),
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
			elements.roleId.value = '';
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
				elements.roleId.value = data.id || '';
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
			if (!data.id) { // Only validate code on create
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
			modal: document.getElementById('manageFunctionsModal'),
			title: document.getElementById('mappingModalTitle'),
			list: document.getElementById('functionsList'),
			loader: document.getElementById('functionsListLoader'),
			search: document.getElementById('funcSearch'),
			saveBtn: document.getElementById('btnSaveFunctions'),
		};

		const bootstrapModal = elements.modal && typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
		let currentGroupId = null;
		let initialStates = {}; // Map of code -> boolean

		const open = async (role) => {
			currentGroupId = role.id;
			elements.title.textContent = i18n.mappingTitle.replace('{0}', role.name);
			elements.list.style.display = 'none';
			elements.loader.classList.remove('d-none');
			elements.search.value = '';
			initialStates = {};

			bootstrapModal?.show();
			window.renderIcons?.();

			try {
				const [err, res] = await getFunctionsWithGroupStatus(currentGroupId);
				if (err) throw err;

				const functions = res.data || [];
				renderList(functions);
				functions.forEach(f => {
					initialStates[f.functionCode] = f.isAssignedToGroup;
				});

			} catch (err) {
				console.error('Failed to load permissions:', err);
				elements.list.innerHTML = `<div class="alert alert-danger">${i18n.error}</div>`;
			} finally {
				elements.loader.classList.add('d-none');
				elements.list.style.display = 'block';
				window.renderIcons?.();
			}
		};

		const renderList = (functions) => {
			if (functions.length === 0) {
				elements.list.innerHTML = `<div class="text-center py-4 text-muted">${i18n.noData}</div>`;
				return;
			}

			elements.list.innerHTML = functions.map(func => `
				<div class="selection-list-item d-flex align-items-start func-item" data-search="${func.name.toLowerCase()} ${func.functionCode.toLowerCase()}">
					<div class="form-check pt-1">
						<input class="form-check-input scale-checkbox func-chk" type="checkbox" 
							id="func_${func.id}" 
							data-code="${func.functionCode}"
							${func.isAssignedToGroup ? 'checked' : ''}>
						<label class="form-check-label" for="func_${func.id}"></label>
					</div>
					<div class="ms-2 w-100 cursor-pointer" onclick="document.getElementById('func_${func.id}').click()">
						<div class="d-flex justify-content-between align-items-center">
							<div class="fw-bold text-dark">${func.name}</div>
							<span class="badge bg-light text-muted border badge-code small">${func.functionCode}</span>
						</div>
						<div class="small text-muted mt-1">${func.description || '--'}</div>
					</div>
				</div>
			`).join('');
		};

		const handleSearch = (query) => {
			const q = query.toLowerCase();
			const items = elements.list.querySelectorAll('.func-item');
			items.forEach(item => {
				const text = item.dataset.search;
				item.style.display = text.includes(q) ? 'flex' : 'none';
			});
		};

		const getChanges = () => {
			const toggles = {};
			const checks = elements.list.querySelectorAll('.func-chk');
			let hasChanges = false;

			checks.forEach(chk => {
				const code = chk.dataset.code;
				const isChecked = chk.checked;
				if (isChecked !== initialStates[code]) {
					toggles[code] = isChecked;
					hasChanges = true;
				}
			});

			return hasChanges ? toggles : null;
		};

		const save = async () => {
			const toggles = getChanges();
			if (!toggles) {
				Swal.fire(i18n.info, i18n.mappingNoChanges, 'info');
				bootstrapModal?.hide();
				return;
			}

			const originalHtml = elements.saveBtn.innerHTML;
			elements.saveBtn.disabled = true;
			elements.saveBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing}`;

			try {
				const [err, res] = await toggleGroupFunctions({
					groupId: currentGroupId,
					functionToggles: toggles
				});

				if (err) throw err;

				const { successCount, skippedCount } = res.data || {};
				// We need to know how many were added vs removed for the message
				// but the API response is a bit generic. 
				// Based on the toggles we can calculate.
				const added = Object.values(toggles).filter(v => v).length;
				const removed = Object.values(toggles).filter(v => !v).length;

				Swal.fire(i18n.success, i18n.mappingSuccess.replace('{0}', added).replace('{1}', removed), 'success');
				bootstrapModal?.hide();
			} catch (err) {
				console.error('Save mapping error:', err);
				Swal.fire(i18n.error, i18n.mappingError, 'error');
			} finally {
				elements.saveBtn.disabled = false;
				elements.saveBtn.innerHTML = originalHtml;
			}
		};

		elements.search?.addEventListener('input', (e) => handleSearch(e.target.value));
		elements.saveBtn?.addEventListener('click', () => save());

		return { open };
	})();

	const Datatable = (() => {
		let table = null;
		let isLoadingData = false;

		const init = (onEditRow, onManagePermissions, onSelectionChange) => {
			table = new Tabulator('#rolesTable', {
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
						title: i18n.colDescription,
						field: 'description',
						formatter: (cell) => `<div class="d-flex align-items-center h-100 text-muted small">${cell.getValue() || '--'}</div>`,
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
									<button class="btn btn-light btn-sm rounded-pill btn-permissions" data-id="${data.id}" title="Manage Permissions">
										<i data-lucide="shield-check" class="lucide-sm text-success"></i>
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
				const btnPerm = e.target.closest('.btn-permissions');
				
				if (btnEdit) {
					const row = table.getRow(btnEdit.dataset.id);
					if (row) onEditRow?.(row.getData());
				} else if (btnPerm) {
					const row = table.getRow(btnPerm.dataset.id);
					if (row) onManagePermissions?.(row.getData());
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
				text: selected.length > 1 ? i18n.confirmDeleteTextBatch.replace('{0}', selected.length) : i18n.confirmDeleteTextSingle.replace('{0}', selected.length),
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
						if (errorCount === selected.length && lastErrorMessage.includes('client')) {
							Swal.fire(i18n.error, i18n.deleteErrorHasClients.replace('{0}', ''), 'error');
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
	};

	Controller.init();
});
