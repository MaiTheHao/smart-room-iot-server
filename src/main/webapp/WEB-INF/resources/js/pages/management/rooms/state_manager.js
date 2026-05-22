export const StateManager = (() => {
	let config = { i18n: {} };
	let floorsMap = {};

	const init = (cfg) => {
		config = cfg || { i18n: {} };
	};

	const getConfig = () => config;
	const getI18n = () => config.i18n;
	const getFloorsMap = () => floorsMap;
	const setFloorName = (id, name) => {
		floorsMap[id] = name;
	};

	return {
		init,
		getConfig,
		getI18n,
		getFloorsMap,
		setFloorName,
	};
})();
