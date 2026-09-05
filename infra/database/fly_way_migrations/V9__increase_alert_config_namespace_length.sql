-- =========================================================================
-- V9: Increase alert_config namespace column length to 100
-- =========================================================================

ALTER TABLE `alert_config` MODIFY COLUMN `namespace` varchar(100) NOT NULL;
