import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { acknowledgeAlert, resolveAlert } from '../../../api/alert.api.js';

const { i18n } = window.__ALERT_CONFIG__;

document.addEventListener('DOMContentLoaded', () => {
  const tableContainer = document.querySelector('#alertsTable');
  if (!tableContainer) return;

  const Controller = {
    init() {
      UiRenderer.init(
        (id, btn) => this.handleAck(id, btn),
        (id, btn) => this.handleResolve(id, btn)
      );
      this.bindEvents();
    },

    bindEvents() {
      document.getElementById('btnReloadAlerts')?.addEventListener('click', () => UiRenderer.reload());
      document.getElementById('filterStatus')?.addEventListener('change', () => UiRenderer.reload());
      document.getElementById('filterSeverity')?.addEventListener('change', () => UiRenderer.reload());
    },

    async handleAck(id, btn) {
      const icon = btn.querySelector('svg, i');
      if (!icon) return;

      btn.disabled = true;
      const tempI = document.createElement('i');
      tempI.setAttribute('data-lucide', 'loader-2');
      tempI.className = 'lucide-sm lucide-spin';
      icon.replaceWith(tempI);
      window.renderIcons?.();

      try {
        const [err, res] = await acknowledgeAlert(id);
        if (err) throw err;

        // Update local state and table row
        StateManager.updateAlert(id, res.data);
        UiRenderer.updateRow(id, res.data);

        Swal.fire({
          title: i18n.success,
          icon: 'success',
          timer: 1500,
          showConfirmButton: false,
        });
      } catch (e) {
        Swal.fire(i18n.error, e.message || i18n.error, 'error');
        // Restore icon and button status on failure
        const currentIcon = btn.querySelector('svg, i');
        if (currentIcon) {
          const resetI = document.createElement('i');
          resetI.setAttribute('data-lucide', 'check');
          resetI.className = 'lucide-sm text-warning';
          currentIcon.replaceWith(resetI);
          window.renderIcons?.();
        }
        btn.disabled = false;
      }
    },

    async handleResolve(id, btn) {
      const icon = btn.querySelector('svg, i');
      if (!icon) return;

      btn.disabled = true;
      const tempI = document.createElement('i');
      tempI.setAttribute('data-lucide', 'loader-2');
      tempI.className = 'lucide-sm lucide-spin';
      icon.replaceWith(tempI);
      window.renderIcons?.();

      try {
        const [err, res] = await resolveAlert(id);
        if (err) throw err;

        // Update local state and table row
        StateManager.updateAlert(id, res.data);
        UiRenderer.updateRow(id, res.data);

        Swal.fire({
          title: i18n.success,
          icon: 'success',
          timer: 1500,
          showConfirmButton: false,
        });
      } catch (e) {
        Swal.fire(i18n.error, e.message || i18n.error, 'error');
        // Restore icon and button status on failure
        const currentIcon = btn.querySelector('svg, i');
        if (currentIcon) {
          const resetI = document.createElement('i');
          resetI.setAttribute('data-lucide', 'check-check');
          resetI.className = 'lucide-sm text-success';
          currentIcon.replaceWith(resetI);
          window.renderIcons?.();
        }
        btn.disabled = false;
      }
    }
  };

  Controller.init();
});
