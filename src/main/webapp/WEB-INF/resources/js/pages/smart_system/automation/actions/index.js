import { getAutomationActions, addAutomationAction, updateAutomationAction, deleteAutomationAction } from '../../../../api/automation.api.js';
import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { ActionModal } from './action_modal.js';

document.addEventListener('DOMContentLoaded', () => {
    const automationId = document.getElementById('automationId')?.value;
    if (!automationId) return;

    const Controller = {
        async init() {
            UiRenderer.init(
                (localId) => ActionModal.open(localId),
                (localId) => this.handleDelete(localId)
            );
            ActionModal.init();
            
            StateManager.subscribe((isDirty) => {
                const indicator = document.getElementById('unsavedStatus');
                if (isDirty) {
                    indicator.classList.add('visible');
                } else {
                    indicator.classList.remove('visible');
                }
            });

            this.bindEvents();
            await this.loadData();
        },

        bindEvents() {
            document.getElementById('btnAddAction')?.addEventListener('click', () => ActionModal.open());
            document.getElementById('actionForm')?.addEventListener('submit', (e) => ActionModal.submit(e));
            document.getElementById('btnSaveAll')?.addEventListener('click', () => this.handleSaveAll());
        },

        async loadData() {
            try {
                const [err, res] = await getAutomationActions(automationId);
                if (err) throw err;
                StateManager.init(res.data || []);
                UiRenderer.render();
            } catch (error) {
                Swal.fire('Error', 'Failed to load actions', 'error');
            }
        },

        async handleDelete(localId) {
            const result = await Swal.fire({
                title: 'Delete action?',
                text: 'This will be marked for deletion. Click Save All to apply.',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Yes, delete',
                confirmButtonColor: '#d33'
            });

            if (result.isConfirmed) {
                StateManager.deleteAction(localId);
                UiRenderer.render();
            }
        },

        async handleSaveAll() {
            const changes = StateManager.getChanges();
            if (changes.toAdd.length === 0 && changes.toUpdate.length === 0 && changes.toDelete.length === 0) {
                Swal.fire('Info', 'No changes to save.', 'info');
                return;
            }

            const btnSave = document.getElementById('btnSaveAll');
            const originalHtml = btnSave.innerHTML;
            btnSave.disabled = true;
            btnSave.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Saving...`;

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
                    title: 'Success',
                    text: 'All changes have been saved successfully.',
                    icon: 'success',
                    timer: 1500,
                    showConfirmButton: false
                });

                await this.loadData(); // Reload to get fresh IDs and clear dirty state
            } catch (error) {
                Swal.fire('Error', error.message, 'error');
            } finally {
                btnSave.disabled = false;
                btnSave.innerHTML = originalHtml;
            }
        }
    };

    Controller.init();
});
