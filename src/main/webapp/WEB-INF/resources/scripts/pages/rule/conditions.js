class ConditionManager {
  // =========================================================================
  // 1. INITIALIZATION & STATE MANAGEMENT
  // =========================================================================
  static init(ruleData) {
    if (typeof window === 'undefined')
      throw new Error('ConditionManager can only be initialized in a browser environment');

    // Khởi tạo các Services
    this.ruleService = window.ruleApiV1Service;
    this.floorService = window.floorApiV1Service;
    this.roomService = window.roomApiV1Service;
    this.deviceMetadataService = window.deviceMetadataApiV1Service;

    // Khởi tạo dữ liệu trạng thái
    this.ruleData = ruleData;
    this.ruleId = ruleData.id;

    let counter = 1;
    this.conditions = (ruleData.conditions || []).map((c) => ({
      ...c,
      resourceParam:
        typeof c.resourceParam === 'string' ? JSON.parse(c.resourceParam) : c.resourceParam || {},
      _localId: counter++,
    }));

    this._nextLocalId = counter;
    this.isDirty = false;

    // Chạy các luồng giao diện
    this.renderTable();
    this.bindEvents();
  }

  static markDirty() {
    this.isDirty = true;
    $('#dirtyBadge').addClass('visible');
  }

  static markClean() {
    this.isDirty = false;
    $('#dirtyBadge').removeClass('visible');
  }

  static getPropertyMeta(category, property) {
    return (RuleCommon.PROPERTY_META[category] || {})[property] || { type: 'string' };
  }

  // =========================================================================
  // 3. UI RENDERING & FORMATTING (TABLE)
  // =========================================================================
  static renderTable() {
    this.conditions.sort((a, b) => a.sortOrder - b.sortOrder);

    const $tbody = $('#conditionsTableBody');
    $tbody.empty();

    if (this.conditions.length === 0) {
      $('#conditionsEmptyState').show();
      return;
    }
    $('#conditionsEmptyState').hide();

    this.conditions.forEach((c) => {
      const resourceDisplay = this.formatResourceParam(c.dataSource, c.resourceParam);
      const operatorDisplay = this.escapeHtml(c.operator || '');
      const nextLogicColor = c.nextLogic === 'OR' ? 'danger' : 'primary';

      const rowHtml = $('#conditionRowTemplate').html()
        .replace(/{localId}/g, c._localId)
        .replace(/{sortOrder}/g, c.sortOrder)
        .replace(/{dataSource}/g, RuleCommon.escapeHtml(c.dataSource || ''))
        .replace(/{resourceDisplay}/g, resourceDisplay)
        .replace(/{operatorDisplay}/g, operatorDisplay)
        .replace(/{value}/g, RuleCommon.escapeHtml(c.value || ''))
        .replace(/{nextLogicColor}/g, nextLogicColor)
        .replace(/{nextLogic}/g, RuleCommon.escapeHtml(c.nextLogic || 'AND'));
      
      $tbody.append($(rowHtml));
    });

    $tbody.find('.order-input').on('change', (e) => {
      const localId = parseInt($(e.currentTarget).data('local-id'));
      const newValue = parseInt(e.currentTarget.value);
      this.handleSortOrderInput(localId, newValue);
    });
  }

  static formatResourceParam(dataSource, rp) {
    if (!rp) return '<span class="text-muted">—</span>';

    if (dataSource === 'SYSTEM') {
      return `<code class="text-dark">${this.escapeHtml(rp.property || '')}</code>`;
    }

    if (dataSource === 'ROOM') {
      const prop = rp.property || '';
      const roomId = rp.roomId || '';
      return `<span class="badge badge-secondary mr-1">ROOM</span>
        <span class="text-muted">#${roomId}</span>
        <i class="fas fa-angle-right mx-1 text-muted small"></i>
        <code class="text-dark">${this.escapeHtml(prop)}</code>`;
    }

    if (dataSource === 'DEVICE' || dataSource === 'SENSOR') {
      const cat = rp.category || '';
      const deviceId = rp.deviceId || rp.sensorId || '';
      const prop = rp.property || '';
      const catColors = {
        LIGHT: 'warning',
        FAN: 'info',
        AIR_CONDITION: 'primary',
        TEMPERATURE: 'danger',
        POWER_CONSUMPTION: 'success',
      };
      const c = catColors[cat] || 'secondary';
      return `<span class="badge badge-${c} mr-1">${this.escapeHtml(cat)}</span>
        <span class="text-muted">#${deviceId}</span>
        <i class="fas fa-angle-right mx-1 text-muted small"></i>
        <code class="text-dark">${this.escapeHtml(prop)}</code>`;
    }

    return `<code>${JSON.stringify(rp)}</code>`;
  }

  static handleSortOrderInput(localId, newOrder) {
    if (isNaN(newOrder) || newOrder < 0) {
      this.renderTable();
      return;
    }

    const moved = this.conditions.find((c) => c._localId === localId);
    if (!moved || moved.sortOrder === newOrder) return;

    moved.sortOrder = newOrder;

    this.conditions.sort((a, b) => a.sortOrder - b.sortOrder);
    this.renderTable();
    this.markDirty();
  }

  // =========================================================================
  // 4. DOM EVENTS BINDING
  // =========================================================================
  static bindEvents() {
    // Actions cơ bản
    $('#btnAddCondition, #btnAddConditionEmpty').on('click', () => this.openConditionModal());
    $('#btnSaveCondition').on('click', () => this.handleModalSave());
    $('#btnSaveConditions').on('click', () => this.handleSaveAll());

    // Thao tác trên bảng
    $('#conditionsTableBody').on('click', '.btn-edit-cond', (e) => {
      const localId = parseInt($(e.currentTarget).data('local-id'));
      this.openConditionModal(localId);
    });

    $('#conditionsTableBody').on('click', '.btn-delete-cond', (e) => {
      const localId = parseInt($(e.currentTarget).data('local-id'));
      this.handleDeleteCondition(localId);
    });

    // Thay đổi Data Source & Category
    $('#condDataSourceSelect').on('change', (e) => this.handleDataSourceChange(e.target.value));

    $('#condCategorySelect').on('change', (e) => {
      this.updateCondPropertyOptions(e.target.value, $('#condDataSourceSelect').val());
      this.resetCondDeviceSelection();
      this.updateOperatorAndValueByProperty('', '');

      if ($('#condFloorSelect option').length <= 1) {
        RuleCommon.loadFloors('#condFloorSelect');
      } else {
        $('#condFloorSelect').prop('disabled', false);
      }
    });

    // Device / Sensor Selects
    $('#condFloorSelect').on('change', (e) =>
      RuleCommon.loadRooms(e.target.value, '#condRoomSelect', '#condDeviceSelect'),
    );
    $('#condRoomSelect').on('change', (e) => RuleCommon.loadDevices(e.target.value, $('#condCategorySelect').val(), '#condDeviceSelect'));

    // Room Data Source Selects
    $('#condRoomFloorSelect').on('change', (e) =>
      RuleCommon.loadRooms(e.target.value, '#condRoomRoomSelect'),
    );

    // Property Selects
    $('#condPropertySelect').on('change', (e) => {
      const category = $('#condCategorySelect').val();
      this.updateOperatorAndValueByProperty(category, e.target.value);
    });

    $('#condSystemProperty').on('change', (e) => {
      this.updateOperatorAndValueByProperty('SYSTEM', e.target.value);
    });

    $('#condRoomProperty').on('change', (e) => {
      this.updateOperatorAndValueByProperty('ROOM', e.target.value);

      // Fix: Mở khóa chọn Tầng cho ROOM sau khi chọn Property
      if ($('#condRoomFloorSelect option').length <= 1) {
        RuleCommon.loadFloors('#condRoomFloorSelect');
      } else {
        $('#condRoomFloorSelect').prop('disabled', false);
      }
    });
  }

  // =========================================================================
  // 5. MODAL HANDLING & FORM LOGIC
  // =========================================================================
  static async openConditionModal(localId = null) {
    const isEdit = localId !== null;
    $('#conditionForm')[0].reset();
    $('#conditionLocalId').val('');

    $('#condSectionSystem, #condSectionRoom, #condSectionDeviceSensor').hide();

    if (!isEdit) {
      // Chế độ thêm mới
      $('#conditionModalTitleText').text('Add Condition');
      $('#condDataSourceSelect').val('');
      $('#condNextLogicSelect').val('AND');
      this.updateOperatorAndValueByProperty('', '');
    } else {
      // Chế độ chỉnh sửa
      const c = this.conditions.find((x) => x._localId === localId);
      if (!c) return;

      $('#conditionModalTitleText').text('Edit Condition');
      $('#conditionLocalId').val(localId);
      $('#condDataSourceSelect').val(c.dataSource);
      $('#condOperatorSelect').val(c.operator);
      $('#condNextLogicSelect').val(c.nextLogic || 'AND');

      this.handleDataSourceChange(c.dataSource);

      const rp = c.resourceParam || {};

      if (c.dataSource === 'SYSTEM') {
        $('#condSystemProperty').val(rp.property || '');
        this.updateOperatorAndValueByProperty('SYSTEM', rp.property || '');
        $('#condValue').val(c.value);
      } else if (c.dataSource === 'ROOM') {
        $('#condRoomProperty').val(rp.property || '');
        this.updateOperatorAndValueByProperty('ROOM', rp.property || '');
        $('#condValue').val(c.value);

        // Fix: Tải danh sách Tầng và mở khóa khi Edit
        if ($('#condRoomFloorSelect option').length <= 1) {
          await RuleCommon.loadFloors('#condRoomFloorSelect');
        }
        $('#condRoomFloorSelect').prop('disabled', false);

        const roomId = rp.roomId;
        if (roomId) {
          const existingHint = $('#condRoomIdHint');
          if (existingHint.length) existingHint.text(`Current Room: #${roomId}`);
          else {
            $('#condRoomRoomSelect').after(
              `<small id="condRoomIdHint" class="form-text text-info"><i class="fas fa-info-circle mr-1"></i>Current Room: #${roomId} — re-select to change</small>`,
            );
          }
        }
      } else if (c.dataSource === 'DEVICE' || c.dataSource === 'SENSOR') {
        const category = rp.category || '';
        $('#condCategorySelect').val(category);
        this.updateCondPropertyOptions(category, c.dataSource);
        $('#condPropertySelect').val(rp.property || '');
        this.updateOperatorAndValueByProperty(category, rp.property || '');

        const propMeta = this.getPropertyMeta(category, rp.property || '');
        if (propMeta.type === 'enum') {
          $('#condValueEnum').val(c.value);
        } else {
          $('#condValue').val(c.value);
        }

        if ($('#condFloorSelect option').length <= 1) {
          await RuleCommon.loadFloors('#condFloorSelect');
        }
        $('#condFloorSelect').prop('disabled', false);

        const idKey = c.dataSource === 'SENSOR' ? 'sensorId' : 'deviceId';
        const deviceId = rp[idKey];
        if (deviceId) {
          const existingHint = $('#condDeviceIdHint');
          if (existingHint.length) existingHint.text(`Current: #${deviceId}`);
          else {
            $('#condDeviceSelect').after(
              `<small id="condDeviceIdHint" class="form-text text-info"><i class="fas fa-info-circle mr-1"></i>Current: #${deviceId} — re-select to change</small>`,
            );
          }
        }
      }
    }

    $('#conditionModal').modal('show');
  }

  static handleDataSourceChange(dataSource) {
    $('#condSectionSystem, #condSectionRoom, #condSectionDeviceSensor').hide();
    $('#condPropertySelect')
      .prop('disabled', true)
      .html('<option value="" disabled selected>Select property</option>');
    $('#condFloorSelect')
      .prop('disabled', true)
      .html('<option value="" disabled selected>Select floor</option>');
    $('#condRoomSelect')
      .prop('disabled', true)
      .html('<option value="" disabled selected>Select room</option>');
    $('#condDeviceSelect')
      .prop('disabled', true)
      .html('<option value="" disabled selected>Select device/sensor</option>');
    $('#condRoomFloorSelect')
      .prop('disabled', true)
      .html('<option value="" disabled selected>Select floor</option>');
    $('#condRoomRoomSelect')
      .prop('disabled', true)
      .html('<option value="" disabled selected>Select room</option>');

    $('#condDeviceIdHint, #condRoomIdHint').remove();

    if (dataSource === 'SYSTEM') {
      $('#condSectionSystem').show();
    } else if (dataSource === 'ROOM') {
      $('#condSectionRoom').show();
      RuleCommon.loadFloors('#condRoomFloorSelect');
    } else if (dataSource === 'DEVICE' || dataSource === 'SENSOR') {
      $('#condSectionDeviceSensor').show();

      $('#condCategorySelect option[value]').each(function () {
        const forAttr = $(this).data('for') || '';
        $(this).toggle(forAttr.includes(dataSource));
      });
      $('#condCategorySelect').val('');
      RuleCommon.loadFloors('#condFloorSelect');
    }
  }

  static updateCondPropertyOptions(category, dataSource) {
    const $select = $('#condPropertySelect');
    $select.empty().append('<option value="" disabled selected>Select property</option>');

    const propMap = {
      LIGHT: ['power', 'level'],
      FAN: ['power', 'mode', 'speed', 'swing', 'light'],
      AIR_CONDITION: ['power', 'temp', 'mode', 'fan_speed', 'swing'],
      TEMPERATURE: ['temperature'],
      POWER_CONSUMPTION: ['watt'],
    };

    const props = propMap[category] || [];
    if (props.length > 0) {
      props.forEach((p) => $select.append(`<option value="${p}">${p}</option>`));
      $select.prop('disabled', false);
    } else {
      $select.prop('disabled', true);
    }
  }

  static resetCondDeviceSelection() {
    $('#condRoomSelect')
      .prop('disabled', true)
      .html('<option value="" disabled selected>Select room</option>');
    $('#condDeviceSelect')
      .prop('disabled', true)
      .html('<option value="" disabled selected>Select device/sensor</option>');
  }

  static updateOperatorAndValueByProperty(category, property) {
    const meta = this.getPropertyMeta(category, property);
    const $operatorSelect = $('#condOperatorSelect');
    const $valueInput = $('#condValue');
    const $valueEnum = $('#condValueEnum');
    const ENUM_OPERATORS = ['=', '!='];

    if (meta.type === 'enum') {
      $operatorSelect.find('option[value]').each(function () {
        if (!ENUM_OPERATORS.includes($(this).val())) {
          $(this).prop('disabled', true).hide();
        } else {
          $(this).prop('disabled', false).show();
        }
      });
      if (!ENUM_OPERATORS.includes($operatorSelect.val())) {
        $operatorSelect.val('=');
      }

      $valueInput.hide().val('');
      $valueEnum.show().empty().append('<option value="" disabled selected>Select value</option>');
      (meta.values || []).forEach((v) => $valueEnum.append(`<option value="${v}">${v}</option>`));
    } else {
      $operatorSelect.find('option[value]').prop('disabled', false).show();
      $valueEnum.hide().val('');
      $valueInput.show();
    }
  }

  static getCondValue() {
    const $valueEnum = $('#condValueEnum');
    if ($valueEnum.is(':visible')) return $valueEnum.val() || '';
    return $('#condValue').val()?.trim() || '';
  }

  // =========================================================================

  static handleModalSave() {
    const localId = $('#conditionLocalId').val() ? parseInt($('#conditionLocalId').val()) : null;
    const dataSource = $('#condDataSourceSelect').val();
    const operator = $('#condOperatorSelect').val();
    const value = this.getCondValue();
    const nextLogic = $('#condNextLogicSelect').val();

    if (!dataSource) {
      notify.warning('Please select a data source');
      return;
    }
    if (!operator) {
      notify.warning('Please select an operator');
      return;
    }
    if (!value) {
      notify.warning('Please enter a comparison value');
      return;
    }

    const resourceParam = this.buildResourceParam(dataSource);
    if (!resourceParam) return;

    const isEdit = localId !== null;
    if (isEdit) {
      const c = this.conditions.find((x) => x._localId === localId);
      if (!c) return;
      c.dataSource = dataSource;
      c.resourceParam = resourceParam;
      c.operator = operator;
      c.value = value;
      c.nextLogic = nextLogic;
    } else {
      const maxOrder = this.conditions.reduce((m, c) => Math.max(m, c.sortOrder || 0), 0);
      const newCondition = {
        dataSource,
        resourceParam,
        operator,
        value,
        nextLogic,
        sortOrder: maxOrder + 1,
        _localId: this._nextLocalId++,
      };
      this.conditions.push(newCondition);
    }

    $('#conditionModal').modal('hide');
    this.renderTable();
    this.markDirty();
  }

  static buildResourceParam(dataSource) {
    if (dataSource === 'SYSTEM') {
      const property = $('#condSystemProperty').val();
      if (!property) {
        notify.warning('Please select a system property');
        return null;
      }
      return { property };
    }

    if (dataSource === 'ROOM') {
      const property = $('#condRoomProperty').val();
      const roomSelect = $('#condRoomRoomSelect').val();

      if (!property) {
        notify.warning('Please select a room property');
        return null;
      }

      const localId = $('#conditionLocalId').val() ? parseInt($('#conditionLocalId').val()) : null;
      let roomId = roomSelect ? parseInt(roomSelect) : null;

      if (!roomId && localId !== null) {
        const existing = this.conditions.find((c) => c._localId === localId);
        if (existing && existing.dataSource === 'ROOM') {
          roomId = (existing.resourceParam || {}).roomId;
        }
      }

      if (!roomId) {
        notify.warning('Please select a room');
        return null;
      }

      return { property, roomId };
    }

    if (dataSource === 'DEVICE' || dataSource === 'SENSOR') {
      const category = $('#condCategorySelect').val();
      const property = $('#condPropertySelect').val();
      const deviceSelect = $('#condDeviceSelect').val();

      if (!category) {
        notify.warning('Please select a category');
        return null;
      }
      if (!property) {
        notify.warning('Please select a property');
        return null;
      }

      const localId = $('#conditionLocalId').val() ? parseInt($('#conditionLocalId').val()) : null;
      let deviceId = deviceSelect ? parseInt(deviceSelect) : null;

      if (!deviceId && localId !== null) {
        const existing = this.conditions.find((c) => c._localId === localId);
        if (existing) {
          const rp = existing.resourceParam || {};
          deviceId = dataSource === 'SENSOR' ? rp.sensorId : rp.deviceId;
        }
      }

      if (!deviceId) {
        notify.warning('Please select a device/sensor');
        return null;
      }

      const rp = { category, property };
      if (dataSource === 'SENSOR') {
        rp.sensorId = deviceId;
      } else {
        rp.deviceId = deviceId;
      }
      return rp;
    }

    return {};
  }

  static async handleDeleteCondition(localId) {
    if (!(await notify.confirm('Delete Condition', 'Remove this condition?'))) return;
    this.conditions = this.conditions.filter((c) => c._localId !== localId);
    this.renderTable();
    this.markDirty();
  }

  static async handleSaveAll() {
    if (
      !(await notify.confirm(
        'Save All Changes',
        'This will overwrite all conditions on the server. Continue?',
        'warning',
      ))
    )
      return;

    const $btn = $('#btnSaveConditions');
    try {
      $btn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-1"></i> Saving...');

      const conditionsPayload = this.conditions.map((c, index) => {
        const resourceParam =
          typeof c.resourceParam === 'string' ? JSON.parse(c.resourceParam) : c.resourceParam || {};

        const item = {
          sortOrder: c.sortOrder,
          dataSource: c.dataSource,
          resourceParam,
          operator: c.operator,
          value: String(c.value),
          nextLogic: c.nextLogic || 'AND',
        };
        if (c.id) item.id = c.id;
        return item;
      });

      await this.ruleService.update(this.ruleId, { conditions: conditionsPayload });
      notify.success('Conditions saved successfully');

      const refreshed = await this.ruleService.getById(this.ruleId);
      if (refreshed?.data) {
        let counter = 1;
        this.conditions = (refreshed.data.conditions || []).map((c) => ({
          ...c,
          resourceParam:
            typeof c.resourceParam === 'string'
              ? JSON.parse(c.resourceParam)
              : c.resourceParam || {},
          _localId: counter++,
        }));
        this._nextLocalId = counter;
        this.ruleData = refreshed.data;
        this.renderTable();
        this.markClean();
      } else {
        this.markClean();
      }
    } catch (error) {
      const msg = error.responseJSON?.message || error.message || 'Unknown error';
      notify.error('Failed to save: ' + msg);
      console.error('API Error (handleSaveAll):', error);
    } finally {
      $btn.prop('disabled', false).html('<i class="fas fa-save mr-1"></i> Save All Changes');
    }
  }
}

window.ConditionManager = ConditionManager;
