class RuleManager {
  static init() {
    if (typeof window === 'undefined')
      throw new Error('RuleManager can only be initialized in a browser environment');

    this.ruleService = window.ruleApiV1Service;
    this.floorService = window.floorApiV1Service;
    this.roomService = window.roomApiV1Service;
    this.deviceMetadataService = window.deviceMetadataApiV1Service;
    this.table = null;

    this.initDataTable();
    this.bindEvents();
  }

  static bindEvents() {
    $('#btnCreateRule').on('click', () => this.openModal());
    $('#btnSaveRule').on('click', () => this.handleSave());
    $('#btnScanRules').on('click', () => this.handleScan());
    $('#btnReloadRules').on('click', () => this.handleReload());

    $('#ruleCategorySelect').on('change', (e) => {
      this.handleCategoryChange(e.target.value);
      this.resetDeviceSelection();
    });
    $('#ruleFloorSelect').on('change', (e) => this.loadRooms(e.target.value));
    $('#ruleRoomSelect').on('change', (e) => this.loadDevices(e.target.value));

    const $tbody = $('#rulesTable tbody');
    $tbody.on('click', '.btn-edit', (e) => this.handleEdit($(e.currentTarget).data('id')));
    $tbody.on('click', '.btn-delete', (e) => this.handleDelete($(e.currentTarget).data('id')));
    $tbody.on('change', '.toggle-status', (e) => {
      const $chk = $(e.currentTarget);
      this.handleToggleStatus($chk.data('id'), $chk.is(':checked'), $chk);
    });
  }

  static initDataTable() {
    this.table = $('#rulesTable').DataTable({
      processing: true,
      serverSide: true,
      ajax: async (data, callback) => {
        try {
          const page = Math.floor(data.start / data.length);
          const size = data.length;
          const res = await this.ruleService.getList(page, size);
          callback({
            recordsTotal: res.data.totalElements,
            recordsFiltered: res.data.totalElements,
            data: res.data.content || [],
          });
        } catch (error) {
          console.error('[RuleManager] DataTable load error:', error);
          callback({ data: [] });
        }
      },
      columns: [
        { data: 'id', width: '50px', className: 'align-middle' },
        {
          data: 'name',
          className: 'align-middle',
          render: (d) => `<span class="font-weight-bold text-dark">${d}</span>`,
        },
        {
          data: 'priority',
          className: 'align-middle text-center',
          render: (p) => `<span class="badge badge-light border">${p}</span>`,
        },
        {
          data: 'roomId',
          className: 'align-middle text-center',
          render: (id) => `<span class="text-muted">#${id}</span>`,
        },
        {
          data: 'targetDeviceId',
          className: 'align-middle text-center',
          render: (id) => `<span class="text-muted">#${id}</span>`,
        },
        {
          data: 'targetDeviceCategory',
          className: 'align-middle',
          render: (cat) => {
            const colors = { LIGHT: 'warning', FAN: 'info', AIR_CONDITION: 'primary' };
            const c = colors[cat] || 'secondary';
            return `<span class="badge badge-${c}">${cat}</span>`;
          },
        },
        {
          data: 'isActive',
          className: 'text-center align-middle',
          render: (active, _, row) =>
            `<div class="custom-control custom-switch">
							<input type="checkbox" class="custom-control-input toggle-status" id="rs_${row.id}" data-id="${row.id}" ${active ? 'checked' : ''}>
							<label class="custom-control-label" for="rs_${row.id}"></label>
						</div>`,
        },
        {
          data: 'conditions',
          className: 'text-center align-middle',
          render: (conds, _, row) => {
            const count = Array.isArray(conds) ? conds.length : 0;
            return `<a href="/rule/rules/${row.id}/conditions" class="badge badge-pill badge-success" style="font-size:0.85em;">${count} conditions</a>`;
          },
        },
        {
          data: null,
          className: 'text-right align-middle',
          orderable: false,
          render: (_, __, row) =>
            `<div class="btn-group">
							<a href="/rule/rules/${row.id}/conditions" class="btn btn-sm btn-default" title="Manage Conditions">
								<i class="fas fa-filter text-success"></i>
							</a>
							<button class="btn btn-sm btn-default btn-edit" data-id="${row.id}" title="Edit Rule">
								<i class="fas fa-pen text-primary"></i>
							</button>
							<button class="btn btn-sm btn-default btn-delete" data-id="${row.id}" title="Delete Rule">
								<i class="fas fa-trash text-danger"></i>
							</button>
						</div>`,
        },
      ],
      order: [[0, 'desc']],
      language: { emptyTable: 'No rules found.' },
    });
  }

