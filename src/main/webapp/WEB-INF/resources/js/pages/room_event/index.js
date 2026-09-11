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

const config = window.__ROOM_EVENT_PAGE_CONFIG__ || {};
const { roomId, i18n = {} } = config;

const RoomEventController = {
  async init() {
    if (!roomId) {
      console.error('Missing roomId in page configuration.');
      return;
    }

    ConditionModal.init();
    ActionModal.init();

    // Subscribe UiRenderer to StateManager updates
    StateManager.subscribe((state) => {
      UiRenderer.renderEventTabs(
        state.availableCodes,
        state.selectedCode,
        (code) => this.handleSelectEventCode(code)
      );
      UiRenderer.renderMainContent(state, {
        onCreateConfig: (data) => this.handleCreateConfig(data),
        onDeleteConfig: () => this.handleDeleteConfig(),
        onUpdateDraft: (changes) => StateManager.updateConfigDraft(changes),
        onOpenConditionModal: (localId) => ConditionModal.open(localId),
        onDeleteCondition: (localId) => StateManager.deleteCondition(localId),
        onOpenActionModal: (localId) => ActionModal.open(localId),
        onDeleteAction: (localId) => StateManager.deleteAction(localId),
      });
      UiRenderer.updateSaveBar(state.isDirty);
    });

    // Bind save button
    document.getElementById('btnSaveAll')?.addEventListener('click', () => this.handleSaveAll());

    await this.loadInitialData();
  },

  async loadInitialData() {
    try {
      const [errCodes, resCodes] = await getEventCodes();
      if (errCodes) throw errCodes;
      const eventCodes = resCodes?.data || [];

      const [errConfigs, resConfigs] = await getConfigsByRoom(roomId);
      if (errConfigs) throw errConfigs;
      const configs = resConfigs?.data || [];

      StateManager.init(roomId, eventCodes, configs);

      // Load sub-resources for initial selected config if present
      const initialConfig = StateManager.getCurrentConfig();
      if (initialConfig?.id) {
        await this.loadSubResources(initialConfig.id);
      }
    } catch (err) {
      console.error('Failed to load initial room event data', err);
      Alert.error(err.message || 'Không thể tải cấu hình sự kiện phòng.', 'Lỗi tải dữ liệu');
    }
  },

  async handleSelectEventCode(code) {
    StateManager.selectEventCode(code);
    const cfg = StateManager.getCurrentConfig();
    if (cfg?.id) {
      await this.loadSubResources(cfg.id);
    }
  },

  async loadSubResources(configId) {
    try {
      const [[errCond, resCond], [errAct, resAct]] = await Promise.all([
        getConditions(roomId, configId),
        getActions(roomId, configId),
      ]);

      if (errCond) throw errCond;
      if (errAct) throw errAct;

      StateManager.setLoadedSubResources(resCond?.data || [], resAct?.data || []);
    } catch (err) {
      console.error('Failed to load conditions/actions', err);
      Toast.error('Không thể tải danh sách điều kiện hoặc hành động.');
    }
  },

  async handleCreateConfig(draftData) {
    const btn = document.getElementById('btnCreateConfig');
    if (btn) {
      btn.disabled = true;
      btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang tạo...';
    }

    try {
      const [err, res] = await createConfig(roomId, draftData);
      if (err) {
        // Handle 409 Conflict if already exists
        if (err.status === 409) {
          Toast.warning(i18n.conflictError || 'Cấu hình sự kiện đã tồn tại.');
          await this.loadInitialData();
          return;
        }
        throw err;
      }

      const created = res?.data;
      Toast.success('Khởi tạo cấu hình sự kiện thành công!');
      StateManager.onSavedSuccess(created);
      await this.loadSubResources(created.id);
    } catch (err) {
      console.error('Failed to create config', err);
      Alert.error(err.message || i18n.saveError || 'Tạo cấu hình thất bại!', 'Lỗi');
    } finally {
      if (btn) {
        btn.disabled = false;
        btn.innerHTML = '<i data-lucide="plus-circle" class="me-2" style="width: 18px; height: 18px"></i><span>Khởi tạo cấu hình sự kiện</span>';
        if (window.lucide) window.lucide.createIcons();
      }
    }
  },

  async handleDeleteConfig() {
    const currentConfig = StateManager.getCurrentConfig();
    if (!currentConfig?.id) return;

    const confirmResult = await Alert.confirm({
      title: i18n.confirmDelete || 'Xác nhận xóa',
      text: i18n.confirmDeleteText || 'Bạn có chắc chắn muốn xóa cấu hình sự kiện này cùng tất cả điều kiện và hành động liên quan?',
      confirmText: i18n.yesDelete || 'Xóa',
      cancelText: i18n.cancel || 'Hủy',
    });

    if (!confirmResult.isConfirmed) return;

    try {
      const [err] = await deleteConfig(roomId, currentConfig.id);
      if (err) throw err;

      Toast.success('Đã xóa cấu hình sự kiện thành công!');
      StateManager.onDeletedSuccess(currentConfig.eventCode);
    } catch (err) {
      console.error('Failed to delete config', err);
      Alert.error(err.message || 'Xóa cấu hình thất bại!', 'Lỗi');
    }
  },

  async handleSaveAll() {
    const currentConfig = StateManager.getCurrentConfig();
    if (!currentConfig?.id) return;

    const actions = StateManager.getActions();
    // UI Validation Warning if no actions
    if (actions.length === 0) {
      const confirmWarning = await Alert.confirm({
        title: 'Chưa có hành động nào',
        text: i18n.requireActionWarning || 'Cấu hình sự kiện chưa có hành động điều khiển thiết bị nào. Bạn có chắc chắn muốn tiếp tục lưu?',
        confirmText: 'Vẫn lưu',
        cancelText: 'Quay lại thêm hành động',
      });
      if (!confirmWarning.isConfirmed) return;
    }

    const btn = document.getElementById('btnSaveAll');
    const originalHtml = btn ? btn.innerHTML : '';
    if (btn) {
      btn.disabled = true;
      btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang lưu cấu hình...';
    }

    try {
      const draft = StateManager.getConfigDraft();
      // 1. Update config if changed
      const [errCfg, resCfg] = await updateConfig(roomId, currentConfig.id, {
        isActive: draft.isActive,
        cooldownSeconds: draft.cooldownSeconds,
      });
      if (errCfg) throw errCfg;

      // 2. Replace conditions
      const conditionsPayload = StateManager.buildConditionsPayload(currentConfig.id);
      const [errCond] = await replaceConditions(roomId, currentConfig.id, conditionsPayload);
      if (errCond) throw errCond;

      // 3. Replace actions
      const actionsPayload = StateManager.buildActionsPayload(currentConfig.id);
      const [errAct] = await replaceActions(roomId, currentConfig.id, actionsPayload);
      if (errAct) throw errAct;

      Alert.success(i18n.saveSuccess || 'Đã lưu cấu hình sự kiện phòng thành công!');
      StateManager.onSavedSuccess(resCfg?.data || currentConfig);
    } catch (err) {
      console.error('Failed to save all changes', err);
      Alert.error(err.message || i18n.saveError || 'Lưu cấu hình thất bại!', 'Lỗi');
    } finally {
      if (btn) {
        btn.disabled = false;
        btn.innerHTML = originalHtml;
      }
    }
  },
};

document.addEventListener('DOMContentLoaded', () => {
  RoomEventController.init();
});
