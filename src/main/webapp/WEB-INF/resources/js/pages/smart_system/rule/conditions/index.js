import { getRuleById, updateRule } from '../../../../api/rule.api.js';
import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { ConditionModal } from './condition_modal.js';

const { i18n } = window.__CONDITIONS_CONFIG__;

document.addEventListener('DOMContentLoaded', () => {
  const ruleId = document.getElementById('ruleId')?.value;
  if (!ruleId) return;

  const Controller = {
    async init() {
      UiRenderer.init(
        (localId) => ConditionModal.open(localId),
        (localId) => this.handleDelete(localId),
      );
      ConditionModal.init();

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
      document
        .getElementById('btnAddCondition')
        ?.addEventListener('click', () => ConditionModal.open());
      document
        .getElementById('conditionForm')
        ?.addEventListener('submit', (e) => ConditionModal.submit(e));
      document.getElementById('btnSaveAll')?.addEventListener('click', () => this.handleSaveAll());
    },

    async loadData() {
      try {
        const [err, res] = await getRuleById(ruleId);
        if (err) throw err;
        StateManager.init(res.data?.conditions || []);
        UiRenderer.render();
      } catch (error) {
        Swal.fire(i18n.error, i18n.loadFailed, 'error');
      }
    },

    async handleDelete(localId) {
      const result = await Swal.fire({
        title: i18n.confirmDelete,
        text: i18n.confirmDeleteText,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: i18n.yesDelete,
        cancelButtonText: i18n.cancel,
        confirmButtonColor: '#d33',
      });

      if (result.isConfirmed) {
        StateManager.deleteCondition(localId);
        UiRenderer.render();
      }
    },

    async handleSaveAll() {
      if (!StateManager.getIsDirty()) {
        Swal.fire(i18n.info, i18n.noChanges, 'info');
        return;
      }

      const btn = document.getElementById('btnSaveAll');
      const originalHtml = btn.innerHTML;
      btn.disabled = true;
      btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>${i18n.saving}`;

      try {
        const payload = StateManager.buildPayload();
        const [err] = await updateRule(ruleId, { conditions: payload });
        if (err) throw err;

        Swal.fire({
          title: i18n.success,
          text: i18n.saveSuccess,
          icon: 'success',
          timer: 1500,
          showConfirmButton: false,
        });

        await this.loadData();
      } catch (error) {
        Swal.fire(i18n.error, error.message || i18n.error, 'error');
      } finally {
        btn.disabled = false;
        btn.innerHTML = originalHtml;
      }
    },
  };

  Controller.init();
});
