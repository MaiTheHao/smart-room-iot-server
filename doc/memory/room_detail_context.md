# Room Detail Implementation Context

## 1. Mock Environment Analysis (Step 1)
- **Layout Reference**: `local/mock_fe_env/room.html`
- **Logic Reference**: `local/mock_fe_env/js/pages/room.js`
- **Key UI Components**:
    - Room Identity Card: Room Name, Description, Status, Health, ID.
    - Analytics Toolbar: Flatpickr range picker for filtering chart data.
    - Charts (ApexCharts): Temperature (Red, Area) and Power Usage (Amber, Area).
    - Device Tabs (Smart Controls):
        - **AC**: Power, Temp (16-32), Modes (Cool/Heat/Dry/Fan/Auto), Fan Speed (0-5), Swing.
        - **Fans**: Power, Speed (0-9999), Modes (Normal/Sleep/Natural), Swing, Light (State).
        - **Lights**: Power, Level (0-100).

## 2. SSR vs CSR Data Split (Step 2)
Based on `RoomDetailViewModel` and `RoomDetailViewServiceImpl`:

### Server-Side Rendered (SSR - Thymeleaf)
- `room`: Object containing `id`, `code`, `name`, `description`.
- `lastestAvgTemperature`: Current average temperature (Double).
- `lastestSumWatt`: Current sum of power usage (Double).

### Client-Side Rendered (CSR - AJAX/API)
- **Charts Data**: Historical temperature and energy metrics (Energy Domain) based on date range.
- **Devices List**: Fetching the unified list of devices (AC, Fan, Light) via `device_metadata` API.
- **Device Control**: Specific control APIs for AC, Fan, and Light using standardized request bodies.

## 3. Technology & Guidelines
- **Framework**: AdminLTE 4 (Bootstrap 5), Vanilla JS.
- **Icons**: Lucide Icons.
- **Charts**: ApexCharts.
- **Picker**: Flatpickr.
- **Standards**: No jQuery, use `http-client.js` for API calls, follow ESM/functional module pattern for JS.