  static async openModal(data = null) {
    const isEdit = !!data;
    $('#ruleForm')[0].reset();
    $('#ruleId').val('');
    $('#hiddenRuleCategory').val('');
    $('#hiddenRuleRoomId').val('');
    $('#hiddenRuleDeviceId').val('');

    // Reset action params visibility
    $('#ruleParamLight, #ruleParamFan, #ruleParamAc').hide();
    $('#ruleParamPlaceholder').show();

    if (!isEdit) {
      $('#ruleModalTitleText').text('Create New Rule');
      $('#ruleTargetSelection').show();
      $('#ruleTargetDisplay').hide();

      // Reset cascades
      $('#ruleFloorSelect')
        .prop('disabled', true)
        .html('<option value="" disabled selected>Select floor</option>');
      $('#ruleRoomSelect')
        .prop('disabled', true)
        .html('<option value="" disabled selected>Select room</option>');
      $('#ruleDeviceSelect')
        .prop('disabled', true)
        .html('<option value="" disabled selected>Select device</option>');
      $('#ruleCategorySelect').val('');

      // Preload floors if needed
      if ($('#ruleFloorSelect option').length <= 1) {
        await this.loadFloors();
      }
    } else {
      $('#ruleModalTitleText').text('Edit Rule');
      $('#ruleId').val(data.id);
      $('#ruleName').val(data.name);
      $('#rulePriority').val(data.priority);
      $('#ruleIsActive').prop('checked', data.isActive);

      // Readonly display for target device
      $('#ruleTargetSelection').hide();
      $('#ruleTargetDisplay').show();
      $('#ruleCategoryDisplay').val(data.targetDeviceCategory);
      $('#ruleDeviceDisplay').val(`#${data.targetDeviceId}`);
      $('#hiddenRuleCategory').val(data.targetDeviceCategory);
      $('#hiddenRuleRoomId').val(data.roomId);
      $('#hiddenRuleDeviceId').val(data.targetDeviceId);

      // Show correct action params
      this.handleCategoryChange(data.targetDeviceCategory);
      this.populateActionParams(data.targetDeviceCategory, data.actionParams || {});
    }

    $('#ruleModal').modal('show');
  }

  static handleCategoryChange(category) {
    $('#ruleParamLight, #ruleParamFan, #ruleParamAc').hide();
    $('#ruleParamPlaceholder').hide();

    if (category === 'LIGHT') {
      $('#ruleParamLight').show();
    } else if (category === 'FAN') {
      $('#ruleParamFan').show();
    } else if (category === 'AIR_CONDITION') {
      $('#ruleParamAc').show();
    } else {
      $('#ruleParamPlaceholder').show();
    }

    // Enable floor select on category chosen
    if (
      category &&
      $('#ruleFloorSelect').prop('disabled') &&
      !$('#ruleTargetDisplay').is(':visible')
    ) {
      $('#ruleFloorSelect').prop('disabled', false);
    }
  }

  static resetDeviceSelection() {
    $('#ruleRoomSelect')
      .prop('disabled', true)
      .html('<option value="" disabled selected>Select room</option>');
    $('#ruleDeviceSelect')
      .prop('disabled', true)
      .html('<option value="" disabled selected>Select device</option>');
    $('#hiddenRuleRoomId').val('');
    $('#hiddenRuleDeviceId').val('');
  }

  static async loadFloors() {
    try {
      const res = await this.floorService.getAllWithoutPagination();
      const floors = res?.data || [];
      const $select = $('#ruleFloorSelect');
      $select.empty().append('<option value="" disabled selected>Select floor</option>');
      floors.forEach((f) => $select.append(`<option value="${f.id}">${f.name}</option>`));
      $select.prop('disabled', false);
    } catch (error) {
      console.error('[RuleManager] loadFloors error:', error);
      notify.error('Failed to load floors');
    }
  }

