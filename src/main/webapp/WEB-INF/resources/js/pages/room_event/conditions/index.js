import { getConditions, replaceConditions } from '../../../api/room_event.api.js';
import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { ConditionModal } from './condition_modal.js';
import { Alert } from '../../../common/notification_util.js';

const { roomId, configId, i18n } = window.__ROOM_EVENT_CONDITIONS_CONFIG__;

document.addEventListener('DOMContentLoaded', () => {
  if (!roomId || !configId) return;

  const Controller = {
    async init() {
      UiRenderer.init(
        (localId) => ConditionModal.open(localId),
        (localId) => this.handleDelete(localId),
        (count) => {
          const btnDelete = document.getElementById('btnDeleteSelected');
          if (btnDelete) btnDelete.disabled = count === 0;
        }
      );
      ConditionModal.init();

      StateManager.subscribe((isDirty) => {
        const btnSave = document.getElementById('btnSaveAll');
        const unsavedStatus = document.getElementById('unsavedStatus');
        if (isDirty) {
          btnSave?.classList.remove('d-none');
          btnSave?.removeAttribute('disabled');
          unsavedStatus?.classList.add('visible');
        } else {
          btnSave?.classList.add('d-none');
          btnSave?.setAttribute('disabled', 'true');
          unsavedStatus?.classList.remove('visible');
        }
      });

      this.bindEvents();
      await this.loadData();
    },

    bindEvents() {
      document.getElementById('btnAddCondition')?.addEventListener('click', () => ConditionModal.open());
      document.getElementById('conditionForm')?.addEventListener('submit', (e) => ConditionModal.submit(e));
      document.getElementById('btnSaveAll')?.addEventListener('click', () => this.handleSaveAll());
      document.getElementById('btnDeleteSelected')?.addEventListener('click', () => this.handleBatchDelete());
    },

    async loadData() {
      try {
        const [err, res] = await getConditions(roomId, configId);
        if (err) throw err;
        StateManager.init(res?.data || []);
        UiRenderer.render();
      } catch (error) {
        Alert.error(i18n.error, i18n.loadFailed || 'Failed to load conditions.');
      }
    },

    async handleDelete(localId) {
      const result = await Alert.confirm({
        title: i18n.confirmDelete,
        text: i18n.confirmDeleteText,
        confirmText: i18n.yesDelete,
        cancelText: i18n.cancel,
      });

      if (result.isConfirmed) {
        StateManager.deleteCondition(localId);
        UiRenderer.render();
      }
    },

    async handleBatchDelete() {
      const selected = UiRenderer.getSelectedData();
      if (selected.length === 0) return;

      const result = await Alert.confirm({
        title: i18n.confirmDelete,
        text: selected.length > 1 ? `Xóa ${selected.length} điều kiện đã chọn?` : i18n.confirmDeleteText,
        confirmText: i18n.yesDelete,
        cancelText: i18n.cancel,
      });

      if (result.isConfirmed) {
        for (const row of selected) {
          StateManager.deleteCondition(row._localId);
        }
        UiRenderer.render();
        const btnDelete = document.getElementById('btnDeleteSelected');
        if (btnDelete) btnDelete.disabled = true;
      }
    },

    async handleSaveAll() {
      if (!StateManager.getIsDirty()) {
        Alert.info(i18n.info, i18n.noChanges);
        return;
      }

      const payload = StateManager.buildPayload();
      const [err] = await replaceConditions(roomId, configId, payload);

      if (err) {
        Alert.error(i18n.error, err.message || 'Error saving conditions');
        return;
      }

      Alert.success(i18n.success, i18n.saveSuccess);
      await this.loadData();
    },
  };

  Controller.init();
});
