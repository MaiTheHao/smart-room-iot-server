-- =========================================================
-- Migration 005: Alert Polymorphic Hub
-- Mục tiêu:
--   1. Xóa sạch các bảng cũ từ migration 003, 004
--   2. Tạo các bảng mới tinh gọn: alert_config, alert_config_group, alert_recipient
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================
-- BƯỚC 1: DROP các bảng cũ để tạo mới cho sạch
-- =========================================================
DROP TABLE IF EXISTS `alert_recipient`;
DROP TABLE IF EXISTS `alert_instance`;
DROP TABLE IF EXISTS `rule_action_alert`;

-- =========================================================
-- BƯỚC 2: CREATE TABLE alert_config (Polymorphic Alert Hub)
-- =========================================================
CREATE TABLE IF NOT EXISTS `alert_config` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `created_at`       datetime(6)  DEFAULT NULL,
  `created_by`       varchar(256) DEFAULT NULL,
  `updated_at`       datetime(6)  DEFAULT NULL,
  `updated_by`       varchar(256) DEFAULT NULL,
  `v`                bigint       NOT NULL DEFAULT 0,
  `namespace`        varchar(50)  NOT NULL COMMENT 'Domain phân vùng: RULE | GATEWAY | SYSTEM',
  `alert_code`       varchar(100) NOT NULL COMMENT 'Mã lỗi cụ thể trong namespace: SENSOR_VIOLATION | OFFLINE',
  `source_ref_id`    varchar(256) NOT NULL COMMENT 'ID string của entity nguồn (rule_id, gateway_id...)',
  `alert_name`       varchar(256) NOT NULL,
  `severity`         varchar(50)  NOT NULL COMMENT 'Enum: INFO | WARNING | CRITICAL',
  `channels`         text         DEFAULT NULL COMMENT 'JSON array: ["PUSH","EMAIL","SMS"]',
  `message_template` text         NOT NULL,
  `cooldown_minutes` int          NOT NULL DEFAULT 0,
  `auto_resolve`     tinyint(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_alert_config_polymorphic` (`namespace`, `alert_code`, `source_ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- BƯỚC 3: CREATE TABLE alert_config_group
-- =========================================================
CREATE TABLE IF NOT EXISTS `alert_config_group` (
  `alert_config_id` bigint NOT NULL,
  `group_id`        bigint NOT NULL,
  PRIMARY KEY (`alert_config_id`, `group_id`),
  KEY `idx_alert_config_group_group_id` (`group_id`),
  CONSTRAINT `fk_acg_alert_config` FOREIGN KEY (`alert_config_id`) REFERENCES `alert_config` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_acg_sys_group`    FOREIGN KEY (`group_id`)        REFERENCES `sys_group`    (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- BƯỚC 4: CREATE TABLE alert_recipient (thay thế alert_instance)
-- =========================================================
CREATE TABLE IF NOT EXISTS `alert_recipient` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `created_at`       datetime(6)  DEFAULT NULL,
  `created_by`       varchar(256) DEFAULT NULL,
  `updated_at`       datetime(6)  DEFAULT NULL,
  `updated_by`       varchar(256) DEFAULT NULL,
  `v`                bigint       NOT NULL DEFAULT 0,
  `alert_config_id`  bigint       NOT NULL,
  `title`            varchar(256) NOT NULL,
  `body`             text         NOT NULL,
  `severity`         varchar(50)  NOT NULL    COMMENT 'Enum: INFO | WARNING | CRITICAL',
  `status`           varchar(50)  NOT NULL    COMMENT 'Enum: ACTIVE | ACKNOWLEDGED | RESOLVED',
  `triggered_at`     datetime(6)  NOT NULL,
  `acknowledged_at`  datetime(6)  DEFAULT NULL,
  `acknowledged_by`  bigint       DEFAULT NULL COMMENT 'FK to client.id: user who acknowledged',
  `resolved_at`      datetime(6)  DEFAULT NULL,
  `resolved_by`      bigint       DEFAULT NULL COMMENT 'FK to client.id: user who resolved. NULL = auto-resolved by system',
  PRIMARY KEY (`id`),
  KEY `idx_alert_recipient_config_id`   (`alert_config_id`),
  KEY `idx_alert_recipient_status`      (`status`),
  KEY `idx_alert_recipient_status_time` (`status`, `triggered_at`),
  CONSTRAINT `fk_alert_recipient_config`
    FOREIGN KEY (`alert_config_id`) REFERENCES `alert_config` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_alert_recipient_ack_by`
    FOREIGN KEY (`acknowledged_by`) REFERENCES `client` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_alert_recipient_res_by`
    FOREIGN KEY (`resolved_by`)     REFERENCES `client` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
