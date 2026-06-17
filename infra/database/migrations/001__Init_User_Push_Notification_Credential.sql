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
