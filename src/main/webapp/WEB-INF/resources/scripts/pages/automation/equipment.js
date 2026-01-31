class EquipmentManager {
	static init() {
		if (typeof window === 'undefined') throw new Error('EquipmentManager can only be initialized in a browser environment');

		this.automationService = window.automationApiV1Service;
		this.floorService = window.floorApiV1Service;
		this.roomApiService = window.roomApiV1Service;
		this.lightService = window.lightApiV1Service;
		this.acService = window.airConditionApiV1Service;

		this.initDataTable();
		this.bindEvents();
	}

	static initDataTable() {
		this.table = $('#actionsTable').DataTable({
			paging: true,
			lengthChange: false,
			searching: true,
			ordering: true,
			info: true,
			autoWidth: false,
			responsive: true,
			order: [[0, 'asc']],
			columnDefs: [{ orderable: false, targets: 5 }],
			language: {
				emptyTable: 'No actions configured for this job',
			},
		});
	}

	static bindEvents() {
		$('#btnCreateAction').on('click', () => this.openModal());
		$('#btnSaveEquipment').on('click', () => this.handleSave());

		$('#actionsTable tbody').on('click', '.btn-edit', (e) => {
			const $btn = $(e.currentTarget);
			const data = {
				id: $btn.data('id'),
				targetId: $btn.data('target-id'),
				targetName: $btn.data('target-name'),
				targetType: $btn.data('target-type'),
				actionType: $btn.data('action-type'),
				parameterValue: $btn.data('parameter'),
				executionOrder: $btn.data('order'),
			};
			this.openModal(data);
		});

		$('#actionsTable tbody').on('click', '.btn-delete', (e) => this.handleDelete($(e.currentTarget).data('id')));

		$('#floorSelect').on('change', (e) => this.loadRooms(e.target.value));
		$('#roomSelect').on('change', (e) => this.loadDevices(e.target.value));
	}

	static async openModal(data = null) {
		const isEdit = !!data;
		const $form = $('#equipmentForm');
		$form[0].reset();
		$('#actionId').val('');

		if (isEdit) {
			$('#equipmentModalTitle span').text('Edit Action');
			$('#actionId').val(data.id);
			$('#targetType').val(data.targetType);
			$('#actionType').val(data.actionType);
			$('#executionOrder').val(data.executionOrder);
			$('#parameterValue').val(data.parameterValue || '');

			$('#group-target-selection').hide();
			$('#group-target-display').show();
			$('#targetNameDisplay').val(data.targetName);

			if ($('#hiddenTargetId').length === 0) {
				$form.append(`<input type="hidden" id="hiddenTargetId" value="${data.targetId}">`);
			} else {
				$('#hiddenTargetId').val(data.targetId);
			}
		} else {
			$('#equipmentModalTitle span').text('Add New Action');
			$('#group-target-selection').show();
			$('#group-target-display').hide();
			$('#hiddenTargetId').remove();

			if ($('#floorSelect option').length <= 1) {
				await this.loadFloors();
			}

			$('#roomSelect').html('<option value="" disabled selected>Select Room</option>').prop('disabled', true);
			$('#targetId').html('<option value="" disabled selected>Select Device</option>').prop('disabled', true);
		}

		$('#equipmentModal').modal('show');
	}

	static async loadFloors() {
		try {
			const response = await this.floorService.getAllWithoutPagination();
			const $select = $('#floorSelect');
			$select.empty().append('<option value="" disabled selected>Select Floor</option>');
			const floors = response?.data || [];

			if (floors && floors.length > 0) {
				floors.forEach((floor) => {
					$select.append(`<option value="${floor.id}">${floor.name} (Level ${floor.level})</option>`);
				});
			}
		} catch (error) {
			console.error(error);
			notify.error('Failed to load floors');
		}
	}

	static async loadRooms(floorId) {
		if (!floorId) return;
		try {
			const $select = $('#roomSelect');
			$select.prop('disabled', true).html('<option>Loading...</option>');

			const response = await this.roomApiService.getAllByFloor(floorId);
			const rooms = response?.data || [];
			$select.empty().append('<option value="" disabled selected>Select Room</option>');

			if (rooms) {
				rooms.forEach((room) => {
					$select.append(`<option value="${room.id}">${room.name}</option>`);
				});
			}
			$select.prop('disabled', false);

			$('#targetId').html('<option value="" disabled selected>Select Device</option>').prop('disabled', true);
		} catch (error) {
			notify.error('Failed to load rooms');
			$('#roomSelect').prop('disabled', false).html('<option value="" disabled selected>Select Room</option>');
		}
	}

	static async loadDevices(roomId) {
		if (!roomId) return;
		const devices = [];

		try {
			const $select = $('#targetId');
			$select.prop('disabled', true).html('<option><i class="fas fa-spinner fa-spin"></i></option>');

			let lights = [];
			let airConditions = [];

			// Load lights
			try {
				const lightResponse = await this.lightService.getAllByRoom(roomId);
				lights = lightResponse?.data || [];
			} catch (error) {
				console.error('Failed to load lights:', error);
			}

			// Load air conditioners
			try {
				const acResponse = await this.acService.getAllByRoom(roomId);
				airConditions = acResponse?.data || [];
			} catch (error) {
				console.error('Failed to load air conditioners:', error);
			}

			$select.empty().append('<option value="" disabled selected>Select Device</option>');

			if (lights && lights.length > 0) {
				lights.forEach((light) => {
					$select.append(`<option data-category="LIGHT" value="${light.id}">${light.name || 'Light: ' + light.naturalId}</option>`);
				});
			}

			if (airConditions && airConditions.length > 0) {
				airConditions.forEach((ac) => {
					$select.append(`<option data-category="AIR_CONDITION" value="${ac.id}">${ac.name || 'AC: ' + ac.naturalId}</option>`);
				});
			}

			$select.prop('disabled', false);
		} catch (error) {
			notify.error('Failed to load devices');
			$('#targetId').prop('disabled', false).html('<option value="" disabled selected>Select Device</option>');
		}
	}

	static async handleSave() {
		const $btn = $('#btnSaveEquipment');
		const $targetSelect = $('#targetId');
		const actionId = $('#actionId').val();
		const automationId = $('#automationId').val();
		const executionOrder = parseInt($('#executionOrder').val());
		const targetId = actionId ? $('#hiddenTargetId').val() : $targetSelect.val();

		const dto = {
			targetType: $targetSelect.find('option:selected').data('category'),
			targetId: parseInt(targetId),
			actionType: $('#actionType').val(),
			parameterValue: $('#parameterValue').val()?.trim() || ' ',
			executionOrder: executionOrder,
		};

		if (!dto.targetId || isNaN(dto.targetId)) {
			notify.warning('Please select a target device');
			return;
		}

		if (!isNaN(executionOrder) && (executionOrder < 1 || executionOrder > 1000)) {
			notify.warning('Execution order must be between 1 and 1000');
			return;
		}

		try {
			$btn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-1"></i> Saving...');

			if (actionId) {
				await this.automationService.updateAction(actionId, dto);
				notify.success('Action updated successfully');
			} else {
				await this.automationService.addAction(automationId, dto);
				notify.success('Action added successfully');
			}

			$('#equipmentModal').modal('hide');
			setTimeout(() => window.location.reload(), 500);
		} catch (error) {
			notify.error(error.response?.data?.message || 'Failed to save action');
			console.error(error);
		} finally {
			$btn.prop('disabled', false).html('<i class="fas fa-save mr-1"></i> Save Changes');
		}
	}

	static async handleDelete(id) {
		if (!(await notify.confirm('Delete Action', 'Are you sure you want to delete this action?'))) return;

		try {
			await this.automationService.removeAction(id);
			notify.success('Action deleted successfully');
			window.location.reload();
		} catch (error) {
			notify.error('Failed to delete action');
		}
	}
}

window.EquipmentManager = EquipmentManager;
