import {
  getEventCodes,
  getConfigsByRoom,
  updateConfig,
  deleteConfig,
} from '../../api/room_event.api.js';
import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { ConfigModal } from './config_modal.js';
import { Alert } from '../../common/notification_util.js';

const { roomId, i18n } = window.__ROOM_EVENT_PAGE_CONFIG__;

document.addEventListener('DOMContentLoaded', () => {
  if (!roomId) return;

  const Controller = {
    async init() {
      UiRenderer.init(
        (configId) => ConfigModal.open(configId),
        (configId) => this.handleDelete(configId),
        (configId, newStatus) => this.handleToggleStatus(configId, newStatus),
        (count) => {
          const btnDelete = document.getElementById('btnDeleteSelected');
          if (btnDelete) btnDelete.disabled = count === 0;
        }
      );

      ConfigModal.init();

      StateManager.subscribe(() => {
        UiRenderer.render();
      });

      this.bindEvents();
      await this.loadData();
    },

    bindEvents() {
      document.getElementById('btnAddConfig')?.addEventListener('click', () => ConfigModal.open());
      document.getElementById('btnReload')?.addEventListener('click', () => this.loadData());
      document.getElementById('btnDeleteSelected')?.addEventListener('click', () => this.handleBatchDelete());
    },

    async loadData() {
      try {
        const [
          [codesErr, codesRes],
          [configsErr, configsRes],
        ] = await Promise.all([getEventCodes(), getConfigsByRoom(roomId)]);

        if (codesErr || configsErr) {
          throw codesErr || configsErr;
        }

        StateManager.init(roomId, codesRes?.data || [], configsRes?.data || []);
        UiRenderer.render();
      } catch (error) {
        Alert.error(i18n.saveError || 'Lỗi', error.message || 'Không thể tải cấu hình sự kiện phòng.');
      }
    },

    async handleToggleStatus(configId, newStatus) {
      const config = StateManager.getConfig(configId);
      if (!config) return;

      const [err, res] = await updateConfig(roomId, configId, {
        isActive: newStatus,
        cooldownSeconds: config.cooldownSeconds,
      });

      if (err) {
        Alert.error(i18n.error || 'Lỗi', err.message || i18n.updateStatusError || 'Không thể cập nhật trạng thái');
        UiRenderer.render();
        return;
      }

      StateManager.updateConfig(res?.data);
    },

    async handleDelete(configId) {
      const result = await Alert.confirm({
        title: i18n.confirmDelete,
        text: i18n.confirmDeleteText,
        confirmText: i18n.yesDelete,
        cancelText: i18n.cancel,
      });

      if (result.isConfirmed) {
        const [err] = await deleteConfig(roomId, configId);
        if (err) {
          Alert.error(i18n.error || 'Lỗi', err.message || i18n.deleteError || 'Xóa cấu hình sự kiện thất bại');
          return;
        }

        StateManager.removeConfig(configId);
        Alert.success(i18n.success || 'Thành công', i18n.deleteSuccess || 'Đã xóa cấu hình sự kiện');
      }
    },

    async executeBatchDelete(selected) {
      for (const row of selected) {
        const [err] = await deleteConfig(roomId, row.id);
        if (!err) StateManager.removeConfig(row.id);
      }
    },

    async handleBatchDelete() {
      const selected = UiRenderer.getSelectedData();
      if (selected.length === 0) return;

      const text = selected.length > 1
        ? (i18n.batchDeleteConfirm ? i18n.batchDeleteConfirm.replace('{0}', selected.length) : `Xóa ${selected.length} cấu hình sự kiện đã chọn?`)
        : i18n.confirmDeleteText;

      const result = await Alert.confirm({
        title: i18n.confirmDelete,
        text,
        confirmText: i18n.yesDelete,
        cancelText: i18n.cancel,
      });

      if (result.isConfirmed) {
        await this.executeBatchDelete(selected);
        Alert.success(i18n.success || 'Thành công', i18n.batchDeleteSuccess || 'Đã xóa các cấu hình sự kiện đã chọn');
        const btnDelete = document.getElementById('btnDeleteSelected');
        if (btnDelete) btnDelete.disabled = true;
      }
    },
  };

  Controller.init();
});
