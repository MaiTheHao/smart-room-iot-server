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
		if ($('#startTimePicker').length === 0) {
			console.warn('⚠️ UI Elements missing.');
			return;
		}

		$('#startTimePicker').daterangepicker({
			singleDatePicker: true,
			timePicker: true,
			timePicker24Hour: true,
			timePickerSeconds: false,
			autoApply: true,
			locale: { format: 'DD/MM/YYYY HH:mm' },
			opens: 'right',
			drops: 'auto',
		});
	}

	static bindEvents() {
		$('#btnCreateAutomation').on('click', () => this.openModal());
		$('#btnSaveAutomation').on('click', () => this.handleSave());
		$('#btnReloadAll').on('click', () => this.handleReloadSystem());

		$('#scheduleType').on('change', (e) => this.updateFormUI(e.target.value));

		$('#chkEnableLimit').on('change', (e) => {
			const isChecked = $(e.target).is(':checked');
			if (isChecked) {
				$('#limitInputArea').removeClass('d-none');
				$('#limitInfiniteText').addClass('d-none');
			} else {
				$('#limitInputArea').addClass('d-none');
				$('#limitInfiniteText').removeClass('d-none');
			}
		});

		$(document).on('click', '.day-cell', function () {
			$(this).toggleClass('active btn-primary text-white btn-outline-light text-dark');
		});

		// Reset Month Grid
		$('#btnResetMonth').on('click', () => {
			$('.day-cell').removeClass('active btn-primary text-white').addClass('btn-outline-light text-dark');
		});

		// Table Actions
		const tbody = $('#automationsTable tbody');
		tbody.on('click', '.btn-edit', (e) => this.handleEdit($(e.currentTarget).data('id')));
		tbody.on('click', '.btn-delete', (e) => this.handleDelete($(e.currentTarget).data('id')));
		tbody.on('change', '.toggle-status', (e) => {
			const $chk = $(e.currentTarget);
			this.handleToggleStatus($chk.data('id'), $chk.is(':checked'), $chk);
		});
	}

	static updateFormUI(type) {
		$('.schedule-option').addClass('d-none');

		const $desc = $('#patternDesc');

		if (type === 'ONCE') {
			$('#opt-simple').removeClass('d-none');
			$('#simpleMessage').text('Task runs once at the Start Time, then stops.');
			$desc.text('Single execution');
			$('#chkEnableLimit').prop('checked', false).prop('disabled', true).trigger('change');
		} else if (type === 'DAILY') {
			$('#opt-simple').removeClass('d-none');
			$('#simpleMessage').text('Task repeats every day at the time specified above.');
			$desc.text('Repeats every day');
			$('#chkEnableLimit').prop('disabled', false);
		} else if (type === 'WEEKLY') {
			$('#opt-weekly').removeClass('d-none');
			$desc.text('Repeats on specific weekdays');
			$('#chkEnableLimit').prop('disabled', false);
		} else if (type === 'MONTHLY') {
			$('#opt-monthly').removeClass('d-none');
			$desc.text('Repeats on specific days of the month');
			$('#chkEnableLimit').prop('disabled', false);
		}
	}

	static openModal(data = null) {
		const isEdit = !!data;
		const $form = $('#automationForm');
		$form[0].reset();
		$('#autoId').val('');

		$('input[name="weekDays"]').parent().removeClass('active');
		$('.day-cell').removeClass('active btn-primary text-white').addClass('btn-outline-light text-dark');
		$('#chkEnableLimit').prop('checked', false).trigger('change');

		const now = moment().add(5, 'minutes');

		if (!isEdit) {
			$('#modalTitle').html('<i class="fas fa-plus-circle mr-2"></i> New Schedule');
			$('#scheduleType').val('ONCE').trigger('change');
			$('#startTimePicker').data('daterangepicker').setStartDate(now);
			$('#startTimePicker').data('daterangepicker').setEndDate(now);
		} else {
			$('#modalTitle').html(`<i class="fas fa-edit mr-2"></i> Edit Schedule #${data.id}`);
			$('#autoId').val(data.id);
			$('#autoName').val(data.name);
			$('#autoDescription').val(data.description);
			$('#autoIsActive').prop('checked', data.isActive);

			// 1. Parse Cron to UI
			const parsed = CronUtils.fromCron(data.cronExpression);
			$('#scheduleType').val(parsed.type).trigger('change');

			// Set Start Time (Giờ/Phút/Ngày từ Cron hoặc dữ liệu lưu trữ nếu có)
			// Lưu ý: Cron chỉ chứa thời gian lặp, không nhất thiết là "Start Time" gốc.
			// Nếu muốn chính xác StartTime, BE nên trả về field 'startTime'. Tạm thời dùng Cron Moment.
			$('#startTimePicker').data('daterangepicker').setStartDate(parsed.moment);
			$('#startTimePicker').data('daterangepicker').setEndDate(parsed.moment);

			if (parsed.type === 'WEEKLY' && parsed.daysOfWeek) {
				parsed.daysOfWeek.forEach((day) => {
					$(`input[name="weekDays"][value="${day}"]`).prop('checked', true).parent().addClass('active');
				});
			} else if (parsed.type === 'MONTHLY' && parsed.dayOfMonth) {
				const days = parsed.dayOfMonth.split(',');
				days.forEach((d) => {
					$(`.day-cell[data-val="${d}"]`).removeClass('btn-outline-light text-dark').addClass('active btn-primary text-white');
				});
			}

			if (data.maxExecutions && data.maxExecutions > 0) {
				$('#chkEnableLimit').prop('checked', true).trigger('change');
				$('#maxExecutions').val(data.maxExecutions);
			} else {
				$('#chkEnableLimit').prop('checked', false).trigger('change');
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
		const picker = $('#startTimePicker').data('daterangepicker');
		if (!picker) return;

		const startMoment = picker.startDate;

		let daysOfWeek = [];
		let dayOfMonthStr = '';

		if (type === 'WEEKLY') {
			$('input[name="weekDays"]:checked').each(function () {
				daysOfWeek.push($(this).val());
			});
			if (daysOfWeek.length === 0) {
				notify.warning('Select at least one weekday.');
				return;
			}
		} else if (type === 'MONTHLY') {
			const selectedDays = [];
			$('.day-cell.active').each(function () {
				selectedDays.push($(this).data('val'));
			});
			if (selectedDays.length === 0) {
				notify.warning('Select at least one day of the month.');
				return;
			}
			dayOfMonthStr = selectedDays.join(',');
		}

		const cronConfig = {
			type: type,
			moment: startMoment,
			daysOfWeek: daysOfWeek,
			dayOfMonth: dayOfMonthStr,
		};
		const cronString = CronUtils.toCron(cronConfig);

		let maxExecutions = null;
		if (type !== 'ONCE' && $('#chkEnableLimit').is(':checked')) {
			const val = parseInt($('#maxExecutions').val());
			if (val > 0) maxExecutions = val;
		}

		const payload = {
			name: $('#autoName').val(),
			cronExpression: cronString,
			description: $('#autoDescription').val(),
			isActive: $('#autoIsActive').is(':checked'),
			maxExecutions: maxExecutions,
			startTime: startMoment.format('YYYY-MM-DD HH:mm:ss'),
		};

		const id = $('#autoId').val();
		try {
			if (id) await this.automationService.update(id, payload);
			else await this.automationService.create(payload);

			notify.success('Schedule saved successfully');
			$('#automationModal').modal('hide');
			this.table.ajax.reload();
		} catch (error) {
			notify.error('Error: ' + (error.message || 'Unknown'));
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
					const res = await this.automationService.getAll(page, data.length);
					callback({
						recordsTotal: res.data.totalElements,
						recordsFiltered: res.data.totalElements,
						data: res.data.content || [],
					});
				} catch (error) {
					callback({ data: [] });
				}
			},
			columns: [
				{ data: 'id', width: '50px' },
				{ data: 'name', render: (d) => `<span class="font-weight-bold text-dark">${d}</span>` },
				{
					data: 'cronExpression',
					render: (cron, type, row) => {
						let html = CronUtils.formatDisplay(cron);
						if (row.maxExecutions && row.maxExecutions > 0) {
							html += ` <span class="badge badge-light border text-muted ml-1" title="Limit"><i class="fas fa-stopwatch"></i> x${row.maxExecutions}</span>`;
						}
						return html;
					},
				},
				{
					data: 'isActive',
					className: 'text-center',
					render: (active, type, row) => `
                        <div class="custom-control custom-switch">
                            <input type="checkbox" class="custom-control-input toggle-status" id="status_${row.id}" data-id="${row.id}" ${active ? 'checked' : ''}>
                            <label class="custom-control-label" for="status_${row.id}"></label>
                        </div>`,
				},
				{
					data: null,
					className: 'text-center',
					render: (data, type, row) => `
                        <button class="btn btn-sm btn-outline-primary btn-edit" data-id="${row.id}"><i class="fas fa-pen"></i></button>
                        <a href="automations/${row.id}/design" class="btn btn-sm btn-outline-warning"><i class="fas fa-layer-group"></i></a>
                        <button class="btn btn-sm btn-outline-danger btn-delete" data-id="${row.id}"><i class="fas fa-trash"></i></button>
                    `,
				},
			],
			order: [[0, 'desc']],
			language: { emptyTable: 'No schedules found.' },
		});
	}

	static handleDelete(id) {
		if (confirm('Delete this schedule?')) {
			this.automationService.delete(id).then(() => {
				notify.success('Deleted');
				this.table.ajax.reload();
			});
		}
	}
	static handleToggleStatus(id, isActive, $chk) {
		this.automationService
			.toggleStatus(id, isActive)
			.then(() => notify.success('Status updated'))
			.catch(() => {
				$chk.prop('checked', !isActive);
				notify.error('Failed');
			});
	}
	static handleReloadSystem() {
		this.automationService.reload().then(() => notify.success('Reloaded'));
	}
}

window.JobManager = JobManager;
