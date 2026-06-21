-- =========================================================
-- Migration 008: Alert RBAC Cleanup
-- Mục tiêu:
--   1. Xóa 3 function cũ (chồng chéo): F_ACCESS_ALERT_ALL, _GROUP, _OWN
--   2. Thêm 2 function mới tinh gọn: F_ACCESS_ALERT, F_HANDLE_ALERT
--   3. Gán quyền cho các nhóm phù hợp
-- =========================================================

-- =========================================================
-- BƯỚC 1: Xóa sys_role trỏ vào các function cũ
-- =========================================================
DELETE FROM `sys_role`
WHERE `function_id` IN (
  SELECT `id` FROM `sys_function`
  WHERE `function_code` IN ('F_ACCESS_ALERT_ALL', 'F_ACCESS_ALERT_GROUP', 'F_ACCESS_ALERT_OWN')
);

-- =========================================================
-- BƯỚC 2: Xóa translation của các function cũ
-- =========================================================
DELETE FROM `sys_function_lan`
WHERE `owner_id` IN (
  SELECT `id` FROM `sys_function`
  WHERE `function_code` IN ('F_ACCESS_ALERT_ALL', 'F_ACCESS_ALERT_GROUP', 'F_ACCESS_ALERT_OWN')
);

-- =========================================================
-- BƯỚC 3: Xóa các function cũ
-- =========================================================
DELETE FROM `sys_function`
WHERE `function_code` IN ('F_ACCESS_ALERT_ALL', 'F_ACCESS_ALERT_GROUP', 'F_ACCESS_ALERT_OWN');

-- =========================================================
-- BƯỚC 4: Thêm 2 function mới
-- =========================================================
INSERT INTO `sys_function` (`created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_code`) VALUES
  (NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ALERT'),
  (NOW(), 'system', NOW(), 'system', 0, 'F_HANDLE_ALERT');

INSERT INTO `sys_function_lan` (`created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`) VALUES
  (NULL, NULL, NULL, NULL, 0, 'Xem danh sách cảnh báo thuộc phạm vi nhóm của mình', 'vi', 'Xem Cảnh báo', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT')),
  (NULL, NULL, NULL, NULL, 0, 'View alerts within own group scope', 'en', 'Access Alerts', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT')),
  (NULL, NULL, NULL, NULL, 0, 'Xác nhận và giải quyết cảnh báo (Acknowledge/Resolve)', 'vi', 'Xử lý Cảnh báo', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_HANDLE_ALERT')),
  (NULL, NULL, NULL, NULL, 0, 'Acknowledge and resolve alerts', 'en', 'Handle Alerts', (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_HANDLE_ALERT'));

-- =========================================================
-- BƯỚC 5: Gán quyền F_ACCESS_ALERT cho G_ADMIN, G_MAINTENANCE, G_USER
-- Gán quyền F_HANDLE_ALERT cho G_ADMIN, G_MAINTENANCE
-- =========================================================
INSERT INTO `sys_role` (`created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_id`, `group_id`) VALUES
  -- F_ACCESS_ALERT
  (NOW(), 'system', NOW(), 'system', 0,
    (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT'),
    (SELECT `id` FROM `sys_group`    WHERE `group_code`    = 'G_ADMIN')),
  (NOW(), 'system', NOW(), 'system', 0,
    (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT'),
    (SELECT `id` FROM `sys_group`    WHERE `group_code`    = 'G_MAINTENANCE')),
  (NOW(), 'system', NOW(), 'system', 0,
    (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_ACCESS_ALERT'),
    (SELECT `id` FROM `sys_group`    WHERE `group_code`    = 'G_USER')),
  -- F_HANDLE_ALERT
  (NOW(), 'system', NOW(), 'system', 0,
    (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_HANDLE_ALERT'),
    (SELECT `id` FROM `sys_group`    WHERE `group_code`    = 'G_ADMIN')),
  (NOW(), 'system', NOW(), 'system', 0,
    (SELECT `id` FROM `sys_function` WHERE `function_code` = 'F_HANDLE_ALERT'),
    (SELECT `id` FROM `sys_group`    WHERE `group_code`    = 'G_MAINTENANCE'));
