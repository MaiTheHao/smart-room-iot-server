export const CHART_COLORS = Object.freeze({
  TEMPERATURE: '#ef4444',
  POWER: '#f59e0b', // Công suất (W) — room ENERGY chart & device power
  HUMIDITY: '#06b6d4',
  CO2: '#10b981',
  LUX: '#8b5cf6',
  VOLTAGE: '#3b82f6',
  CURRENT: '#8b5cf6',
  ENERGY: '#10b981', // Điện năng tích luỹ (kWh) — device energy
});

const AXIS_TEXT_COLOR = '#94a3b8';
const GRID_COLOR = '#f1f5f9';

const formatValue = (value, unit) => {
  if (value === undefined || value === null) return '--';
  const formatted = typeof value.toFixed === 'function' ? value.toFixed(1) : value;
  return unit ? `${formatted} ${unit}`.trim() : String(formatted);
};

export const ChartFactory = {
  createBaseOptions(config, seriesName, overrides = {}) {
    const {
      height = 280,
      fontSize = '11px',
      strokeWidth = 2.5,
      markerSize = 3,
      markerHoverSize = 5,
      fillOpacityFrom = 0.35,
      showYAxisTitle = true,
      tooltipValueFormatter,
    } = overrides;

    return {
      chart: {
        height,
        type: 'area',
        toolbar: { show: false },
        fontFamily: 'inherit',
        animations: { enabled: true },
      },
      dataLabels: { enabled: false },
      stroke: { curve: 'smooth', width: strokeWidth },
      xaxis: {
        type: 'datetime',
        labels: {
          datetimeUTC: false,
          style: { colors: AXIS_TEXT_COLOR, fontSize },
        },
      },
      grid: { borderColor: GRID_COLOR, strokeDashArray: 4 },
      tooltip: {
        x: { format: 'dd MMM HH:mm' },
        y: { formatter: tooltipValueFormatter || ((val) => formatValue(val, config.unit)) },
      },
      yaxis: {
        ...(config.unit && showYAxisTitle
          ? {
              title: {
                text: `(${config.unit})`,
                rotate: -90,
                style: { colors: AXIS_TEXT_COLOR, fontSize, fontWeight: 500 },
              },
            }
          : {}),
        labels: {
          formatter: (val) => (val?.toFixed ? val.toFixed(1) : val),
          style: { colors: AXIS_TEXT_COLOR, fontSize },
        },
      },
      fill: {
        type: 'gradient',
        gradient: { shadeIntensity: 1, opacityFrom: fillOpacityFrom, opacityTo: 0.05, stops: [0, 90, 100] },
      },
      series: [{ name: seriesName, data: [] }],
      colors: [config.color],
      markers: {
        size: markerSize,
        strokeColors: '#fff',
        strokeWidth: 2,
        hover: { size: markerHoverSize },
      },
    };
  },

  /**
   * Wrapper race-safe quanh một ApexCharts instance.
   * - Request token: bỏ qua response cũ khi đã có request mới hơn (out-of-order).
   * - Re-check sau await: không thao tác chart đã bị destroy().
   *
   * @param {Object} config Cấu hình metric: { titleKey, defaultTitle, unit, color, valueExtractor, options? }
   * @param {Element} chartElement
   * @param {Object} deps
   * @param {() => Object} [deps.getI18n]
   * @param {(from: string, to: string) => Promise<[Error|null, Array|null]>} deps.fetchData
   * @param {(chart, data, { config, seriesName }) => Promise<void>|void} [deps.onData]
   */
  createInstance(config, chartElement, { getI18n, fetchData, onData } = {}) {
    if (!chartElement || !window.ApexCharts) return null;

    let chart = null;
    let currentConfig = config;
    let latestRangeKey = '';
    let seriesName = (getI18n?.() || {})[currentConfig.titleKey] || currentConfig.defaultTitle;

    const mapData = (data) =>
      (data || []).map((item) => ({
        x: Date.parse(item.timestamp),
        y: currentConfig.valueExtractor(item),
      }));

    const render = (data) =>
      chart.updateOptions({
        colors: [currentConfig.color],
        series: [{ name: seriesName, data: mapData(data) }],
      });

    chart = new ApexCharts(chartElement, this.createBaseOptions(currentConfig, seriesName, currentConfig.options));
    chart.render();

    return {
      metricId: currentConfig.id,
      async update(from, to) {
        const rangeKey = `${from}-${to}`;
        if (rangeKey === latestRangeKey) return;

        latestRangeKey = rangeKey;

        const [err, data] = await fetchData(from, to);

        if (err || data == null) {
          if (rangeKey === latestRangeKey) latestRangeKey = '';
          return;
        }

        if (!chart || rangeKey !== latestRangeKey) return;

        await render(data);
        if (onData) await onData(chart, data, { config: currentConfig, seriesName });
      },

      setConfig(newConfig) {
        currentConfig = newConfig;
        seriesName = (getI18n?.() || {})[newConfig.titleKey] || newConfig.defaultTitle;
      },

      async renderData(data) {
        if (!chart) return;
        await render(data);
        if (onData) await onData(chart, data, { config: currentConfig, seriesName });
      },
      
      destroy() {
        const instance = chart;
        chart = null;
        latestRangeKey = '';
        if (instance) instance.destroy();
      },
    };
  },
};
