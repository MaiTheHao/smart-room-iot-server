class JobManager {
	static init(contextPath) {
		this.contextPath = contextPath;
		this.apiClient = new HttpClient(`${contextPath}api/v1/`);

		this.automationService = new AutomationApiV1Service(this.apiClient);
		this.table = null;

		this.initDataTable();
		this.initPickers();
		this.bindEvents();
	}

	static initPickers() {
		if ($('#timeOnlyPicker').length === 0) {
			console.warn('⚠️ UI Elements missing.');
			return;
		}

		$('#timeOnlyPicker')
			.daterangepicker({
				singleDatePicker: true,
				timePicker: true,
				timePicker24Hour: true,
				timePickerSeconds: true,
				locale: { format: 'HH:mm:ss' },
				opens: 'right',
				drops: 'auto',
			})
			.on('show.daterangepicker', function (ev, picker) {
				picker.container.find('.calendar-table').hide();
			});
	}

	static bindEvents() {
		$('#btnCreateAutomation').on('click', () => this.openModal());
		$('#btnSaveAutomation').on('click', () => this.handleSave());
		$('#btnReloadAll').on('click', () => this.handleReloadSystem());

		$('#scheduleType').on('change', (e) => this.updateFormUI(e.target.value));

		const tbody = $('#automationsTable tbody');
		tbody.on('click', '.btn-edit', (e) => this.handleEdit($(e.currentTarget).data('id')));
		tbody.on('click', '.btn-delete', (e) => this.handleDelete($(e.currentTarget).data('id')));

		tbody.on('change', '.toggle-status', (e) => {
			const $chk = $(e.currentTarget);
			this.handleToggleStatus($chk.data('id'), $chk.is(':checked'), $chk);
		});
	}

	static updateFormUI(type) {
		$('#group-daily, #group-weekly, #group-monthly').addClass('d-none');

		if (type === 'DAILY') {
			$('#group-daily').removeClass('d-none');
		} else if (type === 'WEEKLY') {
			$('#group-weekly').removeClass('d-none');
		} else if (type === 'MONTHLY') {
			$('#group-monthly').removeClass('d-none');
		}
	}

	static openModal(data = null) {
		const isEdit = !!data;
		const $form = $('#automationForm');
		$form[0].reset();
		$('#autoId').val('');

		const now = moment().add(5, 'minutes');

		if (!isEdit) {
			$('#modalTitle').html('<i class="fas fa-plus-circle mr-2"></i> New Schedule');
			$('#scheduleType').val('DAILY').trigger('change');
			$('#timeOnlyPicker').data('daterangepicker').setStartDate(now);
			$('#timeOnlyPicker').data('daterangepicker').setEndDate(now);
		} else {
			$('#modalTitle').html(`<i class="fas fa-edit mr-2"></i> Edit Schedule #${data.id}`);
			$('#autoId').val(data.id);
			$('#autoName').val(data.name);
			$('#autoDescription').val(data.description);

			const parsed = CronUtils.fromCron(data.cronExpression);
			$('#scheduleType').val(parsed.type).trigger('change');

			$('#timeOnlyPicker').data('daterangepicker').setStartDate(parsed.timeMoment);
			$('#timeOnlyPicker').data('daterangepicker').setEndDate(parsed.timeMoment);

			if (parsed.type === 'WEEKLY' && parsed.daysOfWeek && parsed.daysOfWeek.length > 0) {
				$('#weeklySelect').val(parsed.daysOfWeek[0]);
			} else if (parsed.type === 'MONTHLY' && parsed.dayOfMonth) {
				const dom = parsed.dayOfMonth.includes(',') ? parsed.dayOfMonth.split(',')[0] : parsed.dayOfMonth;
				$('#monthlySelect').val(dom);
			}
		}
		$('#automationModal').modal('show');
	}

	static async handleSave() {
		if (!$('#autoName').val()) {
			notify.warning('Task Name is required');
			return;
		}

		const type = $('#scheduleType').val();
		const picker = $('#timeOnlyPicker').data('daterangepicker');
		if (!picker) return;

		const cronConfig = {
			type: type,
			timeMoment: picker.startDate,
			daysOfWeek: type === 'WEEKLY' ? [$('#weeklySelect').val()] : [],
			dayOfMonth: type === 'MONTHLY' ? $('#monthlySelect').val() : '',
		};
		const cronString = CronUtils.toCron(cronConfig);

		const payload = {
			name: $('#autoName').val(),
			cronExpression: cronString,
			description: $('#autoDescription').val(),
			isActive: true,
		};

		const id = $('#autoId').val();
		try {
			if (id) {
				await this.automationService.update(id, payload);
				notify.success('Schedule updated successfully');
			} else {
				await this.automationService.create(payload);
				notify.success('Schedule created successfully');
			}

			$('#automationModal').modal('hide');
			this.table.ajax.reload();
		} catch (error) {
			const msg = error.responseJSON?.message || error.message || 'Unknown error';
			notify.error('Error: ' + msg);
		}
	}

	static async handleEdit(id) {
		try {
			const res = await this.automationService.getById(id);
			if (res.data) this.openModal(res.data);
		} catch (e) {
			notify.error('Failed to load details');
		}
	}

	static initDataTable() {
		this.table = $('#automationsTable').DataTable({
			processing: true,
			serverSide: true,
			ajax: async (data, callback) => {
				try {
					const page = Math.floor(data.start / data.length);
					const size = data.length;
					const res = await this.automationService.getAll(page, size);

					callback({
						recordsTotal: res.data.totalElements,
						recordsFiltered: res.data.totalElements,
						data: res.data.content || [],
					});
				} catch (error) {
					console.error(error);
					callback({ data: [] });
				}
			},
			columns: [
				{ data: 'id', width: '50px', className: 'align-middle' },
				{ data: 'name', className: 'align-middle', render: (d) => `<span class="font-weight-bold text-dark">${d}</span>` },
				{
					data: 'cronExpression',
					className: 'align-middle',
					render: (cron) => {
						return CronUtils.formatDisplay ? CronUtils.formatDisplay(cron) : cron;
					},
				},
				{
					data: 'isActive',
					className: 'text-center align-middle',
					render: (active, type, row) => `
                        <div class="custom-control custom-switch">
                            <input type="checkbox" class="custom-control-input toggle-status" id="status_${row.id}" data-id="${row.id}" ${active ? 'checked' : ''}>
                            <label class="custom-control-label" for="status_${row.id}"></label>
                        </div>`,
				},
				{
					data: null,
					className: 'text-center align-middle',
					render: (data, type, row) => `
                        <div class="btn-group">
                            <button class="btn btn-sm btn-default btn-edit" title="Edit Info" data-id="${row.id}">
                                <i class="fas fa-pen text-primary"></i>
                            </button>
                            <a href="/automation/jobs/${row.id}/equipments" class="btn btn-sm btn-default" title="Equipment Actions">
                                <i class="fas fa-cogs text-warning"></i>
                            </a>
                            <button class="btn btn-sm btn-default btn-delete" title="Delete" data-id="${row.id}">
                                <i class="fas fa-trash text-danger"></i>
                            </button>
                        </div>
                    `,
				},
			],
			order: [[0, 'desc']],
			language: { emptyTable: 'No automations found.' },
		});
	}

	static async handleDelete(id) {
		if (await notify.confirmDelete(`Automation #${id}`)) {
			this.automationService
				.delete(id)
				.then(() => {
					notify.success('Deleted successfully');
					this.table.ajax.reload();
				})
				.catch(() => notify.error('Failed to delete automation'));
		}
	}

	static handleToggleStatus(id, isActive, $chk) {
		this.automationService
			.toggleStatus(id, isActive)
			.then(() => notify.success(`Automation ${isActive ? 'enabled' : 'disabled'}`))
			.catch(() => {
				$chk.prop('checked', !isActive);
				notify.error('Failed to update status');
			});
	}

	static async handleReloadSystem() {
		if (await notify.confirm('Reload Scheduler System', 'Are you sure you want to reload the scheduler system?', 'warning')) {
			this.automationService
				.reloadSystem()
				.then(() => notify.success('System reloaded'))
				.catch((err) => notify.error('Reload failed'));
		}
	}
}

window.JobManager = JobManager;
