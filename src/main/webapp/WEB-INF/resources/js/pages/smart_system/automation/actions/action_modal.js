import { StateManager } from './state_manager.js';
import { UiRenderer } from './ui_renderer.js';
import { getAllRooms } from '../../../../api/room.api.js';
import { getDevicesByRoom } from '../../../../api/device.api.js';

export const ActionModal = (() => {
  let bootstrapModal = null;
  let isRoomsLoaded = false;

  const elements = {
    modal: null,
    form: null,
    title: null,
    localId: null,
    executionOrder: null,
    roomId: null,
    targetId: null,
    targetType: null,
    actionType: null,
  };

  const loadRooms = async () => {
    if (isRoomsLoaded) return;
    try {
      const [err, res] = await getAllRooms();
      if (!err && res && res.data) {
        const rooms = res.data;
        elements.roomId.innerHTML = '<option value="" disabled selected>Select a room</option>';
        rooms.forEach((room) => {
          const option = document.createElement('option');
          option.value = room.id;
          option.textContent = room.name;
          elements.roomId.appendChild(option);
        });
        isRoomsLoaded = true;
      }
    } catch (error) {
      console.error('Failed to load rooms', error);
    }
  };

  const loadDevices = async (roomId, category, selectedId = null) => {
    elements.targetId.disabled = true;
    elements.targetId.innerHTML = '<option value="" disabled selected>Loading devices...</option>';

    if (!roomId || !category) {
      elements.targetId.innerHTML =
        '<option value="" disabled selected>Please select room and category first</option>';
      return;
    }

    try {
      const [err, res] = await getDevicesByRoom(roomId, category);
      if (!err && res && res.data) {
        const devices = res.data;
        elements.targetId.innerHTML = '<option value="" disabled selected>Select a device</option>';

        if (devices.length === 0) {
          elements.targetId.innerHTML =
            '<option value="" disabled selected>No devices found</option>';
        } else {
          devices.forEach((device) => {
            const option = document.createElement('option');
            option.value = device.id;
            option.textContent = device.name;
            if (selectedId && String(device.id) === String(selectedId)) {
              option.selected = true;
            }
            elements.targetId.appendChild(option);
          });
          elements.targetId.disabled = false;
        }
      } else {
        elements.targetId.innerHTML =
          '<option value="" disabled selected>Error loading devices</option>';
      }
    } catch (error) {
      console.error('Failed to load devices', error);
      elements.targetId.innerHTML =
        '<option value="" disabled selected>Error loading devices</option>';
    }
  };

  const init = () => {
    elements.modal = document.getElementById('actionModal');
    elements.form = document.getElementById('actionForm');
    elements.title = document.getElementById('modalTitle');
    elements.localId = document.getElementById('actionLocalId');
    elements.executionOrder = document.getElementById('executionOrder');
    elements.roomId = document.getElementById('roomId');
    elements.targetId = document.getElementById('targetId');
    elements.targetType = document.getElementById('targetType');
    elements.actionType = document.getElementById('actionType');

    if (elements.modal) {
      bootstrapModal =
        typeof bootstrap !== 'undefined' ? new bootstrap.Modal(elements.modal) : null;
    }

    const handleDependencyChange = () => {
      const roomId = elements.roomId.value;
      const category = elements.targetType.value;
      loadDevices(roomId, category);
    };

    elements.roomId?.addEventListener('change', handleDependencyChange);
    elements.targetType?.addEventListener('change', handleDependencyChange);
  };

  const open = async (localId = null) => {
    elements.form.reset();
    elements.localId.value = '';
    elements.targetId.disabled = true;
    elements.targetId.innerHTML =
      '<option value="" disabled selected>Please select room and category first</option>';

    await loadRooms();

    if (localId) {
      const data = StateManager.getAction(localId);
      if (data) {
        elements.title.textContent = 'Edit Action';
        elements.localId.value = data._localId;
        elements.executionOrder.value = data.executionOrder;
        elements.targetType.value = data.targetType;
        elements.actionType.value = data.actionType;

        // Because we don't know the room of an existing targetId,
        // we create a custom option for it to retain the value without forcing room selection
        elements.roomId.value = '';
        elements.targetId.innerHTML = '';
        const option = document.createElement('option');
        option.value = data.targetId;
        option.textContent = data.targetName || `Device #${data.targetId} (Kept as is)`;
        option.selected = true;
        elements.targetId.appendChild(option);
        elements.targetId.disabled = false;
      }
    } else {
      elements.title.textContent = 'Add Action';
      elements.executionOrder.value = StateManager.getActions().length + 1;
    }

    bootstrapModal?.show();
    window.renderIcons?.();
  };

  const submit = (e) => {
    e.preventDefault();

    // Basic validation
    if (!elements.targetId.value) {
      elements.targetId.classList.add('is-invalid');
      return;
    } else {
      elements.targetId.classList.remove('is-invalid');
    }

    const selectedOption = elements.targetId.options[elements.targetId.selectedIndex];
    const targetName = selectedOption ? selectedOption.textContent : '';

    const data = {
      executionOrder: parseInt(elements.executionOrder.value, 10),
      targetId: parseInt(elements.targetId.value, 10),
      targetType: elements.targetType.value,
      actionType: elements.actionType.value,
      targetName: targetName,
    };

    const localId = elements.localId.value;
    if (localId) {
      StateManager.updateAction(localId, data);
    } else {
      StateManager.addAction(data);
    }

    UiRenderer.render();
    bootstrapModal?.hide();
  };

  return { init, open, submit };
})();
