class RuleV2Manager {
  static init() {
    if (typeof window === 'undefined')
      throw new Error('RuleV2Manager can only be initialized in a browser environment');

    this.ruleService = window.ruleApiV2Service;
    this.ruleService = window.ruleApiV2Service;
    this.table = null;

    this.initDataTable();
    this.bindEvents();
  }

  static bindEvents() {
    $('#btnCreateRule').on('click', () => this.openModal());
    $('#btnSaveRule').on('click', () => this.handleSave());
    $('#btnReloadRules').on('click', () => this.handleReload());

    const $tbody = $('#rulesTable tbody');
    $tbody.on('click', '.btn-edit', (e) => this.handleEdit($(e.currentTarget).data('id')));
    $tbody.on('click', '.btn-delete', (e) => this.handleDelete($(e.currentTarget).data('id')));
    $tbody.on('click', '.btn-execute', (e) => this.handleExecute($(e.currentTarget).data('id')));
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
          console.error('[RuleV2Manager] DataTable load error:', error);
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
          data: 'intervalSeconds',
          className: 'align-middle text-center font-weight-bold text-danger',
          render: (sec) => `${sec}s`,
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
            return `<a href="/view/v2/rules/${row.id}/conditions" class="badge badge-pill badge-success" style="font-size:0.85em;">${count} conditions</a>`;
          },
        },
        {
          data: 'actions',
          className: 'text-center align-middle',
          render: (acts, _, row) => {
            const count = Array.isArray(acts) ? acts.length : 0;
            return `<a href="/view/v2/rules/${row.id}/actions" class="badge badge-pill badge-primary" style="font-size:0.85em;">${count} actions</a>`;
          },
        },
        {
          data: null,
          className: 'text-right align-middle',
          orderable: false,
          render: (_, __, row) =>
            `<div class="btn-group">
							<a href="/view/v2/rules/${row.id}/conditions" class="btn btn-sm btn-link text-success p-1" title="Manage Conditions">
								<i class="fas fa-filter"></i>
							</a>
							<a href="/view/v2/rules/${row.id}/actions" class="btn btn-sm btn-link text-warning p-1" title="Manage Actions">
								<i class="fas fa-bolt"></i>
							</a>
							<button class="btn btn-sm btn-link text-primary p-1 btn-edit" data-id="${row.id}" title="Edit Rule Metadata">
								<i class="fas fa-edit"></i>
							</button>
							<button class="btn btn-sm btn-link text-danger p-1 btn-delete" data-id="${row.id}" title="Delete Rule">
								<i class="fas fa-trash-alt"></i>
							</button>
              <button class="btn btn-sm btn-link text-info p-1 btn-execute" data-id="${row.id}" title="Trigger Immediately">
								<i class="fas fa-play"></i>
							</button>
						</div>`,
        },
      ],
      order: [[0, 'desc']],
      language: { emptyTable: 'No rule v2 found.' },
    });
  }

  static async openModal(data = null) {
    const isEdit = !!data;
    $('#ruleForm')[0].reset();
    $('#ruleId').val('');
    if (!isEdit) {
      $('#ruleModalTitleText').text('Create New Rule V2');
    } else {
      $('#ruleModalTitleText').text('Edit Rule V2 Metadata');
      $('#ruleId').val(data.id);
      $('#ruleName').val(data.name);
      $('#rulePriority').val(data.priority);
      $('#ruleInterval').val(data.intervalSeconds);
      $('#ruleIsActive').prop('checked', data.isActive);
    }

    $('#ruleModal').modal('show');
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

    const intervalSeconds = parseInt($('#ruleInterval').val());
    if (isNaN(intervalSeconds) || intervalSeconds < 60) {
      notify.warning('Interval must be at least 60 seconds');
      return;
    }

    // IMPORTANT: When patching metadata, omit `conditions` and `actions` completely
    // to prevent the API from overwriting them with empty arrays.
    const dto = {
      name,
      priority,
      intervalSeconds,
      isActive: $('#ruleIsActive').is(':checked'),
    };

    if (!isEdit) {
      // When creating, the API requires empty arrays
      dto.conditions = [];
      dto.actions = [];
    }

    try {
      $btn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-1"></i> Saving...');

      if (isEdit) {
        await this.ruleService.update(id, dto);
        notify.success('Rule V2 metadata updated successfully');
      } else {
        await this.ruleService.create(dto);
        notify.success('Rule V2 created successfully');
      }

      $('#ruleModal').modal('hide');
      this.table.ajax.reload();
    } catch (error) {
      const msg = error.responseJSON?.message || error.message || 'Unknown error';
      notify.error('Failed to save rule: ' + msg);
      console.error('[RuleV2Manager] handleSave error:', error);
    } finally {
      $btn.prop('disabled', false).html('<i class="fas fa-save mr-1"></i> Save Rules V2');
    }
  }

  static async handleEdit(id) {
    try {
      const res = await this.ruleService.getById(id);
      if (res?.data) this.openModal(res.data);
    } catch (e) {
      notify.error('Failed to load rule details');
      console.error('[RuleV2Manager] handleEdit error:', e);
    }
  }

  static async handleDelete(id) {
    if (!(await notify.confirmDelete(`Rule V2 #${id}`))) return;
    try {
      await this.ruleService.delete(id);
      notify.success('Rule deleted successfully');
      this.table.ajax.reload();
    } catch (error) {
      notify.error('Failed to delete rule');
      console.error('[RuleV2Manager] handleDelete error:', error);
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

  static async handleExecute(id) {
    if (!(await notify.confirm('Trigger Rule', 'Execute this rule immediately?', 'info'))) return;
    try {
      await this.ruleService.execute(id);
      notify.success('Rule triggered successfully');
    } catch (error) {
      notify.error('Execute failed');
      console.error('[RuleV2Manager] handleExecute error:', error);
    }
  }

  static async handleReload() {
    if (
      !(await notify.confirm('Reload Rules', 'Reload all active V2 rules into Quartz?', 'warning'))
    )
      return;
    try {
      await this.ruleService.reload();
      notify.success('Rules reloaded successfully');
      this.table.ajax.reload();
    } catch (error) {
      notify.error('Reload failed');
      console.error('[RuleV2Manager] handleReload error:', error);
    }
  }
}

window.RuleV2Manager = RuleV2Manager;
