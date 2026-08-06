export class FloorDto {
  constructor(builder) {
    this.id = builder._id;
    this.name = builder._name;
    this.code = builder._code;
    this.level = builder._level;
    this.description = builder._description;
    this.version = builder._version;
  }

  static get Builder() {
    class Builder {
      setId(id) { this._id = id; return this; }
      setName(name) { this._name = name; return this; }
      setCode(code) { this._code = code; return this; }
      setLevel(level) { this._level = level; return this; }
      setDescription(description) { this._description = description; return this; }
      setVersion(version) { this._version = version; return this; }
      build() { return new FloorDto(this); }
    }
    return Builder;
  }
}

export class CreateFloorDto {
  constructor(builder) {
    this.name = builder._name;
    this.code = builder._code;
    this.level = builder._level;
    this.description = builder._description;
    this.langCode = builder._langCode;
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setCode(code) { this._code = code; return this; }
      setLevel(level) { this._level = level; return this; }
      setDescription(description) { this._description = description; return this; }
      setLangCode(langCode) { this._langCode = langCode; return this; }
      build() { return new CreateFloorDto(this); }
    }
    return Builder;
  }
}

export class UpdateFloorDto {
  constructor(builder) {
    this.name = builder._name;
    this.level = builder._level;
    this.description = builder._description;
    this.langCode = builder._langCode;
  }

  static get Builder() {
    class Builder {
      setName(name) { this._name = name; return this; }
      setLevel(level) { this._level = level; return this; }
      setDescription(description) { this._description = description; return this; }
      setLangCode(langCode) { this._langCode = langCode; return this; }
      build() { return new UpdateFloorDto(this); }
    }
    return Builder;
  }
}
