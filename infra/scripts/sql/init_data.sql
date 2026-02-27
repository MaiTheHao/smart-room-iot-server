USE smart_room_iot;

SET
  FOREIGN_KEY_CHECKS = 0;

SET NAMES utf8;

SET
  TIME_ZONE = '+00:00';

-- ----------------------------
-- 1. Dữ liệu bảng floor (Tầng)
-- ----------------------------
INSERT INTO
  `floor` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `code`, `level`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 0, 'F00', 0),
  (2, NOW(), 'system', NOW(), 'system', 0, 'F01', 1),
  (3, NOW(), 'system', NOW(), 'system', 0, 'F02', 2);

-- ----------------------------
-- 2. Dữ liệu bảng floor_lan (Đa ngôn ngữ cho Tầng)
-- ----------------------------
INSERT INTO
  `floor_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, NULL, NULL, NULL, 0, 'Khu vực kỹ thuật và sảnh chính', 'vi', 'Tầng Trệt', 1),
  (2, NULL, NULL, NULL, NULL, 0, 'Technical area and main lobby', 'en', 'Ground Floor', 1),
  (3, NULL, NULL, NULL, NULL, 0, 'Khu vực văn phòng và bếp', 'vi', 'Tầng 1', 2),
  (4, NULL, NULL, NULL, NULL, 0, 'Office and kitchen area', 'en', '1st Floor', 2),
  (5, NULL, NULL, NULL, NULL, 0, 'Khu vực nghỉ ngơi', 'vi', 'Tầng 2', 3),
  (6, NULL, NULL, NULL, NULL, 0, 'Rest area', 'en', '2nd Floor', 3);

-- ----------------------------
-- 3. Dữ liệu bảng room (Phòng)
-- ----------------------------
INSERT INTO
  `room` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `code`, `floor_id`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 0, 'R-Main-Server', 1),
  (2, NOW(), 'system', NOW(), 'system', 0, 'R-F01-Kitchen', 2),
  (3, NOW(), 'system', NOW(), 'system', 0, 'R-F02-Master', 3);

-- ----------------------------
-- 4. Dữ liệu bảng room_lan (Đa ngôn ngữ cho Phòng)
-- ----------------------------
INSERT INTO
  `room_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, NULL, NULL, NULL, 0, 'Nơi đặt hệ thống quản trị', 'vi', 'Phòng Máy Chủ', 1),
  (2, NULL, NULL, NULL, NULL, 0, 'Central Server Room', 'en', 'Server Room', 1),
  (3, NULL, NULL, NULL, NULL, 0, 'Khu vực bếp ăn', 'vi', 'Phòng Bếp', 2),
  (4, NULL, NULL, NULL, NULL, 0, 'Cooking area', 'en', 'Kitchen', 2),
  (5, NULL, NULL, NULL, NULL, 0, 'Phòng ngủ chính', 'vi', 'Phòng Ngủ Master', 3),
  (6, NULL, NULL, NULL, NULL, 0, 'Main bedroom', 'en', 'Master Bedroom', 3);

-- ----------------------------
-- 5. Dữ liệu bảng air_condition (Máy lạnh)
-- ----------------------------
INSERT INTO
  `air_condition` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `is_active`, `natural_id`, `fan_speed`, `mode`, `power`, `swing`, `temperature`, `device_control_id`, `room_id`)
VALUES
  (1, NULL, 'admin', NULL, 'admin', 25, _binary '', 'AC-SERVER-01', 1, 'FAN', 'ON', 'OFF', 24, 1, 1);

-- ----------------------------
-- 6. Dữ liệu bảng air_condition_lan (Đa ngôn ngữ cho Máy lạnh)
-- ----------------------------
INSERT INTO
  `air_condition_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, 'admin', NULL, 'admin', 0, 'Máy lạnh trung tâm Generic', 'vi', 'Điều hòa Server', 1),
  (2, NULL, 'admin', NULL, 'admin', 0, 'Generic Central AC', 'en', 'Server AC', 1);

-- ----------------------------
-- 7. Dữ liệu bảng light (Đèn)
-- ----------------------------
INSERT INTO
  `light` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `is_active`, `natural_id`, `power`, `level`, `device_control_id`, `room_id`)
VALUES
  (1, NULL, 'admin', NULL, 'admin', 1, _binary '', 'L-SERVER-01', 'ON', 0, 1, 1);

-- ----------------------------
-- 8. Dữ liệu bảng light_lan (Đa ngôn ngữ cho Đèn)
-- ----------------------------
INSERT INTO
  `light_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, 'admin', NULL, 'admin', 0, 'Đèn trần chính', 'vi', 'Đèn Server', 1),
  (2, NULL, 'admin', NULL, 'admin', 0, 'Main ceiling light', 'en', 'Server Light', 1);

