export const StateManager = (() => {
    let currentActions = [];
    let isDirty = false;

    const generateLocalId = () => 'local_' + Math.random().toString(36).substr(2, 9);

    const reindex = () => {
        currentActions.forEach((a, i) => {
            a.executionOrder = i;
        });
    };

    const init = (actionsFromApi) => {
        currentActions = (actionsFromApi || [])
            .slice()
            .sort((a, b) => a.executionOrder - b.executionOrder)
            .map(a => ({ ...a, _localId: generateLocalId() }));
        isDirty = false;
        triggerListeners();
    };

    const getActions = () => [...currentActions];

    const getAction = (localId) => currentActions.find(a => a._localId === localId);

    const addAction = (action) => {
        action._localId = generateLocalId();
        currentActions.push(action);
        currentActions.sort((a, b) => a.executionOrder - b.executionOrder);
        reindex();
        isDirty = true;
        triggerListeners();
    };

    const updateAction = (localId, updatedData) => {
        const index = currentActions.findIndex(a => a._localId === localId);
        if (index > -1) {
            currentActions[index] = { ...currentActions[index], ...updatedData };
            currentActions.sort((a, b) => a.executionOrder - b.executionOrder);
            reindex();
            isDirty = true;
            triggerListeners();
        }
    };

    const deleteAction = (localId) => {
        currentActions = currentActions.filter(a => a._localId !== localId);
        reindex();
        isDirty = true;
        triggerListeners();
    };

    const buildPayload = () => {
        return currentActions.map((a, i) => ({
            executionOrder: i,
            targetDeviceId: Number(a.targetDeviceId),
            targetDeviceCategory: a.targetDeviceCategory,
            actionParams: typeof a.actionParams === 'string'
                ? JSON.parse(a.actionParams)
                : (a.actionParams || {}),
        }));
    };

    const getIsDirty = () => isDirty;

    const listeners = [];
    const subscribe = (fn) => listeners.push(fn);
    const triggerListeners = () => listeners.forEach(fn => fn(isDirty));

    return {
        init,
        getActions,
        getAction,
        addAction,
        updateAction,
        deleteAction,
        buildPayload,
        getIsDirty,
        subscribe,
    };
})();