  static async loadRooms(floorId) {
    if (!floorId) return;
    try {
      const $select = $('#ruleRoomSelect');
      $select.prop('disabled', true).html('<option>Loading...</option>');
      const res = await this.roomService.getAllByFloor(floorId);
      const rooms = res?.data || [];
      $select.empty().append('<option value="" disabled selected>Select room</option>');
      rooms.forEach((r) => $select.append(`<option value="${r.id}">${r.name}</option>`));
      $select.prop('disabled', false);

      $('#ruleDeviceSelect')
        .prop('disabled', true)
        .html('<option value="" disabled selected>Select device</option>');
      $('#hiddenRuleDeviceId').val('');
    } catch (error) {
      console.error('[RuleManager] loadRooms error:', error);
      notify.error('Failed to load rooms');
      $('#ruleRoomSelect')
        .prop('disabled', false)
        .html('<option value="" disabled selected>Select room</option>');
    }
  }

  static async loadDevices(roomId) {
    if (!roomId) return;
    const category = $('#ruleCategorySelect').val();
    try {
      const $select = $('#ruleDeviceSelect');
      $select.prop('disabled', true).html('<option>Loading...</option>');
      const res = await this.deviceMetadataService.getAllByRoom(roomId);
      const devices = (res?.data || []).filter((d) => !category || d.category === category);
      $select.empty().append('<option value="" disabled selected>Select device</option>');
      if (devices.length === 0) {
        $select.append(`<option disabled>No ${category || ''} devices in this room</option>`);
      } else {
        devices.forEach((d) => {
          $select.append(
            `<option value="${d.id}" data-category="${d.category}">${d.name} (ID: ${d.id})</option>`,
          );
        });
      }
      $select.prop('disabled', false);
      $('#hiddenRuleRoomId').val(roomId);
    } catch (error) {
      console.error('[RuleManager] loadDevices error:', error);
      notify.error('Failed to load devices');
      $('#ruleDeviceSelect')
        .prop('disabled', false)
        .html('<option value="" disabled selected>Select device</option>');
    }
  }

  static buildActionParams(category) {
    const params = {};

    if (category === 'LIGHT') {
      const power = $('#lightPower').val();
      const level = $('#lightLevel').val();
      if (power) params.power = power;
      if (level !== '' && level !== null) params.level = parseInt(level);
    } else if (category === 'FAN') {
      const power = $('#fanPower').val();
      const mode = $('#fanMode').val();
      const speed = $('#fanSpeed').val();
      const swing = $('#fanSwing').val();
      const light = $('#fanLight').val();
      if (power) params.power = power;
      if (mode) params.mode = mode;
      if (speed !== '' && speed !== null) params.speed = parseInt(speed);
      if (swing) params.swing = swing;
      if (light) params.light = light;
    } else if (category === 'AIR_CONDITION') {
      const power = $('#acPower').val();
      const temp = $('#acTemp').val();
      const mode = $('#acMode').val();
      const fanSpeed = $('#acFanSpeed').val();
      const swing = $('#acSwing').val();
      if (power) params.power = power;
      if (temp !== '' && temp !== null) params.temperature = parseInt(temp);
      if (mode) params.mode = mode;
      if (fanSpeed !== '' && fanSpeed !== null) params.fanSpeed = parseInt(fanSpeed);
      if (swing) params.swing = swing;
    }

    return params;
  }

  static populateActionParams(category, actionParams) {
    if (category === 'LIGHT') {
      if (actionParams.power) $('#lightPower').val(actionParams.power);
      if (actionParams.level !== undefined) $('#lightLevel').val(actionParams.level);
    } else if (category === 'FAN') {
      if (actionParams.power) $('#fanPower').val(actionParams.power);
      if (actionParams.mode) $('#fanMode').val(actionParams.mode);
      if (actionParams.speed !== undefined) $('#fanSpeed').val(actionParams.speed);
      if (actionParams.swing) $('#fanSwing').val(actionParams.swing);
      if (actionParams.light) $('#fanLight').val(actionParams.light);
    } else if (category === 'AIR_CONDITION') {
      if (actionParams.power) $('#acPower').val(actionParams.power);
      if (actionParams.temperature !== undefined) $('#acTemp').val(actionParams.temperature);
      if (actionParams.mode) $('#acMode').val(actionParams.mode);
      if (actionParams.fanSpeed !== undefined) $('#acFanSpeed').val(actionParams.fanSpeed);
      if (actionParams.swing) $('#acSwing').val(actionParams.swing);
    }
  }

