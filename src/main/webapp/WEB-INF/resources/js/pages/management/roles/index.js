import { createGroup, deleteGroup, updateGroup } from '../../../api/group.api.js';
import { StateManager } from './state_manager.js';
import { MainForm, MappingModule } from './role_modal.js';
import { UiRenderer } from './ui_renderer.js';

document.addEventListener('DOMContentLoaded', () => {
	const tableContainer = document.querySelector('#rolesTable');
	if (!tableContainer) return;

	const config = window.__ROLES_CONFIG__ || { i18n: {} };
	StateManager.init(config);

	const Controller = {
		init() {
			UiRenderer.init(
				(data) => MainForm.open(data),
				(data) => MappingModule.open(data),
				(count) => {
					const btnDelete = document.getElementById('btnDeleteSelected');
					if (btnDelete) btnDelete.disabled = count === 0;
				},
			);
			MainForm.init();
			MappingModule.init();
			this.bindEvents();
		},

		bindEvents() {
			document.getElementById('btnReload')?.addEventListener('click', () => UiRenderer.refresh());
			document.getElementById('btnAdd')?.addEventListener('click', () => MainForm.open());
			document.getElementById('btnDeleteSelected')?.addEventListener('click', () => this.handleBatchDelete());
			MainForm.getElements().form?.addEventListener('submit', (e) => this.handleFormSubmit(e));
		},

		async handleFormSubmit(e) {
			e.preventDefault();
			const data = MainForm.validate();
			if (!data) return;

			const i18n = StateManager.getI18n();
			const { submitBtn } = MainForm.getElements();
			const originalHtml = submitBtn.innerHTML;

			submitBtn.disabled = true;
			submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing || ''}`;

			try {
				const isUpdate = !!data.id;
				if (isUpdate) delete data.groupCode;

				const [err, res] = isUpdate ? await updateGroup(data.id, data) : await createGroup(data);

				if (err) {
					Swal.fire(i18n.error || '', err.message || i18n.error || '', 'error');
				} else {
					Swal.fire(i18n.success || '', isUpdate ? i18n.updatedSuccess || '' : i18n.createdSuccess || '', 'success');
					MainForm.close();
					UiRenderer.refresh();
				}
			} catch (error) {
				console.error('Submit error:', error);
				Swal.fire(i18n.error || '', i18n.error || '', 'error');
			} finally {
				submitBtn.disabled = false;
				submitBtn.innerHTML = originalHtml;
			}
		},

		async handleBatchDelete() {
			const i18n = StateManager.getI18n();
			const selected = UiRenderer.getSelectedData();
			if (selected.length === 0) return;

			const result = await Swal.fire({
				title: i18n.confirmDelete || '',
				text: selected.length > 1 ? (i18n.confirmDeleteTextBatch || '').replace('{0}', selected.length) : (i18n.confirmDeleteTextSingle || '').replace('{0}', selected.length),
				icon: 'warning',
				showCancelButton: true,
				confirmButtonColor: '#d33',
				cancelButtonColor: '#3085d6',
				confirmButtonText: i18n.yesDelete || '',
				cancelButtonText: i18n.cancel || '',
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
							Swal.fire(i18n.error || '', i18n.deleteErrorHasClients || '', 'error');
						} else {
							Swal.fire(i18n.error || '', `Failed to delete ${errorCount} items. ${lastErrorMessage}`, 'error');
						}
					} else {
						await Swal.fire(i18n.success || '', i18n.success || '', 'success');
					}
					UiRenderer.refresh();
				} catch (err) {
					Swal.fire(i18n.error || '', err.message, 'error');
				}
			}
		},
	};

	Controller.init();
});
