-- =========================================================
-- Migration 007: Alert Incident Log & Deduplication
-- Mục tiêu:
--   1. ADD COLUMN trigger_count vào alert_recipient (ex alert_instance)
--   2. CREATE TABLE alert_incident_log (append-only audit log)
-- =========================================================

-- =========================================================
-- BƯỚC 1: Thêm cột trigger_count vào alert_recipient
-- Đếm số lần sự kiện lặp lại trong thời gian cooldown
-- =========================================================
ALTER TABLE `alert_recipient`
  ADD COLUMN `trigger_count` int NOT NULL DEFAULT 1
    COMMENT 'Số lần Rule match trong thời gian cooldown của sự cố này'
  AFTER `triggered_at`;

-- =========================================================
-- BƯỚC 2: CREATE TABLE alert_incident_log
-- Append-only audit log cho mọi thay đổi trạng thái
-- Polymorphic actor: USER | SYSTEM | EXTERNAL_API
-- =========================================================
CREATE TABLE IF NOT EXISTS `alert_incident_log` (
  `id`          bigint        NOT NULL AUTO_INCREMENT,
  `alert_id`    bigint        NOT NULL,
  `action_type` varchar(50)   NOT NULL COMMENT 'TRIGGERED|RE_TRIGGERED|ACKNOWLEDGED|RESOLVED|AUTO_RESOLVED',
  `actor_type`  varchar(50)   NOT NULL COMMENT 'USER|SYSTEM|EXTERNAL_API',
  `actor_id`    varchar(256)  NOT NULL COMMENT 'Client ID (nếu USER) hoặc tên process (RULE_ENGINE)',
  `message`     varchar(512)  NOT NULL COMMENT 'Nội dung mô tả để hiển thị trên timeline UI',
  `payload`     text          DEFAULT NULL COMMENT 'JSON dữ liệu telemetry vi phạm (schema-less)',
  `created_at`  datetime(6)   NOT NULL   COMMENT 'Thời điểm chính xác đến mili-giây',
  PRIMARY KEY (`id`),
  KEY `idx_alert_incident_log_alert_id` (`alert_id`),
  KEY `idx_alert_incident_log_action`   (`action_type`),
  CONSTRAINT `fk_ail_alert`
    FOREIGN KEY (`alert_id`) REFERENCES `alert_recipient` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
