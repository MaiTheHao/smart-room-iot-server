-- Migration: Add Rule Engine V2 Tables (Multi-Action & Scheduling Support)
-- Version: 2026-03-24
-- Description: Creates rule_v2, rule_condition_v2, and rule_action_v2 tables.
USE smart_room_iot;

SET
  FOREIGN_KEY_CHECKS = 0;

-- 1. Main Rule V2 table
CREATE TABLE IF NOT EXISTS
  `rule_v2` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `name` varchar(256) NOT NULL,
    `priority` int NOT NULL,
    `is_active` bit(1) NOT NULL DEFAULT(1),
    `room_id` bigint NOT NULL,
    `cron_expression` varchar(256) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_rule_v2_room` (`room_id`),
    KEY `idx_rule_v2_status` (`is_active`),
    CONSTRAINT `fk_rule_v2_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2. Condition table for Rule V2
CREATE TABLE IF NOT EXISTS
  `rule_condition_v2` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `rule_v2_id` bigint NOT NULL,
    `sort_order` int NOT NULL,
    `data_source` varchar(256) NOT NULL COMMENT 'Enum: SYSTEM, ROOM, DEVICE, SENSOR',
    `resource_param` text DEFAULT NULL COMMENT 'JSON storage',
    `operator` varchar(5) NOT NULL,
    `value_param` varchar(256) NOT NULL,
    `next_logic` varchar(3) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_rule_condition_v2_rule_id` (`rule_v2_id`),
    CONSTRAINT `fk_rule_condition_v2_rule` FOREIGN KEY (`rule_v2_id`) REFERENCES `rule_v2` (`id`) ON DELETE CASCADE
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 3. Multi-Action table for Rule V2
CREATE TABLE IF NOT EXISTS
  `rule_action_v2` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `created_by` varchar(256) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `updated_by` varchar(256) DEFAULT NULL,
    `v` bigint NOT NULL,
    `rule_v2_id` bigint NOT NULL,
    `execution_order` int DEFAULT NULL,
    `target_device_id` bigint NOT NULL,
    `target_device_category` varchar(256) NOT NULL COMMENT 'Enum: AIR_CONDITION, LIGHT, FAN',
    `action_params` text DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_rule_action_v2_rule_id` (`rule_v2_id`),
    KEY `idx_rule_action_v2_target_device` (`target_device_id`),
    CONSTRAINT `fk_rule_action_v2_rule` FOREIGN KEY (`rule_v2_id`) REFERENCES `rule_v2` (`id`) ON DELETE CASCADE
  ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

SET
  FOREIGN_KEY_CHECKS = 1;