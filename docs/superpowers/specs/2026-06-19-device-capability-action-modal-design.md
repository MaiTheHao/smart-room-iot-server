# Device Capability-Based Action Modal Design Spec

## Goal Description
Filter action parameters dynamically in the rule action modal based on the selected device's category and specific type (such as GPIO vs. IRSEND/IR_CTL), matching the capabilities defined in `DeviceCapabilityRegistry.java`. Additionally, improve the edit mode experience by resolving and auto-populating Floor and Room dropdowns when editing an existing action.

## Proposed Changes

### Frontend Changes

#### `device.api.js`
- Expose a new function `getAllDevices(category = null)` fetching `/api/v1/devices/all` to support device lookup on the frontend.

#### `action_modal.js`
- Define `DEVICE_CAPABILITIES` registry mimicking `DeviceCapabilityRegistry.java`.
- Load all devices via `getAllDevices` when opening the modal.
- Cache device lists and lookup `specificType` and `roomId` for selected or edited devices.
- Pre-populate Floor and Room select dropdowns on edit using `getRoomById` to retrieve the `floorId`.
- Add a `change` event listener to the target device select to re-render parameter inputs matching the selected device's `specificType`, retaining current inputs.

## Verification Plan
1. Open the Action Modal to add a new action. Verify all fields render based on category.
2. Select a room and device (e.g. GPIO Light vs IRSEND AC). Verify parameters change dynamically.
3. Edit an existing action. Verify that the correct Floor, Room, and Device are automatically selected, and only supported parameters are rendered.
4. Save the actions and check that the correct payloads are sent to the backend.
