/**
 * Fan Domain Types and Builders
 */

import { ActuatorPower, ActuatorMode, ActuatorSwing, ActuatorState } from '../constants/actuator.constants.js';

export { ActuatorPower, ActuatorMode, ActuatorSwing, ActuatorState };

export class FanControlRequestBody {
  constructor(builder) {
    this.power = builder._power;
    this.mode = builder._mode;
    this.speed = builder._speed;
    this.swing = builder._swing;
    this.light = builder._light;
  }

  static get Builder() {
    class Builder {
      setPower(power) {
        this._power = power;
        return this;
      }
      setMode(mode) {
        this._mode = mode;
        return this;
      }
      setSpeed(speed) {
        this._speed = speed;
        return this;
      }
      setSwing(swing) {
        this._swing = swing;
        return this;
      }
      setLight(light) {
        this._light = light;
        return this;
      }
      build() {
        return new FanControlRequestBody(this);
      }
    }
    return Builder;
  }
}
