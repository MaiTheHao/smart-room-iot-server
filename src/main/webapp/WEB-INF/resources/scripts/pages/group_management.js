class GroupManager {
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
                    <button class="btn btn-info action-btn btn-functions" data-id="${row.id}" data-code="${row.groupCode}" title="Manage Functions">
                        <i class="fas fa-tasks"></i>
                    </button>
                    <button class="btn btn-warning action-btn btn-edit" data-id="${row.id}" title="Edit Group">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-danger action-btn btn-delete" data-id="${row.id}" data-code="${row.groupCode}" title="Delete Group">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>`;
	}

	static bindEvents() {
		$('#groupsTable tbody')
			.on('click', '.btn-edit', (e) => this.openEditModal($(e.currentTarget).data('id')))
			.on('click', '.btn-delete', (e) => this.handleDeleteGroup($(e.currentTarget)))
			.on('click', '.btn-functions', (e) => this.openFunctionsModal($(e.currentTarget)));

		$('#btnSaveCreate').on('click', () => this.handleCreateGroup());
		$('#btnSaveEdit').on('click', () => this.handleUpdateGroup());
		$('#btnSaveFunctions').on('click', () => this.handleSaveFunctions());

		$('#createGroupModal').on('show.bs.modal', () => {
			const currentLang = document.documentElement.lang || navigator.language || 'vi';
			const langCode = currentLang.toLowerCase().includes('en') ? 'en' : 'vi';
			$('#createLangCode').val(langCode);
		});
	}

	static async handleCreateGroup() {
		const form = $('#createGroupForm')[0];
		if (!form.checkValidity()) {
			form.reportValidity();
			return;
		}

		const data = {
			groupCode: $('#createGroupCode').val().toUpperCase(),
			name: $('#createName').val(),
			description: $('#createDescription').val(),
			langCode: $('#createLangCode').val(),
		};

		try {
			await this.apiClient.post('groups', data);
			notify.success('Group created successfully');
			$('#createGroupModal').modal('hide');
			form.reset();
			this.table.ajax.reload();
		} catch (error) {
			notify.error(error.message || 'Failed to create group');
		}
	}

	static async openEditModal(id) {
		try {
			const res = await this.apiClient.get(`groups/${id}`);
			const group = res.data;

			$('#editGroupId').val(group.id);
			$('#editGroupCode').val(group.groupCode);
			$('#editName').val(group.name);
			$('#editDescription').val(group.description || '');
			$('#editLangCode').val('vi');

			$('#editGroupModal').modal('show');
		} catch (error) {
			notify.error(error.message || 'Failed to load group details');
		}
	}

	static async handleUpdateGroup() {
		const form = $('#editGroupForm')[0];
		if (!form.checkValidity()) {
			form.reportValidity();
			return;
		}

		const id = $('#editGroupId').val();
		const data = {
			name: $('#editName').val(),
			description: $('#editDescription').val(),
			langCode: $('#editLangCode').val(),
		};

		try {
			await this.apiClient.put(`groups/${id}`, data);
			notify.success('Group updated successfully');
			$('#editGroupModal').modal('hide');
			this.table.ajax.reload();
		} catch (error) {
			notify.error(error.message || 'Failed to update group');
		}
	}

	static async handleDeleteGroup($btn) {
		const id = $btn.data('id');
		const code = $btn.data('code');
		const confirmed = await notify.confirmDelete(`Group: ${code}`);

		if (!confirmed) return;

		try {
			await this.apiClient.delete(`groups/${id}`);
			notify.success('Group deleted successfully');
			this.table.ajax.reload();
		} catch (error) {
			notify.error(error.message || 'Failed to delete group');
		}
	}

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
                    <div class="function-list-item d-flex align-items-start">
                        <div class="custom-control custom-checkbox pt-1">
                            <input type="checkbox" class="custom-control-input function-chk" 
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
                                <span class="badge badge-light border code-badge text-monospace text-muted small">${func.functionCode}</span>
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

window.GroupManager = GroupManager;
