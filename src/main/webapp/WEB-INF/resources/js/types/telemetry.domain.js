/**
 * Telemetry Domain Types and Builders
 */

import { MetricDomain, EnergyMetricCategory } from '../constants/telemetry.constants.js';

export { MetricDomain, EnergyMetricCategory };

export class TemperatureValueDto {
  constructor(builder) {
    this.id = builder._id;
    this.sensorId = builder._sensorId;
    this.tempC = builder._tempC;
    this.timestamp = builder._timestamp;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setSensorId(sensorId) { this._sensorId = sensorId; return this; }
      setTempC(tempC) { this._tempC = tempC; return this; }
      setTimestamp(timestamp) { this._timestamp = timestamp; return this; }
      build() { return new TemperatureValueDto(this); }
    }
    return Builder;
  }
}

export class AverageTemperatureValueDto {
  constructor(builder) {
    this.timestamp = builder._timestamp;
    this.avgTempC = builder._avgTempC;
  }

  static get Builder() {
    class Builder {
      setTimestamp(timestamp) { this._timestamp = timestamp; return this; }
      setAvgTempC(avgTempC) { this._avgTempC = avgTempC; return this; }
      build() { return new AverageTemperatureValueDto(this); }
    }
    return Builder;
  }
}

export class EnergyMetricDto {
  constructor(builder) {
    this.timestamp = builder._timestamp;
    this.voltage = builder._voltage;
    this.current = builder._current;
    this.power = builder._power;
    this.energy = builder._energy;
    this.frequency = builder._frequency;
    this.powerFactor = builder._powerFactor;
  }

  static get Builder() {
    class Builder {
      setTimestamp(timestamp) { this._timestamp = timestamp; return this; }
      setVoltage(voltage) { this._voltage = voltage; return this; }
      setCurrent(current) { this._current = current; return this; }
      setPower(power) { this._power = power; return this; }
      setEnergy(energy) { this._energy = energy; return this; }
      setFrequency(frequency) { this._frequency = frequency; return this; }
      setPowerFactor(powerFactor) { this._powerFactor = powerFactor; return this; }
      build() { return new EnergyMetricDto(this); }
    }
    return Builder;
  }
}
