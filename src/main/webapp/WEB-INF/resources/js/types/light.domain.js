import { ActuatorPower, ActuatorState } from './fan.domain.js';
import { Validator } from '../common/validator.js';
import { DomainValidationError } from './common.domain.js';

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
      validate() {
        const errors = {};
        if (!Validator.generic.isNull(this._power)) {
          errors.power = 'valLightPowerRequired';
        } else if (!Validator.LIGHT.power.isValidFormat(this._power)) {
          errors.power = 'valLightPowerInvalid';
        }
        if (this._level !== null && this._level !== undefined && this._level !== ''
            && !Validator.LIGHT.level.isValidFormat(this._level)) {
          errors.level = 'valLightLevelInvalid';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('LightControlRequestBody validation failed', errors);
        }
        return new LightControlRequestBody(this);
      }
    }
    return Builder;
  }
}
