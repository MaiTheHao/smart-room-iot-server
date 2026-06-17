import { getEnergyMetricHistory } from '../../../../api/metric.api.js';
import { StateManager } from '../../state_manager.js';

const METRIC_COLORS = {
  power: '#f59e0b',
  voltage: '#3b82f6',
  current: '#8b5cf6',
  energy: '#10b981',
};

export const DeviceChart = {
  async init(naturalId) {
    const container = document.querySelector(`.device-chart-container[data-natural-id="${naturalId}"]`);
    if (!container || StateManager.getDeviceChart(naturalId)) return;

    const chartEl = container.querySelector('.device-chart-el');
    const rangeInput = container.querySelector('.device-chart-range');
    const category = container.dataset.category;
    const targetId = parseInt(container.dataset.id);

    const initialChartState = {
      chart: null,
      currentType: 'power',
      category: category,
      targetId: targetId,
      data: [],
      range: {
        from: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString(),
        to: new Date().toISOString(),
      },
    };

    StateManager.setDeviceChart(naturalId, initialChartState);

    if (window.flatpickr) {
      flatpickr(rangeInput, {
        mode: 'range',
        enableTime: true,
        time_24hr: true,
        altInput: true,
        altFormat: 'd/m/Y H:i',
        dateFormat: 'Z',
        defaultDate: [new Date(Date.now() - 24 * 60 * 60 * 1000), new Date()],
        onClose: (dates) => {
          if (dates.length === 2) {
            const currentObj = StateManager.getDeviceChart(naturalId);
            if (currentObj) {
              currentObj.range = {
                from: dates[0].toISOString(),
                to: dates[1].toISOString(),
              };
              this.refreshData(naturalId);
            }
          }
        },
      });
    }

    const options = {
      chart: {
        height: 200,
        type: 'area',
        toolbar: { show: false },
        fontFamily: 'inherit',
        animations: { enabled: true },
      },
      dataLabels: { enabled: false },
      stroke: { curve: 'smooth', width: 2 },
      series: [{ name: 'Value', data: [] }],
      xaxis: {
        type: 'datetime',
        labels: { 
          datetimeUTC: false,
          style: { colors: '#94a3b8', fontSize: '10px' } 
        },
      },
      yaxis: {
        labels: {
          formatter: (val) => (val?.toFixed ? val.toFixed(2) : val),
          style: { colors: '#94a3b8', fontSize: '10px' },
        },
      },
      grid: { borderColor: '#f1f5f9', strokeDashArray: 4 },
      tooltip: {
        x: { format: 'dd MMM HH:mm' },
        y: { formatter: (val) => (val?.toFixed ? val.toFixed(2) : val) },
      },
      markers: {
        size: 4,
        strokeColors: '#fff',
        strokeWidth: 2,
        hover: {
          size: 6,
        },
      },
      fill: {
        type: 'gradient',
        gradient: { shadeIntensity: 1, opacityFrom: 0.4, opacityTo: 0.05, stops: [0, 90, 100] },
      },
      colors: [METRIC_COLORS.power],
    };

    const chartInstance = new ApexCharts(chartEl, options);
    StateManager.getDeviceChart(naturalId).chart = chartInstance;
    await chartInstance.render();

    await this.refreshData(naturalId);
  },

  async refreshData(naturalId) {
    const state = StateManager.getDeviceChart(naturalId);
    if (!state) return;

    const [err, res] = await getEnergyMetricHistory({
      category: state.category,
      targetId: state.targetId,
      from: state.range.from,
      to: state.range.to,
    });

    if (err || !res.data) return;

    state.data = res.data;
    this.updateChart(naturalId);
  },

  updateChart(naturalId) {
    const state = StateManager.getDeviceChart(naturalId);
    if (!state || !state.chart) return;

    const i18n = StateManager.getI18n();
    const type = state.currentType;
    const color = METRIC_COLORS[type] || '#3b82f6';
    const metricKey = `metric${type.charAt(0).toUpperCase() + type.slice(1)}`;
    const seriesData = state.data.map((item) => ({
      x: Date.parse(item.timestamp),
      y: item[type] || 0,
    }));

    state.chart.updateOptions({
      colors: [color],
      series: [{ name: i18n[metricKey] || type, data: seriesData }],
    });
  },

  switchType(naturalId, newType) {
    const state = StateManager.getDeviceChart(naturalId);
    if (!state) return;
    state.currentType = newType;
    this.updateChart(naturalId);
  },
};
