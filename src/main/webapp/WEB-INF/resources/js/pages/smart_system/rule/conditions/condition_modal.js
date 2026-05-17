import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';

export const ConditionModal = (() => {
    let bootstrapModal = null;
    const elements = {
        modal: null,
        form: null,
        title: null,
        localId: null,
        sensorId: null,
        metricType: null,
        operator: null,
        threshold: null
    };

    const init = () => {
        elements.modal = document.getElementById('conditionModal');
        elements.form = document.getElementById('conditionForm');
        elements.title = document.getElementById('modalTitle');
        elements.localId = document.getElementById('conditionLocalId');
        elements.sensorId = document.getElementById('sensorId');
        elements.metricType = document.getElementById('metricType');
        elements.operator = document.getElementById('operator');
        elements.threshold = document.getElementById('threshold');

        if (elements.modal) {
            bootstrapModal = typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
        }
    };

    const open = (localId = null) => {
        elements.form.reset();
        elements.localId.value = '';

        if (localId) {
            const data = StateManager.getCondition(localId);
            if (data) {
                elements.title.textContent = 'Edit Condition';
                elements.localId.value = data._localId;
                elements.sensorId.value = data.sensorId;
                elements.metricType.value = data.metricType;
                elements.operator.value = data.operator;
                elements.threshold.value = data.threshold;
            }
        } else {
            elements.title.textContent = 'Add Condition';
        }

        bootstrapModal?.show();
        window.renderIcons?.();
    };

    const submit = (e) => {
        e.preventDefault();
        
        if(!elements.sensorId.value || !elements.threshold.value) {
            return;
        }

        const data = {
            sensorId: parseInt(elements.sensorId.value, 10),
            metricType: elements.metricType.value,
            operator: elements.operator.value,
            threshold: parseFloat(elements.threshold.value)
        };

        const localId = elements.localId.value;
        if (localId) {
            StateManager.updateCondition(localId, data);
        } else {
            StateManager.addCondition(data);
        }

        UiRenderer.render();
        bootstrapModal?.hide();
    };

    return { init, open, submit };
})();
