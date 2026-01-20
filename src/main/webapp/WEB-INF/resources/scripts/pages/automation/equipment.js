class EquipmentManager {
	static init(contextPath) {
		this.contextPath = contextPath;
		this.httpClient = new HttpClient(`${contextPath}api/v1/`);

		this.automationService = new AutomationApiService(this.httpClient);
		this.floorService = new FloorApiV1Service(this.httpClient);

		// Initialize services
		this.initServices();
		this.initDataTable();
		this.bindEvents();
	}

	static initServices() {
		// Temporary simple services for Room and Light if not existing in separate files
		this.roomService = {
			client: this.httpClient,
			getByFloor: async (floorId) => {
				const response = await this.httpClient.get(`floors/${floorId}/rooms?size=1000`);
				return (response && response?.data?.content) || [];
			},
		};

		this.lightService = {
			client: this.httpClient,
			getByRoom: async (roomId) => {
				const response = await this.httpClient.get(`lights/room/${roomId}?size=1000`);
				return (response && response?.data?.content) || [];
			},
		};
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
			order: [[0, 'asc']], // Order by 'Order' column
			columnDefs: [
				{ orderable: false, targets: 5 }, // Disable sorting on Actions column
			],
			language: {
				emptyTable: 'No actions configured for this job',
			},
		});
	}

	static bindEvents() {
		$('#btnCreateAction').on('click', () => this.openModal());
		$('#btnSaveEquipment').on('click', () => this.handleSave());

		// Data Table Action Buttons
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

		// Cascading Dropdowns
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

			// In edit mode, we can't easily change the target hierarchies
			// without complex logic (pre-fetching everything).
			// So we display readonly target info.
			$('#group-target-selection').hide();
			$('#group-target-display').show();
			$('#targetNameDisplay').val(data.targetName);

			// Populate hidden fields if needed or just use current values
			// Note: The backend DTO for update might need targetId too?
			// Check UpdateAutomationActionDto: targetType, targetId, actionType, parameterValue, executionOrder
			// We need to keep targetId in a way that we can submit it.
			// But wait, UpdateDto allows changing targetId?
			// The UI design says "To change device, please delete and create new"

			// We need to store targetId somewhere so update picks it up
			// Or create a hidden input for it that persists the old value
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

			// Load floors if empty
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
			const response = await this.floorService.getAll();
			const $select = $('#floorSelect');
			$select.empty().append('<option value="" disabled selected>Select Floor</option>');
			// response is the body. The list is in response.data.content based on API structure
			const floors = (response && response.content) || [];

			if (floors && floors.length > 0) {
				floors.forEach((floor) => {
					$select.append(`<option value="${floor.id}">${floor.name} (Level ${floor.level})</option>`);
				});
			}
		} catch (error) {
			console.error(error);
			toastr.error('Failed to load floors');
		}
	}

	static async loadRooms(floorId) {
		if (!floorId) return;
		try {
			const $select = $('#roomSelect');
			$select.prop('disabled', true).html('<option>Loading...</option>');

			const rooms = await this.roomService.getByFloor(floorId);
			$select.empty().append('<option value="" disabled selected>Select Room</option>');

			if (rooms) {
				rooms.forEach((room) => {
					$select.append(`<option value="${room.id}">${room.name}</option>`);
				});
			}
			$select.prop('disabled', false);

			// Reset devices
			$('#targetId').html('<option value="" disabled selected>Select Device</option>').prop('disabled', true);
		} catch (error) {
			toastr.error('Failed to load rooms');
			$('#roomSelect').prop('disabled', false).html('<option value="" disabled selected>Select Room</option>');
		}
	}

	static async loadDevices(roomId) {
		if (!roomId) return;
		try {
			const $select = $('#targetId');
			$select.prop('disabled', true).html('<option>Loading...</option>');

			// Currently only supporting LIGHT as per requirement
			// If target type requires switching, we'd need logic here.
			// Assuming TargetType is LIGHT fixed in hidden input

			const lights = await this.lightService.getByRoom(roomId);
			$select.empty().append('<option value="" disabled selected>Select Device</option>');

			if (lights) {
				lights.forEach((light) => {
					$select.append(`<option value="${light.id}">${light.name || 'Light: ' + light.code}</option>`);
				});
			}
			$select.prop('disabled', false);
		} catch (error) {
			toastr.error('Failed to load devices');
			$('#targetId').prop('disabled', false).html('<option value="" disabled selected>Select Device</option>');
		}
	}

	static async handleSave() {
		const actionId = $('#actionId').val();
		const automationId = $('#automationId').val();

		// If Edit mode, we might use hiddenTargetId, else use select value
		const targetId = actionId ? $('#hiddenTargetId').val() : $('#targetId').val();

		const dto = {
			automationId: parseInt(automationId),
			targetType: $('#targetType').val(),
			targetId: parseInt(targetId),
			actionType: $('#actionType').val(),
			parameterValue: $('#parameterValue').val() || ' ', // Handle empty string if backend requires not blank
			executionOrder: parseInt($('#executionOrder').val()),
		};

		// Simple Validation
		if (!dto.targetId || isNaN(dto.targetId)) {
			toastr.warning('Please select a target device');
			return;
		}

		try {
			const $btn = $('#btnSaveEquipment');
			$btn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin mr-1"></i> Saving...');

			if (actionId) {
				// Update
				// The backend update DTO might not contain automationId, but it's safe to send
				await this.automationService.updateAction(actionId, dto);
				toastr.success('Action updated successfully');
			} else {
				// Create
				await this.automationService.addAction(automationId, dto);
				toastr.success('Action added successfully');
			}

			$('#equipmentModal').modal('hide');
			setTimeout(() => window.location.reload(), 500); // Reload to refresh table (SSR)
		} catch (error) {
			toastr.error('Failed to save action');
			console.error(error);
		} finally {
			$('#btnSaveEquipment').prop('disabled', false).html('<i class="fas fa-save mr-1"></i> Save Changes');
		}
	}

	static async handleDelete(id) {
		if (!confirm('Are you sure you want to delete this action?')) return;

		try {
			await this.automationService.removeAction(id);
			toastr.success('Action deleted successfully');
			// Remove row from table for better UX or reload
			window.location.reload();
		} catch (error) {
			toastr.error('Failed to delete action');
		}
	}
}

window.EquipmentManager = EquipmentManager;
