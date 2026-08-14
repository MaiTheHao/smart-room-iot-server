import {
  getTemperatureMetricHistory,
  getEnergyMetricHistory,
  getHumidityMetricHistory,
  getCo2MetricHistory,
  getLuxMetricHistory,
} from '../../../../api/metric.api.js';
import { SensorMetricCategory, EnergyMetricCategory } from '../../../../constants/metric.constants.js';
import { CHART_COLORS } from '../chart/chart_factory.js';

export const METRIC_CONFIGS = {
  TEMPERATURE: {
    id: 'TEMPERATURE',
    titleKey: 'temp',
    defaultTitle: 'Temperature',
    icon: 'thermometer',
    color: CHART_COLORS.TEMPERATURE,
    unit: '°C',
    fetchHistory: (targetId, from, to) =>
      getTemperatureMetricHistory({ category: SensorMetricCategory.ROOM, targetId, from, to }),
    valueExtractor: (item) => item.avgTemp ?? item.temperature ?? 0,
    badgeFormatter: (data, extractor) => {
      if (!data || !data.length) return 'Avg: --';
      const avg = (data.reduce((sum, item) => sum + extractor(item), 0) / data.length).toFixed(1);
      return `Avg: ${avg}°C`;
    },
  },
  ENERGY: {
    id: 'ENERGY',
    titleKey: 'power',
    defaultTitle: 'Power',
    icon: 'zap',
    color: CHART_COLORS.POWER,
    unit: 'W',
    fetchHistory: (targetId, from, to) =>
      getEnergyMetricHistory({ category: EnergyMetricCategory.ROOM, targetId, from, to }),
    valueExtractor: (item) => item.power ?? 0,
    badgeFormatter: (data, extractor) => {
      if (!data || !data.length) return 'Peak: --';
      const peak = Math.max(...data.map(extractor)).toFixed(1);
      return `Peak: ${peak}W`;
    },
  },
  HUMIDITY: {
    id: 'HUMIDITY',
    titleKey: 'humidity',
    defaultTitle: 'Humidity',
    icon: 'droplets',
    color: CHART_COLORS.HUMIDITY,
    unit: '%',
    fetchHistory: (targetId, from, to) =>
      getHumidityMetricHistory({ category: SensorMetricCategory.ROOM, targetId, from, to }),
    valueExtractor: (item) => item.medianHumidity ?? item.humidity ?? 0,
    badgeFormatter: (data, extractor) => {
      if (!data || !data.length) return 'Avg: --';
      const avg = (data.reduce((sum, item) => sum + extractor(item), 0) / data.length).toFixed(1);
      return `Avg: ${avg}%`;
    },
  },
  CO2: {
    id: 'CO2',
    titleKey: 'co2',
    defaultTitle: 'CO2',
    icon: 'wind',
    color: CHART_COLORS.CO2,
    unit: 'ppm',
    fetchHistory: (targetId, from, to) =>
      getCo2MetricHistory({ category: SensorMetricCategory.ROOM, targetId, from, to }),
    valueExtractor: (item) => item.avgCo2 ?? item.co2 ?? 0,
    badgeFormatter: (data, extractor) => {
      if (!data || !data.length) return 'Avg: --';
      const avg = (data.reduce((sum, item) => sum + extractor(item), 0) / data.length).toFixed(0);
      return `Avg: ${avg} ppm`;
    },
  },
  LUX: {
    id: 'LUX',
    titleKey: 'lux',
    defaultTitle: 'Lux',
    icon: 'sun',
    color: CHART_COLORS.LUX,
    unit: 'lux',
    fetchHistory: (targetId, from, to) =>
      getLuxMetricHistory({ category: SensorMetricCategory.ROOM, targetId, from, to }),
    valueExtractor: (item) => item.medianLux ?? item.lux ?? 0,
    badgeFormatter: (data, extractor) => {
      if (!data || !data.length) return 'Avg: --';
      const avg = (data.reduce((sum, item) => sum + extractor(item), 0) / data.length).toFixed(0);
      return `Avg: ${avg} lux`;
    },
  },
};
