import { ActuatorPower, ActuatorMode, ActuatorSwing } from './fan.domain.js';
import { Validator } from '../common/validator.js';
import { DomainValidationError } from './common.domain.js';

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
      validate() {
        const errors = {};
        if (!Validator.generic.isNull(this._power)) {
          errors.power = 'valAcPowerRequired';
        } else if (!Validator.AIR_CONDITION.power.isValidFormat(this._power)) {
          errors.power = 'valAcPowerInvalid';
        }
        if (this._temperature !== null && this._temperature !== undefined && this._temperature !== ''
            && !Validator.AIR_CONDITION.temp.isValidFormat(this._temperature)) {
          errors.temperature = 'valAcTemperatureInvalid';
        }
        if (this._mode !== null && this._mode !== undefined && this._mode !== ''
            && !Validator.AIR_CONDITION.mode.isValidFormat(this._mode)) {
          errors.mode = 'valAcModeInvalid';
        }
        if (this._fanSpeed !== null && this._fanSpeed !== undefined && this._fanSpeed !== ''
            && !Validator.AIR_CONDITION.fan_speed.isValidFormat(this._fanSpeed)) {
          errors.fanSpeed = 'valAcFanSpeedInvalid';
        }
        if (this._swing !== null && this._swing !== undefined && this._swing !== ''
            && !Validator.AIR_CONDITION.swing.isValidFormat(this._swing)) {
          errors.swing = 'valAcSwingInvalid';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('AirConditionControlRequestBody validation failed', errors);
        }
        return new AirConditionControlRequestBody(this);
      }
    }
    return Builder;
  }
}
