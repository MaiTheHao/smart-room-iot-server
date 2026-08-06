export class RoomDto {
  constructor(builder) {
    this.id = builder._id;
    this.name = builder._name;
    this.code = builder._code;
    this.floorId = builder._floorId;
    this.description = builder._description;
    this.version = builder._version;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setName(name) { this._name = name; return this; }
      setCode(code) { this._code = code; return this; }
      setFloorId(floorId) { this._floorId = floorId; return this; }
      setDescription(description) { this._description = description; return this; }
      setVersion(version) { this._version = version; return this; }
      build() { return new RoomDto(this); }
    }
    return Builder;
  }
}

export class CreateRoomDto {
  constructor(builder) {
    this.name = builder._name;
    this.code = builder._code;
    this.floorId = builder._floorId;
    this.description = builder._description;
    this.langCode = builder._langCode;
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setCode(code) { this._code = code; return this; }
      setFloorId(floorId) { this._floorId = floorId; return this; }
      setDescription(description) { this._description = description; return this; }
      setLangCode(langCode) { this._langCode = langCode; return this; }
      build() { return new CreateRoomDto(this); }
    }
    return Builder;
  }
}

export class UpdateRoomDto {
  constructor(builder) {
    this.name = builder._name;
    this.floorId = builder._floorId;
    this.description = builder._description;
    this.langCode = builder._langCode;
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setFloorId(floorId) { this._floorId = floorId; return this; }
      setDescription(description) { this._description = description; return this; }
      setLangCode(langCode) { this._langCode = langCode; return this; }
      build() { return new UpdateRoomDto(this); }
    }
    return Builder;
  }
}
