-- =========================================================
-- Migration 011: Remove auto_resolve column from alert_config
-- =========================================================

ALTER TABLE `alert_config` DROP COLUMN `auto_resolve`;
