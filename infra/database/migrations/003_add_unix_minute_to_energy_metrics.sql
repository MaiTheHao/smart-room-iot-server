-- ============================================================
-- Migration: 003_add_unix_minute_to_energy_metrics.sql
-- Date      : 2026-04-18
-- Author    : antigravity
-- Scope     : Add unix_minute column and trigger to energy_metrics
-- ============================================================

-- 1. Add column
ALTER TABLE `energy_metrics`
ADD COLUMN `unix_minute` INT;

-- 2. Backfill data
UPDATE `energy_metrics`
SET `unix_minute` = UNIX_TIMESTAMP(`timestamp`) DIV 60;

-- 3. Create index
CREATE INDEX `idx_em_unix_minute` ON `energy_metrics` (`unix_minute`);

-- 4. Create trigger
DELIMITER //
CREATE TRIGGER `trg_em_before_insert` BEFORE INSERT
ON `energy_metrics` FOR EACH ROW
BEGIN
    SET NEW.unix_minute = UNIX_TIMESTAMP(NEW.timestamp) DIV 60;
END //
DELIMITER ;
