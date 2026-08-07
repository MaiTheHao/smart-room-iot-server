/**
 * Client & User Domain Types and Builders
 */

import { ClientType } from '../constants/client.constants.js';
import { Validator } from '../common/validator.js';
import { DomainValidationError } from './common.domain.js';

export { ClientType };

export class ClientDto {
  constructor(builder) {
    this.id = builder._id;
    this.username = builder._username;
    this.clientType = builder._clientType;
    this.ipAddress = builder._ipAddress;
    this.macAddress = builder._macAddress;
    this.avatarUrl = builder._avatarUrl;
    this.lastLoginAt = builder._lastLoginAt;
    this.gatewayPassword = builder._gatewayPassword;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setUsername(username) { this._username = username; return this; }
      setClientType(clientType) { this._clientType = clientType; return this; }
      setIpAddress(ipAddress) { this._ipAddress = ipAddress; return this; }
      setMacAddress(macAddress) { this._macAddress = macAddress; return this; }
      setAvatarUrl(avatarUrl) { this._avatarUrl = avatarUrl; return this; }
      setLastLoginAt(lastLoginAt) { this._lastLoginAt = lastLoginAt; return this; }
      setGatewayPassword(gatewayPassword) { this._gatewayPassword = gatewayPassword; return this; }
      build() { return new ClientDto(this); }
    }
    return Builder;
  }
}

export class CreateClientDto {
  constructor(builder) {
    this.username = builder._username;
    this.password = builder._password;
    this.clientType = builder._clientType;
    this.ipAddress = builder._ipAddress;
    this.macAddress = builder._macAddress;
    this.avatarUrl = builder._avatarUrl;
    this.gatewayPassword = builder._gatewayPassword;
  }

  static get Builder() {
    class Builder {
      setUsername(username) { this._username = username; return this; }
      setPassword(password) { this._password = password; return this; }
      setClientType(clientType) { this._clientType = clientType; return this; }
      setIpAddress(ipAddress) { this._ipAddress = ipAddress; return this; }
      setMacAddress(macAddress) { this._macAddress = macAddress; return this; }
      setAvatarUrl(avatarUrl) { this._avatarUrl = avatarUrl; return this; }
      setGatewayPassword(gatewayPassword) { this._gatewayPassword = gatewayPassword; return this; }
      validate() {
        const errors = {};
        if (!Validator.username.isNull(this._username) || !Validator.username.isBlank(this._username)) {
          errors.username = 'valUsernameRequired';
        } else if (!Validator.username.isLowerMin(this._username) || !Validator.username.isHigherMax(this._username)) {
          errors.username = 'valUsernameLen';
        }
        if (!Validator.password.isNull(this._password) || !Validator.password.isBlank(this._password)) {
          errors.password = 'valPasswordRequired';
        } else if (!Validator.password.isLowerMin(this._password) || !Validator.password.isHigherMax(this._password)) {
          errors.password = 'valPasswordLen';
        }
        if (!Validator.clientType.isNull(this._clientType) || !Validator.clientType.isBlank(this._clientType)) {
          errors.clientType = 'valClientTypeRequired';
        }
        if (this._ipAddress && !Validator.ip.isValidFormat(this._ipAddress)) {
          errors.ipAddress = 'valIpInvalid';
        }
        if (this._macAddress && !Validator.mac.isValidFormat(this._macAddress)) {
          errors.macAddress = 'valMacInvalid';
        }
        if (this._avatarUrl && !Validator.url.isValidFormat(this._avatarUrl)) {
          errors.avatarUrl = 'valUrlInvalid';
        }
        if (this._gatewayPassword && (!Validator.password.isLowerMin(this._gatewayPassword) || !Validator.password.isHigherMax(this._gatewayPassword))) {
          errors.gatewayPassword = 'valPasswordLen';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('CreateClientDto validation failed', errors);
        }
        return new CreateClientDto(this);
      }
    }
    return Builder;
  }
}