-- ----------------------------
-- 8.1. Dữ liệu bảng fan (Quạt)
-- ----------------------------
INSERT INTO
  `fan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `is_active`, `natural_id`, `power`, `type`, `speed`, `mode`, `light`, `swing`, `device_control_id`, `room_id`)
VALUES
  (1, NULL, 'admin', NULL, 'admin', 0, b'1', 'FAN-IR-01', 'OFF', 'IR', 500, 'NORMAL', 'OFF', 'OFF', 1, 1),
  (2, NULL, 'admin', NULL, 'admin', 0, b'1', 'FAN-GPIO-01', 'OFF', 'GPIO', NULL, NULL, NULL, NULL, NULL, 3);

-- ----------------------------
-- 8.2. Dữ liệu bảng fan_lan (Đa ngôn ngữ cho Quạt)
-- ----------------------------
INSERT INTO
  `fan_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, 'admin', NULL, 'admin', 0, 'Quạt thông gió IR', 'vi', 'Quạt IR Server', 1),
  (2, NULL, 'admin', NULL, 'admin', 0, 'IR Exhaust Fan', 'en', 'Server IR Fan', 1),
  (3, NULL, 'admin', NULL, 'admin', 0, 'Quạt đứng phòng ngủ', 'vi', 'Quạt Master', 2),
  (4, NULL, 'admin', NULL, 'admin', 0, 'Bedroom stand fan', 'en', 'Master Fan', 2);

-- ----------------------------
-- 9. Dữ liệu bảng device_control (Thiết bị điều khiển chung)
-- ----------------------------
INSERT INTO
  `device_control` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `api_endpoint`, `ble_mac_address`, `device_control_type`, `gpio_pin`, `client_id`, `room_id`)
VALUES
  (1, NULL, 'admin', NULL, 'admin', 0, NULL, NULL, 'GPIO', 20, 2, 1);

-- ----------------------------
-- 10. Dữ liệu bảng client (Thông tin đăng nhập và phân quyền người dùng)
-- ----------------------------
INSERT INTO
  `client` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `avatar_url`, `client_type`, `ip_address`, `last_login_at`, `mac_address`, `password_hash`, `username`)
VALUES
  (1, '2026-01-09 19:46:16.000000', NULL, '2026-01-09 19:46:16.000000', 'admin', 7, NULL, 'USER', '192.168.22.1:8080', NULL, NULL, '$2a$10$bDa1sDKylxJojO359TgMDuLOD.iaSQgT8ZThL3xtmjY7.3zK9QpKe', 'admin'),
  (2, NULL, NULL, NULL, 'admin', 30, NULL, 'HARDWARE_GATEWAY', '172.28.47.93', NULL, NULL, '$2a$10$bDa1sDKylxJojO359TgMDuLOD.iaSQgT8ZThL3xtmjY7.3zK9QpKe', 'test_client_01');

-- ----------------------------
-- 11. Dữ liệu bảng client_group
-- ----------------------------
INSERT INTO
  `client_group` (`client_id`, `group_id`)
VALUES
  (1, 1),
  (1, 2),
  (2, 2);

-- ----------------------------
-- 12. Dữ liệu bảng language
-- ----------------------------
INSERT INTO
  `language` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `code`, `description`, `name`)
VALUES
  (1, NULL, NULL, NULL, NULL, 0, 'vi', 'Ngôn ngữ hệ thống chính', 'Tiếng Việt'),
  (2, NULL, NULL, NULL, NULL, 0, 'en', 'System English language', 'English');

