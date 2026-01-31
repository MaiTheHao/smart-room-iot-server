class RoomDetailPage {
	static instance;

	CONFIG = {
		DATE_FORMAT: 'HH:mm DD/MM/YYYY',
		DEFAULT_RANGE_MONTHS: 1,
	};

	constructor(roomId, initialTempData, initialPowerData) {
		if (RoomDetailPage.instance) return RoomDetailPage.instance;
		RoomDetailPage.instance = this;

		this.roomId = roomId;
		this.initialTempData = initialTempData || [];
		this.initialPowerData = initialPowerData || [];

		this.roomService = window.roomApiV1Service;
		this.healthService = window.healthCheckApiV1Service;
		this.lightService = window.lightApiV1Service;
		this.acService = window.airConditionApiV1Service;
		this.telemetryService = window.telemetryApiV1Service;

		this.logger = window.logger('RoomDetailPage');

		this.state = this._parseUrlParams();
		this.charts = {};

		if (this._shouldRedirect()) {
			this._navigateToCurrentRange();
		} else {
			this.init();
		}
	}

	init() {
		try {
			this.initCharts();
			this.initDateRangePicker();
			this.renderInitialData();
			this.loadHealthScore();
			this.fetchTelemetryData();
			this.bindEvents();
		} catch (error) {
			this.logger.error('Init failed:', error);
			notify.error('Failed to initialize dashboard');
		}
	}

	initCharts() {
		this.charts = {
			temp: ChartFactory.createTemperatureChart('tempChart'),
			power: ChartFactory.createPowerChart('powerChart'),
		};
	}

	renderInitialData() {
		const { temp, power } = this.charts;
		if (this.initialTempData.length) temp.render(this.initialTempData, 'avgTempC');
		if (this.initialPowerData.length) power.render(this.initialPowerData, 'sumWatt');
	}

	async loadHealthScore() {
		const $badge = $('#healthScoreBadge');
		const $text = $('#healthScoreValue');
		const $icon = $('#healthIcon');

		try {
			$text.html(`<i class="fas fa-spinner fa-spin"></i>`);

			const uiConfig = await this.healthService.getRoomHealthUiConfig(this.roomId);

			$badge.removeClass('badge-secondary badge-success badge-warning badge-danger').addClass(uiConfig.className);

			$text.text(`${uiConfig.score}%`);
			$icon.attr('class', `fas ${uiConfig.icon} mr-1`);
		} catch (error) {
			this.logger.error('Failed to load health score:', error);
			$badge.removeClass('badge-success badge-warning badge-danger').addClass('badge-secondary');
			$text.text('N/A');
			$icon.attr('class', 'fas fa-question-circle mr-1');
		}
	}

	fetchTelemetryData() {
		const roomCode = $('#roomCode').val();

		if (!roomCode) {
			this.logger.warn('Room code not found, skipping telemetry fetch');
			return;
		}

		this.telemetryService
			.fetchByRoom(roomCode)
			.then(() => {
				this.logger.info(`Telemetry data fetched successfully for room: ${roomCode}`);
			})
			.catch((error) => {
				this.logger.error('Failed to fetch telemetry data:', error);
			});

		this.logger.debug(`Telemetry fetch initiated for room: ${roomCode}`);
	}

	bindEvents() {
		$('#btn-refresh-chart').on('click', (e) => {
			$(e.currentTarget).find('i').addClass('fa-spin');
			this._navigateToCurrentRange();
		});

		$('#lightsList').on('change', '.light-toggle', (e) => this.handleLightToggle($(e.currentTarget)));

		$('.ac-master-switch').on('change', (e) => this.handleAcMasterToggle($(e.currentTarget)));
		$('.ac-temp-decrease').on('click', (e) => this.handleAcTempChange($(e.currentTarget), -1));
		$('.ac-temp-increase').on('click', (e) => this.handleAcTempChange($(e.currentTarget), 1));
		$('.ac-mode-btn').on('click', (e) => this.handleAcModeChange($(e.currentTarget)));
		$('.ac-fan-slider').on('input', (e) => this.handleAcFanSpeedChange($(e.currentTarget)));
		$('.ac-swing-switch').on('change', (e) => this.handleAcSwingToggle($(e.currentTarget)));
	}

	async handleLightToggle($input) {
		const lightId = $input.data('light-id');
		const isChecked = $input.is(':checked');
		const $item = $input.closest('.light-control-item');
		const lightName = $item.find('p').text() || 'Light';

		$input.prop('disabled', true);

		try {
			await this.lightService.toggleState(lightId);
			notify.success(`${lightName} turned ${isChecked ? 'ON' : 'OFF'}`);
		} catch (error) {
			notify.error(error.message || `Failed to toggle ${lightName}`);
			$input.prop('checked', !isChecked);
		} finally {
			$input.prop('disabled', false);
		}
	}

	async handleAcMasterToggle($input) {
		const acId = $input.data('ac-id');
		const isChecked = $input.is(':checked');
		const $acItem = $(`.ac-item[data-ac-id="${acId}"]`);
		const $controls = $acItem.find('.collapse');
		const $status = $acItem.find('.ac-status-text');
		const $icon = $acItem.find('.default-icon');
		const acName = $acItem.find('h6').text() || 'AC';

		$acItem.addClass('is-loading');
		$input.prop('disabled', true);

		try {
			const response = await this.acService.setPower(acId, isChecked ? 'ON' : 'OFF');

			if (response && response.status === 202) {
				notify.success(`${acName} turned ${isChecked ? 'ON' : 'OFF'}`);

				if (isChecked) {
					$controls.removeClass('disabled-overlay');
					$icon.addClass('active-ac');
					const temp = $acItem.find('.ac-temp-value').text();
					$status.text(`Cooling • ${temp}°C`);
				} else {
					$controls.addClass('disabled-overlay');
					$controls.collapse('hide');
					$icon.removeClass('active-ac');
					$status.text('Đã tắt');
				}
			}
		} catch (error) {
			notify.error(error.message || `Failed to toggle ${acName}`);
			$input.prop('checked', !isChecked);
		} finally {
			$acItem.removeClass('is-loading');
			$input.prop('disabled', false);
		}
	}

	async handleAcTempChange($btn, delta) {
		const acId = $btn.data('ac-id');
		const $acItem = $(`.ac-item[data-ac-id="${acId}"]`);
		const $tempEl = $acItem.find('.ac-temp-value');
		const $status = $acItem.find('.ac-status-text');
		const currentTemp = parseInt($tempEl.text());
		const newTemp = currentTemp + delta;

		if (newTemp < 16 || newTemp > 32) {
			notify.warning('Temperature must be between 16°C and 32°C');
			return;
		}

		$tempEl.text(newTemp);
		$status.text(`Cooling • ${newTemp}°C`);

		clearTimeout(this._tempChangeDebounce);
		this._tempChangeDebounce = setTimeout(async () => {
			$acItem.addClass('is-loading');

			try {
				const response = await this.acService.setTemperature(acId, newTemp);

				if (response && response.status === 202) {
					notify.success(`Temperature set to ${newTemp}°C`);
				}
			} catch (error) {
				notify.error(error.message || 'Failed to change temperature');
				$tempEl.text(currentTemp);
				$status.text(`Cooling • ${currentTemp}°C`);
			} finally {
				$acItem.removeClass('is-loading');
			}
		}, 500);
	}

	async handleAcModeChange($btn) {
		const acId = $btn.data('ac-id');
		const mode = $btn.data('mode');
		const $acItem = $(`.ac-item[data-ac-id="${acId}"]`);

		$acItem.addClass('is-loading');
		$btn.prop('disabled', true);

		try {
			const response = await this.acService.setMode(acId, mode);

			if (response && response.status === 202) {
				$acItem.find('.ac-mode-btn').removeClass('active-cool active-heat active-dry active-fan');

				switch (mode) {
					case 'COOL':
						$btn.addClass('active-cool');
						break;
					case 'HEAT':
						$btn.addClass('active-heat');
						break;
					case 'DRY':
						$btn.addClass('active-dry');
						break;
					case 'FAN':
						$btn.addClass('active-fan');
						break;
				}

				notify.success(`Mode changed to ${mode}`);
			}
		} catch (error) {
			notify.error(error.message || 'Failed to change mode');
		} finally {
			$acItem.removeClass('is-loading');
			$btn.prop('disabled', false);
		}
	}

	async handleAcFanSpeedChange($slider) {
		const acId = $slider.data('ac-id');
		const speed = parseInt($slider.val());
		const $acItem = $(`.ac-item[data-ac-id="${acId}"]`);
		const $badge = $acItem.find('.ac-fan-badge');

		const text = speed === 0 ? 'AUTO' : 'Mức ' + speed;
		$badge.text(text);

		clearTimeout(this._fanSpeedDebounce);
		this._fanSpeedDebounce = setTimeout(async () => {
			try {
				const response = await this.acService.setFanSpeed(acId, speed);

				if (response && response.status === 202) {
					notify.success(`Fan speed set to ${text}`);
				}
			} catch (error) {
				notify.error(error.message || 'Failed to change fan speed');
			}
		}, 500);
	}

	async handleAcSwingToggle($input) {
		const acId = $input.data('ac-id');
		const isChecked = $input.is(':checked');
		const $acItem = $(`.ac-item[data-ac-id="${acId}"]`);
		const $label = $acItem.find('.ac-swing-label');

		$input.prop('disabled', true);

		try {
			const response = await this.acService.setSwing(acId, isChecked ? 'ON' : 'OFF');

			if (response && response.status === 202) {
				$label.text(isChecked ? 'ON' : 'OFF');

				if (isChecked) {
					$label.addClass('text-success');
				} else {
					$label.removeClass('text-success');
				}

				notify.success(`Swing turned ${isChecked ? 'ON' : 'OFF'}`);
			}
		} catch (error) {
			notify.error(error.message || 'Failed to toggle swing');
			$input.prop('checked', !isChecked);
		} finally {
			$input.prop('disabled', false);
		}
	}

	_parseUrlParams() {
		const params = new URLSearchParams(window.location.search);
		const startedAt = params.get('startedAt');
		const endedAt = params.get('endedAt');

		return {
			isMissingParams: !startedAt || !endedAt,
			start: startedAt ? moment(startedAt) : moment().subtract(this.CONFIG.DEFAULT_RANGE_MONTHS, 'months'),
			end: endedAt ? moment(endedAt) : moment(),
		};
	}

	_shouldRedirect() {
		return this.state.isMissingParams;
	}

	_navigateToCurrentRange() {
		const query = new URLSearchParams({
			startedAt: this.state.start.toISOString(),
			endedAt: this.state.end.toISOString(),
		});
		window.location.href = `${window.location.pathname}?${query.toString()}`;
	}

	initDateRangePicker() {
		$('#date-range-picker').daterangepicker(
			{
				timePicker: true,
				timePicker24Hour: true,
				startDate: this.state.start,
				endDate: this.state.end,
				locale: { format: this.CONFIG.DATE_FORMAT },
			},
			(start, end) => {
				this.state.start = start;
				this.state.end = end;
				this._navigateToCurrentRange();
			},
		);
	}
}

window.RoomDetailPage = RoomDetailPage;
