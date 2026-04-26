-- Migration: Rename device_control to hardware_config
-- Date: 2026-04-26
-- Compatible with MySQL 8.0+ and MariaDB 11.0+

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Rename the main table
RENAME TABLE `device_control` TO `hardware_config`;

-- 2. Rename columns
-- Rename control_type in hardware_config
ALTER TABLE `hardware_config` 
    CHANGE COLUMN `device_control_type` `control_type` varchar(256) NOT NULL;

-- Rename foreign key columns in referencing tables and preserve UNIQUE attribute
-- Note: 'UNIQUE' in CHANGE COLUMN may create a new index named 'hardware_config_id'
ALTER TABLE `air_condition` 
    CHANGE COLUMN `device_control_id` `hardware_config_id` bigint UNIQUE DEFAULT NULL;

ALTER TABLE `fan` 
    CHANGE COLUMN `device_control_id` `hardware_config_id` bigint UNIQUE DEFAULT NULL;

ALTER TABLE `light` 
    CHANGE COLUMN `device_control_id` `hardware_config_id` bigint UNIQUE DEFAULT NULL;

ALTER TABLE `temperature` 
    CHANGE COLUMN `device_control_id` `hardware_config_id` bigint UNIQUE DEFAULT NULL;

ALTER TABLE `power_consumption` 
    CHANGE COLUMN `device_control_id` `hardware_config_id` bigint UNIQUE DEFAULT NULL;

-- 3. Update Index Names and Cleanup Duplicates
-- First, rename the old descriptive indexes
ALTER TABLE `air_condition` RENAME INDEX `idx_air_condition_device_control_id` TO `idx_air_condition_hardware_config_id`;
ALTER TABLE `fan` RENAME INDEX `idx_fan_device_control_id` TO `idx_fan_hardware_config_id`;
ALTER TABLE `light` RENAME INDEX `idx_light_device_control_id` TO `idx_light_hardware_config_id`;
ALTER TABLE `temperature` RENAME INDEX `idx_temperature_device_control_id` TO `idx_temperature_hardware_config_id`;
ALTER TABLE `power_consumption` RENAME INDEX `idx_power_consumption_device_control_id` TO `idx_power_consumption_hardware_config_id`;

-- Rename indexes in hardware_config table
ALTER TABLE `hardware_config` RENAME INDEX `idx_device_control_client_id` TO `idx_hardware_config_client_id`;
ALTER TABLE `hardware_config` RENAME INDEX `idx_device_control_room_id` TO `idx_hardware_config_room_id`;

-- Drop duplicate indexes created by 'CHANGE COLUMN ... UNIQUE'
-- (MySQL often creates an index with the column name when 'UNIQUE' is specified)
ALTER TABLE `air_condition` DROP INDEX `hardware_config_id`;
ALTER TABLE `fan` DROP INDEX `hardware_config_id`;
ALTER TABLE `light` DROP INDEX `hardware_config_id`;
ALTER TABLE `temperature` DROP INDEX `hardware_config_id`;
ALTER TABLE `power_consumption` DROP INDEX `hardware_config_id`;

-- 4. Update Foreign Key Constraint Names for consistency
-- We drop old FKs and recreate them with names matching the new table/column names.
-- Note: MySQL 8.x does NOT support 'DROP FOREIGN KEY IF EXISTS', so we use standard DROP.

-- air_condition
ALTER TABLE `air_condition` DROP FOREIGN KEY `fk_air_condition_device_control`;
ALTER TABLE `air_condition` ADD CONSTRAINT `fk_air_condition_hardware_config` 
    FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config`(`id`);

-- fan
ALTER TABLE `fan` DROP FOREIGN KEY `fk_fan_device_control`;
ALTER TABLE `fan` ADD CONSTRAINT `fk_fan_hardware_config` 
    FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config`(`id`);

-- light
ALTER TABLE `light` DROP FOREIGN KEY `fk_light_device_control`;
ALTER TABLE `light` ADD CONSTRAINT `fk_light_hardware_config` 
    FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config`(`id`);

-- temperature
ALTER TABLE `temperature` DROP FOREIGN KEY `fk_temperature_device_control`;
ALTER TABLE `temperature` ADD CONSTRAINT `fk_temperature_hardware_config` 
    FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config`(`id`);

-- power_consumption
ALTER TABLE `power_consumption` DROP FOREIGN KEY `fk_power_consumption_device_control`;
ALTER TABLE `power_consumption` ADD CONSTRAINT `fk_power_consumption_hardware_config` 
    FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config`(`id`);

-- hardware_config (references to client and room)
ALTER TABLE `hardware_config` DROP FOREIGN KEY `fk_device_control_client`;
ALTER TABLE `hardware_config` ADD CONSTRAINT `fk_hardware_config_client` 
    FOREIGN KEY (`client_id`) REFERENCES `client`(`id`);

ALTER TABLE `hardware_config` DROP FOREIGN KEY `fk_device_control_room`;
ALTER TABLE `hardware_config` ADD CONSTRAINT `fk_hardware_config_room` 
    FOREIGN KEY (`room_id`) REFERENCES `room`(`id`);

SET FOREIGN_KEY_CHECKS = 1;
