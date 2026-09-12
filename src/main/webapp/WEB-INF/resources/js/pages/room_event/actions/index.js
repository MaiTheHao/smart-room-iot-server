import { getActions, replaceActions } from '../../../api/room_event.api.js';
import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { ActionModal } from './action_modal.js';
import { Alert } from '../../../common/notification_util.js';

const { roomId, configId, i18n } = window.__ROOM_EVENT_ACTIONS_CONFIG__;

document.addEventListener('DOMContentLoaded', () => {
  if (!roomId || !configId) return;

  const Controller = {
    async init() {
      UiRenderer.init(
        (localId) => ActionModal.open(localId),
        (localId) => this.handleDelete(localId),
        (count) => {
          const btnDelete = document.getElementById('btnDeleteSelected');
          if (btnDelete) btnDelete.disabled = count === 0;
        }
      );
      ActionModal.init();

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
      document.getElementById('btnAddAction')?.addEventListener('click', () => ActionModal.open());
      document.getElementById('actionForm')?.addEventListener('submit', (e) => ActionModal.submit(e));
      document.getElementById('btnSaveAll')?.addEventListener('click', () => this.handleSaveAll());
      document.getElementById('btnDeleteSelected')?.addEventListener('click', () => this.handleBatchDelete());
    },

    async loadData() {
      try {
        const [err, res] = await getActions(roomId, configId);
        if (err) throw err;
        StateManager.init(res?.data || []);
        UiRenderer.render();
      } catch (error) {
        Alert.error(i18n.error, i18n.loadFailed || 'Failed to load actions.');
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
        StateManager.deleteAction(localId);
        UiRenderer.render();
      }
    },

    async handleBatchDelete() {
      const selected = UiRenderer.getSelectedData();
      if (selected.length === 0) return;

      const result = await Alert.confirm({
        title: i18n.confirmDelete,
        text: selected.length > 1 ? `Xóa ${selected.length} hành động đã chọn?` : i18n.confirmDeleteText,
        confirmText: i18n.yesDelete,
        cancelText: i18n.cancel,
      });

      if (result.isConfirmed) {
        for (const row of selected) {
          StateManager.deleteAction(row._localId);
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
      const [err] = await replaceActions(roomId, configId, payload);

      if (err) {
        Alert.error(i18n.error, err.message || 'Error saving actions');
        return;
      }

      Alert.success(i18n.success, i18n.saveSuccess);
      await this.loadData();
    },
  };

  Controller.init();
});
