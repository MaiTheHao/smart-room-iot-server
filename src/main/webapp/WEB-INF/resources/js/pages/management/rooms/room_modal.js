import { getAllFloors } from '../../../api/floor.api.js';
import { Validator } from '../../../common/validator.js';
import { StateManager } from './state_manager.js';

export const RoomModal = (() => {
  const elements = {
    modal: null,
    form: null,
    title: null,
    roomId: null,
    name: null,
    code: null,
    floorId: null,
    description: null,
    submitBtn: null,
  };

  let bootstrapModal = null;

  const init = () => {
    elements.modal = document.getElementById('roomModal');
    elements.form = document.getElementById('roomForm');
    elements.title = document.getElementById('modalTitle');
    elements.roomId = document.getElementById('roomId');
    elements.name = document.getElementById('name');
    elements.code = document.getElementById('code');
    elements.floorId = document.getElementById('floorId');
    elements.description = document.getElementById('description');
    elements.submitBtn = document.getElementById('btnSubmitRoom');

    if (elements.modal) {
      bootstrapModal =
        typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
    }
  };

  const getInputsAndFeedbacks = () => {
    if (!elements.form) return { inputs: [], feedbacks: [] };
    return {
      inputs: elements.form.querySelectorAll('.form-control, .form-select'),
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
    if (elements.roomId) elements.roomId.value = '';
    clearValidation();
  };

  const loadFloors = async () => {
    const i18n = StateManager.getI18n();
    try {
      const [err, res] = await getAllFloors();
      if (err) throw err;
      const floors = res.data || [];

      if (elements.floorId) {
        elements.floorId.innerHTML = `<option value="">${i18n.valFloorRequired || ''}</option>`;
        floors.forEach((f) => {
          StateManager.setFloorName(f.id, f.name);
          const option = document.createElement('option');
          option.value = f.id;
          option.textContent = f.name;
          elements.floorId.appendChild(option);
        });
      }
    } catch (err) {
      console.error('Load floors error:', err);
    }
  };

  const open = async (data = null) => {
    reset();
    await loadFloors();
    const isEdit = !!data;
    const i18n = StateManager.getI18n();

    if (elements.title) elements.title.textContent = isEdit ? i18n.editTitle : i18n.addTitle;
    if (elements.code) {
      elements.code.readOnly = isEdit;
      elements.code.parentElement?.classList.toggle('opacity-75', isEdit);
    }

    if (isEdit && data) {
      Object.entries(data).forEach(([key, value]) => {
        const input = elements.form.elements[key];
        if (input) input.value = value ?? '';
      });
      if (elements.roomId) elements.roomId.value = data.id || '';
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

    // Validate Description
    if (data.description && !Validator.description.isHigherMax(data.description)) {
      setError('description', i18n.valDescriptionLen || '');
    }

    // Validate Code
    if (!Validator.code.isBlank(data.code)) {
      setError('code', (i18n.valRequired || '').replace('{0}', i18n.colCode || ''));
    } else if (!Validator.code.isHigherMax(data.code)) {
      setError('code', i18n.valCodeLen || '');
    }

    // Validate Floor
    if (!Validator.id.isBlank(data.floorId)) {
      setError('floorId', i18n.valFloorRequired || '');
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
    loadFloors,
    getElements: () => elements,
  };
})();
