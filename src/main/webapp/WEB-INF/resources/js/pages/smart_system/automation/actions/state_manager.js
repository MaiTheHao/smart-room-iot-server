export const StateManager = (() => {
    let originalActions = [];
    let currentActions = [];
    let deletedActionIds = [];
    let isDirty = false;

    const generateLocalId = () => 'local_' + Math.random().toString(36).substr(2, 9);

    const init = (actionsFromApi) => {
        originalActions = JSON.parse(JSON.stringify(actionsFromApi));
        currentActions = actionsFromApi.map(a => ({ ...a, _localId: a.id ? String(a.id) : generateLocalId() }));
        deletedActionIds = [];
        isDirty = false;
        triggerListeners();
    };

    const getActions = () => [...currentActions].sort((a, b) => a.executionOrder - b.executionOrder);
    
    const getAction = (localId) => currentActions.find(a => a._localId === localId);

    const addAction = (action) => {
        action._localId = generateLocalId();
        currentActions.push(action);
        isDirty = true;
        triggerListeners();
    };

    const updateAction = (localId, updatedData) => {
        const index = currentActions.findIndex(a => a._localId === localId);
        if (index > -1) {
            currentActions[index] = { ...currentActions[index], ...updatedData };
            isDirty = true;
            triggerListeners();
        }
    };

    const deleteAction = (localId) => {
        const action = getAction(localId);
        if (action && action.id) {
            deletedActionIds.push(action.id);
        }
        currentActions = currentActions.filter(a => a._localId !== localId);
        isDirty = true;
        triggerListeners();
    };

    const getChanges = () => {
        const toAdd = currentActions.filter(a => !a.id);
        const toUpdate = currentActions.filter(a => a.id && JSON.stringify(a) !== JSON.stringify(originalActions.find(o => o.id === a.id)));
        return { toAdd, toUpdate, toDelete: deletedActionIds };
    };

    const listeners = [];
    const subscribe = (fn) => listeners.push(fn);
    const triggerListeners = () => listeners.forEach(fn => fn(isDirty));

    return { init, getActions, getAction, addAction, updateAction, deleteAction, getChanges, subscribe };
})();
