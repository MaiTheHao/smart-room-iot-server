-- =========================================================
-- Migration 009: Alert System Performance Indexes
-- =========================================================

-- Index tối ưu tìm kiếm config theo namespace/source
-- (đã có UNIQUE KEY, không cần thêm)

-- Index tối ưu tìm kiếm event theo triggered_at (phân trang giảm dần)
ALTER TABLE `alert_recipient`
  ADD KEY `idx_alert_recipient_triggered_at` (`triggered_at` DESC);

-- Index tối ưu truy vấn audit log theo alert + thời gian (timeline UI)
ALTER TABLE `alert_incident_log`
  ADD KEY `idx_ail_alert_created_at` (`alert_id`, `created_at` DESC);
