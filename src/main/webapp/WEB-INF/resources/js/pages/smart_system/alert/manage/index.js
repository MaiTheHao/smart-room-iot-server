import { UiRenderer } from './ui_renderer.js';
import { AlertConfigModal } from './alert_modal.js';
import { Alert, Toast } from '../../../../common/notification_util.js';
import { getConfigs, deleteConfig } from '../../../../api/alert.api.js';

const { i18n } = window.__ALERT_MANAGE_CONFIG__;

let allConfigs = [];

// ─── Load data ────────────────────────────────────────────────────────────────
async function loadData() {
  try {
    const [err, res] = await getConfigs({ page: 0, size: 100 });
    if (err) throw err;
    allConfigs = res.data?.content || [];
    UiRenderer.setData(allConfigs);
  } catch (err) {
    console.error('[AlertManage] load error', err);
    Swal.fire(i18n.error, i18n.errLoadData || 'Failed to load data.', 'error');
  }
}

// ─── Delete ───────────────────────────────────────────────────────────────────
async function handleDelete(id) {
  const result = await Alert.confirm({
    title: i18n.confirmDelete,
    text: i18n.deleteConfirmText,
    confirmText: i18n.yesDelete,
    cancelText: i18n.cancel,
  });
  if (!result.isConfirmed) return;

  const [err] = await deleteConfig(id);
  if (err) return Swal.fire(i18n.error, err.message, 'error');
  Toast.success(i18n.success);
  await loadData();
}

async function handleBatchDelete(selected) {
  if (!selected.length) return;
  const count = selected.length;
  const text = count > 1
    ? (i18n.confirmDeleteBatch || 'Delete {0} items?').replace('{0}', count)
    : (i18n.confirmDeleteSingle || 'Delete {0} item?').replace('{0}', 1);

  const result = await Alert.confirm({
    title: i18n.confirmDelete,
    text,
    confirmText: i18n.yesDelete,
    cancelText: i18n.cancel,
  });
  if (!result.isConfirmed) return;

  const errors = [];
  for (const row of selected) {
    const [err] = await deleteConfig(row.id);
    if (err) errors.push(err.message);
  }
  if (errors.length) Swal.fire(i18n.error, errors.join('\n'), 'error');
  else Toast.success(i18n.success);

  document.getElementById('btnDeleteSelected').disabled = true;
  await loadData();
}

// ─── Init ─────────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  UiRenderer.init(
    (id) => AlertConfigModal.open(id, allConfigs.find(c => String(c.id) === String(id))),
    (id) => handleDelete(id),
    (count) => {
      const btn = document.getElementById('btnDeleteSelected');
      if (btn) btn.disabled = count === 0;
    }
  );

  AlertConfigModal.init(loadData);

  document.getElementById('btnAddAlertConfig')?.addEventListener('click', () => AlertConfigModal.open());
  document.getElementById('alertConfigForm')?.addEventListener('submit', (e) => AlertConfigModal.submit(e));
  document.getElementById('btnDeleteSelected')?.addEventListener('click', () => {
    handleBatchDelete(UiRenderer.getSelectedData());
  });

  loadData();
});