-- ----------------------------
-- 13. Dữ liệu bảng sys_function (Chức năng hệ thống)
-- ----------------------------
INSERT INTO
  `sys_function` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_code`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_CLIENT'),
  (4, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_DEVICE'),
  (5, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_ALL'),
  (100, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_FLOOR_F00'),
  (101, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_FLOOR_F01'),
  (102, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_FLOOR_F02'),
  (104, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_FLOOR_ALL'),
  (200, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ROOM_R-Main-Server'),
  (201, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ROOM_R-F01-Kitchen'),
  (202, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ROOM_R-F02-Master'),
  (205, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ROOM_ALL');

-- ----------------------------
-- 14. Dữ liệu bảng sys_function_lan
-- ----------------------------
INSERT INTO
  `sys_function_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, NULL, NULL, NULL, 0, 'Cho phép thực hiện mọi thao tác', 'vi', 'Quản lý toàn bộ', 5),
  (2, NULL, NULL, NULL, NULL, 0, 'Quyền xem dữ liệu Tầng Trệt', 'vi', 'Truy cập Tầng Trệt', 100),
  (3, NULL, NULL, NULL, NULL, 0, 'Permission to view Ground Floor', 'en', 'Access Ground Floor', 100),
  (4, NULL, NULL, NULL, NULL, 0, 'Quyền xem dữ liệu Tầng 1', 'vi', 'Truy cập Tầng 1', 101),
  (5, NULL, NULL, NULL, NULL, 0, 'Permission to view 1st Floor', 'en', 'Access 1st Floor', 101),
  (6, NULL, NULL, NULL, NULL, 0, 'Quyền xem dữ liệu Tầng 2', 'vi', 'Truy cập Tầng 2', 102),
  (7, NULL, NULL, NULL, NULL, 0, 'Permission to view 2nd Floor', 'en', 'Access 2nd Floor', 102),
  (8, NULL, NULL, NULL, NULL, 0, 'Quyền hạn cao cấp điều khiển Server', 'vi', 'Truy Cập Server', 200),
  (9, NULL, NULL, NULL, NULL, 0, 'Control devices in Server Room', 'en', 'Server Access', 200),
  (10, NULL, NULL, NULL, NULL, 0, 'Cho phép điều khiển thiết bị Bếp', 'vi', 'Truy cập Phòng Bếp', 201),
  (11, NULL, NULL, NULL, NULL, 0, 'Control devices in Kitchen', 'en', 'Access Kitchen', 201),
  (12, NULL, NULL, NULL, NULL, 0, 'Cho phép điều khiển thiết bị Master', 'vi', 'Truy cập Master', 202),
  (13, NULL, NULL, NULL, NULL, 0, 'Control devices in Master Room', 'en', 'Access Master', 202),
  (14, NULL, NULL, NULL, NULL, 1, 'Thực hiện mọi thao tác trên Client', 'vi', 'Quản lý Clients', 1),
  (15, NULL, NULL, NULL, NULL, 0, 'Thực hiện mọi thao tác trên Device', 'vi', 'Quản lý Devices', 4),
  (16, NULL, NULL, NULL, NULL, 0, 'Cho phép truy cập tất cả các tầng', 'vi', 'Truy cập toàn bộ tầng', 104);

-- ----------------------------
-- 15. Dữ liệu bảng sys_group
-- ----------------------------
INSERT INTO
  `sys_group` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `group_code`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 0, 'G_ADMIN'),
  (2, NOW(), 'system', NOW(), 'system', 0, 'G_USER');

-- ----------------------------
-- 16. Dữ liệu bảng sys_group_lan
-- ----------------------------
INSERT INTO
  `sys_group_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, NULL, NULL, NULL, 2, 'Toàn quyền truy cập hệ thống', 'vi', 'Quản trị viên', 1),
  (2, NULL, NULL, NULL, NULL, 0, 'Full system access', 'en', 'Administrator', 1),
  (3, NULL, NULL, NULL, NULL, 0, 'Quyền sử dụng cơ bản', 'vi', 'Người dùng thường', 2);

-- ----------------------------
-- 17. Dữ liệu bảng sys_role
-- ----------------------------
INSERT INTO
  `sys_role` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_id`, `group_id`)
VALUES
  (1, NOW(), NULL, NOW(), NULL, 0, 5, 1),
  (3, NOW(), NULL, NOW(), NULL, 0, 205, 1),
  (17, NULL, NULL, NULL, NULL, 0, 104, 1),
  (18, NULL, NULL, NULL, NULL, 0, 200, 2),
  (19, NULL, 'admin', NULL, 'admin', 0, 104, 2),
  (20, NULL, 'admin', NULL, 'admin', 0, 100, 2),
  (21, NULL, 'admin', NULL, 'admin', 0, 101, 2),
  (22, NULL, 'admin', NULL, 'admin', 0, 102, 2);

-- ----------------------------
-- 18. Dữ liệu bảng sys_client_function_cache (Mapping sẵn)
-- ----------------------------
INSERT INTO
  `sys_client_function_cache` (`client_id`, `function_code`, `group_id`)
VALUES
  (1, 'F_ACCESS_FLOOR_ALL', 1),
  (1, 'F_ACCESS_FLOOR_ALL', 2),
  (1, 'F_ACCESS_FLOOR_F00', 2),
  (1, 'F_ACCESS_FLOOR_F01', 2),
  (1, 'F_ACCESS_FLOOR_F02', 2),
  (1, 'F_ACCESS_ROOM_ALL', 1),
  (1, 'F_ACCESS_ROOM_R-Main-Server', 2),
  (1, 'F_MANAGE_ALL', 1),
  (2, 'F_ACCESS_FLOOR_ALL', 2),
  (2, 'F_ACCESS_FLOOR_F00', 2),
  (2, 'F_ACCESS_FLOOR_F01', 2),
  (2, 'F_ACCESS_FLOOR_F02', 2),
  (2, 'F_ACCESS_ROOM_R-Main-Server', 2);

SET
  FOREIGN_KEY_CHECKS = 1;