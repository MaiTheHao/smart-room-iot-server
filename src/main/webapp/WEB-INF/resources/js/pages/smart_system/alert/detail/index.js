import { getAlertById, getAlertLogs, acknowledgeAlert, resolveAlert } from '../../../../api/alert.api.js';
import { AlertStatus, AlertActionType } from '../../../../constants/alert.constants.js';
import { Alert } from '../../../../common/notification_util.js';

const cfg = window.__ALERT_DETAIL_CONFIG__ || {};
const { i18n = {} } = cfg;
const alertConfigId = cfg.alertConfigId;
const instanceId = cfg.instanceId;

// ─── Severity / Status helpers ───────────────────────────────────────────────
const SEV_BADGE = {
  CRITICAL: 'bg-danger',
  WARNING:  'bg-warning text-dark',
  INFO:     'bg-info text-dark',
};
const STATUS_BADGE = {
  ACTIVE:       'bg-danger-subtle text-danger fw-semibold',
  ACKNOWLEDGED: 'bg-warning-subtle text-warning fw-semibold',
  RESOLVED:     'bg-success-subtle text-success fw-semibold',
};
const SEV_BORDER = {
  CRITICAL: '#dc3545',
  WARNING:  '#ffc107',
  INFO:     '#0dcaf0',
};
const ACTION_DOT_CLASS = {
  TRIGGERED:     'TRIGGERED',
  ACKNOWLEDGED:  'ACKNOWLEDGED',
  RESOLVED:      'RESOLVED',
  RE_TRIGGERED:  'RE_TRIGGERED',
  AUTO_RESOLVED: 'AUTO_RESOLVED',
};

function fmt(isoStr) {
  if (!isoStr) return i18n.notAvail || 'N/A';
  return new Date(isoStr).toLocaleString();
}
function escapeHtml(str) {
  return String(str ?? '')
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

// ─── Render top card ──────────────────────────────────────────────────────────
function renderTopCard(a) {
  const sev = a.severity || 'INFO';

  // Header border
  const header = document.getElementById('detailCardHeader');
  if (header) header.style.borderLeftColor = SEV_BORDER[sev] || '#dee2e6';

  // Badges
  const sevBadge = document.getElementById('detailSeverityBadge');
  if (sevBadge) {
    sevBadge.className = `badge ${SEV_BADGE[sev] || 'bg-secondary'} rounded-pill`;
    sevBadge.textContent = sev;
  }
  const stBadge = document.getElementById('detailStatusBadge');
  if (stBadge) {
    stBadge.className = `badge ${STATUS_BADGE[a.status] || 'bg-secondary'} rounded-pill`;
    stBadge.textContent = a.status || '—';
  }

  // Main fields
  const set = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val ?? '—'; };
  set('detailNamespace',  a.namespace);
  set('detailAlertCode',  a.alertConfigName || a.alertCode);
  set('detailTitle',      a.title);
  set('detailBody',       a.body);
  set('detailSourceId',   a.sourceId);
  set('detailTriggeredAt', fmt(a.triggeredAt));
  set('detailTriggerCount', a.triggerCount ?? 1);

  set('detailAckBy',       a.acknowledgedByUsername || (i18n.notAvail || 'N/A'));
  set('detailAckAt',       a.acknowledgedAt ? fmt(a.acknowledgedAt) : (i18n.notAvail || 'N/A'));
  set('detailResolvedBy',  a.resolvedByUsername || (a.status === AlertStatus.RESOLVED ? (i18n.autoResolved || 'Auto-resolved') : (i18n.notAvail || 'N/A')));
  set('detailResolvedAt',  a.resolvedAt ? fmt(a.resolvedAt) : (i18n.notAvail || 'N/A'));

  // Action buttons visibility
  const btnAck = document.getElementById('btnAcknowledge');
  const btnRes = document.getElementById('btnResolve');
  if (a.status === AlertStatus.ACTIVE) {
    btnAck?.classList.remove('d-none');
    btnRes?.classList.remove('d-none');
  } else if (a.status === AlertStatus.ACKNOWLEDGED) {
    btnAck?.classList.add('d-none');
    btnRes?.classList.remove('d-none');
  } else {
    btnAck?.classList.add('d-none');
    btnRes?.classList.add('d-none');
  }
}

const LOG_PAGE_SIZE = 10;
const LogState = {
  page: 0,
  hasMore: false,
  loading: false,
};

