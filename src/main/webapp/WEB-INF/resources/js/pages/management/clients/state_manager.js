export const StateManager = (() => {
	let config = { i18n: {}, constants: {} };

	const init = (cfg) => {
		config = cfg || { i18n: {}, constants: {} };
	};

	const getConfig = () => config;
	const getI18n = () => config.i18n;
	const getConstants = () => config.constants || {};

	const isGateway = (type) => {
		const types = getConstants().CLIENT_TYPE || {};
		return type === types.HARDWARE_GATEWAY || type === types.HARDWARE_GATEWAY_ESP32;
	};

	return {
		init,
		getConfig,
		getI18n,
		getConstants,
		isGateway,
	};
})();
