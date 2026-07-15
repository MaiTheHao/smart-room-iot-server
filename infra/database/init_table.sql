USE smart_room_iot;

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;
SET GLOBAL time_zone = '+00:00';
SET time_zone = '+00:00';

CREATE TABLE `language` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `code` varchar(10) NOT NULL,
  `description` varchar(256) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_language_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `client` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `avatar_url` varchar(256) DEFAULT NULL,
  `client_type` varchar(256) DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `mac_address` varchar(100) DEFAULT NULL,
  `password_hash` varchar(256) DEFAULT NULL,
  `access_token` varchar(1024) DEFAULT NULL,
  `gateway_password` varchar(255) DEFAULT NULL,
  `username` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`),
  UNIQUE KEY `idx_client_type_ip_address` (`client_type`, `ip_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `client_device` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `client_id` bigint NOT NULL,
  `fcm_token` varchar(512) NOT NULL,
  `device_identifier` varchar(255) NOT NULL,
  `platform` varchar(50) DEFAULT NULL,
  `last_updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_fcm_token` (`fcm_token`),
  UNIQUE KEY `idx_device_identifier` (`device_identifier`),
  KEY `idx_client_id` (`client_id`),
  CONSTRAINT `fk_client_device_client` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `sys_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `group_code` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_sys_group_code` (`group_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `sys_group_lan` (
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
  UNIQUE KEY `idx_sys_group_lan_owner_lang` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_sys_group_lan_sys_group` FOREIGN KEY (`owner_id`) REFERENCES `sys_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `client_group` (
  `client_id` bigint NOT NULL,
  `group_id` bigint NOT NULL,
  PRIMARY KEY (`client_id`, `group_id`),
  KEY `idx_client_group_group_id` (`group_id`),
  CONSTRAINT `fk_client_group_client` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`),
  CONSTRAINT `fk_client_group_sys_group` FOREIGN KEY (`group_id`) REFERENCES `sys_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `sys_function` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `function_code` varchar(256) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_sys_function_code` (`function_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `sys_function_lan` (
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
  UNIQUE KEY `idx_sys_function_lan_owner_lang` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_sys_function_lan_sys_function` FOREIGN KEY (`owner_id`) REFERENCES `sys_function` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `function_id` bigint NOT NULL,
  `group_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_sys_role_group_function` (`group_id`, `function_id`),
  CONSTRAINT `fk_sys_role_sys_function` FOREIGN KEY (`function_id`) REFERENCES `sys_function` (`id`),
  CONSTRAINT `fk_sys_role_sys_group` FOREIGN KEY (`group_id`) REFERENCES `sys_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `persistent_logins` (
  `series` varchar(64) NOT NULL,
  `last_used` datetime(6) NOT NULL,
  `token` varchar(64) NOT NULL,
  `username` varchar(64) NOT NULL,
  PRIMARY KEY (`series`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `floor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `code` varchar(256) NOT NULL,
  `level` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_floor_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `floor_lan` (
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
  UNIQUE KEY `idx_floor_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_floor_lan_floor` FOREIGN KEY (`owner_id`) REFERENCES `floor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `code` varchar(256) NOT NULL,
  `floor_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_room_code` (`code`),
  CONSTRAINT `fk_room_floor` FOREIGN KEY (`floor_id`) REFERENCES `floor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `room_lan` (
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
  UNIQUE KEY `idx_room_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_room_lan_room` FOREIGN KEY (`owner_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `hardware_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `api_endpoint` varchar(256) DEFAULT NULL,
  `ble_mac_address` varchar(100) DEFAULT NULL,
  `control_type` varchar(256) NOT NULL,
  `gpio_pin` int NOT NULL,
  `client_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_hardware_config_client` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`),
  CONSTRAINT `fk_hardware_config_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `air_condition` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `is_active` BOOLEAN NOT NULL,
  `natural_id` varchar(256) NOT NULL,
  `specific_type` varchar(256) DEFAULT NULL,
  `duration` int DEFAULT NULL,
  `fan_speed` int DEFAULT NULL,
  `mode` varchar(256) DEFAULT NULL,
  `power` varchar(256) DEFAULT NULL,
  `swing` varchar(256) DEFAULT NULL,
  `temperature` int DEFAULT NULL,
  `hardware_config_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_air_condition_natural_id` (`natural_id`),
  UNIQUE KEY `idx_air_condition_hardware_config_id` (`hardware_config_id`),
  KEY `idx_air_condition_room_id` (`room_id`),
  CONSTRAINT `fk_air_condition_hardware_config` FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config` (`id`),
  CONSTRAINT `fk_air_condition_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `air_condition_lan` (
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
  UNIQUE KEY `idx_air_condition_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_air_condition_lan_air_condition` FOREIGN KEY (`owner_id`) REFERENCES `air_condition` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `fan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `is_active` BOOLEAN NOT NULL,
  `natural_id` varchar(256) NOT NULL,
  `power` varchar(256) DEFAULT NULL,
  `specific_type` varchar(256) NOT NULL,
  `duration` int DEFAULT NULL,
  `speed` int DEFAULT NULL,
  `mode` varchar(256) DEFAULT NULL,
  `light` varchar(256) DEFAULT NULL,
  `swing` varchar(256) DEFAULT NULL,
  `hardware_config_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_fan_natural_id` (`natural_id`),
  UNIQUE KEY `idx_fan_hardware_config_id` (`hardware_config_id`),
  KEY `idx_fan_room_id` (`room_id`),
  CONSTRAINT `fk_fan_hardware_config` FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config` (`id`),
  CONSTRAINT `fk_fan_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `fan_lan` (
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
  UNIQUE KEY `idx_fan_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_fan_lan_fan` FOREIGN KEY (`owner_id`) REFERENCES `fan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `light` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `is_active` BOOLEAN NOT NULL,
  `natural_id` varchar(256) NOT NULL,
  `specific_type` varchar(256) DEFAULT NULL,
  `power` varchar(256) DEFAULT NULL,
  `level` int DEFAULT NULL,
  `hardware_config_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_light_natural_id` (`natural_id`),
  UNIQUE KEY `idx_light_hardware_config_id` (`hardware_config_id`),
  KEY `idx_light_room_id` (`room_id`),
  CONSTRAINT `fk_light_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`),
  CONSTRAINT `fk_light_hardware_config` FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `light_lan` (
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
  UNIQUE KEY `idx_light_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_light_lan_light` FOREIGN KEY (`owner_id`) REFERENCES `light` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `temperature` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `is_active` BOOLEAN NOT NULL,
  `natural_id` varchar(256) NOT NULL,
  `specific_type` varchar(256) DEFAULT NULL,
  `current_value` double DEFAULT NULL,
  `hardware_config_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_temperature_natural_id` (`natural_id`),
  UNIQUE KEY `idx_temperature_hardware_config_id` (`hardware_config_id`),
  KEY `idx_temperature_room_id` (`room_id`),
  CONSTRAINT `fk_temperature_hardware_config` FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config` (`id`),
  CONSTRAINT `fk_temperature_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `temperature_lan` (
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
  UNIQUE KEY `idx_temperature_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_temperature_lan_temperature` FOREIGN KEY (`owner_id`) REFERENCES `temperature` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `temperature_value` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `timestamp` datetime(6) NOT NULL,
  `temp_c` double DEFAULT NULL,
  `sensor_id` bigint NOT NULL,
  `unix_minute` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_sensor_timestamp` (`sensor_id`, `timestamp`),
  KEY `idx_timestamp` (`timestamp`),
  KEY `idx_tv_unix_minute` (`unix_minute`),
  CONSTRAINT `fk_temperature_value_temperature` FOREIGN KEY (`sensor_id`) REFERENCES `temperature` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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



CREATE TABLE `power_consumption` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `is_active` BOOLEAN NOT NULL,
  `natural_id` varchar(256) NOT NULL,
  `specific_type` varchar(256) DEFAULT NULL,
  `current_watt` double DEFAULT NULL,
  `hardware_config_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_power_consumption_natural_id` (`natural_id`),
  UNIQUE KEY `idx_power_consumption_hardware_config_id` (`hardware_config_id`),
  KEY `idx_power_consumption_room_id` (`room_id`),
  CONSTRAINT `fk_power_consumption_hardware_config` FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config` (`id`),
  CONSTRAINT `fk_power_consumption_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `power_consumption_lan` (
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
  UNIQUE KEY `idx_power_consumption_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_power_consumption_lan_power_consumption` FOREIGN KEY (`owner_id`) REFERENCES `power_consumption` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `energy_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_category` varchar(50) NOT NULL,
  `target_id` bigint NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `voltage` double DEFAULT NULL,
  `current` double DEFAULT NULL,
  `power` double DEFAULT NULL,
  `energy` double DEFAULT NULL,
  `frequency` double DEFAULT NULL,
  `power_factor` double DEFAULT NULL,
  `unix_minute` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_energy_metrics_target` (`target_category`, `target_id`, `timestamp`),
  KEY `idx_energy_metrics_timestamp` (`timestamp`),
  KEY `idx_em_unix_minute` (`unix_minute`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `device_status_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_category` varchar(50) NOT NULL,
  `target_id` bigint NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `status_data` text DEFAULT NULL,
  `unix_minute` bigint NOT NULL,
  `device_version` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_device_status_metrics_target` (`target_category`, `target_id`, `timestamp`),
  KEY `idx_device_status_metrics_timestamp` (`timestamp`),
  KEY `idx_dsm_unix_minute` (`unix_minute`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for humidity_metrics
-- ----------------------------
DROP TABLE IF EXISTS `humidity_metrics`;
CREATE TABLE `humidity_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
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

-- ----------------------------
-- Table structure for temperature_metrics
-- ----------------------------
DROP TABLE IF EXISTS `temperature_metrics`;
CREATE TABLE `temperature_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
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

CREATE TABLE `QRTZ_JOB_DETAILS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) NOT NULL,
  `IS_DURABLE` BOOLEAN NOT NULL,
  `IS_NONCONCURRENT` BOOLEAN NOT NULL,
  `IS_UPDATE_DATA` BOOLEAN NOT NULL,
  `REQUESTS_RECOVERY` BOOLEAN NOT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint DEFAULT NULL,
  `PREV_FIRE_TIME` bigint DEFAULT NULL,
  `PRIORITY` int DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) NOT NULL,
  `TRIGGER_TYPE` varchar(8) NOT NULL,
  `START_TIME` bigint NOT NULL,
  `END_TIME` bigint DEFAULT NULL,
  `CALENDAR_NAME` varchar(200) DEFAULT NULL,
  `MISFIRE_INSTR` smallint DEFAULT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`) REFERENCES `QRTZ_JOB_DETAILS` (`SCHED_NAME`, `JOB_NAME`, `JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_SIMPLE_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `REPEAT_COUNT` bigint NOT NULL,
  `REPEAT_INTERVAL` bigint NOT NULL,
  `TIMES_TRIGGERED` bigint NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_CRON_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `CRON_EXPRESSION` varchar(120) NOT NULL,
  `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_SIMPROP_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `STR_PROP_1` varchar(512) DEFAULT NULL,
  `STR_PROP_2` varchar(512) DEFAULT NULL,
  `STR_PROP_3` varchar(512) DEFAULT NULL,
  `INT_PROP_1` int DEFAULT NULL,
  `INT_PROP_2` int DEFAULT NULL,
  `LONG_PROP_1` bigint DEFAULT NULL,
  `LONG_PROP_2` bigint DEFAULT NULL,
  `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
  `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
  `BOOL_PROP_1` BOOLEAN DEFAULT NULL,
  `BOOL_PROP_2` BOOLEAN DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_BLOB_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `BLOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`) REFERENCES `QRTZ_TRIGGERS` (`SCHED_NAME`, `TRIGGER_NAME`, `TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_CALENDARS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `CALENDAR_NAME` varchar(200) NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_PAUSED_TRIGGER_GRPS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_FIRED_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `ENTRY_ID` varchar(95) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `FIRED_TIME` bigint NOT NULL,
  `SCHED_TIME` bigint NOT NULL,
  `PRIORITY` int NOT NULL,
  `STATE` varchar(16) NOT NULL,
  `JOB_NAME` varchar(200) DEFAULT NULL,
  `JOB_GROUP` varchar(200) DEFAULT NULL,
  `IS_NONCONCURRENT` BOOLEAN DEFAULT NULL,
  `REQUESTS_RECOVERY` BOOLEAN DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_SCHEDULER_STATE` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `LAST_CHECKIN_TIME` bigint NOT NULL,
  `CHECKIN_INTERVAL` bigint NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `QRTZ_LOCKS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `LOCK_NAME` varchar(40) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `alert_config` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `created_at`       datetime(6)  DEFAULT NULL,
  `created_by`       varchar(256) DEFAULT NULL,
  `updated_at`       datetime(6)  DEFAULT NULL,
  `updated_by`       varchar(256) DEFAULT NULL,
  `v`                bigint       NOT NULL DEFAULT 0,
  `namespace`        varchar(50)  NOT NULL,
  `alert_code`       varchar(100) NOT NULL,
  `source_id`        varchar(256) NOT NULL,
  `alert_name`       varchar(256) NOT NULL,
  `severity`         varchar(50)  NOT NULL,
  `channels`         text         DEFAULT NULL,
  `message_template` text         NOT NULL,
  `cooldown_minutes` int          NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_alert_config_polymorphic` (`namespace`, `alert_code`, `source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `alert_config_group` (
  `alert_config_id` bigint NOT NULL,
  `group_id`        bigint NOT NULL,
  PRIMARY KEY (`alert_config_id`, `group_id`),
  KEY `idx_alert_config_group_group_id` (`group_id`),
  CONSTRAINT `fk_acg_alert_config` FOREIGN KEY (`alert_config_id`) REFERENCES `alert_config` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_acg_sys_group`    FOREIGN KEY (`group_id`)        REFERENCES `sys_group`    (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `alert_instance` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `created_at`       datetime(6)  DEFAULT NULL,
  `created_by`       varchar(256) DEFAULT NULL,
  `updated_at`       datetime(6)  DEFAULT NULL,
  `updated_by`       varchar(256) DEFAULT NULL,
  `v`                bigint       NOT NULL DEFAULT 0,
  `alert_config_id`  bigint       NOT NULL,
  `title`            varchar(256) NOT NULL,
  `body`             text         NOT NULL,
  `severity`         varchar(50)  NOT NULL,
  `status`           varchar(50)  NOT NULL,
  `triggered_at`     datetime(6)  NOT NULL,
  `trigger_count`    int          NOT NULL DEFAULT 1,
  `acknowledged_at`  datetime(6)  DEFAULT NULL,
  `acknowledged_by`  bigint       DEFAULT NULL,
  `resolved_at`      datetime(6)  DEFAULT NULL,
  `resolved_by`      bigint       DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_alert_instance_config_id`   (`alert_config_id`),
  KEY `idx_alert_instance_status`      (`status`),
  KEY `idx_alert_instance_status_time` (`status`, `triggered_at`),
  KEY `idx_alert_instance_triggered_at` (`triggered_at` DESC),
  CONSTRAINT `fk_alert_instance_config` FOREIGN KEY (`alert_config_id`) REFERENCES `alert_config` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_alert_instance_ack_by` FOREIGN KEY (`acknowledged_by`) REFERENCES `client` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_alert_instance_res_by` FOREIGN KEY (`resolved_by`) REFERENCES `client` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `alert_instance_group` (
  `alert_id`  bigint NOT NULL,
  `group_id`  bigint NOT NULL,
  PRIMARY KEY (`alert_id`, `group_id`),
  KEY `idx_alert_instance_group_group_id` (`group_id`),
  CONSTRAINT `fk_aig_alert`     FOREIGN KEY (`alert_id`)  REFERENCES `alert_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_aig_sys_group` FOREIGN KEY (`group_id`)  REFERENCES `sys_group`      (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `alert_instance_log` (
  `id`          bigint        NOT NULL AUTO_INCREMENT,
  `alert_id`    bigint        NOT NULL,
  `action_type` varchar(50)   NOT NULL,
  `actor_type`  varchar(50)   NOT NULL,
  `actor_id`    varchar(256)  NOT NULL,
  `message`     varchar(512)  NOT NULL,
  `payload`     text          DEFAULT NULL,
  `created_at`  datetime(6)   NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_alert_instance_log_alert_id` (`alert_id`),
  KEY `idx_alert_instance_log_action`   (`action_type`),
  KEY `idx_ail_alert_created_at` (`alert_id`, `created_at` DESC),
  CONSTRAINT `fk_ail_alert` FOREIGN KEY (`alert_id`) REFERENCES `alert_instance` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


SET FOREIGN_KEY_CHECKS = 1;