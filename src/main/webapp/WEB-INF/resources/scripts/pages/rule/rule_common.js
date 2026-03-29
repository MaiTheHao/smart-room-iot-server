class RuleCommon {
  static PROPERTY_META = {
    FAN: {
      power: { type: 'enum', values: ['ON', 'OFF'] },
      mode: { type: 'enum', values: ['NORMAL', 'SLEEP', 'NATURAL'] },
      speed: { type: 'number' },
      swing: { type: 'enum', values: ['ON', 'OFF'] },
      light: { type: 'enum', values: ['ON', 'OFF'] },
    },
    AIR_CONDITION: {
      power: { type: 'enum', values: ['ON', 'OFF'] },
      temp: { type: 'number' },
      mode: { type: 'enum', values: ['COOL', 'HEAT', 'DRY', 'FAN', 'AUTO'] },
      fan_speed: { type: 'number' },
      swing: { type: 'enum', values: ['ON', 'OFF'] },
    },
    LIGHT: {
      power: { type: 'enum', values: ['ON', 'OFF'] },
      level: { type: 'number' },
    },
    TEMPERATURE: {
      temperature: { type: 'number' },
    },
    POWER_CONSUMPTION: {
      watt: { type: 'number' },
    },
    SYSTEM: {
      current_time: { type: 'number' },
      day_of_week: { type: 'number' },
      day_of_month: { type: 'number' },
    },
    ROOM: {
      avg_temperature: { type: 'number' },
      sum_watt: { type: 'number' },
    },
  };

  static async loadFloors(targetSelector) {
    try {
      const res = await window.floorApiV1Service.getAllWithoutPagination();
      const floors = res?.data || [];
      const $select = $(targetSelector);
      $select.empty().append('<option value="" disabled selected>Select floor</option>');
      floors.forEach((f) => $select.append(`<option value="${f.id}">${f.name}</option>`));
      $select.prop('disabled', false);
    } catch (error) {
      console.error(`API Error (loadFloors for ${targetSelector}):`, error);
      if (window.notify) window.notify.error('Failed to load floors');
    }
  }

  static async loadRooms(floorId, targetRoomSelector, targetDeviceSelector = null) {
    if (!floorId) return;
    try {
      const $select = $(targetRoomSelector);
      $select.prop('disabled', true).html('<option>Loading...</option>');

      const res = await window.roomApiV1Service.getAllByFloor(floorId);
      const rooms = res?.data || [];
      $select.empty().append('<option value="" disabled selected>Select room</option>');
      rooms.forEach((r) => $select.append(`<option value="${r.id}">${r.name}</option>`));
      $select.prop('disabled', false);

      if (targetDeviceSelector) {
        $(targetDeviceSelector)
          .prop('disabled', true)
          .html('<option value="" disabled selected>Select device/sensor</option>');
      }
    } catch (error) {
      console.error(`API Error (loadRooms for ${targetRoomSelector}):`, error);
      if (window.notify) window.notify.error('Failed to load rooms');
      $(targetRoomSelector)
        .prop('disabled', false)
        .html('<option value="" disabled selected>Select room</option>');
    }
  }

  static async loadDevices(roomId, dataSource, category, targetDeviceSelector) {
    if (!roomId) return;
    try {
      const $select = $(targetDeviceSelector);
      $select.prop('disabled', true).html('<option>Loading...</option>');

      let devices = [];
      if (dataSource === 'SENSOR') {
        if (category === 'TEMPERATURE') {
          const res = await window.temperatureApiV1Service.getAllByRoom(roomId);
          devices = (res?.data || []).map(d => ({ ...d, category: 'TEMPERATURE' }));
        } else if (category === 'POWER_CONSUMPTION') {
          const res = await window.powerConsumptionApiV1Service.getAllByRoom(roomId);
          devices = (res?.data || []).map(d => ({ ...d, category: 'POWER_CONSUMPTION' }));
        }
      } else {
        const res = await window.deviceMetadataApiV1Service.getAllByRoom(roomId, category);
        devices = res?.data || [];
      }

      $select.empty().append('<option value="" disabled selected>Select device/sensor</option>');

      if (devices.length === 0) {
        $select.append(`<option disabled>No devices found</option>`);
      } else {
        devices.forEach((d) =>
          $select.append(
            `<option value="${d.id}" data-category="${d.category}" data-natural-id="${d.naturalId || ''}">${d.name} (#${d.id})</option>`,
          ),
        );
      }
      $select.prop('disabled', false);
    } catch (error) {
      console.error('API Error (loadDevices):', error);
      if (window.notify) window.notify.error('Failed to load devices/sensors');
      $(targetDeviceSelector)
        .prop('disabled', false)
        .html('<option value="" disabled selected>Select device/sensor</option>');
    }
  }

  static escapeHtml(str) {
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }
}

window.RuleCommon = RuleCommon;
