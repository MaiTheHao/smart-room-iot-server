-- =========================================================
-- Migration 003: Alert System Tables
-- Adds 3 new tables: rule_action_alert, alert_instance, alert_recipient
-- =========================================================

-- Table: rule_action_alert
-- Alert configuration linked 1:N to a Rule.
-- If this row exists for a rule_id, alert firing is enabled for that rule.
CREATE TABLE IF NOT EXISTS `rule_action_alert` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `created_at`       datetime(6)  DEFAULT NULL,
  `created_by`       varchar(256) DEFAULT NULL,
  `updated_at`       datetime(6)  DEFAULT NULL,
  `updated_by`       varchar(256) DEFAULT NULL,
  `v`                bigint       NOT NULL DEFAULT 0,
  `rule_id`          bigint       NOT NULL,
  `alert_name`       varchar(256) NOT NULL,
  `severity`         varchar(50)  NOT NULL    COMMENT 'Enum: INFO | WARNING | CRITICAL',
  `recipient_groups` json         DEFAULT NULL COMMENT 'e.g. ["G_ADMIN","G_MAINTENANCE"]',
  `channels`         json         DEFAULT NULL COMMENT 'e.g. ["PUSH","EMAIL","SMS"]',
  `message_template` text         NOT NULL,
  `cooldown_minutes` int          NOT NULL DEFAULT 0,
  `auto_resolve`     tinyint(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_rule_action_alert_rule_id` (`rule_id`),
  CONSTRAINT `fk_rule_action_alert_rule`
    FOREIGN KEY (`rule_id`) REFERENCES `rule` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Table: alert_instance
-- Each triggered alert event. Lifecycle: ACTIVE -> ACKNOWLEDGED -> RESOLVED.
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
  `severity`         varchar(50)  NOT NULL    COMMENT 'Enum: INFO | WARNING | CRITICAL',
  `status`           varchar(50)  NOT NULL    COMMENT 'Enum: ACTIVE | ACKNOWLEDGED | RESOLVED',
  `triggered_at`     datetime(6)  NOT NULL,
  `acknowledged_at`  datetime(6)  DEFAULT NULL,
  `acknowledged_by`  bigint       DEFAULT NULL COMMENT 'FK to client.id: user who acknowledged',
  `resolved_at`      datetime(6)  DEFAULT NULL,
  `resolved_by`      bigint       DEFAULT NULL COMMENT 'FK to client.id: user who resolved. NULL = auto-resolved by system',
  PRIMARY KEY (`id`),
  KEY `idx_alert_instance_config_id`   (`alert_config_id`),
  KEY `idx_alert_instance_status`      (`status`),
  KEY `idx_alert_instance_status_time` (`status`, `triggered_at`),
  CONSTRAINT `fk_alert_instance_config`
    FOREIGN KEY (`alert_config_id`) REFERENCES `rule_action_alert` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_alert_instance_ack_by`
    FOREIGN KEY (`acknowledged_by`) REFERENCES `client` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_alert_instance_res_by`
    FOREIGN KEY (`resolved_by`)     REFERENCES `client` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Table: alert_recipient
-- Mapping which clients received notification for each alert.
-- Used by RBAC: G_USER queries join this table to return "My Alerts".
CREATE TABLE IF NOT EXISTS `alert_recipient` (
  `alert_id`  bigint NOT NULL,
  `client_id` bigint NOT NULL,
  PRIMARY KEY (`alert_id`, `client_id`),
  KEY `idx_alert_recipient_client_id` (`client_id`),
  CONSTRAINT `fk_alert_recipient_alert`
    FOREIGN KEY (`alert_id`)  REFERENCES `alert_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_alert_recipient_client`
    FOREIGN KEY (`client_id`) REFERENCES `client`         (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- Insert new system groups and functions introduced for Alert System
-- =========================================================

-- Insert missing system functions
INSERT INTO `sys_function` (`created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_code`) VALUES
  (NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_ROLE'),
  (NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ALERT_ALL'),
  (NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ALERT_GROUP'),
  (NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ALERT_OWN');

-- Insert translation for the new system functions
INSERT INTO `sys_function_lan` (`created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`) VALUES
  (NULL, NULL, NULL, NULL, 0, 'Quản lý các vai trò trong hệ thống', 'vi', 'Quản lý Vai Trò', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_MANAGE_ROLE')),
  (NULL, NULL, NULL, NULL, 0, 'Manage roles in system', 'en', 'Manage Role', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_MANAGE_ROLE')),
  (NULL, NULL, NULL, NULL, 0, 'Xem toàn bộ cảnh báo', 'vi', 'Truy cập Tất cả Cảnh báo', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT_ALL')),
  (NULL, NULL, NULL, NULL, 0, 'View all alerts', 'en', 'Access All Alerts', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT_ALL')),
  (NULL, NULL, NULL, NULL, 0, 'Xem cảnh báo của nhóm', 'vi', 'Truy cập Cảnh báo Nhóm', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT_GROUP')),
  (NULL, NULL, NULL, NULL, 0, 'View alerts of group', 'en', 'Access Group Alerts', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT_GROUP')),
  (NULL, NULL, NULL, NULL, 0, 'Chỉ xem cảnh báo cá nhân', 'vi', 'Truy cập Cảnh báo Cá nhân', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT_OWN')),
  (NULL, NULL, NULL, NULL, 0, 'Only view own alerts', 'en', 'Access Own Alerts', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT_OWN'));

-- Insert missing system groups
INSERT INTO `sys_group` (`created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `group_code`) VALUES
  (NOW(), 'system', NOW(), 'system', 0, 'G_USER'),
  (NOW(), 'system', NOW(), 'system', 0, 'G_MANAGER'),
  (NOW(), 'system', NOW(), 'system', 0, 'G_MAINTENANCE'),
  (NOW(), 'system', NOW(), 'system', 0, 'G_HARDWARE_GATEWAY');

