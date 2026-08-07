/**
 * Fan Domain Types and Builders
 */

import { ActuatorPower, ActuatorMode, ActuatorSwing, ActuatorState } from '../constants/actuator.constants.js';
import { Validator } from '../common/validator.js';
import { DomainValidationError } from './common.domain.js';

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
      validate() {
        const errors = {};
        if (!Validator.generic.isNull(this._power)) {
          errors.power = 'valFanPowerRequired';
        } else if (!Validator.FAN.power.isValidFormat(this._power)) {
          errors.power = 'valFanPowerInvalid';
        }
        if (this._mode !== null && this._mode !== undefined && this._mode !== ''
            && !Validator.FAN.mode.isValidFormat(this._mode)) {
          errors.mode = 'valFanModeInvalid';
        }
        if (this._speed !== null && this._speed !== undefined && this._speed !== ''
            && !Validator.FAN.speed.isValidFormat(this._speed)) {
          errors.speed = 'valFanSpeedInvalid';
        }
        if (this._swing !== null && this._swing !== undefined && this._swing !== ''
            && !Validator.FAN.swing.isValidFormat(this._swing)) {
          errors.swing = 'valFanSwingInvalid';
        }
        if (this._light !== null && this._light !== undefined && this._light !== ''
            && !Validator.FAN.light.isValidFormat(this._light)) {
          errors.light = 'valFanLightInvalid';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('FanControlRequestBody validation failed', errors);
        }
        return new FanControlRequestBody(this);
      }
    }
    return Builder;
  }
}
