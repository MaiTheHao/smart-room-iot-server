-- smart_room_iot.alert_config definition

CREATE TABLE `alert_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint(20) NOT NULL DEFAULT 0,
  `namespace` enum('RULE','GATEWAY','SYSTEM') NOT NULL,
  `alert_code` varchar(100) NOT NULL COMMENT 'Mã lỗi cụ thể trong namespace: SENSOR_VIOLATION | OFFLINE',
  `source_id` varchar(256) NOT NULL COMMENT 'ID string của entity nguồn (rule_id, gateway_id...)',
  `alert_name` varchar(256) NOT NULL,
  `severity` enum('INFO','WARNING','CRITICAL') NOT NULL,
  `channels` text DEFAULT NULL COMMENT 'JSON array: ["PUSH","EMAIL","SMS"]',
  `message_template` text NOT NULL,
  `cooldown_minutes` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_alert_config_polymorphic` (`namespace`,`alert_code`,`source_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- smart_room_iot.sys_function definition

CREATE TABLE `sys_function` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint(20) NOT NULL,
  `function_code` varchar(256) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_sys_function_code` (`function_code`)
) ENGINE=InnoDB AUTO_INCREMENT=213 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- smart_room_iot.sys_group definition

CREATE TABLE `sys_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint(20) NOT NULL,
  `group_code` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_sys_group_code` (`group_code`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- smart_room_iot.alert_config_group definition

CREATE TABLE `alert_config_group` (
  `alert_config_id` bigint(20) NOT NULL,
  `group_id` bigint(20) NOT NULL,
  PRIMARY KEY (`alert_config_id`,`group_id`),
  KEY `idx_alert_config_group_group_id` (`group_id`),
  CONSTRAINT `fk_acg_alert_config` FOREIGN KEY (`alert_config_id`) REFERENCES `alert_config` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_acg_sys_group` FOREIGN KEY (`group_id`) REFERENCES `sys_group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- smart_room_iot.sys_role definition

CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint(20) NOT NULL,
  `function_id` bigint(20) NOT NULL,
  `group_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_sys_role_group_function` (`group_id`,`function_id`),
  KEY `fk_sys_role_sys_function` (`function_id`),
  CONSTRAINT `fk_sys_role_sys_function` FOREIGN KEY (`function_id`) REFERENCES `sys_function` (`id`),
  CONSTRAINT `fk_sys_role_sys_group` FOREIGN KEY (`group_id`) REFERENCES `sys_group` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- smart_room_iot.alert_instance definition

CREATE TABLE `alert_instance` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint(20) NOT NULL DEFAULT 0,
  `alert_config_id` bigint(20) NOT NULL,
  `title` varchar(256) NOT NULL,
  `body` text NOT NULL,
  `severity` enum('INFO','WARNING','CRITICAL') NOT NULL,
  `status` enum('ACTIVE','ACKNOWLEDGED','RESOLVED') NOT NULL,
  `triggered_at` datetime(6) NOT NULL,
  `trigger_count` int(11) NOT NULL DEFAULT 1 COMMENT 'Số lần lặp trong cooldown',
  `acknowledged_at` datetime(6) DEFAULT NULL,
  `acknowledged_by` bigint(20) DEFAULT NULL COMMENT 'FK to client.id',
  `resolved_at` datetime(6) DEFAULT NULL,
  `resolved_by` bigint(20) DEFAULT NULL COMMENT 'FK to client.id',
  PRIMARY KEY (`id`),
  KEY `idx_alert_instance_config_id` (`alert_config_id`),
  KEY `idx_alert_instance_status` (`status`),
  KEY `idx_alert_instance_status_time` (`status`,`triggered_at`),
  KEY `idx_alert_instance_triggered_at` (`triggered_at` DESC),
  KEY `fk_alert_instance_ack_by` (`acknowledged_by`),
  KEY `fk_alert_instance_res_by` (`resolved_by`),
  CONSTRAINT `fk_alert_instance_ack_by` FOREIGN KEY (`acknowledged_by`) REFERENCES `client` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_alert_instance_config` FOREIGN KEY (`alert_config_id`) REFERENCES `alert_config` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_alert_instance_res_by` FOREIGN KEY (`resolved_by`) REFERENCES `client` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- smart_room_iot.alert_instance_group definition

CREATE TABLE `alert_instance_group` (
  `alert_id` bigint(20) NOT NULL,
  `group_id` bigint(20) NOT NULL,
  PRIMARY KEY (`alert_id`,`group_id`),
  KEY `idx_alert_instance_group_group_id` (`group_id`),
  CONSTRAINT `fk_aig_alert` FOREIGN KEY (`alert_id`) REFERENCES `alert_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_aig_sys_group` FOREIGN KEY (`group_id`) REFERENCES `sys_group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- smart_room_iot.alert_instance_log definition

CREATE TABLE `alert_instance_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `alert_id` bigint(20) NOT NULL,
  `action_type` enum('TRIGGERED','RE_TRIGGERED','ACKNOWLEDGED','RESOLVED','AUTO_RESOLVED') NOT NULL,
  `actor_type` enum('USER','SYSTEM','RULE_ENGINE') NOT NULL,
  `actor_id` varchar(256) NOT NULL COMMENT 'Client ID (USER) or process name (RULE_ENGINE)',
  `message` varchar(512) NOT NULL COMMENT 'Timeline UI description',
  `payload` text DEFAULT NULL COMMENT 'JSON telemetry data',
  `created_at` datetime(6) NOT NULL COMMENT 'Precision in ms',
  PRIMARY KEY (`id`),
  KEY `idx_alert_instance_log_alert_id` (`alert_id`),
  KEY `idx_alert_instance_log_action` (`action_type`),
  KEY `idx_ail_alert_created_at` (`alert_id`,`created_at` DESC),
  CONSTRAINT `fk_ail_alert` FOREIGN KEY (`alert_id`) REFERENCES `alert_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=239 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;