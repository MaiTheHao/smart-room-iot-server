import { CHART_COLORS } from '../chart/chart_factory.js';

const DEVICE_CHART_OPTIONS = {
  height: 200,
  fontSize: '10px',
  strokeWidth: 2,
  markerSize: 4,
  markerHoverSize: 6,
  fillOpacityFrom: 0.4,
  showYAxisTitle: false,
  tooltipValueFormatter: (val) => (val?.toFixed ? val.toFixed(2) : val),
};

export const DEVICE_METRIC_CONFIGS = {
  power: {
    id: 'power',
    titleKey: 'metricPower',
    defaultTitle: 'Power',
    unit: 'W',
    color: CHART_COLORS.POWER,
    valueExtractor: (item) => item.power ?? 0,
    options: DEVICE_CHART_OPTIONS,
  },
  voltage: {
    id: 'voltage',
    titleKey: 'metricVoltage',
    defaultTitle: 'Voltage',
    unit: 'V',
    color: CHART_COLORS.VOLTAGE,
    valueExtractor: (item) => item.voltage ?? 0,
    options: DEVICE_CHART_OPTIONS,
  },
  current: {
    id: 'current',
    titleKey: 'metricCurrent',
    defaultTitle: 'Current',
    unit: 'A',
    color: CHART_COLORS.CURRENT,
    valueExtractor: (item) => item.current ?? 0,
    options: DEVICE_CHART_OPTIONS,
  },
  energy: {
    id: 'energy',
    titleKey: 'metricEnergy',
    defaultTitle: 'Energy',
    unit: 'kWh',
    color: CHART_COLORS.ENERGY,
    valueExtractor: (item) => item.energy ?? 0,
    options: DEVICE_CHART_OPTIONS,
  },
};
