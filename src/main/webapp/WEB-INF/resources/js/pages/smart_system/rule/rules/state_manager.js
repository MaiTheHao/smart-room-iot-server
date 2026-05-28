export const StateManager = (() => {
  let currentRules = [];

  const init = (rulesList) => {
    currentRules = rulesList || [];
  };

  const getRules = () => [...currentRules];

  const getRule = (id) => currentRules.find((r) => String(r.id) === String(id));

  const updateRule = (id, updatedFields) => {
    const index = currentRules.findIndex((r) => String(r.id) === String(id));
    if (index > -1) {
      currentRules[index] = { ...currentRules[index], ...updatedFields };
    }
  };

  const deleteRule = (id) => {
    currentRules = currentRules.filter((r) => String(r.id) !== String(id));
  };

  return {
    init,
    getRules,
    getRule,
    updateRule,
    deleteRule,
  };
})();
