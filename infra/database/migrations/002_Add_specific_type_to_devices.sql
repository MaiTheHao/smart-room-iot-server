-- Migration: 002 — Add specific_type to all IoT entity tables

SET FOREIGN_KEY_CHECKS = 0;

-- 1. fan: rename type to specific_type
ALTER TABLE `fan`
    RENAME COLUMN `type` TO `specific_type`;

-- 2. air_condition: add specific_type and duration
ALTER TABLE `air_condition`
    ADD COLUMN `specific_type` VARCHAR(256) DEFAULT NULL,
    ADD COLUMN `duration` INT DEFAULT NULL;

-- 3. light: add specific_type
ALTER TABLE `light`
    ADD COLUMN `specific_type` VARCHAR(256) DEFAULT NULL;

-- 4. temperature: add specific_type
ALTER TABLE `temperature`
    ADD COLUMN `specific_type` VARCHAR(256) DEFAULT NULL;

-- 5. power_consumption: add specific_type
ALTER TABLE `power_consumption`
    ADD COLUMN `specific_type` VARCHAR(256) DEFAULT NULL;

SET FOREIGN_KEY_CHECKS = 1;
