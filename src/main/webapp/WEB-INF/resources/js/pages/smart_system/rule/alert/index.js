import { getRuleById, updateRule } from '../../../../api/rule.api.js';
import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { AlertConfigModal } from './alert_modal.js';
import { Alert } from '../../../../common/notification_util.js';

const { i18n } = window.__ALERT_PAGE_CONFIG__;

document.addEventListener('DOMContentLoaded', () => {
  const ruleId = document.getElementById('ruleId')?.value;
  if (!ruleId) return;

  const Controller = {
    async init() {
      UiRenderer.init(
        (localId) => AlertConfigModal.open(localId),
        (localId) => this.handleDelete(localId),
        (count) => {
          const btnDelete = document.getElementById('btnDeleteSelected');
          if (btnDelete) btnDelete.disabled = count === 0;
        }
      );
      AlertConfigModal.init();

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
        .getElementById('btnAddAlertConfig')
        ?.addEventListener('click', () => AlertConfigModal.open());
      document
        .getElementById('alertForm')
        ?.addEventListener('submit', (e) => AlertConfigModal.submit(e));
      document.getElementById('btnSaveAll')?.addEventListener('click', () => this.handleSaveAll());
      document.getElementById('btnDeleteSelected')?.addEventListener('click', () => this.handleBatchDelete());
    },

    async loadData() {
      try {
        const [err, res] = await getRuleById(ruleId);
        if (err) throw err;
        StateManager.init(res.data?.alertConfigs || []);
        UiRenderer.render();
      } catch (error) {
        Swal.fire(i18n.error, i18n.errLoadData || 'Failed to load alert configurations', 'error');
      }
    },

    async handleDelete(localId) {
      const result = await Alert.confirm({
        title: i18n.confirmDelete,
        text: i18n.deleteConfirmText || 'Are you sure you want to delete this alert config?',
        confirmText: i18n.yesDelete,
        cancelText: i18n.cancel
      });

      if (result.isConfirmed) {
        StateManager.deleteAlert(localId);
        UiRenderer.render();
      }
    },

    async handleBatchDelete() {
      const selected = UiRenderer.getSelectedData();
      if (selected.length === 0) return;

      const result = await Alert.confirm({
        title: i18n.confirmDelete,
        text: selected.length > 1
          ? (i18n.confirmDeleteBatch || 'Delete {0} items?').replace('{0}', selected.length)
          : (i18n.confirmDeleteSingle || 'Delete {0} item?').replace('{0}', 1),
        confirmText: i18n.yesDelete,
        cancelText: i18n.cancel
      });

      if (result.isConfirmed) {
        for (const row of selected) {
          StateManager.deleteAlert(row._localId);
        }
        UiRenderer.render();
        const btnDelete = document.getElementById('btnDeleteSelected');
        if (btnDelete) btnDelete.disabled = true;
      }
    },

    async handleSaveAll() {
      if (!StateManager.getIsDirty()) {
        Swal.fire(i18n.info || 'Info', i18n.noChanges || 'No changes to save.', 'info');
        return;
      }

      const btn = document.getElementById('btnSaveAll');
      const originalHtml = btn.innerHTML;
      btn.disabled = true;
      btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>${i18n.saving || 'Saving...'}`;

      try {
        const payload = StateManager.buildPayload();
        const [err] = await updateRule(ruleId, { alertConfigs: payload });
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
