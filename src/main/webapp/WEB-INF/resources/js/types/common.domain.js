export class ApiResponse {
  constructor(builder) {
    this.status = builder._status;
    this.message = builder._message;
    this.data = builder._data;
    this.timestamp = builder._timestamp;
  }

  static get Builder() {
    class Builder {
      setStatus(status) { this._status = status; return this; }
      setMessage(message) { this._message = message; return this; }
      setData(data) { this._data = data; return this; }
      setTimestamp(timestamp) { this._timestamp = timestamp; return this; }
      build() { return new ApiResponse(this); }
    }
    return Builder;
  }
}

export class PaginatedResponse {
  constructor(builder) {
    this.content = builder._content || [];
    this.page = builder._page;
    this.size = builder._size;
    this.totalElements = builder._totalElements;
    this.totalPages = builder._totalPages;
  }

  static get Builder() {
    class Builder {
      setContent(content) { this._content = content; return this; }
      setPage(page) { this._page = page; return this; }
      setSize(size) { this._size = size; return this; }
      setTotalElements(totalElements) { this._totalElements = totalElements; return this; }
      setTotalPages(totalPages) { this._totalPages = totalPages; return this; }
      build() { return new PaginatedResponse(this); }
    }
    return Builder;
  }
}

export class DomainValidationError extends Error {
  constructor(message, errors = {}) {
    super(message);
    this.name = 'DomainValidationError';
    this.errors = errors;
  }
}
