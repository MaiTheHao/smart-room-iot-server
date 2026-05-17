import { getRuleById, updateRule } from '../../../../api/rule.api.js';
import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { ConditionModal } from './condition_modal.js';

document.addEventListener('DOMContentLoaded', () => {
    const ruleId = document.getElementById('ruleId')?.value;
    if (!ruleId) return;

    const Controller = {
        async init() {
            UiRenderer.init(
                (localId) => ConditionModal.open(localId),
                (localId) => this.handleDelete(localId)
            );
            ConditionModal.init();
            
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
            document.getElementById('btnAddCondition')?.addEventListener('click', () => ConditionModal.open());
            document.getElementById('conditionForm')?.addEventListener('submit', (e) => ConditionModal.submit(e));
            document.getElementById('btnSaveAll')?.addEventListener('click', () => this.handleSaveAll());
        },

        async loadData() {
            try {
                const [err, res] = await getRuleById(ruleId);
                if (err) throw err;
                StateManager.init(res.data?.conditions || []);
                UiRenderer.render();
            } catch (error) {
                Swal.fire('Error', 'Failed to load conditions', 'error');
            }
        },

        async handleDelete(localId) {
            const result = await Swal.fire({
                title: 'Delete condition?',
                text: 'This will be marked for deletion. Click Save All to apply.',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Yes, delete',
                confirmButtonColor: '#d33'
            });

            if (result.isConfirmed) {
                StateManager.deleteCondition(localId);
                UiRenderer.render();
            }
        },

        async handleSaveAll() {
            const btnSave = document.getElementById('btnSaveAll');
            const originalHtml = btnSave.innerHTML;
            btnSave.disabled = true;
            btnSave.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Saving...`;

            try {
                const conditionsPayload = StateManager.getPayload();
                
                // For Rule, it's a PATCH. We send the replaced conditions array.
                const [err] = await updateRule(ruleId, { conditions: conditionsPayload });
                
                if (err) throw err;

                Swal.fire({
                    title: 'Success',
                    text: 'Conditions have been saved successfully.',
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
