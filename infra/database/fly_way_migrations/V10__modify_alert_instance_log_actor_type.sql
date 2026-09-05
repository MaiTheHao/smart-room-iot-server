-- =========================================================================
-- V10: Modify alert_instance_log actor_type and action_type columns to VARCHAR(50)
-- =========================================================================

ALTER TABLE `alert_instance_log` MODIFY COLUMN `actor_type` varchar(50) NOT NULL;
ALTER TABLE `alert_instance_log` MODIFY COLUMN `action_type` varchar(50) NOT NULL;
