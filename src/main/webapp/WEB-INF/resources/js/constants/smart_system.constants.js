export const SYSTEM_PROPERTIES = Object.freeze([
  'current_time',
  'day_of_week',
  'day_of_month',
]);

export const ROOM_PROPERTIES = Object.freeze([
  'avg_temperature',
  'sum_watt',
  'avg_humidity',
  'avg_lux',
  'avg_co2',
  'max_co2',
]);

export const SENSOR_PROPERTIES = Object.freeze({
  TEMPERATURE: Object.freeze(['temperature']),
  POWER_CONSUMPTION: Object.freeze(['watt']),
  HUMIDITY: Object.freeze(['humidity']),
  SENSOR_CO2: Object.freeze(['co2']),
  SENSOR_LUX: Object.freeze(['lux']),
});

export const DEVICE_PROPERTIES = Object.freeze({
  LIGHT: Object.freeze(['power', 'level']),
  AIR_CONDITION: Object.freeze(['power', 'temp', 'mode', 'fan_speed', 'swing']),
  FAN: Object.freeze(['power', 'speed', 'mode', 'swing', 'light']),
});

export const SENSOR_CATEGORY_LABEL_KEYS = Object.freeze({
  TEMPERATURE: 'catTemperature',
  POWER_CONSUMPTION: 'catPowerConsumption',
  HUMIDITY: 'catHumidity',
  SENSOR_CO2: 'catCo2',
  SENSOR_LUX: 'catLux',
});

export const SENSOR_CATEGORY_FALLBACKS = Object.freeze({
  TEMPERATURE: 'Temperature Sensor',
  POWER_CONSUMPTION: 'Power Sensor',
  HUMIDITY: 'Humidity Sensor',
  SENSOR_CO2: 'CO₂ Sensor',
  SENSOR_LUX: 'Lux Sensor',
});

export const CONDITION_PARAMETER_CONFIG = Object.freeze({
  DEVICE: Object.freeze({
    LIGHT: Object.freeze({
      power: { type: 'enum', options: ['ON', 'OFF'] },
      level: { type: 'int', min: 0, max: 100, placeholder: '0 – 100' },
    }),
    AIR_CONDITION: Object.freeze({
      power: { type: 'enum', options: ['ON', 'OFF'] },
      temp: { type: 'int', min: 16, max: 32, placeholder: '16 – 32 °C' },
      mode: { type: 'enum', options: ['COOL', 'HEAT', 'DRY', 'FAN', 'AUTO'] },
      fan_speed: { type: 'int', min: 0, max: 5, placeholder: '0 – 5' },
      swing: { type: 'enum', options: ['ON', 'OFF'] },
    }),
    FAN: Object.freeze({
      power: { type: 'enum', options: ['ON', 'OFF'] },
      mode: { type: 'enum', options: ['NATURAL', 'SLEEP', 'NORMAL'] },
      speed: { type: 'int', min: 1, max: 3, placeholder: '1 – 3' },
      swing: { type: 'enum', options: ['ON', 'OFF'] },
      light: { type: 'enum', options: ['ON', 'OFF'] },
    }),
  }),
  SENSOR: Object.freeze({
    TEMPERATURE: Object.freeze({ temperature: { type: 'float', placeholder: 'Enter temperature (°C)' } }),
    POWER_CONSUMPTION: Object.freeze({ watt: { type: 'float', placeholder: 'Enter wattage (W)' } }),
    HUMIDITY: Object.freeze({ humidity: { type: 'float', placeholder: 'Enter humidity (% RH)' } }),
    SENSOR_CO2: Object.freeze({ co2: { type: 'float', placeholder: 'Enter CO₂ level (ppm)' } }),
    SENSOR_LUX: Object.freeze({ lux: { type: 'float', placeholder: 'Enter illuminance (lux)' } }),
  }),
});

