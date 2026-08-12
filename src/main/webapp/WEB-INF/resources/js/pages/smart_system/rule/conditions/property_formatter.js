export const PROPERTY_LABEL_CONFIG = Object.freeze({
  avg_temperature: { i18nKey: 'propAvgTemp', fallback: 'Avg Temperature (°C)' },
  sum_watt:        { i18nKey: 'propSumWatt', fallback: 'Total Power (W)' },
  avg_humidity:    { i18nKey: 'propAvgHumidity', fallback: 'Avg Humidity (% RH)' },
  avg_lux:         { i18nKey: 'propAvgLux', fallback: 'Avg Illuminance (Lux)' },
  avg_co2:         { i18nKey: 'propAvgCo2', fallback: 'Avg CO₂ Level (ppm)' },
  max_co2:         { i18nKey: 'propMaxCo2', fallback: 'Max CO₂ Level (ppm)' },
  temperature:     { i18nKey: 'propTemp', fallback: 'Temperature (°C)' },
  watt:            { i18nKey: 'propWatt', fallback: 'Power (W)' },
  humidity:        { i18nKey: 'propHumidity', fallback: 'Humidity (% RH)' },
  co2:             { i18nKey: 'propCo2', fallback: 'CO₂ Level (ppm)' },
  lux:             { i18nKey: 'propLux', fallback: 'Illuminance (Lux)' },
  current_time:    { i18nKey: 'propCurrentTime', fallback: 'Current Time (UTC)' },
  day_of_week:     { i18nKey: 'propDayOfWeek', fallback: 'Day of Week' },
  day_of_month:    { i18nKey: 'propDayOfMonth', fallback: 'Day of Month' },
});

export const formatPropertyLabel = (propKey, i18n = {}) => {
  const cfg = PROPERTY_LABEL_CONFIG[propKey];
  if (!cfg) return propKey;
  return i18n[cfg.i18nKey] || cfg.fallback;
};
