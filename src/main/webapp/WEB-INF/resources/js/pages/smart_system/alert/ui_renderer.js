import { TabulatorFull as Tabulator } from '../../../lib/tabulator_esm.min.js';
import { StateManager } from './state_manager.js';
import { getAlerts } from '../../../api/alert.api.js';

const { i18n } = window.__ALERT_CONFIG__;

export const UiRenderer = (() => {
  let table = null;

  const init = (onAck, onResolve) => {
    table = new Tabulator('#alertsTable', {
      height: 'auto',
      layout: 'fitColumns',
      responsiveLayout: 'collapse',
      placeholder: `
        <div class="text-center py-5 text-muted">
          <i data-lucide="inbox" class="mb-2" style="width: 48px; height: 48px"></i>
          <p>${i18n.noData}</p>
        </div>`,
      pagination: true,
      paginationMode: 'remote',
      paginationSize: 10,
      paginationSizeSelector: [10, 25, 50, 100],
      paginationCounter: 'rows',
      ajaxURL: '/api/v1/alerts',
      ajaxRequestFunc: async (url, config, params) => {
        try {
          const page = (params.page || 1) - 1;
          const size = params.size || 10;
          const status = document.getElementById('filterStatus')?.value || '';
          const severity = document.getElementById('filterSeverity')?.value || '';

          const [err, res] = await getAlerts({ page, size, status, severity });
          if (err) throw err;

          const dataList = res.data.content || [];
          StateManager.init(dataList);

          return {
            last_page: res.data.totalPages || 1,
            data: dataList
          };
        } catch (e) {
          console.error('Failed to fetch alerts:', e);
          Swal.fire(i18n.error || 'Error', e.message || 'Failed to load alerts', 'error');
          return {
            last_page: 1,
            data: []
          };
        } finally {
          const btnReload = document.getElementById('btnReloadAlerts');
          const icon = btnReload?.querySelector('[data-lucide="refresh-cw"]');
          if (btnReload) btnReload.disabled = false;
          if (icon) icon.classList.remove('lucide-spin');
          window.renderIcons?.();
        }
      },
      columns: [
        {
          title: i18n.colId,
          field: 'id',
          minWidth: 70,
          width: 90,
          hozAlign: 'center',
          formatter: (cell) => `<div class="d-flex align-items-center justify-content-center h-100"><span class="fw-medium text-muted">#${cell.getValue()}</span></div>`,
        },
        {
          title: i18n.colRule,
          field: 'ruleName',
          minWidth: 150,
          formatter: (cell) => `<div class="d-flex flex-column justify-content-center h-100 py-1"><div class="fw-bold text-dark small">${cell.getValue() || ''}</div></div>`,
        },
        {
          title: i18n.colTitle,
          field: 'title',
          minWidth: 180,
          formatter: (cell) => `<div class="d-flex flex-column justify-content-center h-100 py-1"><div class="text-body small">${cell.getValue() || ''}</div></div>`,
        },
        {
          title: i18n.colSeverity,
          field: 'severity',
          minWidth: 100,
          width: 120,
          hozAlign: 'center',
          formatter: (cell) => {
            const val = cell.getValue();
            let badgeClass = 'bg-secondary';
            if (val === 'INFO') badgeClass = 'bg-info text-white';
            else if (val === 'WARNING') badgeClass = 'bg-warning text-dark';
            else if (val === 'CRITICAL') badgeClass = 'bg-danger text-white';
            return `<div class="d-flex align-items-center justify-content-center h-100"><span class="badge ${badgeClass} rounded-pill px-2.5 py-1.5">${val}</span></div>`;
          },
        },
        {
          title: i18n.colStatus,
          field: 'status',
          minWidth: 120,
          width: 140,
          hozAlign: 'center',
          formatter: (cell) => {
            const val = cell.getValue();
            let badgeClass = 'bg-secondary';
            if (val === 'ACTIVE') badgeClass = 'bg-danger text-white';
            else if (val === 'ACKNOWLEDGED') badgeClass = 'bg-warning text-dark';
            else if (val === 'RESOLVED') badgeClass = 'bg-success text-white';
            return `<div class="d-flex align-items-center justify-content-center h-100"><span class="badge ${badgeClass} rounded-pill px-2.5 py-1.5">${val}</span></div>`;
          },
        },
        {
          title: i18n.colTriggeredAt,
          field: 'triggeredAt',
          minWidth: 150,
          hozAlign: 'center',
          formatter: (cell) => {
            const val = cell.getValue();
            if (!val) return '';
            const date = new Date(val);
            return `<div class="d-flex align-items-center justify-content-center h-100"><span class="text-muted small">${date.toLocaleString()}</span></div>`;
          },
        },
        {
          title: i18n.colActions,
          hozAlign: 'center',
          headerSort: false,
          width: 160,
          responsive: 0,
          formatter: (cell) => {
            const data = cell.getData();
            const isAckDisabled = data.status !== 'ACTIVE';
            const isResolveDisabled = data.status === 'RESOLVED';
            return `
              <div class="d-flex align-items-center justify-content-center h-100 gap-1">
                <button class="btn btn-light btn-sm rounded-pill btn-ack" data-id="${data.id}" title="${i18n.actionAck}" ${isAckDisabled ? 'disabled' : ''}>
                  <i data-lucide="check" class="lucide-sm text-warning"></i>
                </button>
                <button class="btn btn-light btn-sm rounded-pill btn-resolve" data-id="${data.id}" title="${i18n.actionResolve}" ${isResolveDisabled ? 'disabled' : ''}>
                  <i data-lucide="check-check" class="lucide-sm text-success"></i>
                </button>
                <button class="btn btn-light btn-sm rounded-pill btn-detail" data-id="${data.id}" title="${i18n.actionDetail}">
                  <i data-lucide="info" class="lucide-sm text-primary"></i>
                </button>
              </div>`;
          },
        },
      ],
    });

    table.on('renderComplete', () => window.renderIcons?.());

    document.addEventListener('click', (e) => {
      const btnAck = e.target.closest('.btn-ack');
      const btnResolve = e.target.closest('.btn-resolve');
      const btnDetail = e.target.closest('.btn-detail');

      if (btnAck) {
        const id = btnAck.dataset.id;
        onAck?.(id, btnAck);
      } else if (btnResolve) {
        const id = btnResolve.dataset.id;
        onResolve?.(id, btnResolve);
      } else if (btnDetail) {
        const id = btnDetail.dataset.id;
        const alert = StateManager.getAlert(id);
        if (alert) {
          showDetailModal(alert);
        }
      }
    });
  };

  const reload = () => {
    if (table) {
      // Toggle spin on manual triggers
      const btnReload = document.getElementById('btnReloadAlerts');
      const icon = btnReload?.querySelector('[data-lucide="refresh-cw"]');
      if (btnReload) btnReload.disabled = true;
      if (icon) icon.classList.add('lucide-spin');
      table.setData();
    }
  };

  const updateRow = (id, data) => {
    if (table) {
      table.updateRow(id, data);
    }
  };

  const showDetailModal = (alert) => {
    const modalEl = document.getElementById('alertDetailModal');
    if (!modalEl) return;

    document.getElementById('detailTitle').textContent = alert.title || '';
    document.getElementById('detailBody').textContent = alert.body || '';
    document.getElementById('detailRule').textContent = alert.ruleName || '';

    // Severity badge
    const severityEl = document.getElementById('detailSeverity');
    let severityBadge = '';
    if (alert.severity === 'INFO') {
      severityBadge = '<span class="badge bg-info text-white rounded-pill px-2.5 py-1.5">INFO</span>';
    } else if (alert.severity === 'WARNING') {
      severityBadge = '<span class="badge bg-warning text-dark rounded-pill px-2.5 py-1.5">WARNING</span>';
    } else if (alert.severity === 'CRITICAL') {
      severityBadge = '<span class="badge bg-danger text-white rounded-pill px-2.5 py-1.5">CRITICAL</span>';
    }
    severityEl.innerHTML = severityBadge;

    // Status badge
    const statusEl = document.getElementById('detailStatus');
    let statusBadge = '';
    if (alert.status === 'ACTIVE') {
      statusBadge = '<span class="badge bg-danger text-white rounded-pill px-2.5 py-1.5">ACTIVE</span>';
    } else if (alert.status === 'ACKNOWLEDGED') {
      statusBadge = '<span class="badge bg-warning text-dark rounded-pill px-2.5 py-1.5">ACKNOWLEDGED</span>';
    } else if (alert.status === 'RESOLVED') {
      statusBadge = '<span class="badge bg-success text-white rounded-pill px-2.5 py-1.5">RESOLVED</span>';
    }
    statusEl.innerHTML = statusBadge;

    // Triggered At
    document.getElementById('detailTriggeredAt').textContent = alert.triggeredAt
      ? new Date(alert.triggeredAt).toLocaleString()
      : '';

    // Ack Info
    document.getElementById('detailAckBy').textContent = alert.acknowledgedByUsername || i18n.notAvailable;
    document.getElementById('detailAckAt').textContent = alert.acknowledgedAt
      ? new Date(alert.acknowledgedAt).toLocaleString()
      : '';

    // Resolve Info
    document.getElementById('detailResBy').textContent = alert.resolvedByUsername || (alert.status === 'RESOLVED' ? i18n.systemResolved : i18n.notAvailable);
    document.getElementById('detailResAt').textContent = alert.resolvedAt
      ? new Date(alert.resolvedAt).toLocaleString()
      : '';

    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    modal.show();
  };

  return {
    init,
    reload,
    updateRow,
    showDetailModal,
  };
})();
