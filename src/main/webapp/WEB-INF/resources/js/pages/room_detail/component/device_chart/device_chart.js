import { getEnergyMetricHistory } from '../../../../api/metric.api.js';
import { StateManager } from '../../state_manager.js';
import { ChartFactory } from '../chart/chart_factory.js';
import { DEVICE_METRIC_CONFIGS } from './device_metric_registry.js';

const DEFAULT_RANGE_HOURS = 3;

export const DeviceChart = {
  async init(naturalId) {
    const container = document.querySelector(`.device-chart-container[data-natural-id="${naturalId}"]`);
    if (!container || StateManager.getDeviceChart(naturalId)) return;

    const chartEl = container.querySelector('.device-chart-el');
    const rangeInput = container.querySelector('.device-chart-range');
    const category = container.dataset.category;
    const targetId = parseInt(container.dataset.id, 10);

    const state = {
      chart: null,
      currentType: 'power',
      category,
      targetId,
      data: [],
      range: {
        from: new Date(Date.now() - DEFAULT_RANGE_HOURS * 60 * 60 * 1000).toISOString(),
        to: new Date().toISOString(),
      },
    };

    StateManager.setDeviceChart(naturalId, state);

    const instance = ChartFactory.createInstance(DEVICE_METRIC_CONFIGS.power, chartEl, {
      getI18n: () => StateManager.getI18n(),
      fetchData: async (from, to) => {
        const [err, res] = await getEnergyMetricHistory({ category, targetId, from, to });
        return [err, res?.data ?? null];
      },
      onData: (chart, data) => {
        state.data = data;
      },
    });

    if (!instance) return;
    state.chart = instance;

    this.bindRangePicker(naturalId, rangeInput);

    await this.refreshData(naturalId);
  },

  bindRangePicker(naturalId, rangeInput) {
    if (!window.flatpickr) return;

    flatpickr(rangeInput, {
      mode: 'range',
      enableTime: true,
      time_24hr: true,
      altInput: true,
      altFormat: 'd/m/Y H:i',
      dateFormat: 'Z',
      defaultDate: [new Date(Date.now() - 24 * 60 * 60 * 1000), new Date()],
      onClose: (dates) => {
        if (dates.length !== 2) return;
        const state = StateManager.getDeviceChart(naturalId);
        if (!state?.chart) return;
        state.range = { from: dates[0].toISOString(), to: dates[1].toISOString() };
        this.refreshData(naturalId);
      },
    });
  },

  async refreshData(naturalId) {
    const state = StateManager.getDeviceChart(naturalId);
    if (!state?.chart) return;
    try {
      await state.chart.update(state.range.from, state.range.to);
    } catch (error) {
      console.error(`[DeviceChart] Refresh failed for ${naturalId}:`, error);
    }
  },

  switchType(naturalId, newType) {
    const state = StateManager.getDeviceChart(naturalId);
    const config = DEVICE_METRIC_CONFIGS[newType];
    if (!state?.chart || !config) return;

    state.currentType = newType;
    state.chart.setConfig(config);
    state.chart.renderData(state.data).catch((error) => {
      console.error(`[DeviceChart] Switch type failed for ${naturalId}:`, error);
    });
  },
};
