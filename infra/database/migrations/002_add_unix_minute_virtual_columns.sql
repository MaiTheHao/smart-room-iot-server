-- ============================================================
-- Migration: 002_add_unix_minute_virtual_columns.sql
-- Date      : 2026-04-16
-- ============================================================
-- 1. temperature_value
ALTER TABLE temperature_value
ADD COLUMN unix_minute INT;

UPDATE temperature_value
SET
    unix_minute = UNIX_TIMESTAMP(timestamp) DIV 60;

CREATE INDEX idx_tv_unix_minute ON temperature_value (unix_minute);

DELIMITER / /
CREATE TRIGGER trg_tv_before_insert BEFORE
INSERT
    ON temperature_value FOR EACH ROW BEGIN
SET
    NEW.unix_minute = UNIX_TIMESTAMP(NEW.timestamp) DIV 60;

END / / DELIMITER;

-- 2. power_consumption_value
ALTER TABLE power_consumption_value
ADD COLUMN unix_minute INT;

UPDATE power_consumption_value
SET
    unix_minute = UNIX_TIMESTAMP(timestamp) DIV 60;

CREATE INDEX idx_pcv_unix_minute ON power_consumption_value (unix_minute);

DELIMITER / /
CREATE TRIGGER trg_pcv_before_insert BEFORE
INSERT
    ON power_consumption_value FOR EACH ROW BEGIN
SET
    NEW.unix_minute = UNIX_TIMESTAMP(NEW.timestamp) DIV 60;

END / / DELIMITER;