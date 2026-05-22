import { Validator } from '../../../common/validator.js';
import { getAllClientsByGroupId } from '../../../api/group.api.js';
import { StateManager } from './state_manager.js';

export const MainForm = (() => {
	const elements = {
		modal: null,
		form: null,
		title: null,
		groupId: null,
		name: null,
		groupCode: null,
		description: null,
		submitBtn: null,
	};

	let bootstrapModal = null;

	const init = () => {
		elements.modal = document.getElementById('groupModal');
		elements.form = document.getElementById('groupForm');
		elements.title = document.getElementById('modalTitle');
		elements.groupId = document.getElementById('groupId');
		elements.name = document.getElementById('name');
		elements.groupCode = document.getElementById('groupCode');
		elements.description = document.getElementById('description');
		elements.submitBtn = document.getElementById('btnSubmitGroup');

		if (elements.modal) {
			bootstrapModal = typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
		}
	};

	const getInputsAndFeedbacks = () => {
		if (!elements.form) return { inputs: [], feedbacks: [] };
		return {
			inputs: elements.form.querySelectorAll('.form-control'),
			feedbacks: elements.form.querySelectorAll('.invalid-feedback'),
		};
	};

	const clearValidation = () => {
		const { inputs, feedbacks } = getInputsAndFeedbacks();
		inputs.forEach((el) => el.classList.remove('is-invalid'));
		feedbacks.forEach((el) => (el.textContent = ''));
	};

	const reset = () => {
		if (elements.form) elements.form.reset();
		if (elements.groupId) elements.groupId.value = '';
		clearValidation();
	};

	const open = (data = null) => {
		reset();
		const isEdit = !!data;
		const i18n = StateManager.getI18n();

		if (elements.title) elements.title.textContent = isEdit ? i18n.editTitle : i18n.addTitle;
		if (elements.groupCode) {
			elements.groupCode.readOnly = isEdit;
			elements.groupCode.parentElement?.classList.toggle('opacity-75', isEdit);
		}

		if (isEdit && data) {
			Object.entries(data).forEach(([key, value]) => {
				const input = elements.form.elements[key];
				if (input) input.value = value ?? '';
			});
			if (elements.groupId) elements.groupId.value = data.id || '';
		}

		bootstrapModal?.show();
		window.renderIcons?.();
	};

	const close = () => bootstrapModal?.hide();

	const validate = () => {
		const i18n = StateManager.getI18n();
		const formData = new FormData(elements.form);
		const data = Object.fromEntries(formData.entries());
		let isValid = true;

		clearValidation();

		const setError = (field, msg) => {
			const input = elements.form.querySelector(`#${field}`);
			const feedback = elements.form.querySelector(`#val-${field}`);
			if (input) input.classList.add('is-invalid');
			if (feedback) feedback.textContent = msg;
			isValid = false;
		};

		// Validate Name
		if (!Validator.name.isBlank(data.name)) {
			setError('name', (i18n.valRequired || '').replace('{0}', i18n.colName || ''));
		} else if (!Validator.name.isLowerMin(data.name) || !Validator.name.isHigherMax(data.name)) {
			setError('name', i18n.valNameLen || '');
		}

		// Validate Code
		if (!data.id) {
			if (!Validator.groupCode.isBlank(data.groupCode)) {
				setError('groupCode', (i18n.valRequired || '').replace('{0}', i18n.colCode || ''));
			} else if (!Validator.groupCode.isHigherMax(data.groupCode)) {
				setError('groupCode', i18n.valCodeLen || '');
			} else if (!Validator.groupCode.isValidFormat(data.groupCode)) {
				setError('groupCode', i18n.valGroupCodeFormat || '');
			}
			if (data.groupCode) data.groupCode = data.groupCode.toUpperCase();
		}

		// Validate Description
		if (data.description && !Validator.description.isHigherMax(data.description)) {
			setError('description', i18n.valDescriptionLen || '');
		}

		if (isValid) {
			return data;
		}
		return null;
	};

	return {
		init,
		open,
		close,
		validate,
		getElements: () => elements,
	};
})();

export const MappingModule = (() => {
	const elements = {
		modal: null,
		title: null,
		list: null,
		loader: null,
		search: null,
	};

	let bootstrapModal = null;
	let currentGroupId = null;
	let currentGroupName = '';

	const init = () => {
		elements.modal = document.getElementById('manageMembersModal');
		elements.title = document.getElementById('mappingModalTitle');
		elements.list = document.getElementById('membersList');
		elements.loader = document.getElementById('membersListLoader');
		elements.search = document.getElementById('memberSearch');

		if (elements.modal) {
			bootstrapModal = typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
		}

		elements.search?.addEventListener('input', (e) => handleSearch(e.target.value));
	};

	const open = async (group) => {
		const i18n = StateManager.getI18n();
		currentGroupId = group.id;
		currentGroupName = group.name;
		if (elements.title) elements.title.textContent = (i18n.mappingTitle || '').replace('{0}', group.name);
		if (elements.list) elements.list.style.display = 'none';
		if (elements.loader) elements.loader.classList.remove('d-none');
		if (elements.search) elements.search.value = '';

		bootstrapModal?.show();
		window.renderIcons?.();

		try {
			const [err, res] = await getAllClientsByGroupId(currentGroupId);
			if (err) throw err;

			const members = res.data || [];
			renderList(members);
		} catch (err) {
			console.error('Failed to load members:', err);
			if (elements.list) elements.list.innerHTML = `<div class="alert alert-danger">${i18n.error || ''}</div>`;
		} finally {
			if (elements.loader) elements.loader.classList.add('d-none');
			if (elements.list) elements.list.style.display = 'block';
			window.renderIcons?.();
		}
	};

	const renderList = (members) => {
		const i18n = StateManager.getI18n();
		if (!elements.list) return;

		if (members.length === 0) {
			elements.list.innerHTML = `<div class="text-center py-4 text-muted">${i18n.noData || ''}</div>`;
			return;
		}

		elements.list.innerHTML = members
			.map(
				(user) => `
			<div class="selection-list-item d-flex align-items-center member-item" data-search="${user.username.toLowerCase()} ${user.clientType.toLowerCase()}">
				<div class="flex-shrink-0">
					<img src="${user.avatarUrl || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(user.username)}" 
						 class="rounded-circle border shadow-sm user-avatar" 
						 onerror="this.src='https://ui-avatars.com/api/?name=${encodeURIComponent(user.username)}'">
				</div>
				<div class="ms-3 flex-grow-1">
					<div class="fw-bold text-dark">${user.username}</div>
					<div class="small text-muted">${user.clientType}</div>
				</div>
				<button class="btn btn-light btn-sm rounded-pill btn-remove-member" 
					data-id="${user.id}" data-username="${user.username}" title="Remove from group">
					<i data-lucide="user-minus" class="lucide-sm text-danger"></i>
				</button>
			</div>
		`,
			)
			.join('');
		window.renderIcons?.(elements.list);
	};

	const handleSearch = (query) => {
		const q = query.toLowerCase();
		if (!elements.list) return;
		const items = elements.list.querySelectorAll('.member-item');
		items.forEach((item) => {
			const text = item.dataset.search;
			item.style.display = text.includes(q) ? 'flex' : 'none';
		});
	};

	return {
		init,
		open,
		getCurrentGroupId: () => currentGroupId,
		getCurrentGroupName: () => currentGroupName,
	};
})();
