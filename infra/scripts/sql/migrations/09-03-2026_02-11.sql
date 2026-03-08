-- Migration: Drop unique index on device_control (client_id, gpio_pin, ble_mac_address)
-- Date: 09/03/2026 02:11
-- Description: Remove unique constraint idx_device_control_client_gpio_ble to allow
--              multiple devices to share the same GPIO pin (e.g. via IR blaster)
ALTER TABLE `device_control`
DROP INDEX `idx_device_control_client_gpio_ble`;