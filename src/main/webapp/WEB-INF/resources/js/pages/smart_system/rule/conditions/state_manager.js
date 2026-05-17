export const StateManager = (() => {
    let currentConditions = [];
    let isDirty = false;

    const generateLocalId = () => 'local_' + Math.random().toString(36).substr(2, 9);

    const init = (conditionsFromApi) => {
        currentConditions = (conditionsFromApi || []).map(c => ({ ...c, _localId: c.id ? String(c.id) : generateLocalId() }));
        isDirty = false;
        triggerListeners();
    };

    const getConditions = () => [...currentConditions];
    
    const getCondition = (localId) => currentConditions.find(c => c._localId === localId);

    const addCondition = (condition) => {
        condition._localId = generateLocalId();
        currentConditions.push(condition);
        isDirty = true;
        triggerListeners();
    };

    const updateCondition = (localId, updatedData) => {
        const index = currentConditions.findIndex(c => c._localId === localId);
        if (index > -1) {
            currentConditions[index] = { ...currentConditions[index], ...updatedData };
            isDirty = true;
            triggerListeners();
        }
    };

    const deleteCondition = (localId) => {
        currentConditions = currentConditions.filter(c => c._localId !== localId);
        isDirty = true;
        triggerListeners();
    };

    const getPayload = () => {
        return currentConditions.map(c => {
            const copy = { ...c };
            delete copy._localId;
            return copy;
        });
    };

    const listeners = [];
    const subscribe = (fn) => listeners.push(fn);
    const triggerListeners = () => listeners.forEach(fn => fn(isDirty));

    return { init, getConditions, getCondition, addCondition, updateCondition, deleteCondition, getPayload, subscribe };
})();
