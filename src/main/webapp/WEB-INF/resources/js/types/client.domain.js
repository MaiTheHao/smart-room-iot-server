/**
 * Client & User Domain Types and Builders
 */

import { ClientType } from '../constants/client.constants.js';

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
      build() { return new CreateClientDto(this); }
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
      build() { return new UpdateClientDto(this); }
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
      build() { return new LoginDto(this); }
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
