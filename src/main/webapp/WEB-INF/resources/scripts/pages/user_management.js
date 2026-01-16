class UserManager {
	constructor(contextPath) {
		this.contextPath = contextPath;
		this.apiClient = new HttpClient(`${contextPath}api/v1/`);
		this.table = null;
		this.roleChanges = {};
		this.init();
	}

	init() {
		this.initDataTable();
		this.bindEvents();
	}

	initDataTable() {
		this.table = $('#usersTable').DataTable({
			processing: true,
			serverSide: false,
			ajax: {
				url: `${this.contextPath}api/v1/clients`,
				dataSrc: 'data.content',
				error: (xhr) => notify.error('Failed to load users data'),
			},
			columns: [
				{ data: 'id' },
				{ data: 'username', render: this.renderAvatar },
				{ data: 'clientType', render: this.renderStatus },
				{ data: 'ipAddress', render: (data) => data || '<span class="text-muted">N/A</span>' },
				{ data: 'macAddress', render: (data) => data || '<span class="text-muted">N/A</span>' },
				{ data: 'lastLoginAt', render: (data) => (data ? `<small>${new Date(data).toLocaleString()}</small>` : '<span class="text-muted">Never</span>') },
				{ data: null, orderable: false, render: this.renderActions },
			],
			order: [[0, 'asc']],
			pageLength: 10,
			lengthMenu: [5, 10, 25, 50, 100],
			language: {
				search: 'Search:',
				lengthMenu: 'Show _MENU_ entries',
				info: 'Showing _START_ to _END_ of _TOTAL_ users',
				infoEmpty: 'No users found',
				infoFiltered: '(filtered from _MAX_ total users)',
				paginate: { first: 'First', last: 'Last', next: 'Next', previous: 'Previous' },
			},
		});
	}

	renderAvatar(data, type, row) {
		const avatar = row.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(data)}`;
		return `<div class="d-flex align-items-center">
                    <img src="${avatar}" class="img-circle elevation-1 mr-2" width="35" height="35" 
                        onerror="this.src='https://ui-avatars.com/api/?name=${encodeURIComponent(data)}'">
                    <strong>${data}</strong>
                </div>`;
	}

	renderStatus(data) {
		return data === 'USER'
			? '<span class="badge badge-user"><i class="fas fa-user mr-1"></i>User</span>'
			: '<span class="badge badge-gateway"><i class="fas fa-network-wired mr-1"></i>Gateway</span>';
	}

	renderActions(data, type, row) {
		return `<div class="btn-group btn-group-sm" role="group">
                    <button class="btn btn-info action-btn btn-manage-roles" data-id="${row.id}" data-username="${row.username}" title="Manage Roles">
                        <i class="fas fa-users"></i>
                    </button>
                    <button class="btn btn-warning action-btn btn-edit-user" data-id="${row.id}" title="Edit User">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-secondary action-btn btn-reset-password" data-id="${row.id}" data-username="${row.username}" title="Reset Password">
                        <i class="fas fa-key"></i>
                    </button>
                    <button class="btn btn-danger action-btn btn-delete-user" data-id="${row.id}" data-username="${row.username}" title="Delete User">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>`;
	}

	bindEvents() {
		$('#filterClientType').on('change', (e) => this.table.column(2).search(e.target.value).draw());
		$('#createUserBtn').on('click', () => this.handleCreateUser());
		$('#saveEditBtn').on('click', () => this.handleUpdateUser());
		$('#saveGroupsBtn').on('click', () => this.handleSaveRoles());

		$('#usersTable tbody')
			.on('click', '.btn-manage-roles', (e) => this.openRolesModal($(e.currentTarget)))
			.on('click', '.btn-edit-user', (e) => this.openEditModal($(e.currentTarget).data('id')))
			.on('click', '.btn-reset-password', (e) => this.handleResetPassword($(e.currentTarget)))
			.on('click', '.btn-delete-user', (e) => this.handleDeleteUser($(e.currentTarget)));
	}

	async handleCreateUser() {
		const form = $('#createUserForm')[0];
		if (!form.checkValidity()) {
			form.reportValidity();
			return;
		}

		const data = {
			username: $('#createUsername').val(),
			password: $('#createPassword').val(),
			clientType: $('#createClientType').val(),
			ipAddress: $('#createIpAddress').val() || null,
			macAddress: $('#createMacAddress').val() || null,
			avatarUrl: $('#createAvatarUrl').val() || null,
		};

		try {
			const res = await this.apiClient.post('clients', data);
			notify.success(res.message || 'User created successfully');
			$('#createUserModal').modal('hide');
			form.reset();
			this.table.ajax.reload();
		} catch (error) {
			notify.error(error.message || 'Failed to create user');
		}
	}

	async openEditModal(id) {
		try {
			const res = await this.apiClient.get(`clients/${id}`);
			const user = res.data;

			$('#editUserId').val(user.id);
			$('#editClientType').val(user.clientType);
			$('#editIpAddress').val(user.ipAddress || '');
			$('#editMacAddress').val(user.macAddress || '');
			$('#editAvatarUrl').val(user.avatarUrl || '');
			$('#editModalTitle').text(`Edit User: ${user.username}`);
			$('#editUserModal').modal('show');
		} catch (error) {
			notify.error(error.message || 'Failed to load user data');
		}
	}

	async handleUpdateUser() {
		const form = $('#editUserForm')[0];
		if (!form.checkValidity()) {
			form.reportValidity();
			return;
		}

		const userId = $('#editUserId').val();
		if (!userId) return notify.error('User ID is missing');

		const data = {
			clientType: $('#editClientType').val(),
			ipAddress: $('#editIpAddress').val() || null,
			macAddress: $('#editMacAddress').val() || null,
			avatarUrl: $('#editAvatarUrl').val() || null,
		};

		try {
			const res = await this.apiClient.put(`clients/${userId}`, data);
			notify.success(res.message || 'User updated successfully');
			$('#editUserModal').modal('hide');
			this.table.ajax.reload();
		} catch (error) {
			notify.error(error.message || 'Failed to update user');
		}
	}

	async handleResetPassword($btn) {
		const id = $btn.data('id');
		const username = $btn.data('username');
		const newPassword = await notify.prompt(`Reset Password for "${username}"`, 'Enter new password (min 6 chars):', 'password', 'New password...');

		if (!newPassword) return;
		if (newPassword.length < 6) return notify.error('Password must be at least 6 characters');

		try {
			await this.apiClient.put(`clients/${id}`, { password: newPassword });
			notify.success('Password reset successfully');
		} catch (error) {
			notify.error(error.message || 'Failed to reset password');
		}
	}

	async handleDeleteUser($btn) {
		const id = $btn.data('id');
		const username = $btn.data('username');
		const confirmed = await notify.confirmDelete(username);

		if (!confirmed) return;

		try {
			await this.apiClient.delete(`clients/${id}`);
			notify.success('User deleted successfully');
			this.table.ajax.reload();
		} catch (error) {
			notify.error(error.message || 'Failed to delete user');
		}
	}

	openRolesModal($btn) {
		const clientId = $btn.data('id');
		const username = $btn.data('username');

		this.roleChanges = {};
		$('#groupClientId').val(clientId);
		$('#groupModalUsername').text(username);
		$('#manageGroupsModal').modal('show');
		this.loadRolesForClient(clientId);
	}

	async loadRolesForClient(clientId) {
		const $list = $('#groupsList');
		$list.html('<div class="text-center py-3"><div class="spinner-border text-primary"></div></div>');

		try {
			const res = await this.apiClient.get(`groups/with-client-status/${clientId}`);
			const groups = res.data;

			if (!groups.length) {
				$list.html('<div class="alert alert-warning m-0">No roles available</div>');
				return;
			}

			const html = groups
				.map(
					(group) => `
                <div class="selection-list-item">
                    <div class="custom-control custom-checkbox">
                        <input type="checkbox" class="custom-control-input role-checkbox scale-checkbox" 
                            id="group_${group.id}" 
                            data-group-id="${group.id}" 
                            data-initial-state="${group.isAssignedToClient}"
                            ${group.isAssignedToClient ? 'checked' : ''}>
                        <label class="custom-control-label" for="group_${group.id}">
                            <strong>${group.name}</strong> <span class="badge badge-secondary">${group.groupCode}</span>
                            ${group.description ? `<br><small class="text-muted">${group.description}</small>` : ''}
                        </label>
                    </div>
                </div>
            `,
				)
				.join('');

			$list.html(html);
			this.bindRoleCheckboxEvents($list);
		} catch (error) {
			$list.html('<div class="alert alert-danger m-0">Failed to load roles</div>');
			notify.error(error.message || 'Failed to load roles');
		}
	}

	bindRoleCheckboxEvents($container) {
		$container.off('change', '.role-checkbox').on('change', '.role-checkbox', (e) => {
			const $cb = $(e.currentTarget);
			const groupId = $cb.data('group-id');
			const isChecked = $cb.is(':checked');
			const initialState = $cb.data('initial-state') === true;

			if (isChecked !== initialState) {
				this.roleChanges[groupId] = isChecked;
			} else {
				delete this.roleChanges[groupId];
			}
		});
	}

	async handleSaveRoles() {
		const clientId = parseInt($('#groupClientId').val());
		if (!clientId) return notify.error('Client ID is missing');

		const changes = Object.keys(this.roleChanges);
		if (!changes.length) {
			notify.info('No changes to save');
			$('#manageGroupsModal').modal('hide');
			return;
		}

		const toAssign = [];
		const toUnassign = [];

		changes.forEach((groupId) => {
			const id = parseInt(groupId);
			this.roleChanges[groupId] ? toAssign.push(id) : toUnassign.push(id);
		});

		const promises = [];
		if (toAssign.length) promises.push(this.apiClient.post('roles/clients/groups/assign', { clientId, groupIds: toAssign }));
		if (toUnassign.length) promises.push(this.apiClient.post('roles/clients/groups/unassign', { clientId, groupIds: toUnassign }));

		try {
			await Promise.all(promises);
			notify.success('Role assignments updated successfully');
			$('#manageGroupsModal').modal('hide');
			this.roleChanges = {};
		} catch (error) {
			notify.error(error.message || 'Failed to update role assignments');
		}
	}
}
