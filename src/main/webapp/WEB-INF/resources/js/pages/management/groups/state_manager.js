export const StateManager = (() => {
	let config = { i18n: {} };

	const init = (cfg) => {
		config = cfg || { i18n: {} };
	};

	const getConfig = () => config;
	const getI18n = () => config.i18n;

	return {
		init,
		getConfig,
		getI18n,
	};
})();
