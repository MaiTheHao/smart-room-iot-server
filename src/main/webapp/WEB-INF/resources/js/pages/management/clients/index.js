import { deleteClient, create as createClient, patchUpdate, deleteAllHardwareConfigs } from '../../../api/user.api.js';
import { setup as setupGateway } from '../../../api/system.api.js';
import { StateManager } from './state_manager.js';
import { MainForm, PasswordForm, MappingModule } from './client_modal.js';
import { UiRenderer } from './ui_renderer.js';
import { Toast, Alert } from '../../../common/notification_util.js';

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
			const data = await MainForm.validate();
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
					Toast.error(err.message || i18n.error || 'An error occurred');
				} else {
					Toast.success(isUpdate ? i18n.updatedSuccess || 'Cập nhật thành công' : i18n.createdSuccess || 'Added successfully');
					MainForm.close();
					UiRenderer.refresh();
				}
			} catch (error) {
				console.error('Submit error:', error);
				Toast.error(i18n.error || 'An error occurred');
			} finally {
				submitBtn.disabled = false;
				submitBtn.innerHTML = originalHtml;
			}
		},

		async handlePasswordSubmit(e) {
			e.preventDefault();
			const data = await PasswordForm.validate();
			if (!data) return;

			const i18n = StateManager.getI18n();
			const { submitBtn } = PasswordForm.getElements();
			const originalHtml = submitBtn.innerHTML;

			submitBtn.disabled = true;
			submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing || ''}`;

			try {
				const [err, res] = await patchUpdate(data.id, data);

				if (err) {
					Toast.error(err.message || i18n.pwdError || 'An error occurred');
				} else {
					Toast.success(i18n.pwdSuccess || 'Password changed successfully');
					PasswordForm.close();
				}
			} catch (error) {
				console.error('Password submit error:', error);
				Toast.error(i18n.error || 'An error occurred');
			} finally {
				submitBtn.disabled = false;
				submitBtn.innerHTML = originalHtml;
			}
		},

		async handleBatchDelete() {
			const i18n = StateManager.getI18n();
			const selected = UiRenderer.getSelectedData();
			if (selected.length === 0) return;

			const result = await Alert.confirm({
				title: i18n.confirmDelete || 'Confirm Delete',
				text: selected.length > 1 ? (i18n.confirmDeleteTextBatch || 'Are you sure you want to delete {0} items?').replace('{0}', selected.length) : (i18n.confirmDeleteTextSingle || 'Are you sure you want to delete this item?').replace('{0}', selected.length),
				confirmText: i18n.yesDelete || 'Delete',
				cancelText: i18n.cancel || 'Cancel',
			});

			if (result.isConfirmed) {
				try {
					const deletePromises = selected.map((row) => deleteClient(row.id));
					const results = await Promise.all(deletePromises);
					const errors = results.filter(([err]) => err !== null);

					if (errors.length > 0) throw new Error(`Failed to delete ${errors.length} items`);

					Toast.success(i18n.success || 'Deleted successfully');
					UiRenderer.refresh();
				} catch (err) {
					Toast.error(err.message || i18n.error || 'An error occurred');
				}
			}
		},

		async handleSetup(id, username) {
			const i18n = StateManager.getI18n();
			const result = await Alert.confirm({
				title: i18n.setupTitle || 'Confirm Setup',
				text: (i18n.setupText || '').replace('{0}', username),
				icon: 'question',
				confirmText: i18n.confirm || 'Confirm',
				cancelText: i18n.cancel || 'Cancel',
			});

			if (result.isConfirmed) {
				Swal.fire({
					title: i18n.setupProcessing || 'Processing...',
					allowOutsideClick: false,
					didOpen: () => {
						Swal.showLoading();
					},
				});

				try {
					const [err, res] = await setupGateway(id);

					if (err) {
						Toast.error(err.message || i18n.error || 'An error occurred');
					} else {
						Toast.success(i18n.setupSuccessText || 'Setup successful');
					}
				} catch (error) {
					console.error('Setup error:', error);
					Toast.error(i18n.error || 'An error occurred');
				}
			}
		},

		async handleClearHardwareConfig(id, username) {
			const i18n = StateManager.getI18n();
			const result = await Alert.confirm({
				title: i18n.clearConfigTitle || 'Confirm Clearing Configuration',
				text: (i18n.clearConfigText || '').replace('{0}', username),
				icon: 'warning',
				confirmText: i18n.confirm || 'Confirm',
				cancelText: i18n.cancel || 'Cancel',
			});

			if (result.isConfirmed) {
				Swal.fire({
					title: i18n.clearConfigProcessing || 'Clearing configuration...',
					allowOutsideClick: false,
					didOpen: () => {
						Swal.showLoading();
					},
				});

				try {
					const [err, res] = await deleteAllHardwareConfigs(id);

					if (err) {
						Toast.error(err.message || i18n.error || 'An error occurred');
					} else {
						Toast.success(i18n.clearConfigSuccess || 'Configuration cleared successfully');
						UiRenderer.refresh();
					}
				} catch (error) {
					console.error('Clear hardware config error:', error);
					Toast.error(i18n.error || 'An error occurred');
				}
			}
		},
	};

	Controller.init();
});
