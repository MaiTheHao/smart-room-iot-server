class GroupManager {
	static init(contextPath) {
		this.contextPath = contextPath;
		this.apiClient = new HttpClient(`${contextPath}api/v1/`);
		this.groupService = new GroupApiV1Service(this.apiClient);
		this.table = null;

		this.initDataTable();
		this.bindEvents();
	}

	static initDataTable() {
		this.table = $('#groupsTable').DataTable({
			processing: true,
			serverSide: false,
			ajax: async (data, callback, settings) => {
				try {
					const res = await this.groupService.getAll();
					callback({ data: res.data });
				} catch (error) {
					notify.error('Failed to load groups data');
					callback({ data: [] });
				}
			},
			columns: [
				{ data: 'id' },
				{
					data: 'groupCode',
					render: (data) => `<span class="badge badge-secondary badge-code px-2 py-1">${data}</span>`,
				},
				{ data: 'name', render: (data) => `<strong>${data}</strong>` },
				{ data: 'description', render: (data) => data || '<span class="text-muted font-italic">No description</span>' },
				{
					data: null,
					className: 'text-center',
					render: (data, type, row) =>
						`<span class="badge badge-light border group-count-badge" data-id="${row.id}"><i class="fas fa-spinner fa-spin small text-muted"></i></span>`,
				},
				{ data: null, orderable: false, render: this.renderActions },
			],
			order: [[0, 'asc']],
			language: {
				search: 'Filter Groups:',
				emptyTable: 'No groups available',
			},
			drawCallback: () => this.loadGroupCounts(),
		});
	}

	static renderActions(data, type, row) {
		return `<div class="btn-group btn-group-sm">
                    <button class="btn btn-info action-btn btn-clients" data-id="${row.id}" data-name="${row.name}" title="Manage Members">
                        <i class="fas fa-users"></i>
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
			.on('click', '.btn-clients', (e) => this.openClientsModal($(e.currentTarget)));

		$('#btnSaveCreate').on('click', () => this.handleCreateGroup());
		$('#btnSaveEdit').on('click', () => this.handleUpdateGroup());

		$('#groupClientsTable tbody').on('click', '.btn-remove-client', (e) => this.handleRemoveClient($(e.currentTarget)));

		$('#createGroupModal').on('show.bs.modal', () => {
			const currentLang = document.documentElement.lang || navigator.language || 'vi';
			const langCode = currentLang.toLowerCase().includes('en') ? 'en' : 'vi';
			$('#createLangCode').val(langCode);
		});
	}

	static async loadGroupCounts() {
		const $badges = $('.group-count-badge');
		$badges.each(async (index, el) => {
			const $el = $(el);
			const id = $el.data('id');
			try {
				const count = await this.groupService.getClientCount(id);
				$el.text(count || 0);
				$el.addClass('badge-pill');
			} catch (error) {
				$el.text('-');
			}
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
			await this.groupService.create(data);
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
			const res = await this.groupService.getById(id);
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
			await this.groupService.update(id, data);
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
			await this.groupService.delete(id);
			notify.success('Group deleted successfully');
			this.table.ajax.reload();
		} catch (error) {
			notify.error(error.message || 'Failed to delete group. Ensure it has no members.');
		}
	}

	static openClientsModal($btn) {
		const groupId = $btn.data('id');
		const groupName = $btn.data('name');

		$('#clientGroupId').val(groupId);
		$('#clientModalGroupName').text(groupName);
		$('#manageClientsModal').modal('show');

		this.loadClientsInGroup(groupId);
	}

	static async loadClientsInGroup(groupId) {
		const $loader = $('#clientsLoader');
		const $tbody = $('#groupClientsTable tbody');

		$tbody.empty();
		$loader.removeClass('d-none');

		try {
			const res = await this.groupService.getClients(groupId);
			const clients = res.data;

			if (!clients || clients.length === 0) {
				$tbody.html('<tr><td colspan="4" class="text-center text-muted py-3">No members in this group</td></tr>');
			} else {
				const html = clients
					.map(
						(client) => `
                    <tr>
                        <td>
                            <div class="d-flex align-items-center">
                                <img src="${client.avatarUrl || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(client.username)}" 
                                    class="rounded-circle mr-2" 
                                    style="width:28px;height:28px"
                                    onerror="this.src='https://ui-avatars.com/api/?name=${encodeURIComponent(client.username)}'">
                                <strong>${client.username}</strong>
                            </div>
                        </td>
                        <td>${this.renderClientTypeBadge(client.clientType)}</td>
                        <td class="text-monospace small">${client.ipAddress || '<span class="text-muted">N/A</span>'}</td>
                        <td class="text-center">
                            <button class="btn btn-sm btn-outline-danger btn-remove-client" 
                                data-client-id="${client.id}" 
                                data-username="${client.username}"
                                title="Remove user from group">
                                <i class="fas fa-trash-alt"></i>
                            </button>
                        </td>
                    </tr>
                `,
					)
					.join('');
				$tbody.html(html);
			}
		} catch (error) {
			$tbody.html('<tr><td colspan="4" class="text-center text-danger py-3">Error loading clients</td></tr>');
			notify.error(error.message || 'Failed to load group members');
		} finally {
			$loader.addClass('d-none');
		}
	}

	static renderClientTypeBadge(type) {
		if (type === 'HARDWARE_GATEWAY') {
			return '<span class="badge badge-gateway"><i class="fas fa-network-wired mr-1"></i>Gateway</span>';
		}
		return '<span class="badge badge-user"><i class="fas fa-user mr-1"></i>User</span>';
	}

	static async handleRemoveClient($btn) {
		const clientId = $btn.data('client-id');
		const username = $btn.data('username');
		const groupId = $('#clientGroupId').val();

		const confirmed = await notify.confirm(`Remove Member`, `Are you sure you want to remove user "${username}" from this group?`, 'warning');

		if (!confirmed) return;

		try {
			await this.groupService.removeClient(clientId, groupId);
			notify.success(`Removed ${username} from group`);
			this.loadClientsInGroup(groupId);
			this.table.ajax.reload(null, false);
		} catch (error) {
			notify.error(error.message || 'Failed to remove user from group');
		}
	}
}

window.GroupManager = GroupManager;
