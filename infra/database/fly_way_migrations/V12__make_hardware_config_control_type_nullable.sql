-- =========================================================================
-- V12: Make hardware_config.control_type nullable
-- =========================================================================

ALTER TABLE `hardware_config` MODIFY COLUMN `control_type` varchar(256) NULL;
