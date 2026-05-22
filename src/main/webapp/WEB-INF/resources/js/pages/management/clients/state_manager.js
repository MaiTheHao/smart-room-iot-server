export const StateManager = (() => {
	let config = { i18n: {}, constants: {} };

	const init = (cfg) => {
		config = cfg || { i18n: {}, constants: {} };
	};

	const getConfig = () => config;
	const getI18n = () => config.i18n;
	const getConstants = () => config.constants || {};

	return {
		init,
		getConfig,
		getI18n,
		getConstants,
	};
})();
