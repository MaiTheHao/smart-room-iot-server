import { Validator } from '../../../common/validator.js';
import { StateManager } from './state_manager.js';

export const FunctionModal = (() => {
	const elements = {
		modal: null,
		form: null,
		title: null,
		functionId: null,
		name: null,
		functionCode: null,
		description: null,
		submitBtn: null,
	};

	let bootstrapModal = null;

	const init = () => {
		elements.modal = document.getElementById('functionModal');
		elements.form = document.getElementById('functionForm');
		elements.title = document.getElementById('modalTitle');
		elements.functionId = document.getElementById('functionId');
		elements.name = document.getElementById('name');
		elements.functionCode = document.getElementById('functionCode');
		elements.description = document.getElementById('description');
		elements.submitBtn = document.getElementById('btnSubmitFunction');

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
		if (elements.functionId) elements.functionId.value = '';
		clearValidation();
	};

	const open = (data = null) => {
		reset();
		const isEdit = !!data;
		const i18n = StateManager.getI18n();

		if (elements.title) elements.title.textContent = isEdit ? i18n.editTitle : i18n.addTitle;
		if (elements.functionCode) {
			elements.functionCode.readOnly = isEdit;
			elements.functionCode.parentElement?.classList.toggle('opacity-75', isEdit);
		}

		if (isEdit && data) {
			Object.entries(data).forEach(([key, value]) => {
				const input = elements.form.elements[key];
				if (input) input.value = value ?? '';
			});
			if (elements.functionId) elements.functionId.value = data.id || '';
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
			if (!Validator.functionCode.isBlank(data.functionCode)) {
				setError('functionCode', (i18n.valRequired || '').replace('{0}', i18n.colCode || ''));
			} else if (!Validator.functionCode.isHigherMax(data.functionCode)) {
				setError('functionCode', i18n.valCodeLen || '');
			} else if (!Validator.functionCode.isValidFormat(data.functionCode)) {
				setError('functionCode', i18n.valFunctionCodeFormat || '');
			}
			if (data.functionCode) data.functionCode = data.functionCode.toUpperCase();
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
