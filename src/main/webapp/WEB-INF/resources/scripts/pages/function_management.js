class FunctionManager {
	static init(contextPath) {
		this.contextPath = contextPath;
		this.apiClient = new HttpClient(`${contextPath}api/v1/`);
		this.table = null;

		this.initDataTable();
		this.bindEvents();
		this.bindCodeBuilder();
	}

	static initDataTable() {
		this.table = $('#functionsTable').DataTable({
			processing: true,
			serverSide: false,
			ajax: {
				url: `${this.contextPath}api/v1/functions/all`,
				dataSrc: 'data',
				error: (xhr) => notify.error('Failed to load functions data'),
			},
			columns: [
				{ data: 'id' },
				{
					data: 'functionCode',
					render: (data) => `<span class="badge badge-code px-2 py-1">${data}</span>`,
				},
				{ data: 'name', render: (data) => `<strong>${data}</strong>` },
				{ data: 'description', render: (data) => data || '<span class="text-muted font-italic">No description</span>' },
				{ data: null, orderable: false, render: this.renderActions },
			],
			order: [[0, 'asc']],
			language: {
				search: 'Filter Functions:',
				emptyTable: 'No functions available',
			},
		});
	}

	static renderActions(data, type, row) {
		return `<div class="btn-group btn-group-sm">
                    <button class="btn btn-warning action-btn btn-edit" data-id="${row.id}" title="Edit Function">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-danger action-btn btn-delete" data-id="${row.id}" data-code="${row.functionCode}" title="Delete Function">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>`;
	}

	static bindEvents() {
		$('#functionsTable tbody')
			.on('click', '.btn-edit', (e) => this.openEditModal($(e.currentTarget).data('id')))
			.on('click', '.btn-delete', (e) => this.handleDeleteFunction($(e.currentTarget)));

		$('#btnSaveCreate').on('click', () => this.handleCreateFunction());
		$('#btnSaveEdit').on('click', () => this.handleUpdateFunction());

		$('#createFunctionModal').on('show.bs.modal', () => {
			const currentLang = document.documentElement.lang || navigator.language || 'vi';
			const langCode = currentLang.toLowerCase().includes('en') ? 'en' : 'vi';
			$('#createLangCode').val(langCode);

			$('#builderScope').val('ALL');
			this.generateCode();
		});
	}

	static bindCodeBuilder() {
		$('#builderType, #builderScope').on('input change', () => this.generateCode());

		$('.builder-suggestion').on('click', (e) => {
			e.preventDefault();
			const val = $(e.currentTarget).text();
			$('#builderScope').val(val);
			this.generateCode();
		});

		$('#createFunctionCode').on('input', function () {
			$(this).val($(this).val().toUpperCase());
		});
	}

	static generateCode() {
		const typePrefix = $('#builderType').val();
		let scope = $('#builderScope').val() || 'ALL';

		scope = scope
			.trim()
			.toUpperCase()
			.replace(/[^A-Z0-9_]/g, '');

		const finalCode = `F_${typePrefix}_${scope}`;
		$('#createFunctionCode').val(finalCode);
	}

	static async handleCreateFunction() {
		const form = $('#createFunctionForm')[0];
		if (!form.checkValidity()) {
			form.reportValidity();
			return;
		}

		let code = $('#createFunctionCode').val().trim().toUpperCase();

		if (!code.startsWith('F_')) {
			notify.error('Function Code must start with "F_"');
			return;
		}

		const data = {
			functionCode: code,
			name: $('#createName').val(),
			description: $('#createDescription').val(),
			langCode: $('#createLangCode').val(),
		};

		try {
			await this.apiClient.post('functions', data);
			notify.success('Function created successfully');
			$('#createFunctionModal').modal('hide');
			form.reset();
			this.table.ajax.reload();
		} catch (error) {
			notify.error(error.message || 'Failed to create function');
		}
	}

	static async openEditModal(id) {
		try {
			const res = await this.apiClient.get(`functions/${id}`);
			const func = res.data;

			$('#editFunctionId').val(func.id);
			$('#editFunctionCode').val(func.functionCode);
			$('#editName').val(func.name);
			$('#editDescription').val(func.description || '');
			$('#editLangCode').val('vi');

			$('#editFunctionModal').modal('show');
		} catch (error) {
			notify.error(error.message || 'Failed to load function details');
		}
	}

	static async handleUpdateFunction() {
		const form = $('#editFunctionForm')[0];
		if (!form.checkValidity()) {
			form.reportValidity();
			return;
		}

		const id = $('#editFunctionId').val();
		const data = {
			name: $('#editName').val(),
			description: $('#editDescription').val(),
			langCode: $('#editLangCode').val(),
		};

		try {
			await this.apiClient.put(`functions/${id}`, data);
			notify.success('Function updated successfully');
			$('#editFunctionModal').modal('hide');
			this.table.ajax.reload();
		} catch (error) {
			notify.error(error.message || 'Failed to update function');
		}
	}

	static async handleDeleteFunction($btn) {
		const id = $btn.data('id');
		const code = $btn.data('code');

		const confirmed = await notify.confirmDelete(`Function: ${code}`);

		if (!confirmed) return;

		try {
			await this.apiClient.delete(`functions/${id}`);
			notify.success('Function deleted successfully');
			this.table.ajax.reload();
		} catch (error) {
			notify.error(error.message || 'Failed to delete function');
		}
	}
}

window.FunctionManager = FunctionManager;
