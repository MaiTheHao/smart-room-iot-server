USE smart_room_iot;

SET
  FOREIGN_KEY_CHECKS = 0;

SET NAMES utf8;

SET
  TIME_ZONE = '+00:00';

-- ----------------------------
-- 1. Dữ liệu bảng language
-- ----------------------------
INSERT INTO
  `language` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `code`, `description`, `name`)
VALUES
  (1, NULL, NULL, NULL, NULL, 0, 'vi', 'Ngôn ngữ hệ thống chính', 'Tiếng Việt'),
  (2, NULL, NULL, NULL, NULL, 0, 'en', 'System English language', 'English');

-- ----------------------------
-- 2. Dữ liệu bảng floor (Tầng)
-- ----------------------------
INSERT INTO
  `floor` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `code`, `level`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 0, 'F00', 0);

-- ----------------------------
-- 3. Dữ liệu bảng floor_lan (Đa ngôn ngữ cho Tầng)
-- ----------------------------
INSERT INTO
  `floor_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, NULL, NULL, NULL, 0, 'Khu vực kỹ thuật và sảnh chính', 'vi', 'Tầng Trệt', 1),
  (2, NULL, NULL, NULL, NULL, 0, 'Technical area and main lobby', 'en', 'Ground Floor', 1);

-- ----------------------------
-- 4. Dữ liệu bảng room (Phòng)
-- ----------------------------
INSERT INTO
  `room` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `code`, `floor_id`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 0, 'R-Main-Server', 1);

-- ----------------------------
-- 5. Dữ liệu bảng room_lan (Đa ngôn ngữ cho Phòng)
-- ----------------------------
INSERT INTO
  `room_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, NULL, NULL, NULL, 0, 'Nơi đặt hệ thống quản trị', 'vi', 'Phòng Máy Chủ', 1),
  (2, NULL, NULL, NULL, NULL, 0, 'Central Server Room', 'en', 'Server Room', 1);

-- ----------------------------
-- 6. Dữ liệu bảng client (Thông tin đăng nhập)
-- ----------------------------
INSERT INTO
  `client` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `avatar_url`, `client_type`, `ip_address`, `last_login_at`, `mac_address`, `password_hash`, `username`)
VALUES
  (1, '2026-01-09 19:46:16.000000', NULL, '2026-01-09 19:46:16.000000', 'admin', 7, NULL, 'USER', '192.168.22.1:8080', NULL, NULL, '$2a$10$bDa1sDKylxJojO359TgMDuLOD.iaSQgT8ZThL3xtmjY7.3zK9QpKe', 'admin');

-- ----------------------------
-- 7. Dữ liệu bảng sys_function (Chức năng hệ thống)
-- ----------------------------
INSERT INTO
  `sys_function` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_code`)
VALUES
  -- Management Functions
  (1, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_CLIENT'),
  (2, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_FLOOR'),
  (3, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_ROOM'),
  (4, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_DEVICE'),
  (5, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_ALL'),
  (6, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_SOME'),
  (7, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_FUNCTION'),
  (8, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_GROUP'),
  (9, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_AUTOMATION'),
  -- Access Functions
  (10, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_FLOOR_ALL'),
  (11, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ROOM_ALL'),
  -- Legacy specific functions
  (100, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_FLOOR_F00'),
  (200, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ROOM_R-Main-Server');

-- ----------------------------
-- 8. Dữ liệu bảng sys_function_lan
-- ----------------------------
INSERT INTO
  `sys_function_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  -- F_MANAGE_CLIENT (id = 1)
  (1, NULL, NULL, NULL, NULL, 0, 'Quản lý thông tin khách hàng', 'vi', 'Quản lý Khách Hàng', 1),
  (2, NULL, NULL, NULL, NULL, 0, 'Manage customer information', 'en', 'Manage Client', 1),
  -- F_MANAGE_FLOOR (id = 2)
  (3, NULL, NULL, NULL, NULL, 0, 'Quản lý các tầng trong tòa nhà', 'vi', 'Quản lý Tầng', 2),
  (4, NULL, NULL, NULL, NULL, 0, 'Manage building floors', 'en', 'Manage Floor', 2),
  -- F_MANAGE_ROOM (id = 3)
  (5, NULL, NULL, NULL, NULL, 0, 'Quản lý các phòng', 'vi', 'Quản lý Phòng', 3),
  (6, NULL, NULL, NULL, NULL, 0, 'Manage rooms', 'en', 'Manage Room', 3),
  -- F_MANAGE_DEVICE (id = 4)
  (7, NULL, NULL, NULL, NULL, 0, 'Quản lý thiết bị IoT', 'vi', 'Quản lý Thiết Bị', 4),
  (8, NULL, NULL, NULL, NULL, 0, 'Manage IoT devices', 'en', 'Manage Device', 4),
  -- F_MANAGE_ALL (id = 5)
  (9, NULL, NULL, NULL, NULL, 0, 'Cho phép thực hiện mọi thao tác', 'vi', 'Quản lý Toàn Bộ', 5),
  (10, NULL, NULL, NULL, NULL, 0, 'Perform all operations', 'en', 'Manage All', 5),
  -- F_MANAGE_SOME (id = 6)
  (11, NULL, NULL, NULL, NULL, 0, 'Quản lý một phần hệ thống', 'vi', 'Quản lý Một Phần', 6),
  (12, NULL, NULL, NULL, NULL, 0, 'Manage part of system', 'en', 'Manage Some', 6),
  -- F_MANAGE_FUNCTION (id = 7)
  (13, NULL, NULL, NULL, NULL, 0, 'Quản lý các chức năng hệ thống', 'vi', 'Quản lý Chức Năng', 7),
  (14, NULL, NULL, NULL, NULL, 0, 'Manage system functions', 'en', 'Manage Function', 7),
  -- F_MANAGE_GROUP (id = 8)
  (15, NULL, NULL, NULL, NULL, 0, 'Quản lý các nhóm người dùng', 'vi', 'Quản lý Nhóm', 8),
  (16, NULL, NULL, NULL, NULL, 0, 'Manage user groups', 'en', 'Manage Group', 8),
  -- F_MANAGE_AUTOMATION (id = 9)
  (17, NULL, NULL, NULL, NULL, 0, 'Quản lý các quy tắc tự động hóa', 'vi', 'Quản lý Tự Động Hóa', 9),
  (18, NULL, NULL, NULL, NULL, 0, 'Manage automation rules', 'en', 'Manage Automation', 9),
  -- F_ACCESS_FLOOR_ALL (id = 10)
  (19, NULL, NULL, NULL, NULL, 0, 'Truy cập tất cả các tầng', 'vi', 'Truy cập Tất cả Tầng', 10),
  (20, NULL, NULL, NULL, NULL, 0, 'Access all floors', 'en', 'Access Floor All', 10),
  -- F_ACCESS_ROOM_ALL (id = 11)
  (21, NULL, NULL, NULL, NULL, 0, 'Truy cập tất cả các phòng', 'vi', 'Truy cập Tất cả Phòng', 11),
  (22, NULL, NULL, NULL, NULL, 0, 'Access all rooms', 'en', 'Access Room All', 11),
  -- F_ACCESS_FLOOR_F00 (id = 100)
  (23, NULL, NULL, NULL, NULL, 0, 'Quyền xem dữ liệu Tầng Trệt', 'vi', 'Truy cập Tầng Trệt', 100),
  (24, NULL, NULL, NULL, NULL, 0, 'Permission to view Ground Floor', 'en', 'Access Ground Floor', 100),
  -- F_ACCESS_ROOM_R-Main-Server (id = 200)
  (25, NULL, NULL, NULL, NULL, 0, 'Quyền hạn cao cấp điều khiển Server', 'vi', 'Truy Cập Server', 200),
  (26, NULL, NULL, NULL, NULL, 0, 'Control devices in Server Room', 'en', 'Server Access', 200);

