import { getAutomationActions, addAutomationAction, updateAutomationAction, deleteAutomationAction } from '../../../../api/automation.api.js';
import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { ActionModal } from './action_modal.js';
import { Alert } from '../../../../common/notification_util.js';

const { i18n } = window.__ACTIONS_CONFIG__;

document.addEventListener('DOMContentLoaded', () => {
    const automationId = document.getElementById('automationId')?.value;
    if (!automationId) return;

    const Controller = {
        async init() {
            UiRenderer.init(
                (localId) => ActionModal.open(localId),
                (localId) => this.handleDelete(localId),
                (count) => {
                    const btnDelete = document.getElementById('btnDeleteSelected');
                    if (btnDelete) btnDelete.disabled = count === 0;
                }
            );
            ActionModal.init();
            
            StateManager.subscribe((isDirty) => {
                const btnSave = document.getElementById('btnSaveAll');
                if (isDirty) {
                    btnSave?.classList.remove('d-none');
                    btnSave?.removeAttribute('disabled');
                } else {
                    btnSave?.classList.add('d-none');
                    btnSave?.setAttribute('disabled', 'true');
                }
            });

            this.bindEvents();
            await this.loadData();
        },

        bindEvents() {
            document.getElementById('btnAddAction')?.addEventListener('click', () => ActionModal.open());
            document.getElementById('actionForm')?.addEventListener('submit', (e) => ActionModal.submit(e));
            document.getElementById('btnSaveAll')?.addEventListener('click', () => this.handleSaveAll());
            document.getElementById('btnDeleteSelected')?.addEventListener('click', () => this.handleBatchDelete());
        },

        async loadData() {
            try {
                const [err, res] = await getAutomationActions(automationId);
                if (err) throw err;
                StateManager.init(res.data || []);
                UiRenderer.render();
            } catch (error) {
                Swal.fire(i18n.error, i18n.loadFailed, 'error');
            }
        },

        async handleDelete(localId) {
            const result = await Alert.confirm({
                title: i18n.confirmDelete,
                text: i18n.confirmDeleteText,
                confirmText: i18n.yesDelete,
                cancelText: i18n.cancel
            });

            if (result.isConfirmed) {
                StateManager.deleteAction(localId);
                UiRenderer.render();
            }
        },

        async handleBatchDelete() {
            const selected = UiRenderer.getSelectedData();
            if (selected.length === 0) return;

            const result = await Alert.confirm({
                title: i18n.confirmDelete,
                text: selected.length > 1 ? i18n.confirmDeleteTextBatch?.replace('{0}', selected.length) || `Delete ${selected.length} actions?` : i18n.confirmDeleteTextSingle?.replace('{0}', '1') || 'Delete action?',
                confirmText: i18n.yesDelete,
                cancelText: i18n.cancel
            });

            if (result.isConfirmed) {
                for (const row of selected) {
                    StateManager.deleteAction(row._localId);
                }
                UiRenderer.render();
                const btnDelete = document.getElementById('btnDeleteSelected');
                if (btnDelete) btnDelete.disabled = true;
            }
        },

        async handleSaveAll() {
            const changes = StateManager.getChanges();
            if (changes.toAdd.length === 0 && changes.toUpdate.length === 0 && changes.toDelete.length === 0) {
                Swal.fire(i18n.info, i18n.noChanges, 'info');
                return;
            }

            const btnSave = document.getElementById('btnSaveAll');
            const originalHtml = btnSave.innerHTML;
            btnSave.disabled = true;
            btnSave.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>${i18n.saving}`;

            try {
                const promises = [];

                // Deletes
                changes.toDelete.forEach(id => {
                    promises.push(deleteAutomationAction(id));
                });

                // Updates
                changes.toUpdate.forEach(action => {
                    promises.push(updateAutomationAction(action.id, {
                        executionOrder: action.executionOrder,
                        targetId: action.targetId,
                        targetType: action.targetType,
                        actionType: action.actionType,
                        parameterValue: action.parameterValue
                    }));
                });

                // Adds
                changes.toAdd.forEach(action => {
                    promises.push(addAutomationAction(automationId, {
                        executionOrder: action.executionOrder,
                        targetId: action.targetId,
                        targetType: action.targetType,
                        actionType: action.actionType,
                        parameterValue: action.parameterValue
                    }));
                });

                const results = await Promise.all(promises);
                
                // Check if any failed
                const errors = results.filter(([err]) => err !== null);
                if (errors.length > 0) {
                    throw new Error(`Failed to save ${errors.length} changes. Check console for details.`);
                }

                Swal.fire({
                    title: i18n.success,
                    text: i18n.saveSuccess,
                    icon: 'success',
                    timer: 1500,
                    showConfirmButton: false
                });

                await this.loadData(); // Reload to get fresh IDs and clear dirty state
            } catch (error) {
                Swal.fire(i18n.error, error.message || i18n.error, 'error');
            } finally {
                btnSave.disabled = false;
                btnSave.innerHTML = originalHtml;
            }
        }
    };

    Controller.init();
});
