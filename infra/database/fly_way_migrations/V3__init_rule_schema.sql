USE smart_room_iot;

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;
SET GLOBAL time_zone = '+00:00';
SET time_zone = '+00:00';

CREATE TABLE `rule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `name` varchar(256) NOT NULL,
  `priority` int NOT NULL,
  `is_active` BOOLEAN NOT NULL DEFAULT 1,
  `is_interval` BOOLEAN NOT NULL DEFAULT 0,
  `interval_seconds` int DEFAULT NULL,
  `cron_expression` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rule_status` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `rule_condition` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `rule_id` bigint NOT NULL,
  `sort_order` int NOT NULL,
  `data_source` varchar(256) NOT NULL,
  `resource_param` text DEFAULT NULL,
  `operator` varchar(5) NOT NULL,
  `value_param` varchar(256) NOT NULL,
  `next_logic` varchar(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rule_condition_rule_id` (`rule_id`),
  CONSTRAINT `fk_rule_condition_rule` FOREIGN KEY (`rule_id`) REFERENCES `rule` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `rule_action` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `rule_id` bigint NOT NULL,
  `execution_order` int DEFAULT NULL,
  `target_device_id` bigint NOT NULL,
  `target_device_category` varchar(256) NOT NULL,
  `action_params` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_rule_action_rule_id` (`rule_id`),
  KEY `idx_rule_action_target_device` (`target_device_id`),
  CONSTRAINT `fk_rule_action_rule` FOREIGN KEY (`rule_id`) REFERENCES `rule` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
