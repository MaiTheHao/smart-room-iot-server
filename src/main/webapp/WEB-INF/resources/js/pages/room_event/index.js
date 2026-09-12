/**
 * Room Event Controller
 * Điều phối vòng đời trang Quản lý Sự kiện Phòng
 * Tuân thủ Clean Code & phong cách smart_system/rule
 */

import {
  getEventCodes,
  getConfigsByRoom,
  createConfig,
  updateConfig,
  deleteConfig,
  getConditions,
  replaceConditions,
  getActions,
  replaceActions,
} from '../../api/room_event.api.js';
import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { ConditionModal } from './condition_modal.js';
import { ActionModal } from './action_modal.js';
import { Alert, Toast } from '../../common/notification_util.js';
import {
  CreateRoomEventConfigDto,
  UpdateRoomEventConfigDto,
} from '../../types/room_event.domain.js';

const config = window.__ROOM_EVENT_PAGE_CONFIG__ || {};
const { roomId, i18n = {} } = config;

const RoomEventController = {
  async init() {
    if (!roomId) {
      console.error('Missing roomId in page configuration.');
      return;
    }

    UiRenderer.init({
      onSaveConfig: (args) => this.handleSaveConfig(args),
      onOpenConditions: (configId, eventCode) => this.handleOpenConditions(configId, eventCode),
      onOpenActions: (configId, eventCode) => this.handleOpenActions(configId, eventCode),
      onOpenEditConfig: (configId) => this.handleOpenEditConfig(configId),
      onDeleteConfig: (configId, eventCode) => this.handleDeleteConfig(configId, eventCode),
      onToggleStatus: (configId, isActive) => this.handleToggleStatus(configId, isActive),
    });

    ConditionModal.init((configId, payload) => this.handleSaveConditions(configId, payload));
    ActionModal.init((configId, payload) => this.handleSaveActions(configId, payload));

    StateManager.subscribe((state) => {
      UiRenderer.renderTable(state.configs);
    });

    this.bindHeaderButtons();
    await this.loadInitialData();
  },

  bindHeaderButtons() {
    document.getElementById('btnReload')?.addEventListener('click', () => {
      this.loadInitialData();
    });

    document.getElementById('btnAddConfig')?.addEventListener('click', () => {
      const unconfigured = StateManager.getUnconfiguredCodes();
      UiRenderer.openCreateConfigModal(unconfigured);
    });
  },

  async loadInitialData() {
    try {
      const [[errCodes, resCodes], [errConfigs, resConfigs]] = await Promise.all([
        getEventCodes(),
        getConfigsByRoom(roomId),
      ]);

      if (errCodes) throw errCodes;
      if (errConfigs) throw errConfigs;

      const eventCodes = resCodes?.data || [];
      const configs = resConfigs?.data || [];

      StateManager.init(roomId, eventCodes, configs);
    } catch (err) {
      console.error('Failed to load initial room event data', err);
      Alert.error(err.message || 'Không thể tải dữ liệu sự kiện phòng.', 'Lỗi');
    }
  },

  async handleSaveConfig({ isEdit, configId, data }) {
    try {
      if (isEdit) {
        const dto = UpdateRoomEventConfigDto.fromForm(data);
        dto.validate();

        const [err, res] = await updateConfig(roomId, configId, {
          isActive: dto.isActive,
          cooldownSeconds: dto.cooldownSeconds,
        });
        if (err) throw err;

        StateManager.updateConfig(res?.data);
        Toast.success('Cập nhật cấu hình sự kiện thành công!');
      } else {
        const dto = CreateRoomEventConfigDto.fromForm(data);
        dto.validate();

        const [err, res] = await createConfig(roomId, {
          eventCode: dto.eventCode,
          isActive: dto.isActive,
          cooldownSeconds: dto.cooldownSeconds,
        });
        if (err) throw err;

        StateManager.addConfig(res?.data);
        Toast.success('Tạo cấu hình sự kiện mới thành công!');
      }
    } catch (err) {
      console.error('Failed to save event config', err);
      Alert.error(err.message || i18n.saveError || 'Lưu cấu hình thất bại!', 'Lỗi');
    }
  },

  async handleToggleStatus(configId, isActive) {
    try {
      const [err, res] = await updateConfig(roomId, configId, { isActive });
      if (err) throw err;

      StateManager.updateConfig(res?.data);
      Toast.success(`Đã ${isActive ? 'bật' : 'tắt'} sự kiện thành công!`);
    } catch (err) {
      console.error('Failed to toggle event active status', err);
      Toast.error('Không thể thay đổi trạng thái sự kiện.');
      await this.loadInitialData();
    }
  },

  handleOpenEditConfig(configId) {
    const cfg = StateManager.getConfigById(configId);
    if (cfg) {
      UiRenderer.openEditConfigModal(cfg);
    }
  },

  async handleDeleteConfig(configId, eventCode) {
    const confirmResult = await Alert.confirm({
      title: i18n.confirmDelete || 'Xác nhận xóa',
      text: i18n.confirmDeleteText || `Bạn có chắc chắn muốn xóa cấu hình sự kiện ${eventCode}?`,
      confirmText: i18n.yesDelete || 'Xóa',
      cancelText: i18n.cancel || 'Hủy',
    });

    if (!confirmResult.isConfirmed) return;

    try {
      const [err] = await deleteConfig(roomId, configId);
      if (err) throw err;

      StateManager.removeConfig(configId);
      Toast.success('Đã xóa cấu hình sự kiện thành công!');
    } catch (err) {
      console.error('Failed to delete config', err);
      Alert.error(err.message || 'Xóa cấu hình thất bại!', 'Lỗi');
    }
  },

  async handleOpenConditions(configId, eventCode) {
    try {
      const [err, res] = await getConditions(roomId, configId);
      if (err) throw err;

      const conditions = res?.data || [];
      StateManager.setActiveConfig(configId, eventCode, conditions, []);
      ConditionModal.open(configId, eventCode);
    } catch (err) {
      console.error('Failed to load conditions', err);
      Toast.error('Không thể tải danh sách điều kiện của sự kiện.');
    }
  },

  async handleSaveConditions(configId, payload) {
    try {
      const [err] = await replaceConditions(roomId, configId, payload);
      if (err) throw err;

      Toast.success('Đã lưu danh sách điều kiện thành công!');
    } catch (err) {
      console.error('Failed to save conditions', err);
      Alert.error(err.message || 'Lưu điều kiện thất bại!', 'Lỗi');
    }
  },

  async handleOpenActions(configId, eventCode) {
    try {
      const [err, res] = await getActions(roomId, configId);
      if (err) throw err;

      const actions = res?.data || [];
      StateManager.setActiveConfig(configId, eventCode, [], actions);
      ActionModal.open(configId, eventCode);
    } catch (err) {
      console.error('Failed to load actions', err);
      Toast.error('Không thể tải danh sách hành động của sự kiện.');
    }
  },

  async handleSaveActions(configId, payload) {
    try {
      const [err] = await replaceActions(roomId, configId, payload);
      if (err) throw err;

      Toast.success('Đã lưu danh sách hành động thành công!');
    } catch (err) {
      console.error('Failed to save actions', err);
      Alert.error(err.message || 'Lưu hành động thất bại!', 'Lỗi');
    }
  },
};

document.addEventListener('DOMContentLoaded', () => {
  RoomEventController.init();
});
