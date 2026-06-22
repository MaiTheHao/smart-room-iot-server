-- =========================================================
-- Consolidated Alert Polymorphic Hub Migration (005-012)
-- =========================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Create alert_config table
CREATE TABLE IF NOT EXISTS `alert_config` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `created_at`       datetime(6)  DEFAULT NULL,
  `created_by`       varchar(256) DEFAULT NULL,
  `updated_at`       datetime(6)  DEFAULT NULL,
  `updated_by`       varchar(256) DEFAULT NULL,
  `v`                bigint       NOT NULL DEFAULT 0,
  `namespace`        varchar(50)  NOT NULL COMMENT 'Domain phân vùng: RULE | GATEWAY | SYSTEM',
  `alert_code`       varchar(100) NOT NULL COMMENT 'Mã lỗi cụ thể trong namespace: SENSOR_VIOLATION | OFFLINE',
  `source_id`        varchar(256) NOT NULL COMMENT 'ID string của entity nguồn (rule_id, gateway_id...)',
  `alert_name`       varchar(256) NOT NULL,
  `severity`         varchar(50)  NOT NULL COMMENT 'Enum: INFO | WARNING | CRITICAL',
  `channels`         text         DEFAULT NULL COMMENT 'JSON array: ["PUSH","EMAIL","SMS"]',
  `message_template` text         NOT NULL,
  `cooldown_minutes` int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_alert_config_polymorphic` (`namespace`, `alert_code`, `source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Create alert_config_group table
