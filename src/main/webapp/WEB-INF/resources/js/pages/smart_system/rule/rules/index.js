import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { RuleModal } from './rule_modal.js';
import { getRules, deleteRule, toggleRuleStatus, executeRuleNow } from '../../../../api/rule.api.js';

document.addEventListener('DOMContentLoaded', () => {
  const tableContainer = document.querySelector('#rulesTable');
  if (!tableContainer) return;

  const { i18n } = window.__RULE_CONFIG__;
  let isLoadingData = false;

  const Controller = {
    init() {
      RuleModal.init();
      UiRenderer.init(
        (data) => RuleModal.open(data),
        (id, btn) => this.handleExecute(id, btn),
        (id, isActive, el) => this.handleToggleStatus(id, isActive, el),
        (count) => {
          const btnDelete = document.getElementById('btnDeleteSelected');
          if (btnDelete) btnDelete.disabled = count === 0;
        }
      );
      this.bindEvents();
      this.loadData();
    },

    bindEvents() {
      document.getElementById('btnReload')?.addEventListener('click', () => this.loadData());
      document.getElementById('btnAdd')?.addEventListener('click', () => RuleModal.open());
      document.getElementById('btnDeleteSelected')?.addEventListener('click', () => this.handleBatchDelete());
      document.getElementById('ruleForm')?.addEventListener('submit', (e) => RuleModal.submit(e, () => this.loadData()));
    },

    async loadData() {
      if (isLoadingData) return;
      const btnReload = document.getElementById('btnReload');
      const icon = btnReload?.querySelector('[data-lucide="refresh-cw"]');

      try {
        isLoadingData = true;
        if (btnReload) btnReload.disabled = true;
        if (icon) icon.classList.add('lucide-spin');

        const [err, res] = await getRules(0, 1000);
        if (err) throw err;

        const dataList = res.data.content || [];
        StateManager.init(dataList);
        UiRenderer.render();
      } catch (err) {
        console.error('Refresh error:', err);
      } finally {
        isLoadingData = false;
        if (btnReload) btnReload.disabled = false;
        if (icon) icon.classList.remove('lucide-spin');
        window.renderIcons?.();
      }
    },

    async handleToggleStatus(id, isActive, el) {
      el.disabled = true;
      try {
        const [err] = await toggleRuleStatus(id, isActive);
        if (err) throw err;
        StateManager.updateRule(id, { isActive });
        UiRenderer.render();
      } catch (e) {
        el.checked = !isActive;
        Swal.fire(i18n.error, e.message || i18n.error, 'error');
      } finally {
        el.disabled = false;
      }
    },

    async handleExecute(id, btn) {
      const icon = btn.querySelector('svg, i');
      if (!icon) return;

      const tempI = document.createElement('i');
      tempI.setAttribute('data-lucide', 'loader-2');
      tempI.className = 'lucide-sm lucide-spin';
      icon.replaceWith(tempI);
      window.renderIcons?.();

      try {
        const [err] = await executeRuleNow(id);
        if (err) throw err;
        Swal.fire({
          title: 'Executed',
          icon: 'success',
          timer: 1500,
          showConfirmButton: false,
        });
      } catch (e) {
        Swal.fire(i18n.error, e.message || i18n.error, 'error');
      } finally {
        const newIcon = btn.querySelector('svg, i');
        if (newIcon) {
          const resetI = document.createElement('i');
          resetI.setAttribute('data-lucide', 'play');
          resetI.className = 'lucide-sm text-success';
          newIcon.replaceWith(resetI);
          window.renderIcons?.();
        }
      }
    },

    async handleBatchDelete() {
      const selected = UiRenderer.getSelectedData();
      if (selected.length === 0) return;

      const result = await Swal.fire({
        title: i18n.confirmDelete,
        text: selected.length > 1 ? i18n.confirmDeleteTextBatch.replace('{0}', selected.length) : i18n.confirmDeleteTextSingle.replace('{0}', '1'),
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: i18n.yesDelete,
        cancelButtonText: i18n.cancel,
      });

      if (result.isConfirmed) {
        try {
          let errorCount = 0;
          let lastErrorMessage = '';

          for (const row of selected) {
            const [err] = await deleteRule(row.id);
            if (err) {
              errorCount++;
              lastErrorMessage = err.message;
            } else {
              StateManager.deleteRule(row.id);
            }
          }

          if (errorCount > 0) {
            Swal.fire(i18n.error, `Failed to delete ${errorCount} items. ${lastErrorMessage}`, 'error');
          } else {
            await Swal.fire(i18n.success, i18n.success, 'success');
          }
          UiRenderer.render();
          const btnDelete = document.getElementById('btnDeleteSelected');
          if (btnDelete) btnDelete.disabled = true;
        } catch (err) {
          Swal.fire(i18n.error, err.message, 'error');
        }
      }
    },
  };

  Controller.init();
});
