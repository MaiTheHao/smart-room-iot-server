export const StateManager = (() => {
  let currentAlerts = [];

  const init = (alertsList) => {
    currentAlerts = alertsList || [];
  };

  const getAlerts = () => [...currentAlerts];

  const getAlert = (id) => currentAlerts.find((a) => String(a.id) === String(id));

  const updateAlert = (id, updatedFields) => {
    const index = currentAlerts.findIndex((a) => String(a.id) === String(id));
    if (index > -1) {
      currentAlerts[index] = { ...currentAlerts[index], ...updatedFields };
    }
  };

  const deleteAlert = (id) => {
    currentAlerts = currentAlerts.filter((a) => String(a.id) !== String(id));
  };

  return {
    init,
    getAlerts,
    getAlert,
    updateAlert,
    deleteAlert,
  };
})();
