class RoomDetailPage {
	static instance;

	CONFIG = {
		DATE_FORMAT: 'HH:mm DD/MM/YYYY',
		DEFAULT_RANGE_DAYS: 3,
		TEMP_RANGE_MIN: 16,
		TEMP_RANGE_MAX: 32,
		DEBOUNCE_DELAY: 500,
		SYNC_INTERVAL: 5000,
	};

	constructor(roomId, initialTempData, initialPowerData) {
		if (RoomDetailPage.instance) return RoomDetailPage.instance;
		RoomDetailPage.instance = this;

		this.roomId = roomId;
		this.initialTempData = initialTempData || [];
		this.initialPowerData = initialPowerData || [];

		// Service Injection
		this.roomService = window.roomApiV1Service;
		this.healthService = window.healthCheckApiV1Service;
		this.lightService = window.lightApiV1Service;
		this.acService = window.airConditionApiV1Service;
		this.fanService = window.fanApiV1Service;
		this.deviceMetadataService = window.deviceMetadataApiV1Service;

		this.logger = window.logger('RoomDetailPage');
		this.state = this._parseUrlParams();
		this.charts = {};
		this.syncTimer = null;

		if (this._shouldRedirect()) {
			this._navigateToCurrentRange();
		} else {
			this.init();
		}
	}

	// ==========================================
	// LIFECYCLE
	// ==========================================

