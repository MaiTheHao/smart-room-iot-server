import { createGroup, deleteGroup, updateGroup } from '../../../api/group.api.js';
import { unassignGroupsFromClient } from '../../../api/role.api.js';
import { StateManager } from './state_manager.js';
import { MainForm, MappingModule } from './group_modal.js';
import { UiRenderer } from './ui_renderer.js';
import { Toast, Alert } from '../../../common/notification_util.js';

document.addEventListener('DOMContentLoaded', () => {
	const tableContainer = document.querySelector('#groupsTable');
	if (!tableContainer) return;

	const config = window.__GROUPS_CONFIG__ || { i18n: {} };
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

			document.addEventListener('click', (e) => {
				const btnRemove = e.target.closest('.btn-remove-member');
				if (btnRemove) {
					this.handleRemoveMember(btnRemove.dataset.id, btnRemove.dataset.username);
				}
			});
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
				if (isUpdate) delete data.groupCode;

				const [err, res] = isUpdate ? await updateGroup(data.id, data) : await createGroup(data);

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

		async handleBatchDelete() {
			const i18n = StateManager.getI18n();
			const selected = UiRenderer.getSelectedData();
			if (selected.length === 0) return;

			const result = await Alert.confirm({
				title: i18n.confirmDelete || 'Confirm Delete',
				text: selected.length > 1 ? (i18n.confirmDeleteTextBatch || 'Are you sure you want to delete {0} items?').replace('{0}', selected.length) : (i18n.confirmDeleteTextSingle || 'Are you sure you want to delete this item?').replace('{0}', selected[0].groupCode),
				confirmText: i18n.yesDelete || 'Delete',
				cancelText: i18n.cancel || 'Cancel',
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
							Toast.error((i18n.deleteErrorHasClients || 'Lỗi: có {0} client đang liên kết').replace('{0}', count));
						} else {
							Toast.error(`Failed to delete ${errorCount} items. ${lastErrorMessage}`);
						}
					} else {
						Toast.success(i18n.success || 'Deleted successfully');
					}
					UiRenderer.refresh();
				} catch (err) {
					Toast.error(err.message || i18n.error || 'An error occurred');
				}
			}
		},

		async handleRemoveMember(clientId, username) {
			const i18n = StateManager.getI18n();
			const result = await Alert.confirm({
				title: i18n.confirmDelete || 'Confirm Delete',
				text: `Remove ${username} from this group?`,
				confirmText: i18n.yesDelete || 'Delete',
				cancelText: i18n.cancel || 'Cancel',
			});

			if (result.isConfirmed) {
				try {
					const groupId = MappingModule.getCurrentGroupId();
					const [err] = await unassignGroupsFromClient({ clientId, groupIds: [groupId] });
					if (err) throw err;

					Toast.success('Removed successfully');
					MappingModule.open({ id: groupId, name: MappingModule.getCurrentGroupName() });
					UiRenderer.refresh();
				} catch (err) {
					Toast.error(err.message || i18n.error || 'An error occurred');
				}
			}
		},
	};

	Controller.init();
});
