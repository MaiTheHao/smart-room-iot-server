import { createRule, updateRule } from '../../../../api/rule.api.js';
import { Validator } from '../../../../common/validator.js';

export const RuleModal = (() => {
	const elements = {
		modal: null,
		form: null,
		title: null,
		id: null,
		name: null,
		description: null,
		submitBtn: null,
		inputs: [],
		feedbacks: []
	};

	let bootstrapModal = null;
	const { i18n } = window.__RULE_CONFIG__;

	const init = () => {
		elements.modal = document.getElementById('ruleModal');
		elements.form = document.getElementById('ruleForm');
		elements.title = document.getElementById('modalTitle');
		elements.id = document.getElementById('ruleId');
		elements.name = document.getElementById('name');
		elements.description = document.getElementById('description');
		elements.submitBtn = document.getElementById('btnSubmitRule');

		if (!elements.modal) return;
		bootstrapModal = typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
		elements.inputs = elements.form.querySelectorAll('.form-control');
		elements.feedbacks = elements.form.querySelectorAll('.invalid-feedback');
	};

	const clearValidation = () => {
		elements.inputs.forEach((el) => el.classList.remove('is-invalid'));
		elements.feedbacks.forEach((el) => (el.textContent = ''));
	};

	const open = (data = null) => {
		elements.form.reset();
		elements.id.value = '';
		clearValidation();

		const isEdit = !!data;
		elements.title.textContent = isEdit ? i18n.editTitle : i18n.addTitle;

		if (isEdit) {
			elements.id.value = data.id || '';
			elements.name.value = data.name || '';
			elements.description.value = data.description || '';
		}

		bootstrapModal?.show();
		window.renderIcons?.();
	};

	const validate = () => {
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

		if (!Validator.name.isBlank(data.name)) {
			setError('name', i18n.valRequired.replace('{0}', 'Name'));
		}

		return isValid ? data : null;
	};

	const submit = async (e, onRefresh) => {
		e.preventDefault();
		const data = validate();
		if (!data) return;

		const originalHtml = elements.submitBtn.innerHTML;
		elements.submitBtn.disabled = true;
		elements.submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> ${i18n.processing}`;

		try {
			const isUpdate = !!data.id;
            const payload = {
                name: data.name,
                description: data.description,
            };

            if(!isUpdate) {
                payload.isActive = true;
                payload.priority = 1;
                payload.intervalSeconds = 60;
                payload.conditions = [];
                payload.actions = [];
            }

			const [err, res] = isUpdate ? await updateRule(data.id, payload) : await createRule(payload);

			if (err) {
				Swal.fire(i18n.error, err.message || i18n.error, 'error');
			} else {
				Swal.fire(i18n.success, isUpdate ? i18n.updatedSuccess : i18n.createdSuccess, 'success');
				bootstrapModal?.hide();
				onRefresh();
			}
		} catch (error) {
			console.error('Submit error:', error);
			Swal.fire(i18n.error, i18n.error, 'error');
		} finally {
			elements.submitBtn.disabled = false;
			elements.submitBtn.innerHTML = originalHtml;
		}
	};

	return { init, open, submit };
})();