CREATE TABLE IF NOT EXISTS `alert_config_group` (
  `alert_config_id` bigint NOT NULL,
  `group_id`        bigint NOT NULL,
  PRIMARY KEY (`alert_config_id`, `group_id`),
  KEY `idx_alert_config_group_group_id` (`group_id`),
  CONSTRAINT `fk_acg_alert_config` FOREIGN KEY (`alert_config_id`) REFERENCES `alert_config` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_acg_sys_group`    FOREIGN KEY (`group_id`)        REFERENCES `sys_group`    (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Create alert_instance table
CREATE TABLE IF NOT EXISTS `alert_instance` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `created_at`       datetime(6)  DEFAULT NULL,
  `created_by`       varchar(256) DEFAULT NULL,
  `updated_at`       datetime(6)  DEFAULT NULL,
  `updated_by`       varchar(256) DEFAULT NULL,
  `v`                bigint       NOT NULL DEFAULT 0,
  `alert_config_id`  bigint       NOT NULL,
  `title`            varchar(256) NOT NULL,
  `body`             text         NOT NULL,
  `severity`         varchar(50)  NOT NULL COMMENT 'Enum: INFO | WARNING | CRITICAL',
  `status`           varchar(50)  NOT NULL COMMENT 'Enum: ACTIVE | ACKNOWLEDGED | RESOLVED',
  `triggered_at`     datetime(6)  NOT NULL,
  `trigger_count`    int          NOT NULL DEFAULT 1 COMMENT 'Số lần lặp trong cooldown',
  `acknowledged_at`  datetime(6)  DEFAULT NULL,
  `acknowledged_by`  bigint       DEFAULT NULL COMMENT 'FK to client.id',
  `resolved_at`      datetime(6)  DEFAULT NULL,
  `resolved_by`      bigint       DEFAULT NULL COMMENT 'FK to client.id',
  PRIMARY KEY (`id`),
  KEY `idx_alert_instance_config_id`   (`alert_config_id`),
  KEY `idx_alert_instance_status`      (`status`),
  KEY `idx_alert_instance_status_time` (`status`, `triggered_at`),
  KEY `idx_alert_instance_triggered_at` (`triggered_at` DESC),
  CONSTRAINT `fk_alert_instance_config` FOREIGN KEY (`alert_config_id`) REFERENCES `alert_config` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_alert_instance_ack_by` FOREIGN KEY (`acknowledged_by`) REFERENCES `client` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_alert_instance_res_by` FOREIGN KEY (`resolved_by`) REFERENCES `client` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Create alert_instance_group table
CREATE TABLE IF NOT EXISTS `alert_instance_group` (
  `alert_id`  bigint NOT NULL,
  `group_id`  bigint NOT NULL,
  PRIMARY KEY (`alert_id`, `group_id`),
  KEY `idx_alert_instance_group_group_id` (`group_id`),
  CONSTRAINT `fk_aig_alert`     FOREIGN KEY (`alert_id`)  REFERENCES `alert_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_aig_sys_group` FOREIGN KEY (`group_id`)  REFERENCES `sys_group`      (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Create alert_instance_log table
CREATE TABLE IF NOT EXISTS `alert_instance_log` (
  `id`          bigint        NOT NULL AUTO_INCREMENT,
  `alert_id`    bigint        NOT NULL,
  `action_type` varchar(50)   NOT NULL COMMENT 'TRIGGERED|RE_TRIGGERED|ACKNOWLEDGED|RESOLVED|AUTO_RESOLVED',
  `actor_type`  varchar(50)   NOT NULL COMMENT 'USER|SYSTEM|EXTERNAL_API',
  `actor_id`    varchar(256)  NOT NULL COMMENT 'Client ID (USER) or process name (RULE_ENGINE)',
  `message`     varchar(512)  NOT NULL COMMENT 'Timeline UI description',
  `payload`     text          DEFAULT NULL COMMENT 'JSON telemetry data',
  `created_at`  datetime(6)   NOT NULL COMMENT 'Precision in ms',
  PRIMARY KEY (`id`),
  KEY `idx_alert_instance_log_alert_id` (`alert_id`),
  KEY `idx_alert_instance_log_action`   (`action_type`),
  KEY `idx_ail_alert_created_at` (`alert_id`, `created_at` DESC),
  CONSTRAINT `fk_ail_alert` FOREIGN KEY (`alert_id`) REFERENCES `alert_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Clean up old RBAC functions and insert updated ones
DELETE FROM `sys_role` WHERE `function_id` IN (
  SELECT `id` FROM `sys_function` WHERE `function_code` IN ('F_ACCESS_ALERT_ALL', 'F_ACCESS_ALERT_GROUP', 'F_ACCESS_ALERT_OWN')
);

DELETE FROM `sys_function_lan` WHERE `owner_id` IN (
  SELECT `id` FROM `sys_function` WHERE `function_code` IN ('F_ACCESS_ALERT_ALL', 'F_ACCESS_ALERT_GROUP', 'F_ACCESS_ALERT_OWN')
);

DELETE FROM `sys_function` WHERE `function_code` IN ('F_ACCESS_ALERT_ALL', 'F_ACCESS_ALERT_GROUP', 'F_ACCESS_ALERT_OWN');

INSERT INTO `sys_function` (`created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_code`) VALUES
  (NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ALERT'),
  (NOW(), 'system', NOW(), 'system', 0, 'F_HANDLE_ALERT');

INSERT INTO `sys_function_lan` (`created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`) VALUES
  (NULL, NULL, NULL, NULL, 0, 'Xem danh sách cảnh báo thuộc phạm vi nhóm của mình', 'vi', 'Xem Cảnh báo', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT')),
  (NULL, NULL, NULL, NULL, 0, 'View alerts within own group scope', 'en', 'Access Alerts', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT')),
  (NULL, NULL, NULL, NULL, 0, 'Xác nhận và giải quyết cảnh báo (Acknowledge/Resolve)', 'vi', 'Xử lý Cảnh báo', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_HANDLE_ALERT')),
  (NULL, NULL, NULL, NULL, 0, 'Acknowledge and resolve alerts', 'en', 'Handle Alerts', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_HANDLE_ALERT'));

INSERT INTO `sys_role` (`created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_id`, `group_id`) VALUES
  (NOW(), 'system', NOW(), 'system', 0, (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT'), (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_ADMIN')),
  (NOW(), 'system', NOW(), 'system', 0, (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT'), (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_MAINTENANCE')),
  (NOW(), 'system', NOW(), 'system', 0, (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT'), (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_USER')),
  (NOW(), 'system', NOW(), 'system', 0, (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_HANDLE_ALERT'), (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_ADMIN')),
  (NOW(), 'system', NOW(), 'system', 0, (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_HANDLE_ALERT'), (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_MAINTENANCE'));

SET FOREIGN_KEY_CHECKS = 1;
