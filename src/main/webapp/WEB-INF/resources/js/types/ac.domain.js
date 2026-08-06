import { ActuatorPower, ActuatorMode, ActuatorSwing } from './fan.domain.js';

export { ActuatorPower, ActuatorMode, ActuatorSwing };

export class AirConditionControlRequestBody {
  constructor(builder) {
    this.power = builder._power;
    this.temperature = builder._temperature;
    this.mode = builder._mode;
    this.fanSpeed = builder._fanSpeed;
    this.swing = builder._swing;
  }

  static get Builder() {
    class Builder {
      setPower(power) {
        this._power = power;
        return this;
      }
      setTemperature(temperature) {
        this._temperature = temperature;
        return this;
      }
      setMode(mode) {
        this._mode = mode;
        return this;
      }
      setFanSpeed(fanSpeed) {
        this._fanSpeed = fanSpeed;
        return this;
      }
      setSwing(swing) {
        this._swing = swing;
        return this;
      }
      build() {
        return new AirConditionControlRequestBody(this);
      }
    }
    return Builder;
  }
}
