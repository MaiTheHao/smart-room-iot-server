import { deleteClient, create as createClient, patchUpdate, deleteAllHardwareConfigs } from '../../../api/user.api.js';
import { setup as setupGateway } from '../../../api/system.api.js';
import { StateManager } from './state_manager.js';
import { MainForm, PasswordForm, MappingModule } from './client_modal.js';
import { UiRenderer } from './ui_renderer.js';

document.addEventListener('DOMContentLoaded', () => {
	const tableContainer = document.querySelector('#clientsTable');
	if (!tableContainer) return;

	const config = window.__CLIENTS_CONFIG__ || {
		constants: {
			CLIENT_TYPE: {
				USER: 'USER',
				HARDWARE_GATEWAY: 'HARDWARE_GATEWAY',
			},
		},
		i18n: {},
	};
	StateManager.init(config);

	const Controller = {
		init() {
			UiRenderer.init(
				(data) => MainForm.open(data),
				(data) => PasswordForm.open(data),
				(data) => MappingModule.open(data),
				(id, username) => this.handleSetup(id, username),
				(id, username) => this.handleClearHardwareConfig(id, username),
				(count) => {
					const btnDelete = document.getElementById('btnDeleteSelected');
					if (btnDelete) btnDelete.disabled = count === 0;
				},
			);
			MainForm.init();
			PasswordForm.init();
			MappingModule.init();
			this.bindEvents();
		},

		bindEvents() {
			document.getElementById('btnReload')?.addEventListener('click', () => UiRenderer.refresh());
			document.getElementById('btnAdd')?.addEventListener('click', () => MainForm.open());
			document.getElementById('btnDeleteSelected')?.addEventListener('click', () => this.handleBatchDelete());

			MainForm.getElements().form?.addEventListener('submit', (e) => this.handleFormSubmit(e));
			PasswordForm.getElements().form?.addEventListener('submit', (e) => this.handlePasswordSubmit(e));
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
				const [err, res] = isUpdate ? await patchUpdate(data.id, data) : await createClient(data);

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

		async handlePasswordSubmit(e) {
			e.preventDefault();
			const data = PasswordForm.validate();
			if (!data) return;

			const i18n = StateManager.getI18n();
			const { submitBtn } = PasswordForm.getElements();
			const originalHtml = submitBtn.innerHTML;

			submitBtn.disabled = true;
			submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing || ''}`;

			try {
				const [err, res] = await patchUpdate(data.id, data);

				if (err) {
					Swal.fire(i18n.error || '', err.message || i18n.pwdError || '', 'error');
				} else {
					Swal.fire(i18n.success || '', i18n.pwdSuccess || '', 'success');
					PasswordForm.close();
				}
			} catch (error) {
				console.error('Password submit error:', error);
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
					const deletePromises = selected.map((row) => deleteClient(row.id));
					const results = await Promise.all(deletePromises);
					const errors = results.filter(([err]) => err !== null);

					if (errors.length > 0) throw new Error(`Failed to delete ${errors.length} items`);

					await Swal.fire(i18n.success || '', i18n.success || '', 'success');
					UiRenderer.refresh();
				} catch (err) {
					Swal.fire(i18n.error || '', err.message, 'error');
				}
			}
		},

		async handleSetup(id, username) {
			const i18n = StateManager.getI18n();
			const result = await Swal.fire({
				title: i18n.setupTitle || '',
				text: (i18n.setupText || '').replace('{0}', username),
				icon: 'question',
				showCancelButton: true,
				confirmButtonText: i18n.confirm || 'Confirm',
				cancelButtonText: i18n.cancel || 'Cancel',
			});

			if (result.isConfirmed) {
				Swal.fire({
					title: i18n.setupProcessing || '',
					allowOutsideClick: false,
					didOpen: () => {
						Swal.showLoading();
					},
				});

				try {
					const [err, res] = await setupGateway(id);

					if (err) {
						Swal.fire(i18n.setupErrorTitle || '', err.message || i18n.error || '', 'error');
					} else {
						Swal.fire(i18n.setupSuccessTitle || '', i18n.setupSuccessText || '', 'success');
					}
				} catch (error) {
					console.error('Setup error:', error);
					Swal.fire(i18n.setupErrorTitle || '', i18n.error || '', 'error');
				}
			}
		},

		async handleClearHardwareConfig(id, username) {
			const i18n = StateManager.getI18n();
			const result = await Swal.fire({
				title: i18n.clearConfigTitle || '',
				text: (i18n.clearConfigText || '').replace('{0}', username),
				icon: 'warning',
				showCancelButton: true,
				confirmButtonColor: '#d33',
				confirmButtonText: i18n.confirm || 'Confirm',
				cancelButtonText: i18n.cancel || 'Cancel',
			});

			if (result.isConfirmed) {
				Swal.fire({
					title: i18n.clearConfigProcessing || '',
					allowOutsideClick: false,
					didOpen: () => {
						Swal.showLoading();
					},
				});

				try {
					const [err, res] = await deleteAllHardwareConfigs(id);

					if (err) {
						Swal.fire(i18n.error || '', err.message || i18n.error || '', 'error');
					} else {
						Swal.fire(i18n.success || '', i18n.clearConfigSuccess || '', 'success');
						UiRenderer.refresh();
					}
				} catch (error) {
					console.error('Clear hardware config error:', error);
					Swal.fire(i18n.error || '', i18n.error || '', 'error');
				}
			}
		},
	};

	Controller.init();
});
