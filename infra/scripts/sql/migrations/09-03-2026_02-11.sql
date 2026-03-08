-- Migration: Drop unique index on device_control (client_id, gpio_pin, ble_mac_address)
-- Date: 09/03/2026 02:11
-- Description: Remove unique constraint idx_device_control_client_gpio_ble to allow
--              multiple devices to share the same GPIO pin (e.g. via IR blaster)
START TRANSACTION;

-- Step 1: Create index required for foreign key (client_id)
CREATE INDEX idx_device_control_client_id ON device_control (client_id);

-- Step 2: Drop the unique constraint
ALTER TABLE device_control
DROP INDEX idx_device_control_client_gpio_ble;

COMMIT;