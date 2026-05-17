import { getRuleById, updateRule } from '../../../../api/rule.api.js';
import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { ActionModal } from './action_modal.js';

document.addEventListener('DOMContentLoaded', () => {
    const ruleId = document.getElementById('ruleId')?.value;
    if (!ruleId) return;

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
                const [err, res] = await getRuleById(ruleId);
                if (err) throw err;
                StateManager.init(res.data?.actions || []);
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
            const btnSave = document.getElementById('btnSaveAll');
            const originalHtml = btnSave.innerHTML;
            btnSave.disabled = true;
            btnSave.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Saving...`;

            try {
                const actionsPayload = StateManager.getPayload();
                
                // For Rule, it's a PATCH. We send the replaced actions array.
                const [err] = await updateRule(ruleId, { actions: actionsPayload });
                
                if (err) throw err;

                Swal.fire({
                    title: 'Success',
                    text: 'Actions have been saved successfully.',
                    icon: 'success',
                    timer: 1500,
                    showConfirmButton: false
                });

                await this.loadData(); // Reload to get fresh IDs and clear dirty state
            } catch (error) {
                Swal.fire('Error', error.message || 'Failed to save', 'error');
            } finally {
                btnSave.disabled = false;
                btnSave.innerHTML = originalHtml;
            }
        }
    };

    Controller.init();
});
