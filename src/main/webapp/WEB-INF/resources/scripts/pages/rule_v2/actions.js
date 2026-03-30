class ActionV2Manager {
  static async init(ruleId) {
    if (typeof window === 'undefined')
      throw new Error('ActionV2Manager can only be initialized in a browser environment');

    this.ruleService = window.ruleApiV2Service;
    this.floorService = window.floorApiV1Service;
    this.roomService = window.roomApiV1Service;
    this.deviceMetadataService = window.deviceMetadataApiV1Service;

    this.ruleId = ruleId;
    this.ruleData = null;
    this.actions = [];
    this._nextLocalId = 1;
    this.isDirty = false;

    try {
      const res = await this.ruleService.getById(ruleId);
      if (res?.data) {
        this.ruleData = res.data;
        this.populateHeader(this.ruleData);

        let counter = 1;
        this.actions = (this.ruleData.actions || []).map((a) => ({
          ...a,
          actionParams:
            typeof a.actionParams === 'string' ? JSON.parse(a.actionParams) : a.actionParams || {},
          _localId: counter++,
        }));
        this._nextLocalId = counter;

        $('#btnAddAction, #btnSaveActions').prop('disabled', false);
        this.renderTable();
        this.bindEvents();
      }
    } catch (e) {
      notify.error('Failed to load Rule V2 data');
      console.error(e);
    }
  }

  static populateHeader(data) {
    $('#headerRuleName').text(data.name);

    const $status = $('#headerRuleStatus');
    if (data.isActive) {
      $status.addClass('badge-success').removeClass('badge-secondary').text('Active');
    } else {
      $status.addClass('badge-secondary').removeClass('badge-success').text('Inactive');
    }

    $('#headerRulePriority').text(data.priority);
    $('#headerRuleRoom').text(data.roomId);
    $('#headerRuleInterval').text(data.intervalSeconds);
  }

  static markDirty() {
    this.isDirty = true;
    $('#dirtyBadge').addClass('visible');
  }

  static markClean() {
    this.isDirty = false;
    $('#dirtyBadge').removeClass('visible');
  }

  static renderTable() {
    this.actions.sort((a, b) => a.executionOrder - b.executionOrder);

    const $tbody = $('#actionsTableBody');
    $tbody.empty();

    if (this.actions.length === 0) {
      $('#actionsEmptyState').show();
      return;
    }
    $('#actionsEmptyState').hide();

    this.actions.forEach((a) => {
      const colors = { LIGHT: 'warning', FAN: 'info', AIR_CONDITION: 'primary' };
      const c = colors[a.targetDeviceCategory] || 'secondary';
      const categoryDisplay = `<span class="badge badge-${c}">${a.targetDeviceCategory}</span>`;

      const paramsDisplay = `<code>${RuleCommon.escapeHtml(JSON.stringify(a.actionParams))}</code>`;

      const rowHtml = $('#actionRowTemplate')
        .html()
        .replace(/{localId}/g, a._localId)
        .replace(/{executionOrder}/g, a.executionOrder)
        .replace(/{targetDeviceId}/g, a.targetDeviceId)
        .replace(/{categoryDisplay}/g, categoryDisplay)
        .replace(/{paramsDisplay}/g, paramsDisplay);

      $tbody.append($(rowHtml));
    });

    $tbody.find('.order-input').on('change', (e) => {
      const localId = parseInt($(e.currentTarget).data('local-id'));
      const newValue = parseInt(e.currentTarget.value);
      this.handleExecutionOrderInput(localId, newValue);
    });
  }

  static handleExecutionOrderInput(localId, newOrder) {
    if (isNaN(newOrder) || newOrder < 0) {
      this.renderTable();
      return;
    }
    const moved = this.actions.find((a) => a._localId === localId);
    if (!moved || moved.executionOrder === newOrder) return;

    moved.executionOrder = newOrder;
    this.actions.sort((a, b) => a.executionOrder - b.executionOrder);
    this.renderTable();
    this.markDirty();
  }

  static bindEvents() {
    $('#btnAddAction, #btnAddActionEmpty').on('click', () => this.openActionModal());
    $('#btnSaveAction').on('click', () => this.handleModalSave());
    $('#btnSaveActions').on('click', () => this.handleSaveAll());

    $('#actionsTableBody').on('click', '.btn-edit-act', (e) => {
      const localId = parseInt($(e.currentTarget).data('local-id'));
      this.openActionModal(localId);
    });

    $('#actionsTableBody').on('click', '.btn-delete-act', (e) => {
      const localId = parseInt($(e.currentTarget).data('local-id'));
      this.handleDeleteAction(localId);
    });

    $('#actCategorySelect').on('change', (e) => {
      this.handleCategoryChange(e.target.value);
      $('#actFloorSelect').val('');
      this.resetDeviceSelection();
      if ($('#actFloorSelect option').length <= 1) {
        RuleCommon.loadFloors('#actFloorSelect');
      } else {
        $('#actFloorSelect').prop('disabled', false);
      }
    });

    $('#actFloorSelect').on('change', (e) =>
      RuleCommon.loadRooms(e.target.value, '#actRoomSelect', '#actDeviceSelect'),
    );
    $('#actRoomSelect').on('change', (e) => {
      RuleCommon.loadDevices(
        e.target.value,
        'DEVICE',
        $('#actCategorySelect').val(),
        '#actDeviceSelect',
      );
    });
  }

  static handleCategoryChange(category) {
    $('#actParamLight, #actParamFan, #actParamAc').hide();
    $('#actParamPlaceholder').hide();

    if (category === 'LIGHT') {
      $('#actParamLight').show();
    } else if (category === 'FAN') {
      $('#actParamFan').show();
    } else if (category === 'AIR_CONDITION') {
      $('#actParamAc').show();
    } else {
      $('#actParamPlaceholder').show();
    }
  }

  static resetDeviceSelection() {
    $('#actRoomSelect')
      .prop('disabled', true)
      .html('<option value="" disabled selected>Select room</option>');
    $('#actDeviceSelect')
      .prop('disabled', true)
      .html('<option value="" disabled selected>Select device</option>');
  }

  static async openActionModal(localId = null) {
    const isEdit = localId !== null;
    $('#actionForm')[0].reset();
    $('#actionLocalId').val('');

    $('#actParamLight, #actParamFan, #actParamAc').hide();
    $('#actParamPlaceholder').show();

    if (!isEdit) {
      $('#actionModalTitleText').text('Add Action');
      const maxOrder = this.actions.reduce((m, a) => Math.max(m, a.executionOrder || 0), -1);
      $('#actExecutionOrder').val(maxOrder + 1);

      $('#actFloorSelect')
        .prop('disabled', true)
        .html('<option value="" disabled selected>Select floor</option>');
      $('#actRoomSelect')
        .prop('disabled', true)
        .html('<option value="" disabled selected>Select room</option>');
      $('#actDeviceSelect')
        .prop('disabled', true)
        .html('<option value="" disabled selected>Select device</option>');
      $('#actCategorySelect').val('');

      if ($('#actFloorSelect option').length <= 1) {
        await RuleCommon.loadFloors('#actFloorSelect');
      }
    } else {
      const a = this.actions.find((x) => x._localId === localId);
      if (!a) return;

      $('#actionModalTitleText').text('Edit Action');
      $('#actionLocalId').val(localId);
      $('#actExecutionOrder').val(a.executionOrder);
      $('#actCategorySelect').val(a.targetDeviceCategory);

      this.handleCategoryChange(a.targetDeviceCategory);
      this.populateActionParams(a.targetDeviceCategory, a.actionParams || {});

      // For edit, we will rely on deviceSelect having custom option inserted since fetching full path isn't easy without multiple calls,
      // but to be clean, let's just insert the current device as the selected option.
      $('#actFloorSelect').prop('disabled', false);
      $('#actRoomSelect')
        .prop('disabled', false)
        .html('<option value="">-- Manual change required --</option>');
      $('#actDeviceSelect')
        .prop('disabled', false)
        .html(`<option value="${a.targetDeviceId}" selected>Device #${a.targetDeviceId}</option>`);

      if ($('#actFloorSelect option').length <= 1) {
        await RuleCommon.loadFloors('#actFloorSelect');
      }
    }

    $('#actionModal').modal('show');
  }

  static populateActionParams(category, actionParams) {
    if (category === 'LIGHT') {
      if (actionParams.power) $('#actLightPower').val(actionParams.power);
      if (actionParams.level !== undefined) $('#actLightLevel').val(actionParams.level);
    } else if (category === 'FAN') {
      if (actionParams.power) $('#actFanPower').val(actionParams.power);
      if (actionParams.mode) $('#actFanMode').val(actionParams.mode);
      if (actionParams.speed !== undefined) $('#actFanSpeed').val(actionParams.speed);
      if (actionParams.swing) $('#actFanSwing').val(actionParams.swing);
      if (actionParams.light) $('#actFanLight').val(actionParams.light);
    } else if (category === 'AIR_CONDITION') {
      if (actionParams.power) $('#actAcPower').val(actionParams.power);
      if (actionParams.temperature !== undefined) $('#actAcTemp').val(actionParams.temperature);
      if (actionParams.mode) $('#actAcMode').val(actionParams.mode);
      if (actionParams.fanSpeed !== undefined) $('#actAcFanSpeed').val(actionParams.fanSpeed);
      if (actionParams.swing) $('#actAcSwing').val(actionParams.swing);
    }
  }

  static buildActionParams(category) {
    const params = {};
    if (category === 'LIGHT') {
      const power = $('#actLightPower').val();
      const level = $('#actLightLevel').val();
      if (power) params.power = power;
      if (level !== '' && level !== null) params.level = parseInt(level);
    } else if (category === 'FAN') {
      const power = $('#actFanPower').val();
      const mode = $('#actFanMode').val();
      const speed = $('#actFanSpeed').val();
      const swing = $('#actFanSwing').val();
      const light = $('#actFanLight').val();
      if (power) params.power = power;
      if (mode) params.mode = mode;
      if (speed !== '' && speed !== null) params.speed = parseInt(speed);
      if (swing) params.swing = swing;
      if (light) params.light = light;
    } else if (category === 'AIR_CONDITION') {
      const power = $('#actAcPower').val();
      const temp = $('#actAcTemp').val();
      const mode = $('#actAcMode').val();
      const fanSpeed = $('#actAcFanSpeed').val();
      const swing = $('#actAcSwing').val();
      if (power) params.power = power;
      if (temp !== '' && temp !== null) params.temperature = parseInt(temp);
      if (mode) params.mode = mode;
      if (fanSpeed !== '' && fanSpeed !== null) params.fanSpeed = parseInt(fanSpeed);
      if (swing) params.swing = swing;
    }
    return params;
  }

  static handleModalSave() {
    const localId = $('#actionLocalId').val() ? parseInt($('#actionLocalId').val()) : null;
    const isEdit = localId !== null;

    const category = $('#actCategorySelect').val();
    const deviceId = parseInt($('#actDeviceSelect').val());
    const executionOrder = parseInt($('#actExecutionOrder').val());

    if (!category) {
      notify.warning('Please select a target device category');
      return;
    }
    if (isNaN(deviceId)) {
      notify.warning('Please select a target device');
      return;
    }
    if (isNaN(executionOrder) || executionOrder < 0) {
      notify.warning('Execution order must be a valid number >= 0');
      return;
    }

    const actionParams = this.buildActionParams(category);

    if (isEdit) {
      const a = this.actions.find((x) => x._localId === localId);
      if (!a) return;
      a.targetDeviceCategory = category;
      a.targetDeviceId = deviceId;
      a.executionOrder = executionOrder;
      a.actionParams = actionParams;
    } else {
      this.actions.push({
        targetDeviceCategory: category,
        targetDeviceId: deviceId,
        executionOrder: executionOrder,
        actionParams: actionParams,
        _localId: this._nextLocalId++,
      });
    }

    $('#actionModal').modal('hide');
    this.renderTable();
    this.markDirty();
  }

  static async handleDeleteAction(localId) {
    if (!(await notify.confirm('Delete Action', 'Remove this action?'))) return;
    this.actions = this.actions.filter((a) => a._localId !== localId);
    this.renderTable();
    this.markDirty();
  }

  static async handleSaveAll() {
    if (
      !(await notify.confirm(
        'Save Actions',
        'Overwrite actions for this rule on server?',
        'warning',
      ))
    )
      return;

    const $btn = $('#btnSaveActions');
    try {
      $btn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-1"></i> Saving...');

      const actionsPayload = this.actions.map((a) => ({
        executionOrder: a.executionOrder,
        targetDeviceId: a.targetDeviceId,
        targetDeviceCategory: a.targetDeviceCategory,
        actionParams:
          typeof a.actionParams === 'string' ? JSON.parse(a.actionParams) : a.actionParams || {},
        id: a.id,
      }));

      // NOTE: We only send { actions }, ignores conditions
      await this.ruleService.update(this.ruleId, { actions: actionsPayload });
      notify.success('Actions saved successfully');

      const refreshed = await this.ruleService.getById(this.ruleId);
      if (refreshed?.data) {
        let counter = 1;
        this.actions = (refreshed.data.actions || []).map((a) => ({
          ...a,
          actionParams:
            typeof a.actionParams === 'string' ? JSON.parse(a.actionParams) : a.actionParams || {},
          _localId: counter++,
        }));
        this._nextLocalId = counter;
        this.ruleData = refreshed.data;
        this.renderTable();
        this.markClean();
      }
    } catch (error) {
      const msg = error.responseJSON?.message || error.message || 'Unknown error';
      notify.error('Failed to save actions: ' + msg);
    } finally {
      $btn.prop('disabled', false).html('<i class="fas fa-save mr-1"></i> Save All Changes');
    }
  }
}

window.ActionV2Manager = ActionV2Manager;
