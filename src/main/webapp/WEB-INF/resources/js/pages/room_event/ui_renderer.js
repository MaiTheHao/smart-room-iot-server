/**
 * UI Renderer cho trang Quản lý Sự kiện Phòng (Room Event)
 */
export const UiRenderer = (() => {
  const getEl = (id) => document.getElementById(id);

  const formatDateTime = (isoString) => {
    if (!isoString) return 'Chưa từng kích hoạt';
    try {
      const d = new Date(isoString);
      return d.toLocaleString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });
    } catch (e) {
      return isoString;
    }
  };

  const renderEventTabs = (availableCodes, selectedCode, onSelectCode) => {
    const nav = getEl('eventCodesNav');
    if (!nav) return;
    nav.innerHTML = '';

    availableCodes.forEach((item) => {
      const btn = document.createElement('button');
      btn.type = 'button';
      const isSelected = item.code === selectedCode;
      btn.className = `btn btn-sm rounded-pill px-3 py-2 fw-medium border event-tab-btn ${
        isSelected ? 'active bg-primary text-white border-primary' : 'bg-white text-secondary'
      }`;
      btn.innerHTML = `<i data-lucide="radio" style="width: 14px; height: 14px" class="me-1"></i>${item.code}`;
      btn.addEventListener('click', () => onSelectCode(item.code));
      nav.appendChild(btn);
    });

    if (window.lucide) window.lucide.createIcons();
  };

  const renderMainContent = (state, callbacks) => {
    const container = getEl('roomEventMainContent');
    if (!container) return;

    const { selectedCode, currentConfig, currentConfigDraft, conditions, actions } = state;

    if (!selectedCode) {
      container.innerHTML = `
        <div class="card shadow-sm border-0 rounded-3 p-5 text-center text-muted">
          <i data-lucide="alert-circle" class="mx-auto mb-2 text-secondary" style="width: 36px; height: 36px"></i>
          <h6>Không tìm thấy loại sự kiện khả dụng nào trong hệ thống.</h6>
        </div>
      `;
      if (window.lucide) window.lucide.createIcons();
      return;
    }

    if (!currentConfig) {
      // ════════════ STATE 1: EMPTY STATE ════════════
      container.innerHTML = `
        <div class="card shadow-sm border-0 rounded-3 mb-4">
          <div class="card-body p-4 text-center">
            <div class="bg-primary-subtle text-primary p-3 rounded-circle d-inline-flex align-items-center justify-content-center mb-3" style="width: 56px; height: 56px">
              <i data-lucide="bell-plus" style="width: 28px; height: 28px"></i>
            </div>
            <h5 class="fw-bold text-dark mb-2">Chưa cấu hình sự kiện "${selectedCode}"</h5>
            <p class="text-muted small mx-auto mb-4" style="max-width: 520px">
              Phòng này hiện chưa có quy tắc xử lý khi phát sinh sự kiện <strong>${selectedCode}</strong>. Thiết lập cấu hình ban đầu dưới đây để bắt đầu gắn điều kiện cảm biến và hành động thiết bị.
            </p>

            <div class="card bg-light border-0 rounded-3 mx-auto text-start p-3 mb-4" style="max-width: 480px">
              <div class="form-check form-switch mb-3">
                <input class="form-check-input" type="checkbox" role="switch" id="emptyStateIsActive" checked>
                <label class="form-check-label fw-medium small" for="emptyStateIsActive">Kích hoạt sự kiện ngay sau khi tạo</label>
              </div>
              <div class="mb-2">
                <label class="form-label small fw-bold mb-1">Thời gian giãn cách tối thiểu (Cooldown)</label>
                <div class="input-group input-group-sm">
                  <input type="number" class="form-control" id="emptyStateCooldown" value="60" min="0" step="1">
                  <span class="input-group-text">giây</span>
                </div>
                <div class="form-text small" style="font-size: 0.75rem">Khoảng cách an toàn giữa 2 lần kích hoạt liên tiếp để tránh spam thiết bị.</div>
              </div>
            </div>

            <button type="button" class="btn btn-primary px-4 py-2 rounded-pill shadow-sm d-inline-flex align-items-center" id="btnCreateConfig">
              <i data-lucide="plus-circle" class="me-2" style="width: 18px; height: 18px"></i>
              <span>Khởi tạo cấu hình sự kiện</span>
            </button>
          </div>
        </div>
      `;

      getEl('btnCreateConfig')?.addEventListener('click', () => {
        const isActive = getEl('emptyStateIsActive')?.checked ?? true;
        const cooldownSeconds = parseInt(getEl('emptyStateCooldown')?.value || '60', 10);
        callbacks.onCreateConfig({ eventCode: selectedCode, isActive, cooldownSeconds });
      });

      if (window.lucide) window.lucide.createIcons();
      return;
    }

    // ════════════ STATE 2: CONFIGURED STATE ════════════
    container.innerHTML = `
      <!-- Card Cấu hình chung -->
      <div class="card shadow-sm border-0 rounded-3 mb-4">
        <div class="card-header bg-white border-bottom-0 pt-3 pb-0 d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div class="d-flex align-items-center gap-2">
            <h6 class="mb-0 fw-bold text-dark">Cài đặt sự kiện: ${selectedCode}</h6>
            <span class="badge ${currentConfigDraft.isActive ? 'bg-success-subtle text-success border border-success-subtle' : 'bg-secondary-subtle text-secondary border'} px-2 py-1 small">
              ${currentConfigDraft.isActive ? 'Đang hoạt động' : 'Tạm dừng'}
            </span>
          </div>
          <button type="button" class="btn btn-sm btn-outline-danger rounded-pill px-3 d-inline-flex align-items-center" id="btnDeleteConfig">
            <i data-lucide="trash-2" style="width: 14px; height: 14px" class="me-1"></i>
            <span>Xóa cấu hình</span>
          </button>
        </div>
        <div class="card-body pt-3 pb-3">
          <div class="row g-3 align-items-center">
            <div class="col-md-4">
              <div class="form-check form-switch">
                <input class="form-check-input" type="checkbox" role="switch" id="cfgIsActive" ${currentConfigDraft.isActive ? 'checked' : ''}>
                <label class="form-check-label small fw-medium" for="cfgIsActive">Bật / Tắt theo dõi sự kiện</label>
              </div>
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-bold mb-1">Thời gian giãn cách (Cooldown)</label>
              <div class="input-group input-group-sm">
                <input type="number" class="form-control" id="cfgCooldown" value="${currentConfigDraft.cooldownSeconds ?? 60}" min="0" step="1">
                <span class="input-group-text">giây</span>
              </div>
            </div>
            <div class="col-md-4">
              <label class="form-label small fw-bold mb-1 text-muted">Lần kích hoạt gần nhất</label>
              <div class="small text-dark font-monospace">
                <i data-lucide="clock" style="width: 14px; height: 14px" class="me-1 text-muted"></i>
                ${formatDateTime(currentConfig.lastTriggeredAt)}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Card Điều kiện cảm biến (Optional) -->
      <div class="card shadow-sm border-0 rounded-3 mb-4">
        <div class="card-header bg-white border-bottom-0 pt-3 pb-0 d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div>
            <h6 class="mb-0 fw-bold text-dark">Điều kiện cảm biến <span class="text-muted fw-normal small">(Tùy chọn)</span></h6>
            <div class="text-muted small" style="font-size: 0.8rem">Hành động chỉ thực thi khi tất cả điều kiện cảm biến thỏa mãn.</div>
          </div>
          <button type="button" class="btn btn-sm btn-outline-primary rounded-pill px-3 d-inline-flex align-items-center" id="btnAddCondition">
            <i data-lucide="plus" style="width: 14px; height: 14px" class="me-1"></i>
            <span>Thêm điều kiện</span>
          </button>
        </div>
        <div class="card-body pt-2 pb-3" id="conditionsListContainer">
          <!-- Render conditions list -->
        </div>
      </div>

      <!-- Card Hành động thiết bị -->
      <div class="card shadow-sm border-0 rounded-3 mb-4">
        <div class="card-header bg-white border-bottom-0 pt-3 pb-0 d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div>
            <h6 class="mb-0 fw-bold text-dark">Hành động điều khiển thiết bị</h6>
            <div class="text-muted small" style="font-size: 0.8rem">Các lệnh tự động gửi tới thiết bị trong phòng khi phát hiện sự kiện.</div>
          </div>
          <button type="button" class="btn btn-sm btn-primary rounded-pill px-3 d-inline-flex align-items-center shadow-sm" id="btnAddAction">
            <i data-lucide="plus" style="width: 14px; height: 14px" class="me-1"></i>
            <span>Thêm hành động</span>
          </button>
        </div>
        <div class="card-body pt-2 pb-3" id="actionsListContainer">
          <!-- Render actions list -->
        </div>
      </div>
    `;

    // Bind Config inputs
    getEl('cfgIsActive')?.addEventListener('change', (e) => {
      callbacks.onUpdateDraft({ isActive: e.target.checked });
    });
    getEl('cfgCooldown')?.addEventListener('input', (e) => {
      callbacks.onUpdateDraft({ cooldownSeconds: parseInt(e.target.value || '0', 10) });
    });

    // Bind buttons
    getEl('btnDeleteConfig')?.addEventListener('click', () => callbacks.onDeleteConfig());
    getEl('btnAddCondition')?.addEventListener('click', () => callbacks.onOpenConditionModal());
    getEl('btnAddAction')?.addEventListener('click', () => callbacks.onOpenActionModal());

    // Render Sub-resources lists
    renderConditionsList(conditions, callbacks);
    renderActionsList(actions, callbacks);

    if (window.lucide) window.lucide.createIcons();
  };

  const renderConditionsList = (conditions, callbacks) => {
    const container = getEl('conditionsListContainer');
    if (!container) return;

    if (!conditions || conditions.length === 0) {
      container.innerHTML = `
        <div class="alert alert-light border border-dashed rounded-3 p-3 text-center mb-0">
          <i data-lucide="check-circle-2" class="text-success mb-1" style="width: 22px; height: 22px"></i>
          <div class="small text-muted">Không có điều kiện nào. Hành động sẽ <strong>luôn thực thi ngay</strong> khi sự kiện xảy ra.</div>
        </div>
      `;
      if (window.lucide) window.lucide.createIcons();
      return;
    }

    let html = '<div class="d-flex flex-column gap-2">';
    conditions.forEach((c, index) => {
      const isLast = index === conditions.length - 1;
      html += `
        <div class="condition-item d-flex justify-content-between align-items-center bg-light rounded-3 p-2 px-3 border border-light-subtle">
          <div class="d-flex align-items-center flex-wrap gap-2">
            <span class="badge bg-secondary-subtle text-secondary border px-2 py-1 small">#${index + 1}</span>
            <span class="fw-semibold text-dark small">${c.targetDisplay || c.sourceCategory || 'Sensor'}</span>
            <span class="badge bg-white text-dark border px-2 py-1 small">${c.property} ${c.operator} ${c.value}</span>
            ${!isLast ? `<span class="badge bg-primary-subtle text-primary border border-primary-subtle px-2 py-1 small fw-bold">${c.nextLogic || 'AND'}</span>` : ''}
          </div>
          <div class="d-flex gap-1">
            <button type="button" class="btn btn-sm btn-link text-primary p-1 btn-edit-cond" data-local-id="${c._localId}" title="Sửa">
              <i data-lucide="edit-2" style="width: 15px; height: 15px"></i>
            </button>
            <button type="button" class="btn btn-sm btn-link text-danger p-1 btn-del-cond" data-local-id="${c._localId}" title="Xóa">
              <i data-lucide="trash" style="width: 15px; height: 15px"></i>
            </button>
          </div>
        </div>
      `;
    });
    html += '</div>';

    container.innerHTML = html;

    container.querySelectorAll('.btn-edit-cond').forEach((btn) => {
      btn.addEventListener('click', () => callbacks.onOpenConditionModal(btn.dataset.localId));
    });
    container.querySelectorAll('.btn-del-cond').forEach((btn) => {
      btn.addEventListener('click', () => callbacks.onDeleteCondition(btn.dataset.localId));
    });

    if (window.lucide) window.lucide.createIcons();
  };

  const renderActionsList = (actions, callbacks) => {
    const container = getEl('actionsListContainer');
    if (!container) return;

    if (!actions || actions.length === 0) {
      container.innerHTML = `
        <div class="alert alert-warning-subtle border border-warning-subtle rounded-3 p-3 text-center mb-0">
          <i data-lucide="alert-triangle" class="text-warning mb-1" style="width: 22px; height: 22px"></i>
          <div class="small text-dark fw-medium">Chưa cài đặt hành động điều khiển thiết bị nào.</div>
          <div class="small text-muted">Bấm nút "Thêm hành động" ở góc trên để cấu hình bật/tắt đèn, quạt, máy lạnh khi có sự kiện.</div>
        </div>
      `;
      if (window.lucide) window.lucide.createIcons();
      return;
    }

    let html = '<div class="d-flex flex-column gap-2">';
    actions.forEach((a, index) => {
      const cat = a.targetCategory || a.targetDeviceCategory || 'DEVICE';
      let icon = 'power';
      let badgeClass = 'bg-secondary';
      if (cat === 'LIGHT') {
        icon = 'lightbulb';
        badgeClass = 'bg-warning text-dark';
      } else if (cat === 'FAN') {
        icon = 'wind';
        badgeClass = 'bg-info text-dark';
      } else if (cat === 'AIR_CONDITION') {
        icon = 'thermometer-snowflake';
        badgeClass = 'bg-primary text-white';
      }

      const p = a.params || {};
      let paramStr = `Power: ${p.power || 'ON'}`;
      if (cat === 'LIGHT' && p.level !== undefined) paramStr += `, Level: ${p.level}%`;
      if (cat === 'FAN') {
        if (p.speed) paramStr += `, Speed: ${p.speed}`;
        if (p.mode) paramStr += `, Mode: ${p.mode}`;
      }
      if (cat === 'AIR_CONDITION') {
        if (p.temperature) paramStr += `, Temp: ${p.temperature}°C`;
        if (p.mode) paramStr += `, Mode: ${p.mode}`;
      }

      html += `
        <div class="action-item d-flex justify-content-between align-items-center bg-light rounded-3 p-2 px-3 border border-light-subtle">
          <div class="d-flex align-items-center flex-wrap gap-2">
            <span class="badge bg-secondary-subtle text-secondary border px-2 py-1 small">#${index + 1}</span>
            <span class="badge ${badgeClass} px-2 py-1 small d-inline-flex align-items-center">
              <i data-lucide="${icon}" style="width: 12px; height: 12px" class="me-1"></i>${cat}
            </span>
            <span class="fw-semibold text-dark small">${a.targetDisplay || `Device #${a.targetId}`}</span>
            <span class="badge bg-white text-muted border px-2 py-1 small font-monospace">${paramStr}</span>
          </div>
          <div class="d-flex gap-1">
            <button type="button" class="btn btn-sm btn-link text-primary p-1 btn-edit-act" data-local-id="${a._localId}" title="Sửa">
              <i data-lucide="edit-2" style="width: 15px; height: 15px"></i>
            </button>
            <button type="button" class="btn btn-sm btn-link text-danger p-1 btn-del-act" data-local-id="${a._localId}" title="Xóa">
              <i data-lucide="trash" style="width: 15px; height: 15px"></i>
            </button>
          </div>
        </div>
      `;
    });
    html += '</div>';

    container.innerHTML = html;

    container.querySelectorAll('.btn-edit-act').forEach((btn) => {
      btn.addEventListener('click', () => callbacks.onOpenActionModal(btn.dataset.localId));
    });
    container.querySelectorAll('.btn-del-act').forEach((btn) => {
      btn.addEventListener('click', () => callbacks.onDeleteAction(btn.dataset.localId));
    });

    if (window.lucide) window.lucide.createIcons();
  };

  const updateSaveBar = (isDirty) => {
    const bar = getEl('saveBarContainer');
    if (!bar) return;
    if (isDirty) {
      bar.classList.remove('d-none');
    } else {
      bar.classList.add('d-none');
    }
  };

  return {
    renderEventTabs,
    renderMainContent,
    updateSaveBar,
  };
})();
