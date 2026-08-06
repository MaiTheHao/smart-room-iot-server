import { ActuatorPower, ActuatorState } from './fan.domain.js';

export { ActuatorPower, ActuatorState };

export class LightControlRequestBody {
  constructor(builder) {
    this.power = builder._power;
    this.level = builder._level;
  }

  static get Builder() {
    class Builder {
      setPower(power) {
        this._power = power;
        return this;
      }
      setLevel(level) {
        this._level = level;
        return this;
      }
      build() {
        return new LightControlRequestBody(this);
      }
    }
    return Builder;
  }
}
