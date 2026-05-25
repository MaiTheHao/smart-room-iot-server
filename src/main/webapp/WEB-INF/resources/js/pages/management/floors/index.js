import { createFloor, deleteFloor, patchFloor } from '../../../api/floor.api.js';
import { StateManager } from './state_manager.js';
import { FloorModal } from './floor_modal.js';
import { UiRenderer } from './ui_renderer.js';
import { Toast, Alert } from '../../../common/notification_util.js';

document.addEventListener('DOMContentLoaded', () => {
	const tableContainer = document.querySelector('#floorsTable');
	if (!tableContainer) return;

	const config = window.__FLOORS_CONFIG__ || { i18n: {} };
	StateManager.init(config);

	const Controller = {
		init() {
			UiRenderer.init(
				(data) => FloorModal.open(data),
				(count) => {
					const btnDelete = document.getElementById('btnDeleteSelected');
					if (btnDelete) btnDelete.disabled = count === 0;
				},
			);
			FloorModal.init();
			this.bindEvents();
		},

		bindEvents() {
			document.getElementById('btnReload')?.addEventListener('click', () => UiRenderer.refresh());
			document.getElementById('btnAdd')?.addEventListener('click', () => FloorModal.open());
			document.getElementById('btnDeleteSelected')?.addEventListener('click', () => this.handleBatchDelete());
			FloorModal.getElements().form?.addEventListener('submit', (e) => this.handleFormSubmit(e));
		},

		async handleFormSubmit(e) {
			e.preventDefault();
			const data = await FloorModal.validate();
			if (!data) return;

			const i18n = StateManager.getI18n();
			const { submitBtn } = FloorModal.getElements();
			const originalHtml = submitBtn.innerHTML;

			submitBtn.disabled = true;
			submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing || ''}`;

			try {
				const isUpdate = !!data.id;
				if (isUpdate) delete data.code;

				const [err, res] = isUpdate ? await patchFloor(data.id, data) : await createFloor(data);

				if (err) {
					Toast.error(err.message || i18n.error || 'An error occurred');
				} else {
					Toast.success(isUpdate ? i18n.updatedSuccess || 'Cập nhật thành công' : i18n.createdSuccess || 'Added successfully');
					FloorModal.close();
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
					const deletePromises = selected.map((row) => deleteFloor(row.id));
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
	};

	Controller.init();
});