	async init() {
		try {
			this.initCharts();
			this.initDateRangePicker();
			this.renderInitialData();
			this.loadHealthScore();
			this.fetchTelemetryData();
			this.bindEvents();

			// Cache & Sync
			// await this.initCache();
			// this.initSync();
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

	renderInitialData() {
		const { temp, power } = this.charts;
		const { start, end } = this.state;
		if (this.initialTempData.length) temp.render(this.initialTempData, start, end);
		if (this.initialPowerData.length) power.render(this.initialPowerData, start, end);
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
		this.logger.debug(`Telemetry fetch initiated for room: ${roomCode}`);
	}

	// ==========================================
	// CACHE & SYNC LOGIC
	// ==========================================

	async initCache() {
		const cacheKey = `room_meta_${this.roomId}`;
		const cached = JSON.parse(localStorage.getItem(cacheKey));

		try {
			const currentVersion = await this.roomService.getVersion(this.roomId);

			if (!cached || cached.version !== currentVersion) {
				this.logger.info(`Cache miss or version mismatch for room ${this.roomId}. Fetching metadata...`);
				const response = await this.deviceMetadataService.getAllByRoom(this.roomId);
				const devices = response.data || [];

				// Cache only metadata (not control state)
				const metaOnly = devices.map((d) => ({
					id: d.id,
					naturalId: d.naturalId,
					name: d.name,
					description: d.description,
					isActive: d.isActive,
					category: d.category,
				}));

				localStorage.setItem(
					cacheKey,
					JSON.stringify({
						version: currentVersion,
						devices: metaOnly,
						timestamp: Date.now(),
					}),
				);
			} else {
				this.logger.debug(`Metadata cache for room ${this.roomId} is up to date (v${currentVersion})`);
			}
		} catch (error) {
			this.logger.warn('Failed to init metadata cache:', error);
		}
	}

	initSync() {
		this.syncTimer = setInterval(() => this.syncAllDevices(false), this.CONFIG.SYNC_INTERVAL);
	}

	async syncAllDevices(showNotify = false) {
		try {
			const response = await this.deviceMetadataService.getAllByRoom(this.roomId);
			const devices = response.data || [];

			devices.forEach((device) => {
				// Each device object from Metadata API has 'category', 'power', etc.
				if (device.naturalId.startsWith('L')) {
					this._updateLightUI(device);
				} else if (device.naturalId.startsWith('AC')) {
					this._updateAcUI(device);
				} else if (device.naturalId.startsWith('FAN')) {
					this._updateFanUI(device);
				}
			});

			if (showNotify) notify.success('Devices synced successfully');
		} catch (error) {
			this.logger.error('Sync failed:', error);
			if (showNotify) notify.error('Failed to sync devices');
		}
	}

	// ==========================================
	// EVENT BINDING (Delegation)
	// ==========================================

	bindEvents() {
		$('#btn-refresh-chart').on('click', (e) => {
			$(e.currentTarget).find('i').addClass('fa-spin');
			this._navigateToCurrentRange();
		});

		$('#btn-sync-devices').on('click', (e) => {
			const $icon = $(e.currentTarget).find('i');
			$icon.addClass('fa-spin');
			this.syncAllDevices(true).finally(() => {
				setTimeout(() => $icon.removeClass('fa-spin'), 500);
			});
		});

		// Delegation for dynamic content
		$('#lightsList').on('change', '.light-toggle', (e) => this.handleLightToggle($(e.currentTarget)));

		$('#acList')
			.on('change', '.ac-master-switch', (e) => this.handleAcMasterToggle($(e.currentTarget)))
			.on('click', '.ac-temp-decrease', (e) => this.handleAcTempChange($(e.currentTarget), -1))
			.on('click', '.ac-temp-increase', (e) => this.handleAcTempChange($(e.currentTarget), 1))
			.on('click', '.ac-mode-btn', (e) => this.handleAcModeChange($(e.currentTarget)))
			.on('input', '.ac-fan-slider', (e) => this.handleAcFanSpeedChange($(e.currentTarget)))
			.on('change', '.ac-swing-switch', (e) => this.handleAcSwingToggle($(e.currentTarget)));

		$('#fansList')
			.on('change', '.fan-master-switch', (e) => this.handleFanMasterToggle($(e.currentTarget)))
			.on('click', '.fan-mode-btn', (e) => this.handleFanModeChange($(e.currentTarget)))
			.on('input', '.fan-speed-slider', (e) => this.handleFanSpeedChange($(e.currentTarget)))
			.on('change', '.fan-swing-switch', (e) => this.handleFanSwingToggle($(e.currentTarget)))
			.on('change', '.fan-light-switch', (e) => this.handleFanLightToggle($(e.currentTarget)));
	}

	// ==========================================
	// LIGHT HANDLERS
	// ==========================================

	async handleLightToggle($input) {
		const naturalId = $input.data('light-natural-id');
		const isChecked = $input.is(':checked');
		const $item = $input.closest('.light-item');

		await this._executeControl($item, () => this.lightService.control(naturalId, { power: isChecked ? 'ON' : 'OFF' }), {
			onSuccess: () => {
				const lightId = $input.data('light-id');
				const $icon = $item.find('.default-icon');
				const $controls = $item.find(`#lightControls${lightId}`);
				const $statusText = $item.find('.light-status-text');

				if (isChecked) {
					$icon.addClass('active-light');
					$controls.removeClass('disabled-overlay');
					$statusText.text($statusText.data('text-on') || 'On');
				} else {
					$icon.removeClass('active-light');
					$controls.addClass('disabled-overlay');
					$controls.collapse('hide');
					$statusText.text($statusText.data('text-off') || 'Off');
				}
			},
			onError: () => {
				$input.prop('checked', !isChecked);
			},
		});
	}

	// ==========================================
	// AC HANDLERS
	// ==========================================

	async handleAcMasterToggle($input) {
		const naturalId = $input.data('ac-natural-id');
		const isChecked = $input.is(':checked');
		const $item = $input.closest('.ac-item');

		await this._executeControl($item, () => this.acService.control(naturalId, { power: isChecked ? 'ON' : 'OFF' }), {
			onSuccess: () => {
				const acId = $input.data('ac-id');
				const $controls = $item.find(`#acControls${acId}`);
				const $icon = $item.find('.default-icon');
				const $statusText = $item.find('.ac-status-text');

				if (isChecked) {
					$icon.addClass('active-ac');
					$controls.removeClass('disabled-overlay');
					this._updateAcStatusText($item);
				} else {
					$icon.removeClass('active-ac');
					$controls.addClass('disabled-overlay').collapse('hide');
					$statusText.text($statusText.data('text-off') || 'Off');
				}
			},
			onError: () => {
				$input.prop('checked', !isChecked);
			},
		});
	}

	async handleAcTempChange($btn, delta) {
		const naturalId = $btn.data('ac-natural-id');
		const $item = $btn.closest('.ac-item');
		const $tempValue = $item.find('.ac-temp-value');
		const currentTemp = parseInt($tempValue.text());
		const newTemp = currentTemp + delta;

		if (newTemp < this.CONFIG.TEMP_RANGE_MIN || newTemp > this.CONFIG.TEMP_RANGE_MAX) return;

		// Optimistic Update
		$tempValue.text(newTemp);
		this._updateAcStatusText($item, newTemp);

		await this._executeControl($item, () => this.acService.control(naturalId, { temperature: newTemp }), {
			onError: () => {
				$tempValue.text(currentTemp);
				this._updateAcStatusText($item, currentTemp);
			},
		});
	}

	async handleAcModeChange($btn) {
		const naturalId = $btn.data('ac-natural-id');
		const mode = $btn.data('mode');
		const $item = $btn.closest('.ac-item');

		await this._executeControl($item, () => this.acService.control(naturalId, { mode }), {
			onSuccess: () => {
				$item.find('.ac-mode-btn').removeClass('active-mode-cool active-mode-heat active-mode-dry active-mode-fan active-mode-auto');

				const modeClassMap = {
					COOL: 'active-mode-cool',
					HEAT: 'active-mode-heat',
					DRY: 'active-mode-dry',
					FAN: 'active-mode-fan',
					AUTO: 'active-mode-auto',
				};
				$btn.addClass(modeClassMap[mode] ?? 'active-mode-auto');
				this._updateAcStatusText($item);
			},
		});
	}

	async handleAcFanSpeedChange($slider) {
		const naturalId = $slider.data('ac-natural-id');
		const speed = parseInt($slider.val());
		const $item = $slider.closest('.ac-item');
		const $badge = $item.find('.ac-fan-badge');
		const oldText = $badge.text();

		// Optimistic UI
		this._updateAcFanBadge($badge, speed);

		clearTimeout(this._acFanSpeedDebounce);
		this._acFanSpeedDebounce = setTimeout(async () => {
			await this._executeControl($item, () => this.acService.control(naturalId, { fanSpeed: speed }), {
				onError: () => {
					$badge.text(oldText);
				},
			});
		}, this.CONFIG.DEBOUNCE_DELAY);
	}

	async handleAcSwingToggle($input) {
		const naturalId = $input.data('ac-natural-id');
		const isChecked = $input.is(':checked');
		const $item = $input.closest('.ac-item');

		await this._executeControl($item, () => this.acService.control(naturalId, { swing: isChecked ? 'ON' : 'OFF' }), {
			onError: () => {
				$input.prop('checked', !isChecked);
			},
		});
	}

	// ==========================================
	// FAN HANDLERS
	// ==========================================

	async handleFanMasterToggle($input) {
		const naturalId = $input.data('fan-natural-id');
		const isChecked = $input.is(':checked');
		const $item = $input.closest('.fan-item');

		await this._executeControl($item, () => this.fanService.control(naturalId, { power: isChecked ? 'ON' : 'OFF' }), {
			onSuccess: () => {
				const fanId = $input.data('fan-id');
				const $controls = $item.find(`#fanControls${fanId}`);
				const $status = $item.find('.fan-status-text');
				const $icon = $item.find('.default-icon');

				if (isChecked) {
					$controls.removeClass('disabled-overlay');
					$icon.addClass('active-fan');
					this._updateFanStatusText($item);
				} else {
					$controls.addClass('disabled-overlay').collapse('hide');
					$icon.removeClass('active-fan');
					$status.text($status.data('text-off') || 'Off');
				}
			},
			onError: () => {
				$input.prop('checked', !isChecked);
			},
		});
	}

	async handleFanModeChange($btn) {
		const naturalId = $btn.data('fan-natural-id');
		const mode = $btn.data('mode');
		const $item = $btn.closest('.fan-item');

		await this._executeControl($item, () => this.fanService.control(naturalId, { mode }), {
			onSuccess: () => {
				$item.find('.fan-mode-btn').removeClass('active-mode');
				$btn.addClass('active-mode');
				this._updateFanStatusText($item);
			},
		});
	}

	async handleFanSpeedChange($slider) {
		const naturalId = $slider.data('fan-natural-id');
		const speed = parseInt($slider.val());
		const $item = $slider.closest('.fan-item');
		const $badge = $item.find('.fan-speed-display');
		const oldText = $badge.text();

		// Optimistic
		const prefix = $badge.data('text-level') || '';
		$badge.text(`${prefix} ${speed}`.trim());

		clearTimeout(this._fanSpeedDebounce);
		this._fanSpeedDebounce = setTimeout(async () => {
			await this._executeControl($item, () => this.fanService.control(naturalId, { speed }), {
				onSuccess: () => this._updateFanStatusText($item),
				onError: () => $badge.text(oldText),
			});
		}, this.CONFIG.DEBOUNCE_DELAY);
	}

	async handleFanSwingToggle($input) {
		const naturalId = $input.data('fan-natural-id');
		const isChecked = $input.is(':checked');
		const $item = $input.closest('.fan-item');

		await this._executeControl($item, () => this.fanService.control(naturalId, { swing: isChecked ? 'ON' : 'OFF' }), {
			onError: () => $input.prop('checked', !isChecked),
		});
	}

	async handleFanLightToggle($input) {
		const naturalId = $input.data('fan-natural-id');
		const isChecked = $input.is(':checked');
		const $item = $input.closest('.fan-item');

		await this._executeControl($item, () => this.fanService.control(naturalId, { light: isChecked ? 'ON' : 'OFF' }), {
			onError: () => $input.prop('checked', !isChecked),
		});
	}

	// ==========================================
	// CORE CONTROL LOGIC (SOLID)
	// ==========================================

	/**
	 * Centralized command executor with loading states and result handling
	 */
	async _executeControl($item, apiCall, { onSuccess, onError } = {}) {
		$item.addClass('is-loading');
		$item.find('input, button').prop('disabled', true);

		try {
			const response = await apiCall();
			const result = response.data; // ControlDeviceResult

			// Accept 200 (Success) or 202 (Accepted)
			const isOk = (response.status === 200 || response.status === 202) && result && result.successCount > 0;

			if (isOk) {
				if (onSuccess) onSuccess(result);
				const detail = result.details?.[0];
				notify.success(detail?.message || response.message || 'Command executed');
			} else {
				throw new Error(result?.details?.[0]?.message || response.message || 'Operation failed');
			}
		} catch (error) {
			this.logger.error('Control failed:', error);
			if (onError) onError(error);
			notify.error(error.message || 'Failed to execute command');
		} finally {
			$item.removeClass('is-loading');
			$item.find('input, button').prop('disabled', false);
		}
	}

	// ==========================================
	// UI SYNC HELPERS
	// ==========================================

	_updateLightUI(device) {
		const $item = $(`.light-item[data-light-natural-id="${device.naturalId}"]`);
		if (!$item.length || $item.hasClass('is-loading')) return;

		const isPowerOn = device.power === 'ON';
		const $input = $item.find('.light-toggle');
		const $icon = $item.find('.default-icon');
		const $controls = $item.find(`.collapse`);
		const $statusText = $item.find('.light-status-text');

		if ($input.is(':checked') !== isPowerOn) {
			$input.prop('checked', isPowerOn);
			if (isPowerOn) {
				$icon.addClass('active-light');
				$controls.removeClass('disabled-overlay');
				$statusText.text($statusText.data('text-on') || 'On');
			} else {
				$icon.removeClass('active-light');
				$controls.addClass('disabled-overlay').collapse('hide');
				$statusText.text($statusText.data('text-off') || 'Off');
			}
		}
	}

	_updateAcUI(device) {
		const $item = $(`.ac-item[data-ac-natural-id="${device.naturalId}"]`);
		if (!$item.length || $item.hasClass('is-loading')) return;

		const isPowerOn = device.power === 'ON';
		const $input = $item.find('.ac-master-switch');
		const $controls = $item.find(`.collapse`);
		const $icon = $item.find('.default-icon');
		const $statusText = $item.find('.ac-status-text');
		const $tempValue = $item.find('.ac-temp-value');
		const $fanBadge = $item.find('.ac-fan-badge');
		const $fanSlider = $item.find('.ac-fan-slider');
		const $swingSwitch = $item.find('.ac-swing-switch');

		// Master Toggle
		if ($input.is(':checked') !== isPowerOn) {
			$input.prop('checked', isPowerOn);
			if (isPowerOn) {
				$icon.addClass('active-ac');
				$controls.removeClass('disabled-overlay');
			} else {
				$icon.removeClass('active-ac');
				$controls.addClass('disabled-overlay').collapse('hide');
				$statusText.text($statusText.data('text-off') || 'Off');
			}
		}

		if (isPowerOn) {
			// Temperature
			if (parseInt($tempValue.text()) !== device.temperature) {
				$tempValue.text(device.temperature);
			}

			// Mode
			$item.find('.ac-mode-btn').removeClass('active-mode-cool active-mode-heat active-mode-dry active-mode-fan active-mode-auto');
			const modeClassMap = {
				COOL: 'active-mode-cool',
				HEAT: 'active-mode-heat',
				DRY: 'active-mode-dry',
				FAN: 'active-mode-fan',
				AUTO: 'active-mode-auto',
			};
			$item.find(`.ac-mode-btn[data-mode="${device.mode}"]`).addClass(modeClassMap[device.mode] || 'active-mode-auto');

			// Fan Speed
			if (parseInt($fanSlider.val()) !== device.fanSpeed) {
				$fanSlider.val(device.fanSpeed);
				this._updateAcFanBadge($fanBadge, device.fanSpeed);
			}

			// Swing
			const isSwingOn = device.swing === 'ON';
			if ($swingSwitch.is(':checked') !== isSwingOn) {
				$swingSwitch.prop('checked', isSwingOn);
			}

			this._updateAcStatusText($item);
		}
	}

	_updateFanUI(device) {
		const $item = $(`.fan-item[data-fan-natural-id="${device.naturalId}"]`);
		if (!$item.length || $item.hasClass('is-loading')) return;

		const isPowerOn = device.power === 'ON';
		const $input = $item.find('.fan-master-switch');
		const $controls = $item.find(`.collapse`);
		const $status = $item.find('.fan-status-text');
		const $icon = $item.find('.default-icon');
		const $speedBadge = $item.find('.fan-speed-display');
		const $speedSlider = $item.find('.fan-speed-slider');
		const $swingSwitch = $item.find('.fan-swing-switch');
		const $lightSwitch = $item.find('.fan-light-switch');

		if ($input.is(':checked') !== isPowerOn) {
			$input.prop('checked', isPowerOn);
			if (isPowerOn) {
				$controls.removeClass('disabled-overlay');
				$icon.addClass('active-fan');
			} else {
				$controls.addClass('disabled-overlay').collapse('hide');
				$icon.removeClass('active-fan');
				$status.text($status.data('text-off') || 'Off');
			}
		}

		if (isPowerOn) {
			// Mode
			$item.find('.fan-mode-btn').removeClass('active-mode');
			$item.find(`.fan-mode-btn[data-mode="${device.mode}"]`).addClass('active-mode');

			// Speed
			if (parseInt($speedSlider.val()) !== device.speed) {
				$speedSlider.val(device.speed);
				const prefix = $speedBadge.data('text-level') || '';
				$speedBadge.text(`${prefix} ${device.speed}`.trim());
			}

			// Swing & Light
			const isSwingOn = device.swing === 'ON';
			if ($swingSwitch.is(':checked') !== isSwingOn) $swingSwitch.prop('checked', isSwingOn);

			const isLightOn = device.light === 'ON';
			if ($lightSwitch.is(':checked') !== isLightOn) $lightSwitch.prop('checked', isLightOn);

			this._updateFanStatusText($item);
		}
	}

	// ==========================================
	// UI HELPERS
	// ==========================================

	_updateAcStatusText($acItem, tempOverride) {
		const $statusText = $acItem.find('.ac-status-text');
		const $activeMode = $acItem.find('.ac-mode-btn[class*="active-mode-"]');
		const mode = $activeMode.length ? $activeMode.data('mode') : 'N/A';
		const temp = tempOverride !== undefined ? tempOverride : $acItem.find('.ac-temp-value').text().trim();

		$statusText.text(`${mode} • ${temp}°C`);
	}

	_updateAcFanBadge($badge, speed) {
		if (speed === 0) {
			$badge.text($badge.data('text-auto') || 'Auto');
		} else {
			const levelPrefix = $badge.data('text-level') || 'Level';
			$badge.text(`${levelPrefix} ${speed}`);
		}
	}

	_updateFanStatusText($fanItem) {
		const $status = $fanItem.find('.fan-status-text');
		const $activeMode = $fanItem.find('.fan-mode-btn.active-mode');
		const mode = $activeMode.length ? $activeMode.data('mode') : null;
		const speed = $fanItem.find('.fan-speed-display').text().trim();
		const prefix = $status.data('speed-prefix') || '';

		if (mode && speed) {
			const speedOnly = speed.replace(/[^\d]/g, '');
			$status.text(`${mode} • ${prefix} ${speedOnly}`.trim());
		} else {
			$status.text($status.data('text-on') || 'On');
		}
	}

	// ==========================================
	// UTILITIES
	// ==========================================

	_parseUrlParams() {
		const params = new URLSearchParams(window.location.search);
		const startedAt = params.get('startedAt');
		const endedAt = params.get('endedAt');

		return {
			isMissingParams: !startedAt || !endedAt,
			start: startedAt ? moment(startedAt) : moment().subtract(this.CONFIG.DEFAULT_RANGE_DAYS, 'days'),
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
}

window.RoomDetailPage = RoomDetailPage;
