import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';

export const ActionModal = (() => {
    let bootstrapModal = null;
    const elements = {
        modal: null,
        form: null,
        title: null,
        localId: null,
        executionOrder: null,
        targetId: null,
        targetType: null,
        actionType: null,
        parameterValue: null
    };

    const init = () => {
        elements.modal = document.getElementById('actionModal');
        elements.form = document.getElementById('actionForm');
        elements.title = document.getElementById('modalTitle');
        elements.localId = document.getElementById('actionLocalId');
        elements.executionOrder = document.getElementById('executionOrder');
        elements.targetId = document.getElementById('targetId');
        elements.targetType = document.getElementById('targetType');
        elements.actionType = document.getElementById('actionType');
        elements.parameterValue = document.getElementById('parameterValue');

        if (elements.modal) {
            bootstrapModal = typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
        }

        elements.actionType?.addEventListener('change', () => {
            const val = elements.actionType.value;
            elements.parameterValue.disabled = (val !== 'SET_VALUE');
            if(val !== 'SET_VALUE') elements.parameterValue.value = '';
        });
    };

    const open = (localId = null) => {
        elements.form.reset();
        elements.localId.value = '';
        elements.parameterValue.disabled = true;

        if (localId) {
            const data = StateManager.getAction(localId);
            if (data) {
                elements.title.textContent = 'Edit Action';
                elements.localId.value = data._localId;
                elements.executionOrder.value = data.executionOrder;
                elements.targetId.value = data.targetId;
                elements.targetType.value = data.targetType;
                elements.actionType.value = data.actionType;
                elements.parameterValue.value = data.parameterValue || '';
                if(data.actionType === 'SET_VALUE') elements.parameterValue.disabled = false;
            }
        } else {
            elements.title.textContent = 'Add Action';
            elements.executionOrder.value = StateManager.getActions().length + 1;
        }

        bootstrapModal?.show();
        window.renderIcons?.();
    };

    const submit = (e) => {
        e.preventDefault();
        
        if(!elements.targetId.value) {
            elements.targetId.classList.add('is-invalid');
            return;
        } else {
            elements.targetId.classList.remove('is-invalid');
        }

        const data = {
            executionOrder: parseInt(elements.executionOrder.value, 10),
            targetId: parseInt(elements.targetId.value, 10),
            targetType: elements.targetType.value,
            actionType: elements.actionType.value,
            parameterValue: elements.parameterValue.value || null
        };

        const localId = elements.localId.value;
        if (localId) {
            StateManager.updateAction(localId, data);
        } else {
            StateManager.addAction(data);
        }

        UiRenderer.render();
        bootstrapModal?.hide();
    };

    return { init, open, submit };
})();
