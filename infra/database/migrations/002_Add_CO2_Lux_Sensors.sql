-- Migration: Add CO2 and Lux Sensor and Metrics Tables
USE smart_room_iot;

CREATE TABLE `co2_sensor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `is_active` BOOLEAN NOT NULL,
  `natural_id` varchar(256) NOT NULL,
  `specific_type` varchar(256) DEFAULT NULL,
  `current_co2` double DEFAULT NULL,
  `hardware_config_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_co2_sensor_natural_id` (`natural_id`),
  UNIQUE KEY `idx_co2_sensor_hardware_config_id` (`hardware_config_id`),
  KEY `idx_co2_sensor_room_id` (`room_id`),
  CONSTRAINT `fk_co2_sensor_hardware_config` FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config` (`id`),
  CONSTRAINT `fk_co2_sensor_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `co2_sensor_lan` (
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
  UNIQUE KEY `idx_co2_sensor_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_co2_sensor_lan_co2_sensor` FOREIGN KEY (`owner_id`) REFERENCES `co2_sensor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `co2_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_category` varchar(50) NOT NULL,
  `target_id` bigint NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `unix_minute` bigint NOT NULL,
  `co2` double NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_co2_metrics_target` (`target_category`, `target_id`, `timestamp`),
  KEY `idx_co2_metrics_timestamp` (`timestamp`),
  KEY `idx_co2m_unix_minute` (`unix_minute`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lux_sensor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `is_active` BOOLEAN NOT NULL,
  `natural_id` varchar(256) NOT NULL,
  `specific_type` varchar(256) DEFAULT NULL,
  `current_lux` double DEFAULT NULL,
  `hardware_config_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_lux_sensor_natural_id` (`natural_id`),
  UNIQUE KEY `idx_lux_sensor_hardware_config_id` (`hardware_config_id`),
  KEY `idx_lux_sensor_room_id` (`room_id`),
  CONSTRAINT `fk_lux_sensor_hardware_config` FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config` (`id`),
  CONSTRAINT `fk_lux_sensor_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lux_sensor_lan` (
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
  UNIQUE KEY `idx_lux_sensor_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_lux_sensor_lan_lux_sensor` FOREIGN KEY (`owner_id`) REFERENCES `lux_sensor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lux_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_category` varchar(50) NOT NULL,
  `target_id` bigint NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `unix_minute` bigint NOT NULL,
  `lux` double NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lux_metrics_target` (`target_category`, `target_id`, `timestamp`),
  KEY `idx_lux_metrics_timestamp` (`timestamp`),
  KEY `idx_luxm_unix_minute` (`unix_minute`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
