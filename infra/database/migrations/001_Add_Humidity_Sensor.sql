-- Migration: Add Humidity Sensor and Metrics Tables
USE smart_room_iot;

CREATE TABLE `humidity_sensor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `is_active` BOOLEAN NOT NULL,
  `natural_id` varchar(256) NOT NULL,
  `specific_type` varchar(256) DEFAULT NULL,
  `current_humidity` double DEFAULT NULL,
  `hardware_config_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_humidity_sensor_natural_id` (`natural_id`),
  UNIQUE KEY `idx_humidity_sensor_hardware_config_id` (`hardware_config_id`),
  KEY `idx_humidity_sensor_room_id` (`room_id`),
  CONSTRAINT `fk_humidity_sensor_hardware_config` FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config` (`id`),
  CONSTRAINT `fk_humidity_sensor_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `humidity_sensor_lan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `description` text,
  `lang_code` varchar(10) NOT NULL,
  `name` varchar(256) NOT NULL,
  `owner_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_humidity_sensor_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_humidity_sensor_lan_humidity_sensor` FOREIGN KEY (`owner_id`) REFERENCES `humidity_sensor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `humidity_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `target_category` varchar(50) NOT NULL,
  `target_id` bigint NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `unix_minute` bigint NOT NULL,
  `humidity` double NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_humid_metrics_target` (`target_category`, `target_id`, `timestamp`),
  KEY `idx_humid_metrics_timestamp` (`timestamp`),
  KEY `idx_hm_unix_minute` (`unix_minute`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `temperature_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `target_category` varchar(50) NOT NULL,
  `target_id` bigint NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `unix_minute` bigint NOT NULL,
  `temperature` double NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_temp_metrics_target` (`target_category`, `target_id`, `timestamp`),
  KEY `idx_temp_metrics_timestamp` (`timestamp`),
  KEY `idx_tm_unix_minute` (`unix_minute`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
