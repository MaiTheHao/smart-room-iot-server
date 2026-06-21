-- =========================================================
-- Migration 010: Rename source_ref_id to source_id in alert_config
-- =========================================================

ALTER TABLE `alert_config` RENAME COLUMN `source_ref_id` TO `source_id`;
