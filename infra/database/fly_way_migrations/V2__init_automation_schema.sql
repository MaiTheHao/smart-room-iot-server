SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;
SET GLOBAL time_zone = '+00:00';
SET time_zone = '+00:00';

CREATE TABLE `automation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `cron_expression` varchar(256) DEFAULT NULL,
  `description` varchar(256) DEFAULT NULL,
  `is_active` BOOLEAN NOT NULL DEFAULT 1,
  `is_interval` BOOLEAN NOT NULL DEFAULT 0,
  `interval_seconds` int DEFAULT NULL,
  `name` varchar(256) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_auto_status` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `automation_action` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `action_type` varchar(256) NOT NULL,
  `execution_order` int DEFAULT NULL,
  `parameter_value` varchar(256) DEFAULT NULL,
  `target_id` bigint NOT NULL,
  `target_type` varchar(256) NOT NULL,
  `automation_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_automation_action_automation` FOREIGN KEY (`automation_id`) REFERENCES `automation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