export const ACTION_PARAM_SCHEMA = Object.freeze({
  LIGHT: Object.freeze({
    power: { type: 'enum', labelKey: 'power', options: ['ON', 'OFF'] },
    level: { type: 'int', labelKey: 'brightnessLevel', min: 0, max: 100, placeholder: '0 – 100' },
  }),
  AIR_CONDITION: Object.freeze({
    power: { type: 'enum', labelKey: 'power', options: ['ON', 'OFF'] },
    temperature: { type: 'int', labelKey: 'temperature', min: 16, max: 32, placeholder: '16 – 32 °C' },
    mode: { type: 'enum', labelKey: 'mode', options: ['COOL', 'HEAT', 'DRY', 'FAN', 'AUTO'] },
    fanSpeed: { type: 'int', labelKey: 'fanSpeed', min: 0, max: 5, placeholder: '0 – 5' },
    swing: { type: 'enum', labelKey: 'swing', options: ['ON', 'OFF'] },
  }),
  FAN: Object.freeze({
    power: { type: 'enum', labelKey: 'power', options: ['ON', 'OFF'] },
    mode: { type: 'enum', labelKey: 'mode', options: ['NATURAL', 'SLEEP', 'NORMAL'] },
    speed: { type: 'int', labelKey: 'speed', min: 1, max: 3, placeholder: '1 – 3' },
    swing: { type: 'enum', labelKey: 'swing', options: ['ON', 'OFF'] },
  }),
});

export const DEVICE_CAPABILITIES = Object.freeze({
  FAN: Object.freeze({
    GPIO: Object.freeze(['power', 'speed']),
    IRSEND: Object.freeze(['power', 'speed', 'mode', 'swing']),
    IR_CTL: Object.freeze(['power', 'speed', 'mode', 'swing']),
  }),
  LIGHT: Object.freeze({
    GPIO: Object.freeze(['power', 'level']),
    IRSEND: Object.freeze(['power', 'level']),
    IR_CTL: Object.freeze(['power', 'level']),
  }),
  AIR_CONDITION: Object.freeze({
    GPIO: Object.freeze(['power']),
    IRSEND: Object.freeze(['power', 'temperature', 'mode', 'fanSpeed', 'swing']),
    IR_CTL: Object.freeze(['power', 'temperature', 'mode', 'fanSpeed', 'swing']),
  }),
});

export const PROPERTY_LABEL_CONFIG = Object.freeze({
  avg_temperature: { i18nKey: 'propAvgTemp', fallback: 'Avg Temperature (°C)' },
  sum_watt: { i18nKey: 'propSumWatt', fallback: 'Total Power (W)' },
  avg_humidity: { i18nKey: 'propAvgHumidity', fallback: 'Avg Humidity (% RH)' },
  avg_lux: { i18nKey: 'propAvgLux', fallback: 'Avg Illuminance (Lux)' },
  avg_co2: { i18nKey: 'propAvgCo2', fallback: 'Avg CO₂ Level (ppm)' },
  max_co2: { i18nKey: 'propMaxCo2', fallback: 'Max CO₂ Level (ppm)' },
  temperature: { i18nKey: 'propTemp', fallback: 'Temperature (°C)' },
  watt: { i18nKey: 'propWatt', fallback: 'Power (W)' },
  humidity: { i18nKey: 'propHumidity', fallback: 'Humidity (% RH)' },
  co2: { i18nKey: 'propCo2', fallback: 'CO₂ Level (ppm)' },
  lux: { i18nKey: 'propLux', fallback: 'Illuminance (Lux)' },
  current_time: { i18nKey: 'propCurrentTime', fallback: 'Current Time (UTC)' },
  day_of_week: { i18nKey: 'propDayOfWeek', fallback: 'Day of Week' },
  day_of_month: { i18nKey: 'propDayOfMonth', fallback: 'Day of Month' },
  power: { i18nKey: 'propPower', fallback: 'Power' },
  level: { i18nKey: 'propLevel', fallback: 'Level' },
  mode: { i18nKey: 'propMode', fallback: 'Mode' },
  temp: { i18nKey: 'propTemp', fallback: 'Temperature (°C)' },
  fan_speed: { i18nKey: 'propFanSpeed', fallback: 'Fan Speed' },
  swing: { i18nKey: 'propSwing', fallback: 'Swing' },
  speed: { i18nKey: 'propSpeed', fallback: 'Speed' },
  light: { i18nKey: 'propFanLight', fallback: 'Fan Light' },
});
