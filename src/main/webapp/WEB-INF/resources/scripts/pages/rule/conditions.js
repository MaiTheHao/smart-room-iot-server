class ConditionManager {
  static PROPERTY_META = {
    FAN: {
      power: { type: 'enum', values: ['ON', 'OFF'] },
      mode: { type: 'enum', values: ['NORMAL', 'SLEEP', 'NATURAL'] },
      speed: { type: 'number' },
      swing: { type: 'enum', values: ['ON', 'OFF'] },
      light: { type: 'enum', values: ['ON', 'OFF'] },
    },
    AIR_CONDITION: {
      power: { type: 'enum', values: ['ON', 'OFF'] },
      temp: { type: 'number' },
      mode: { type: 'enum', values: ['COOL', 'HEAT', 'DRY', 'FAN', 'AUTO'] },
      fan_speed: { type: 'number' },
      swing: { type: 'enum', values: ['ON', 'OFF'] },
    },
    LIGHT: {
      power: { type: 'enum', values: ['ON', 'OFF'] },
      level: { type: 'number' },
    },
    TEMPERATURE: {
      temperature: { type: 'number' },
    },
    POWER_CONSUMPTION: {
      watt: { type: 'number' },
    },
    SYSTEM: {
      current_time: { type: 'number' },
      day_of_week: { type: 'number' },
      day_of_month: { type: 'number' },
    },
    ROOM: {
      avg_temperature: { type: 'number' },
      sum_watt: { type: 'number' },
    },
  };

  static init(ruleData) {
    if (typeof window === 'undefined')
      throw new Error('ConditionManager can only be initialized in a browser environment');

    this.ruleService = window.ruleApiV1Service;
    this.floorService = window.floorApiV1Service;
    this.roomService = window.roomApiV1Service;
    this.deviceMetadataService = window.deviceMetadataApiV1Service;

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

      const $row = $(`
				<tr data-local-id="${c._localId}">
					<td class="align-middle" style="width:80px;">
						<input type="number" class="form-control form-control-sm order-input text-center"
							value="${c.sortOrder}" min="1"
							data-local-id="${c._localId}" />
					</td>
					<td class="align-middle">
						<span class="badge badge-secondary">${this.escapeHtml(c.dataSource || '')}</span>
					</td>
					<td class="align-middle small text-break" style="max-width:220px;white-space:normal;">
						${resourceDisplay}
					</td>
					<td class="align-middle">
						<span class="badge badge-light border font-weight-bold">${operatorDisplay}</span>
					</td>
					<td class="align-middle font-weight-bold">${this.escapeHtml(c.value || '')}</td>
					<td class="align-middle">
						<span class="badge badge-${nextLogicColor}">${this.escapeHtml(c.nextLogic || 'AND')}</span>
					</td>
					<td class="align-middle text-right">
						<button class="btn btn-sm btn-default btn-edit-cond" data-local-id="${c._localId}" title="Edit">
							<i class="fas fa-pen text-primary"></i>
						</button>
						<button class="btn btn-sm btn-default btn-delete-cond" data-local-id="${c._localId}" title="Delete">
							<i class="fas fa-trash text-danger"></i>
						</button>
					</td>
				</tr>
			`);
      $tbody.append($row);
    });

    $tbody.find('.order-input').on('change', (e) => {
      const localId = parseInt($(e.currentTarget).data('local-id'));
      const newValue = parseInt(e.currentTarget.value);
      this.handleSortOrderInput(localId, newValue);
    });
  }

  static formatResourceParam(dataSource, rp) {
    if (!rp) return '<span class="text-muted">—</span>';

    if (dataSource === 'SYSTEM' || dataSource === 'ROOM') {
      return `<code class="text-dark">${this.escapeHtml(rp.property || '')}</code>`;
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

  static escapeHtml(str) {
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
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

  static getPropertyMeta(category, property) {
    return (this.PROPERTY_META[category] || {})[property] || { type: 'string' };
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

  static bindEvents() {
    $('#btnAddCondition, #btnAddConditionEmpty').on('click', () => this.openConditionModal());
    $('#btnSaveCondition').on('click', () => this.handleModalSave());
    $('#btnSaveConditions').on('click', () => this.handleSaveAll());

    $('#conditionsTableBody').on('click', '.btn-edit-cond', (e) => {
      const localId = parseInt($(e.currentTarget).data('local-id'));
      this.openConditionModal(localId);
    });

    $('#conditionsTableBody').on('click', '.btn-delete-cond', (e) => {
      const localId = parseInt($(e.currentTarget).data('local-id'));
      this.handleDeleteCondition(localId);
    });

    $('#condDataSourceSelect').on('change', (e) => this.handleDataSourceChange(e.target.value));
    $('#condCategorySelect').on('change', (e) => {
      this.updateCondPropertyOptions(e.target.value, $('#condDataSourceSelect').val());
      this.resetCondDeviceSelection();
      this.updateOperatorAndValueByProperty('', '');
      if ($('#condFloorSelect option').length <= 1) {
        this.loadCondFloors();
      } else {
        $('#condFloorSelect').prop('disabled', false);
      }
    });
    $('#condFloorSelect').on('change', (e) => this.loadCondRooms(e.target.value));
    $('#condRoomSelect').on('change', (e) => {
      this.loadCondDevices(e.target.value, $('#condDataSourceSelect').val());
    });

    $('#condPropertySelect').on('change', (e) => {
      const category = $('#condCategorySelect').val();
      this.updateOperatorAndValueByProperty(category, e.target.value);
    });

    $('#condSystemProperty').on('change', (e) => {
      this.updateOperatorAndValueByProperty('SYSTEM', e.target.value);
    });

    $('#condRoomProperty').on('change', (e) => {
      this.updateOperatorAndValueByProperty('ROOM', e.target.value);
    });
  }

  static async openConditionModal(localId = null) {
    const isEdit = localId !== null;
    $('#conditionForm')[0].reset();
    $('#conditionLocalId').val('');

    $('#condSectionSystem, #condSectionRoom, #condSectionDeviceSensor').hide();

    if (!isEdit) {
      $('#conditionModalTitleText').text('Add Condition');
      $('#condDataSourceSelect').val('');
      $('#condNextLogicSelect').val('AND');
      this.updateOperatorAndValueByProperty('', '');
    } else {
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
          await this.loadCondFloors();
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
    $('#condDeviceIdHint').remove();

    if (dataSource === 'SYSTEM') {
      $('#condSectionSystem').show();
    } else if (dataSource === 'ROOM') {
      $('#condSectionRoom').show();
    } else if (dataSource === 'DEVICE' || dataSource === 'SENSOR') {
      $('#condSectionDeviceSensor').show();

      $('#condCategorySelect option[value]').each(function () {
        const forAttr = $(this).data('for') || '';
        $(this).toggle(forAttr.includes(dataSource));
      });
      $('#condCategorySelect').val('');
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

  static async loadCondFloors() {
    try {
      const res = await this.floorService.getAllWithoutPagination();
      const floors = res?.data || [];
      const $select = $('#condFloorSelect');
      $select.empty().append('<option value="" disabled selected>Select floor</option>');
      floors.forEach((f) => $select.append(`<option value="${f.id}">${f.name}</option>`));
      $select.prop('disabled', false);
    } catch (error) {
      console.error(error);
      notify.error('Failed to load floors');
    }
  }

  static async loadCondRooms(floorId) {
    if (!floorId) return;
    try {
      const $select = $('#condRoomSelect');
      $select.prop('disabled', true).html('<option>Loading...</option>');
      const res = await this.roomService.getAllByFloor(floorId);
      const rooms = res?.data || [];
      $select.empty().append('<option value="" disabled selected>Select room</option>');
      rooms.forEach((r) => $select.append(`<option value="${r.id}">${r.name}</option>`));
      $select.prop('disabled', false);

      $('#condDeviceSelect')
        .prop('disabled', true)
        .html('<option value="" disabled selected>Select device/sensor</option>');
    } catch (error) {
      console.error(error);
      notify.error('Failed to load rooms');
      $('#condRoomSelect')
        .prop('disabled', false)
        .html('<option value="" disabled selected>Select room</option>');
    }
  }

  static async loadCondDevices(roomId, dataSource) {
    if (!roomId) return;
    const category = $('#condCategorySelect').val();
    try {
      const $select = $('#condDeviceSelect');
      $select.prop('disabled', true).html('<option>Loading...</option>');
      const res = await this.deviceMetadataService.getAllByRoom(roomId);
      const devices = (res?.data || []).filter((d) => !category || d.category === category);
      $select.empty().append('<option value="" disabled selected>Select device/sensor</option>');
      if (devices.length === 0) {
        $select.append(`<option disabled>No devices found</option>`);
      } else {
        devices.forEach((d) =>
          $select.append(
            `<option value="${d.id}" data-natural-id="${d.naturalId || ''}">${d.name} (#${d.id})</option>`,
          ),
        );
      }
      $select.prop('disabled', false);
    } catch (error) {
      console.error(error);
      notify.error('Failed to load devices');
      $('#condDeviceSelect')
        .prop('disabled', false)
        .html('<option value="" disabled selected>Select device/sensor</option>');
    }
  }

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
      if (!property) {
        notify.warning('Please select a room property');
        return null;
      }
      return { property };
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
      console.error(error);
    } finally {
      $btn.prop('disabled', false).html('<i class="fas fa-save mr-1"></i> Save All Changes');
    }
  }
}

window.ConditionManager = ConditionManager;
