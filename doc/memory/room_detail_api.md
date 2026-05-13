# Room Detail API Specification

## 1. Core Endpoints

### 1.1. Temperature Data
- **Get Average History (Charts)**: `GET /api/v1/rooms/{roomId}/temperature-values/average?from={Instant}&to={Instant}`

### 1.2. Energy Metrics
- **Get Energy Metric History**: `GET /api/v1/metrics?domain=ENERGY&category={Category}&targetId={targetId}&from={Instant}&to={Instant}&latest=false`
- **Get Energy Metric Latest**: `GET /api/v1/metrics?domain=ENERGY&category={Category}&targetId={targetId}&latest=true`
    - Categories: `LIGHT`, `AIR_CONDITION`, `FAN`, `ROOM`.

### 1.3. Device List (Unified)
- **Get All Devices in Room**: `GET /api/v1/rooms/{roomId}/devices`
    - Returns a list of mixed devices (AC, Fan, Light) with their full metadata.

### 1.4. Device Control (Unified Control)
- **AC Control**: `PUT /api/v1/air-conditions/{naturalId}/control`
    - Body: `{ power, temperature, mode, fanSpeed, swing }`
    - constraints: temp 16-32, fanSpeed 0-5.
- **Fan Control**: `PUT /api/v1/fans/{naturalId}/control`
    - Body: `{ power, mode, speed, swing, light }`
    - constraints: speed 0-9999.
- **Light Control**: `PUT /api/v1/lights/{naturalId}/control`
    - Body: `{ power, level }`
    - constraints: level 0-100.

## 2. API JS Structure (Proposed)

### `temperature.api.js`
- `getAverageHistory(roomId, from, to)`

### `metric.api.js`
- `getEnergyMetricHistory(params)` - params: `{ category, targetId, from, to }`
- `getEnergyMetricLatest(params)` - params: `{ category, targetId }`

### `device.api.js`
- `getDevicesByRoom(roomId, category = null)`
- `controlAc(naturalId, data)`
- `controlFan(naturalId, data)`
- `controlLight(naturalId, data)`

## 3. Implementation Pattern
All API calls must use `import { request } from './http-client.js'` and return `[err, data]`.
Example:
```javascript
export const metricService = {
    getEnergyMetricHistory: (params) => {
        const query = new URLSearchParams({ domain: 'ENERGY', latest: false, ...params }).toString();
        return request(`/api/v1/metrics?${query}`);
    }
};
```
