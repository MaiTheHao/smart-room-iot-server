import { httpClient } from './http-client.js';
import { METRIC_DOMAIN, SensorMetricCategory, EnergyMetricCategory } from '../constants/telemetry.constants.js';

/**
 * @param {Object} params
 * @param {EnergyMetricCategory} params.category
 * @param {number} params.targetId
 * @param {string} [params.from]
 * @param {string} [params.to]
 * @returns {Promise<[Error|null, ApiResponse<EnergyMetricDto[]>]>}
 */
export const getEnergyMetricHistory = (params) => {
	const query = new URLSearchParams({
		domain: METRIC_DOMAIN.ENERGY,
		latest: false,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

/**
 * @param {Object} params
 * @param {EnergyMetricCategory} params.category
 * @param {number} params.targetId
 * @returns {Promise<[Error|null, ApiResponse<EnergyMetricDto>]>}
 */
export const getEnergyMetricLatest = (params) => {
	const query = new URLSearchParams({
		domain: METRIC_DOMAIN.ENERGY,
		latest: true,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

/**
 * @param {Object} params
 * @param {SensorMetricCategory} [params.category]
 * @param {number} params.targetId
 * @param {string} [params.from]
 * @param {string} [params.to]
 * @returns {Promise<[Error|null, ApiResponse<TemperatureMetricDto[]>]>}
 */
export const getTemperatureMetricHistory = (params) => {
	const query = new URLSearchParams({
		domain: METRIC_DOMAIN.TEMPERATURE,
		latest: false,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

/**
 * @param {Object} params
 * @param {SensorMetricCategory} [params.category]
 * @param {number} params.targetId
 * @returns {Promise<[Error|null, ApiResponse<TemperatureMetricDto>]>}
 */
export const getTemperatureMetricLatest = (params) => {
	const query = new URLSearchParams({
		domain: METRIC_DOMAIN.TEMPERATURE,
		latest: true,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

/**
 * @param {Object} params
 * @param {SensorMetricCategory} [params.category]
 * @param {number} params.targetId
 * @param {string} [params.from]
 * @param {string} [params.to]
 * @returns {Promise<[Error|null, ApiResponse<HumidityMetricDto[]>]>}
 */
export const getHumidityMetricHistory = (params) => {
	const query = new URLSearchParams({
		domain: METRIC_DOMAIN.HUMIDITY,
		latest: false,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

/**
 * @param {Object} params
 * @param {SensorMetricCategory} [params.category]
 * @param {number} params.targetId
 * @returns {Promise<[Error|null, ApiResponse<HumidityMetricDto>]>}
 */
export const getHumidityMetricLatest = (params) => {
	const query = new URLSearchParams({
		domain: METRIC_DOMAIN.HUMIDITY,
		latest: true,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

/**
 * @param {Object} params
 * @param {DeviceCategory} params.category
 * @param {number} params.targetId
 * @param {string} [params.from]
 * @param {string} [params.to]
 * @returns {Promise<[Error|null, ApiResponse<DeviceStatusMetricDto[]>]>}
 */
export const getDeviceStatusMetricHistory = (params) => {
	const query = new URLSearchParams({
		domain: METRIC_DOMAIN.DEVICE_STATUS,
		latest: false,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

/**
 * @param {Object} params
 * @param {DeviceCategory} params.category
 * @param {number} params.targetId
 * @returns {Promise<[Error|null, ApiResponse<DeviceStatusMetricDto>]>}
 */
export const getDeviceStatusMetricLatest = (params) => {
	const query = new URLSearchParams({
		domain: METRIC_DOMAIN.DEVICE_STATUS,
		latest: true,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

/**
 * @param {Object} params
 * @param {SensorMetricCategory} [params.category]
 * @param {number} params.targetId
 * @param {string} [params.from]
 * @param {string} [params.to]
 * @returns {Promise<[Error|null, ApiResponse<Co2MetricDto[]>]>}
 */
export const getCo2MetricHistory = (params) => {
	const query = new URLSearchParams({
		domain: METRIC_DOMAIN.CO2,
		latest: false,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

/**
 * @param {Object} params
 * @param {SensorMetricCategory} [params.category]
 * @param {number} params.targetId
 * @returns {Promise<[Error|null, ApiResponse<Co2MetricDto>]>}
 */
export const getCo2MetricLatest = (params) => {
	const query = new URLSearchParams({
		domain: METRIC_DOMAIN.CO2,
		latest: true,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

/**
 * @param {Object} params
 * @param {SensorMetricCategory} [params.category]
 * @param {number} params.targetId
 * @param {string} [params.from]
 * @param {string} [params.to]
 * @returns {Promise<[Error|null, ApiResponse<LuxMetricDto[]>]>}
 */
export const getLuxMetricHistory = (params) => {
	const query = new URLSearchParams({
		domain: METRIC_DOMAIN.LUX,
		latest: false,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};

/**
 * @param {Object} params
 * @param {SensorMetricCategory} [params.category]
 * @param {number} params.targetId
 * @returns {Promise<[Error|null, ApiResponse<LuxMetricDto>]>}
 */
export const getLuxMetricLatest = (params) => {
	const query = new URLSearchParams({
		domain: METRIC_DOMAIN.LUX,
		latest: true,
		...params,
	}).toString();
	return httpClient(`/api/v1/metrics?${query}`);
};