-- ----------------------------
-- 9. Dữ liệu bảng sys_group
-- ----------------------------
INSERT INTO
  `sys_group` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `group_code`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 0, 'G_ADMIN');

-- ----------------------------
-- 10. Dữ liệu bảng sys_group_lan
-- ----------------------------
INSERT INTO
  `sys_group_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, NULL, NULL, NULL, 2, 'Toàn quyền truy cập hệ thống', 'vi', 'Quản trị viên', 1),
  (2, NULL, NULL, NULL, NULL, 0, 'Full system access', 'en', 'Administrator', 1);

-- ----------------------------
-- 11. Dữ liệu bảng sys_role
-- ----------------------------
INSERT INTO
  `sys_role` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_id`, `group_id`)
VALUES
  -- Admin group (group_id = 1) has all functions for full system access
  -- Management Functions
  (1, NOW(), 'system', NOW(), 'system', 0, 1, 1), -- F_MANAGE_CLIENT
  (2, NOW(), 'system', NOW(), 'system', 0, 2, 1), -- F_MANAGE_FLOOR
  (3, NOW(), 'system', NOW(), 'system', 0, 3, 1), -- F_MANAGE_ROOM
  (4, NOW(), 'system', NOW(), 'system', 0, 4, 1), -- F_MANAGE_DEVICE
  (5, NOW(), 'system', NOW(), 'system', 0, 5, 1), -- F_MANAGE_ALL
  (6, NOW(), 'system', NOW(), 'system', 0, 6, 1), -- F_MANAGE_SOME
  (7, NOW(), 'system', NOW(), 'system', 0, 7, 1), -- F_MANAGE_FUNCTION
  (8, NOW(), 'system', NOW(), 'system', 0, 8, 1), -- F_MANAGE_GROUP
  (9, NOW(), 'system', NOW(), 'system', 0, 9, 1), -- F_MANAGE_AUTOMATION
  -- Access Functions
  (10, NOW(), 'system', NOW(), 'system', 0, 10, 1), -- F_ACCESS_FLOOR_ALL
  (11, NOW(), 'system', NOW(), 'system', 0, 11, 1), -- F_ACCESS_ROOM_ALL
  -- Legacy specific functions (for backward compatibility)
  (12, NOW(), 'system', NOW(), 'system', 0, 100, 1), -- F_ACCESS_FLOOR_F00
  (13, NOW(), 'system', NOW(), 'system', 0, 200, 1);

-- F_ACCESS_ROOM_R-Main-Server
-- ----------------------------
-- 12. Dữ liệu bảng client_group
-- ----------------------------
INSERT INTO
  `client_group` (`client_id`, `group_id`)
VALUES
  (1, 1);

SET
  FOREIGN_KEY_CHECKS = 1;