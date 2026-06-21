-- =========================================================
-- Migration 012: Rename Alert Tables
-- Targets:
--   - alert_recipient -> alert_instance
--   - alert_incident_log -> alert_instance_log
--   - alert_recipient_group -> alert_instance_group
-- =========================================================

-- 1. Drop foreign keys referencing or belonging to the old tables
ALTER TABLE `alert_recipient_group` DROP FOREIGN KEY `fk_arg_alert`;
ALTER TABLE `alert_recipient_group` DROP FOREIGN KEY `fk_arg_sys_group`;

ALTER TABLE `alert_recipient` DROP FOREIGN KEY `fk_alert_recipient_config`;
ALTER TABLE `alert_recipient` DROP FOREIGN KEY `fk_alert_recipient_ack_by`;
ALTER TABLE `alert_recipient` DROP FOREIGN KEY `fk_alert_recipient_res_by`;

ALTER TABLE `alert_incident_log` DROP FOREIGN KEY `fk_ail_alert`;

-- 2. Rename the tables
RENAME TABLE `alert_recipient` TO `alert_instance`;
RENAME TABLE `alert_incident_log` TO `alert_instance_log`;
RENAME TABLE `alert_recipient_group` TO `alert_instance_group`;

-- 3. Rename indexes to match the new table names
ALTER TABLE `alert_instance` RENAME INDEX `idx_alert_recipient_config_id` TO `idx_alert_instance_config_id`;
ALTER TABLE `alert_instance` RENAME INDEX `idx_alert_recipient_status` TO `idx_alert_instance_status`;
ALTER TABLE `alert_instance` RENAME INDEX `idx_alert_recipient_status_time` TO `idx_alert_instance_status_time`;
ALTER TABLE `alert_instance` RENAME INDEX `idx_alert_recipient_triggered_at` TO `idx_alert_instance_triggered_at`;

ALTER TABLE `alert_instance_log` RENAME INDEX `idx_alert_incident_log_alert_id` TO `idx_alert_instance_log_alert_id`;
ALTER TABLE `alert_instance_log` RENAME INDEX `idx_alert_incident_log_action` TO `idx_alert_instance_log_action`;

ALTER TABLE `alert_instance_group` RENAME INDEX `idx_alert_recipient_group_group_id` TO `idx_alert_instance_group_group_id`;

-- 4. Re-create foreign keys with new names / referencing the renamed tables
ALTER TABLE `alert_instance`
  ADD CONSTRAINT `fk_alert_instance_config` FOREIGN KEY (`alert_config_id`) REFERENCES `alert_config` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_alert_instance_ack_by` FOREIGN KEY (`acknowledged_by`) REFERENCES `client` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_alert_instance_res_by` FOREIGN KEY (`resolved_by`) REFERENCES `client` (`id`) ON DELETE SET NULL;

ALTER TABLE `alert_instance_log`
  ADD CONSTRAINT `fk_ail_alert` FOREIGN KEY (`alert_id`) REFERENCES `alert_instance` (`id`) ON DELETE CASCADE;

ALTER TABLE `alert_instance_group`
  ADD CONSTRAINT `fk_aig_alert` FOREIGN KEY (`alert_id`) REFERENCES `alert_instance` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_aig_sys_group` FOREIGN KEY (`group_id`) REFERENCES `sys_group` (`id`) ON DELETE CASCADE;
