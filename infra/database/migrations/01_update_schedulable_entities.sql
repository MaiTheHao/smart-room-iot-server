-- Migration: Update Schedulable Entities (Automation & Rule V2)
-- Description: Adds is_interval and interval_seconds to support interval-based scheduling.
-- Compatibility: MariaDB / MySQL

USE smart_room_iot;

-- Update automation table
-- Note: automation already has is_interval and interval_seconds in some older definitions, 
-- but we ensure they exist and are correctly typed. 
-- Using a safe approach to add columns if they don't exist is tricky in standard SQL without procedures,
-- so we provide standard ALTER commands.

ALTER TABLE `automation` 
  MODIFY COLUMN `is_active` bit(1) NOT NULL DEFAULT b'1';

-- Update rule_v2 table
ALTER TABLE `rule_v2` 
  ADD COLUMN `is_interval` bit(1) NOT NULL DEFAULT b'0' AFTER `room_id`,
  ADD COLUMN `interval_seconds` int DEFAULT NULL AFTER `is_interval`;
