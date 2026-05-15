import { getFunctions, createFunction, updateFunction, deleteFunction } from '../../api/function.api.js';
import { TabulatorFull as Tabulator } from '../../lib/tabulator_esm.min.js';
import { Validator } from '../../common/validator.js';

document.addEventListener('DOMContentLoaded', () => {
	const tableContainer = document.querySelector('#functionsTable');
	if (!tableContainer) return;

	const config = window.__FUNCTIONS_CONFIG__ || { i18n: {} };
	const { i18n } = config;

	const MainForm = (() => {
		const elements = {
			modal: document.getElementById('functionModal'),
			form: document.getElementById('functionForm'),
			title: document.getElementById('modalTitle'),
			functionId: document.getElementById('functionId'),
			name: document.getElementById('name'),
			functionCode: document.getElementById('functionCode'),
			description: document.getElementById('description'),
			submitBtn: document.getElementById('btnSubmitFunction'),
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
			elements.functionId.value = '';
			clearValidation();
		};

		const open = (data = null) => {
			reset();
			const isEdit = !!data;

			elements.title.textContent = isEdit ? i18n.editTitle : i18n.addTitle;
			elements.functionCode.readOnly = isEdit;
			elements.functionCode.parentElement?.classList.toggle('opacity-75', isEdit);

			if (isEdit) {
				Object.entries(data).forEach(([key, value]) => {
					const input = elements.form.elements[key];
					if (input) input.value = value ?? '';
				});
				elements.functionId.value = data.id || '';
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
				if (!Validator.functionCode.isBlank(data.functionCode)) {
					setError('functionCode', i18n.valRequired.replace('{0}', i18n.colCode));
				} else if (!Validator.functionCode.isHigherMax(data.functionCode)) {
					setError('functionCode', i18n.valCodeLen);
				} else if (!Validator.functionCode.isValidFormat(data.functionCode)) {
					setError('functionCode', i18n.valFunctionCodeFormat);
				}
				data.functionCode = data.functionCode.toUpperCase();
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

	const Datatable = (() => {
		let table = null;
		let isLoadingData = false;

		const init = (onEditRow, onSelectionChange) => {
			table = new Tabulator('#functionsTable', {
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
						field: 'functionCode',
						headerFilter: 'input',
						formatter: (cell) => `<div class="d-flex align-items-center h-100"><span class="badge bg-light text-dark border">${cell.getValue()}</span></div>`,
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

				const [err, res] = await getFunctions(0, 1000);
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
				if (isUpdate) delete data.functionCode;

				const [err, res] = isUpdate ? await updateFunction(data.id, data) : await createFunction(data);

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
						const [err] = await deleteFunction(row.id);
						if (err) {
							errorCount++;
							lastErrorMessage = err.message;
						}
					}

					if (errorCount > 0) {
						Swal.fire(i18n.error, `Failed to delete ${errorCount} items. ${lastErrorMessage}`, 'error');
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
