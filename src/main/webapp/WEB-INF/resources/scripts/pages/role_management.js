class RoleManager {
	static init(contextPath) {
		this.contextPath = contextPath;
		this.apiClient = new HttpClient(`${contextPath}api/v1/`);
		this.table = null;
		this.functionChanges = {};

		this.initDataTable();
		this.bindEvents();
	}

	static initDataTable() {
		this.table = $('#groupsTable').DataTable({
			processing: true,
			serverSide: false,
			ajax: {
				url: `${this.contextPath}api/v1/groups/all`,
				dataSrc: 'data',
				error: (xhr) => notify.error('Failed to load groups data'),
			},
			columns: [
				{ data: 'id' },
				{
					data: 'groupCode',
					render: (data) => `<span class="badge badge-secondary badge-code px-2 py-1">${data}</span>`,
				},
				{ data: 'name', render: (data) => `<strong>${data}</strong>` },
				{ data: 'description', render: (data) => data || '<span class="text-muted font-italic">No description</span>' },
				{ data: null, orderable: false, render: this.renderActions },
			],
			order: [[0, 'asc']],
			language: {
				search: 'Filter Groups:',
				emptyTable: 'No groups available',
			},
		});
	}

	static renderActions(data, type, row) {
		return `<div class="btn-group btn-group-sm">
                    <button class="btn btn-info action-btn btn-functions" data-id="${row.id}" data-code="${row.groupCode}" title="Manage Permissions">
                        <i class="fas fa-shield-alt"></i> Permissions
                    </button>
                </div>`;
	}

	static bindEvents() {
		$('#groupsTable tbody').on('click', '.btn-functions', (e) => this.openFunctionsModal($(e.currentTarget)));

		$('#btnSaveFunctions').on('click', () => this.handleSaveFunctions());
	}

	// --- PERMISSION MANAGEMENT (Functions Assignment) ---

	static openFunctionsModal($btn) {
		const groupId = $btn.data('id');
		const groupCode = $btn.data('code');

		this.functionChanges = {};
		$('#funcGroupId').val(groupId);
		$('#funcModalGroupCode').text(groupCode);
		$('#functionsList').empty();
		$('#manageFunctionsModal').modal('show');

		this.loadFunctions(groupId);
	}

	static async loadFunctions(groupId) {
		const $loader = $('#functionsListLoader');
		const $list = $('#functionsList');

		$loader.removeClass('d-none');
		$list.hide();

		try {
			const res = await this.apiClient.get(`functions/with-group-status/${groupId}`);
			const functions = res.data;

			if (!functions || functions.length === 0) {
				$list.html('<div class="text-center text-muted py-3">No functions available in system.</div>');
			} else {
				const html = functions
					.map(
						(func) => `
                    <div class="selection-list-item d-flex align-items-start">
                        <div class="custom-control custom-checkbox pt-1">
                            <input type="checkbox" class="custom-control-input function-chk scale-checkbox" 
                                id="func_${func.id}" 
                                data-code="${func.functionCode}"
                                data-initial="${func.isAssignedToGroup}"
                                ${func.isAssignedToGroup ? 'checked' : ''}>
                            <label class="custom-control-label" for="func_${func.id}"></label>
                        </div>
                        <div class="ml-2 w-100">
                            <div class="d-flex justify-content-between">
                                <label class="mb-0 cursor-pointer font-weight-bold" for="func_${func.id}">
                                    ${func.name}
                                </label>
                                <span class="badge badge-light border badge-code text-muted small">${func.functionCode}</span>
                            </div>
                            <div class="small text-muted">${func.description || ''}</div>
                        </div>
                    </div>
                `,
					)
					.join('');
				$list.html(html);
				this.bindFunctionCheckboxes();
			}
		} catch (error) {
			$list.html('<div class="alert alert-danger">Failed to load functions list.</div>');
			notify.error(error.message);
		} finally {
			$loader.addClass('d-none');
			$list.fadeIn();
		}
	}

	static bindFunctionCheckboxes() {
		$('.function-chk')
			.off('change')
			.on('change', (e) => {
				const $chk = $(e.currentTarget);
				const code = $chk.data('code');
				const isChecked = $chk.is(':checked');
				const initialState = $chk.data('initial') === true;

				if (isChecked !== initialState) {
					this.functionChanges[code] = isChecked;
				} else {
					delete this.functionChanges[code];
				}
			});
	}

	static async handleSaveFunctions() {
		const groupId = $('#funcGroupId').val();
		const changesMap = this.functionChanges;

		if (Object.keys(changesMap).length === 0) {
			notify.info('No changes detected.');
			$('#manageFunctionsModal').modal('hide');
			return;
		}

		const payload = {
			groupId: parseInt(groupId),
			functionToggles: changesMap,
		};

		try {
			const res = await this.apiClient.post('roles/groups/functions/toggle', payload);

			const { addedCount, removedCount } = res.data || {};
			notify.success(res.data?.message || 'Permissions updated successfully');

			$('#manageFunctionsModal').modal('hide');
		} catch (error) {
			notify.error(error.message || 'Failed to save permissions');
		}
	}
}

window.RoleManager = RoleManager;