-- Insert translations for the new groups
INSERT INTO `sys_group_lan` (`created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`) VALUES
  (NULL, NULL, NULL, NULL, 0, 'Người dùng thông thường', 'vi', 'Người dùng', (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_USER')),
  (NULL, NULL, NULL, NULL, 0, 'Regular user', 'en', 'User', (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_USER')),
  (NULL, NULL, NULL, NULL, 0, 'Quản lý hệ thống/tòa nhà', 'vi', 'Quản lý', (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_MANAGER')),
  (NULL, NULL, NULL, NULL, 0, 'System/building manager', 'en', 'Manager', (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_MANAGER')),
  (NULL, NULL, NULL, NULL, 0, 'Xem và xử lý cảnh báo kỹ thuật', 'vi', 'Nhân viên bảo trì', (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_MAINTENANCE')),
  (NULL, NULL, NULL, NULL, 0, 'View and handle technical alerts', 'en', 'Maintenance staff', (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_MAINTENANCE')),
  (NULL, NULL, NULL, NULL, 0, 'Thiết bị gateway phần cứng', 'vi', 'Cổng phần cứng', (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_HARDWARE_GATEWAY')),
  (NULL, NULL, NULL, NULL, 0, 'Hardware gateway devices', 'en', 'Hardware Gateway', (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_HARDWARE_GATEWAY'));

-- Assign all new functions to G_ADMIN
INSERT INTO `sys_role` (`created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_id`, `group_id`) VALUES
  (NOW(), 'system', NOW(), 'system', 0, (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_MANAGE_ROLE'), (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_ADMIN')),
  (NOW(), 'system', NOW(), 'system', 0, (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT_ALL'), (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_ADMIN')),
  (NOW(), 'system', NOW(), 'system', 0, (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT_GROUP'), (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_ADMIN')),
  (NOW(), 'system', NOW(), 'system', 0, (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT_OWN'), (SELECT `id` FROM `sys_group` WHERE `group_code` = 'G_ADMIN'));
