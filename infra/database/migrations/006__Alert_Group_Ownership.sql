-- =========================================================
-- Migration 006: Alert Group Ownership
-- Mục tiêu:
--   1. CREATE TABLE alert_recipient_group (event → group mapping)
-- =========================================================

-- =========================================================
-- BƯỚC 1: CREATE TABLE alert_recipient_group
-- Thay thế cơ chế lưu client_id cứng bằng group-based ownership
-- =========================================================
CREATE TABLE IF NOT EXISTS `alert_recipient_group` (
  `alert_id`  bigint NOT NULL,
  `group_id`  bigint NOT NULL,
  PRIMARY KEY (`alert_id`, `group_id`),
  KEY `idx_alert_recipient_group_group_id` (`group_id`),
  CONSTRAINT `fk_arg_alert`     FOREIGN KEY (`alert_id`)  REFERENCES `alert_recipient` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_arg_sys_group` FOREIGN KEY (`group_id`)  REFERENCES `sys_group`       (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