  static async handleSave() {
    const $btn = $('#btnSaveRule');
    const id = $('#ruleId').val();
    const isEdit = !!id;

    const name = $('#ruleName').val()?.trim();
    if (!name) {
      notify.warning('Rule name is required');
      return;
    }

    const priority = parseInt($('#rulePriority').val());
    if (isNaN(priority) || priority < 0) {
      notify.warning('Priority must be a non-negative number');
      return;
    }

    let category, roomId, deviceId;
    if (isEdit) {
      category = $('#hiddenRuleCategory').val();
      roomId = parseInt($('#hiddenRuleRoomId').val());
      deviceId = parseInt($('#hiddenRuleDeviceId').val());
    } else {
      category = $('#ruleCategorySelect').val();
      roomId = parseInt($('#hiddenRuleRoomId').val() || $('#ruleRoomSelect').val());
      deviceId = parseInt($('#ruleDeviceSelect').val());
    }

    if (!category) {
      notify.warning('Please select a device category');
      return;
    }
    if (!deviceId || isNaN(deviceId)) {
      notify.warning('Please select a target device');
      return;
    }

    const actionParams = this.buildActionParams(category);

    const dto = {
      name,
      priority,
      isActive: $('#ruleIsActive').is(':checked'),
      targetDeviceCategory: category,
      targetDeviceId: deviceId,
      actionParams,
      conditions: [],
    };

    if (!isEdit) {
      dto.roomId = roomId;
    }

    try {
      $btn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-1"></i> Saving...');

      if (isEdit) {
        await this.ruleService.update(id, dto);
        notify.success('Rule updated successfully');
      } else {
        await this.ruleService.create(dto);
        notify.success('Rule created successfully');
      }

      $('#ruleModal').modal('hide');
      this.table.ajax.reload();
    } catch (error) {
      const msg = error.responseJSON?.message || error.message || 'Unknown error';
      notify.error('Failed to save rule: ' + msg);
      console.error('[RuleManager] handleSave error:', error);
    } finally {
      $btn.prop('disabled', false).html('<i class="fas fa-save mr-1"></i> Save Rule');
    }
  }

  static async handleEdit(id) {
    try {
      const res = await this.ruleService.getById(id);
      if (res?.data) this.openModal(res.data);
    } catch (e) {
      notify.error('Failed to load rule details');
      console.error('[RuleManager] handleEdit error:', e);
    }
  }

  static async handleDelete(id) {
    if (!(await notify.confirmDelete(`Rule #${id}`))) return;
    try {
      await this.ruleService.delete(id);
      notify.success('Rule deleted successfully');
      this.table.ajax.reload();
    } catch (error) {
      notify.error('Failed to delete rule');
      console.error('[RuleManager] handleDelete error:', error);
    }
  }

  static handleToggleStatus(id, isActive, $chk) {
    this.ruleService
      .patchStatus(id, isActive)
      .then(() => notify.success(`Rule ${isActive ? 'enabled' : 'disabled'}`))
      .catch(() => {
        $chk.prop('checked', !isActive);
        notify.error('Failed to update status');
      });
  }

  static async handleScan() {
    if (!(await notify.confirm('Run Rule Scan', 'Execute a global rule scan now?', 'question')))
      return;
    try {
      await this.ruleService.scan();
      notify.success('Rule scan executed successfully');
    } catch (error) {
      notify.error('Rule scan failed');
      console.error('[RuleManager] handleScan error:', error);
    }
  }

  static async handleReload() {
    if (!(await notify.confirm('Reload Rules', 'Reload all rules from database?', 'warning')))
      return;
    try {
      await this.ruleService.reload();
      notify.success('Rules reloaded successfully');
      this.table.ajax.reload();
    } catch (error) {
      notify.error('Reload failed');
      console.error('[RuleManager] handleReload error:', error);
    }
  }
}

window.RuleManager = RuleManager;
