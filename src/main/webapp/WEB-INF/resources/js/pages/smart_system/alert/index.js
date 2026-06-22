import { getAlerts } from '../../../api/alert.api.js';
import { AlertStatus, Severity, AlertNamespace } from '../../../constants/alert.constants.js';

const config = window.__ALERT_LIST_CONFIG__ || {};
const { i18n = {} } = config;

const PAGE_SIZE = 15;

const State = {
  page: 0,
  hasMore: false,
  loading: false,
  params: {},
};

// ─── DOM refs ───────────────────────────────────────────────────────────────
const feedEl       = document.getElementById('alertFeed');
const loadingEl    = document.getElementById('alertLoading');
const emptyEl      = document.getElementById('alertEmpty');
const loadMoreBtn  = document.getElementById('loadMoreBtn');
const countEl      = document.getElementById('alertCount');
const filterNs     = document.getElementById('filterNamespace');
const filterStatus = document.getElementById('filterStatus');
const filterDate   = document.getElementById('filterDateRange');

// ─── Severity helpers ────────────────────────────────────────────────────────
const SEVERITY_META = {
  [Severity.CRITICAL]: { badge: 'bg-danger',   icon: 'alert-octagon',  label: 'CRITICAL' },
  [Severity.WARNING]:  { badge: 'bg-warning text-dark', icon: 'alert-triangle', label: 'WARNING' },
  [Severity.INFO]:     { badge: 'bg-info text-dark',    icon: 'info',           label: 'INFO' },
};

const STATUS_META = {
  [AlertStatus.ACTIVE]:       { badge: 'bg-danger-subtle text-danger fw-semibold',     label: 'ACTIVE' },
  [AlertStatus.ACKNOWLEDGED]: { badge: 'bg-warning-subtle text-warning fw-semibold',   label: 'ACKNOWLEDGED' },
  [AlertStatus.RESOLVED]:     { badge: 'bg-success-subtle text-success fw-semibold',   label: 'RESOLVED' },
};

function formatTime(isoString) {
  if (!isoString) return '';
  const d = new Date(isoString);
  const now = new Date();
  const diffMs = now - d;
  const diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 1)  return i18n.justNow || 'Just now';
  if (diffMin < 60) return `${diffMin}m ago`;
  const diffH = Math.floor(diffMin / 60);
  if (diffH < 24)   return `${diffH}h ago`;
  return d.toLocaleDateString(undefined, { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
}

// ─── Card renderer ────────────────────────────────────────────────────────────
function renderCard(alert) {
  const sev    = SEVERITY_META[alert.severity] || SEVERITY_META[Severity.INFO];
  const status = STATUS_META[alert.status]     || STATUS_META[AlertStatus.ACTIVE];
  const detailUrl = `${config.baseDetailUrl || '/management/smart-system/alerts'}/${alert.alertConfigId}/instances/${alert.id}`;

  const a = document.createElement('a');
  a.href = detailUrl;
  a.className = `alert-card severity-${alert.severity}`;
  a.innerHTML = `
    <div class="d-flex justify-content-between align-items-start mb-1 gap-2">
      <span class="badge ${sev.badge} rounded-pill">${sev.label}</span>
      <div class="d-flex gap-1 ms-auto flex-shrink-0">
        <span class="badge ${status.badge} rounded-pill">${status.label}</span>
      </div>
    </div>
    <div class="alert-card-title">${escapeHtml(alert.title || '—')}</div>
    <div class="alert-card-body">${escapeHtml(alert.body || '')}</div>
    <div class="alert-card-meta">
      <span class="chip chip-namespace">${escapeHtml(alert.namespace || '')}</span>
      <span class="chip chip-code">${escapeHtml(alert.alertCode || alert.alertConfigName || '')}</span>
      <span class="text-secondary">·</span>
      <span>${escapeHtml(String(alert.sourceId || ''))}</span>
      <span class="text-secondary">·</span>
      <span>${formatTime(alert.triggeredAt)}</span>
      ${alert.triggerCount > 1 ? `<span class="text-secondary">·</span><span class="text-danger fw-semibold">${i18n.triggerCount || 'Triggered'} ×${alert.triggerCount}</span>` : ''}
    </div>
  `;
  return a;
}

function escapeHtml(str) {
  return String(str ?? '')
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

// ─── Fetch & render ───────────────────────────────────────────────────────────
async function loadAlerts(append = false) {
  if (State.loading) return;
  State.loading = true;

  if (!append) {
    feedEl.innerHTML = '';
    loadingEl.classList.remove('d-none');
    emptyEl.classList.add('d-none');
    loadMoreBtn.style.display = 'none';
  }

  try {
    const [err, res] = await getAlerts({
      ...State.params,
      page: State.page,
      size: PAGE_SIZE,
    });
    if (err) throw err;

    const { content = [], totalElements = 0, totalPages = 0 } = res.data ?? {};
    if (!append) {
      if (countEl) countEl.textContent = totalElements > 0 ? `${totalElements}` : '';
    }

    if (!append && content.length === 0) {
      emptyEl.classList.remove('d-none');
    } else {
      content.forEach((alert) => feedEl.appendChild(renderCard(alert)));
    }

    State.hasMore = State.page + 1 < totalPages;
    loadMoreBtn.style.display = State.hasMore ? 'inline-flex' : 'none';
  } catch (err) {
    console.error('[AlertList] load error', err);
    if (!append) emptyEl.classList.remove('d-none');
  } finally {
    State.loading = false;
    loadingEl.classList.add('d-none');
    if (window.lucide) lucide.createIcons();
  }
}

// ─── Init ─────────────────────────────────────────────────────────────────────
function collectParams() {
  return {
    status:    filterStatus?.value || undefined,
    namespace: filterNs?.value    || undefined,
  };
}

function applyFilters() {
  State.page   = 0;
  State.params = collectParams();
  loadAlerts(false);
}

document.addEventListener('DOMContentLoaded', () => {
  // Flatpickr date range
  if (window.flatpickr && filterDate) {
    flatpickr(filterDate, {
      mode: 'range',
      enableTime: true,
      time_24hr: true,
      altInput: true,
      altFormat: 'd/m/Y H:i',
      dateFormat: 'Z',
      onClose(dates) {
        if (dates.length === 2) {
          State.params.from = dates[0].toISOString();
          State.params.to   = dates[1].toISOString();
        } else {
          delete State.params.from;
          delete State.params.to;
        }
        State.page = 0;
        loadAlerts(false);
      },
    });
  }

  filterNs?.addEventListener('change', applyFilters);
  filterStatus?.addEventListener('change', applyFilters);

  document.getElementById('btnReloadAlerts')?.addEventListener('click', () => {
    State.page = 0;
    loadAlerts(false);
  });

  document.getElementById('btnClearFilters')?.addEventListener('click', () => {
    if (filterNs)     filterNs.value     = '';
    if (filterStatus) filterStatus.value = '';
    if (filterDate) {
      const fp = filterDate._flatpickr;
      if (fp) fp.clear();
    }
    delete State.params.from;
    delete State.params.to;
    applyFilters();
  });

  loadMoreBtn?.addEventListener('click', () => {
    State.page++;
    loadAlerts(true);
  });

  // Initial load
  applyFilters();
});