// ─── Render timeline ──────────────────────────────────────────────────────────
function renderTimeline(logs = [], append = false) {
  const feedEl   = document.getElementById('timelineFeed');
  const emptyEl  = document.getElementById('timelineEmpty');
  const loadingEl = document.getElementById('timelineLoading');
  const loadMoreBtn = document.getElementById('loadMoreLogsBtn');

  loadingEl?.classList.add('d-none');

  if (!append) {
    if (!logs || logs.length === 0) {
      emptyEl?.classList.remove('d-none');
      feedEl?.classList.add('d-none');
      if (loadMoreBtn) loadMoreBtn.classList.add('d-none');
      return;
    }
    feedEl?.classList.remove('d-none');
    feedEl.innerHTML = '';
  }

  // Sort descending by timestamp (newest logs first)
  const sorted = [...logs].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  
  // Xóa class tl-item-last cũ của phần tử cuối cùng nếu có append
  if (append && feedEl) {
    const lastItem = feedEl.querySelector('.tl-item-last');
    if (lastItem) {
      lastItem.classList.remove('tl-item-last');
    }
  }

  sorted.forEach((log, idx) => {
    const dotClass = ACTION_DOT_CLASS[log.actionType] || 'TRIGGERED';
    const isLast = idx === sorted.length - 1;
    const payloadHtml = (log.payload && log.payload !== 'null')
      ? `<pre class="tl-payload mt-1 mb-0">${escapeHtml(typeof log.payload === 'object' ? JSON.stringify(log.payload, null, 2) : log.payload)}</pre>`
      : '';

    const item = document.createElement('div');
    item.className = `tl-item${isLast ? ' tl-item-last' : ''}`;
    item.innerHTML = `
      <div class="tl-dot ${dotClass}"></div>
      <div class="tl-time">${fmt(log.createdAt)}</div>
      <div class="tl-content">
        <div class="d-flex gap-2 align-items-center mb-1 flex-wrap">
          <span class="badge bg-secondary rounded-pill">${escapeHtml(log.actionType || '')}</span>
          <span class="badge bg-light text-dark border">${escapeHtml(log.actorType || '')}: ${escapeHtml(String(log.actorId || ''))}</span>
        </div>
        <div class="tl-msg">${escapeHtml(log.message || '')}</div>
        ${payloadHtml}
      </div>
    `;
    feedEl?.appendChild(item);
  });

  if (loadMoreBtn) {
    if (LogState.hasMore) {
      loadMoreBtn.classList.remove('d-none');
    } else {
      loadMoreBtn.classList.add('d-none');
    }
  }
}

// ─── Action handlers ──────────────────────────────────────────────────────────
async function handleAck() {
  const result = await Alert.confirm({
    title: i18n.confirmAck || 'Acknowledge this alert?',
    confirmText: i18n.yes || 'Yes',
    cancelText: i18n.cancel || 'Cancel',
  });
  if (!result.isConfirmed) return;

  const [err] = await acknowledgeAlert(alertConfigId, instanceId);
  if (err) return Swal.fire(i18n.error, err.message, 'error');
  Swal.fire({ title: i18n.success, text: i18n.ackSuccess, icon: 'success', timer: 1500, showConfirmButton: false });
  await loadPage();
}

async function handleResolve() {
  const result = await Alert.confirm({
    title: i18n.confirmRes || 'Resolve this alert?',
    confirmText: i18n.yes || 'Yes',
    cancelText: i18n.cancel || 'Cancel',
  });
  if (!result.isConfirmed) return;

  const [err] = await resolveAlert(alertConfigId, instanceId);
  if (err) return Swal.fire(i18n.error, err.message, 'error');
  Swal.fire({ title: i18n.success, text: i18n.resSuccess, icon: 'success', timer: 1500, showConfirmButton: false });
  await loadPage();
}

// ─── Load logs separated ──────────────────────────────────────────────────────
async function loadLogs(append = false) {
  if (LogState.loading) return;
  LogState.loading = true;

  const loadMoreBtn = document.getElementById('loadMoreLogsBtn');
  if (loadMoreBtn && !append) {
    loadMoreBtn.classList.add('d-none');
  }

  try {
    const [err, res] = await getAlertLogs(alertConfigId, instanceId, {
      page: LogState.page,
      size: LOG_PAGE_SIZE,
    });
    if (err) throw err;

    const { content = [], totalPages = 0 } = res.data ?? {};
    LogState.hasMore = LogState.page + 1 < totalPages;
    renderTimeline(content, append);
  } catch (err) {
    console.error('[AlertDetail] Failed to load logs', err);
    if (!append) {
      document.getElementById('timelineLoading')?.classList.add('d-none');
      document.getElementById('timelineEmpty')?.classList.remove('d-none');
    }
  } finally {
    LogState.loading = false;
  }
}

// ─── Init ─────────────────────────────────────────────────────────────────────
async function loadPage() {
  try {
    const [errA, resA] = await getAlertById(alertConfigId, instanceId);
    if (errA) throw errA;
    renderTopCard(resA.data);
    
    LogState.page = 0;
    await loadLogs(false);
  } catch (err) {
    Swal.fire(i18n.error, err.message || i18n.error, 'error');
  }
  if (window.lucide) lucide.createIcons();
}

document.addEventListener('DOMContentLoaded', () => {
  if (!alertConfigId || !instanceId) {
    console.error('[AlertDetail] Missing alertConfigId or instanceId');
    return;
  }

  document.getElementById('btnAcknowledge')?.addEventListener('click', handleAck);
  document.getElementById('btnResolve')?.addEventListener('click', handleResolve);
  document.getElementById('loadMoreLogsBtn')?.addEventListener('click', () => {
    LogState.page++;
    loadLogs(true);
  });

  loadPage();
});