export class UpdateClientDto {
  constructor(builder) {
    this.password = builder._password;
    this.clientType = builder._clientType;
    this.ipAddress = builder._ipAddress;
    this.macAddress = builder._macAddress;
    this.avatarUrl = builder._avatarUrl;
    this.gatewayPassword = builder._gatewayPassword;
  }

  static get Builder() {
    class Builder {
      setPassword(password) { this._password = password; return this; }
      setClientType(clientType) { this._clientType = clientType; return this; }
      setIpAddress(ipAddress) { this._ipAddress = ipAddress; return this; }
      setMacAddress(macAddress) { this._macAddress = macAddress; return this; }
      setAvatarUrl(avatarUrl) { this._avatarUrl = avatarUrl; return this; }
      setGatewayPassword(gatewayPassword) { this._gatewayPassword = gatewayPassword; return this; }
      validate() {
        const errors = {};
        if (this._password && (!Validator.password.isLowerMin(this._password) || !Validator.password.isHigherMax(this._password))) {
          errors.password = 'valPasswordLen';
        }
        if (this._clientType && !Validator.clientType.isBlank(this._clientType)) {
          errors.clientType = 'valClientTypeRequired';
        }
        if (this._ipAddress && !Validator.ip.isValidFormat(this._ipAddress)) {
          errors.ipAddress = 'valIpInvalid';
        }
        if (this._macAddress && !Validator.mac.isValidFormat(this._macAddress)) {
          errors.macAddress = 'valMacInvalid';
        }
        if (this._avatarUrl && !Validator.url.isValidFormat(this._avatarUrl)) {
          errors.avatarUrl = 'valUrlInvalid';
        }
        if (this._gatewayPassword && (!Validator.password.isLowerMin(this._gatewayPassword) || !Validator.password.isHigherMax(this._gatewayPassword))) {
          errors.gatewayPassword = 'valPasswordLen';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('UpdateClientDto validation failed', errors);
        }
        return new UpdateClientDto(this);
      }
    }
    return Builder;
  }
}

export class LoginDto {
  constructor(builder) {
    this.username = builder._username;
    this.password = builder._password;
  }

  static get Builder() {
    class Builder {
      setUsername(username) { this._username = username; return this; }
      setPassword(password) { this._password = password; return this; }
      validate() {
        const errors = {};
        if (!Validator.username.isNull(this._username) || !Validator.username.isBlank(this._username)) {
          errors.username = 'valUsernameRequired';
        } else if (!Validator.username.isLowerMin(this._username) || !Validator.username.isHigherMax(this._username)) {
          errors.username = 'valUsernameLen';
        }
        if (!Validator.password.isNull(this._password) || !Validator.password.isBlank(this._password)) {
          errors.password = 'valPasswordRequired';
        } else if (!Validator.password.isLowerMin(this._password) || !Validator.password.isHigherMax(this._password)) {
          errors.password = 'valPasswordLen';
        }
        return { isValid: Object.keys(errors).length === 0, errors };
      }
      build() {
        const { isValid, errors } = this.validate();
        if (!isValid) {
          throw new DomainValidationError('LoginDto validation failed', errors);
        }
        return new LoginDto(this);
      }
    }
    return Builder;
  }
}

export class JwtResponse {
  constructor(builder) {
    this.token = builder._token;
    this.type = builder._type || 'Bearer';
    this.id = builder._id;
    this.username = builder._username;
    this.clientType = builder._clientType;
    this.avatarUrl = builder._avatarUrl;
    this.lastLoginAt = builder._lastLoginAt;
    this.groups = builder._groups || [];
  }

  static get Builder() {
    class Builder {
      setToken(token) { this._token = token; return this; }
      setType(type) { this._type = type; return this; }
      setId(id) { this._id = id; return this; }
      setUsername(username) { this._username = username; return this; }
      setClientType(clientType) { this._clientType = clientType; return this; }
      setAvatarUrl(avatarUrl) { this._avatarUrl = avatarUrl; return this; }
      setLastLoginAt(lastLoginAt) { this._lastLoginAt = lastLoginAt; return this; }
      setGroups(groups) { this._groups = groups; return this; }
      build() { return new JwtResponse(this); }
    }
    return Builder;
  }
}
