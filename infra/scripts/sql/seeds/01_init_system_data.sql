/**
 * MỤC ĐÍCH & MỤC TIÊU CỦA FILE SEED:
 * 1) Khởi tạo dữ liệu cơ bản vừa đủ để hệ thống có thể vận hành ngay sau khi tạo DB.
 * 2) Thiết lập môi trường dữ liệu nền với các giá trị hard-coded (ngôn ngữ, chức năng, nhóm quyền, tài khoản admin).
 *
 * CHI TIẾT CÁC THÀNH PHẦN:
 * - Languages: Hỗ trợ đa ngôn ngữ (vi, en).
 * - Sys Functions: Danh mục các chức năng hệ thống dựa trên SysFunctionEnum.
 * - Sys Groups: Nhóm quyền quản trị mặc định (G_ADMIN).
 * - Sys Roles: Phân bổ toàn bộ quyền (F_MANAGE_*) cho nhóm G_ADMIN.
 * - Client: Tạo tài khoản quản trị tối cao (admin / 123456789).
 */
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
-- 2. Dữ liệu bảng sys_function (Dựa trên SysFunctionEnum.java)
-- ----------------------------
INSERT INTO
  `sys_function` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_code`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_CLIENT'),
  (2, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_FLOOR'),
  (3, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_ROOM'),
  (4, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_DEVICE'),
  (5, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_ALL'),
  (6, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_SOME'),
  (7, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_FUNCTION'),
  (8, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_GROUP'),
  (9, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_AUTOMATION'),
  (10, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_FLOOR_ALL'),
  (11, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ROOM_ALL'),
  (12, NOW(), 'system', NOW(), 'system', 0, 'F_MANAGE_RULE');

-- ----------------------------
-- 3. Dữ liệu bảng sys_function_lan
-- ----------------------------
INSERT INTO
  `sys_function_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, NULL, NULL, NULL, 0, 'Quản lý thông tin khách hàng', 'vi', 'Quản lý Khách Hàng', 1),
  (2, NULL, NULL, NULL, NULL, 0, 'Manage customer information', 'en', 'Manage Client', 1),
  (3, NULL, NULL, NULL, NULL, 0, 'Quản lý các tầng trong tòa nhà', 'vi', 'Quản lý Tầng', 2),
  (4, NULL, NULL, NULL, NULL, 0, 'Manage building floors', 'en', 'Manage Floor', 2),
  (5, NULL, NULL, NULL, NULL, 0, 'Quản lý các phòng', 'vi', 'Quản lý Phòng', 3),
  (6, NULL, NULL, NULL, NULL, 0, 'Manage rooms', 'en', 'Manage Room', 3),
  (7, NULL, NULL, NULL, NULL, 0, 'Quản lý thiết bị IoT', 'vi', 'Quản lý Thiết Bị', 4),
  (8, NULL, NULL, NULL, NULL, 0, 'Manage IoT devices', 'en', 'Manage Device', 4),
  (9, NULL, NULL, NULL, NULL, 0, 'Cho phép thực hiện mọi thao tác', 'vi', 'Quản lý Toàn Bộ', 5),
  (10, NULL, NULL, NULL, NULL, 0, 'Perform all operations', 'en', 'Manage All', 5),
  (11, NULL, NULL, NULL, NULL, 0, 'Quản lý một phần hệ thống', 'vi', 'Quản lý Một Phần', 6),
  (12, NULL, NULL, NULL, NULL, 0, 'Manage part of system', 'en', 'Manage Some', 6),
  (13, NULL, NULL, NULL, NULL, 0, 'Quản lý các chức năng hệ thống', 'vi', 'Quản lý Chức Năng', 7),
  (14, NULL, NULL, NULL, NULL, 0, 'Manage system functions', 'en', 'Manage Function', 7),
  (15, NULL, NULL, NULL, NULL, 0, 'Quản lý các nhóm người dùng', 'vi', 'Quản lý Nhóm', 8),
  (16, NULL, NULL, NULL, NULL, 0, 'Manage user groups', 'en', 'Manage Group', 8),
  (17, NULL, NULL, NULL, NULL, 0, 'Quản lý các quy tắc tự động hóa', 'vi', 'Quản lý Tự Động Hóa', 9),
  (18, NULL, NULL, NULL, NULL, 0, 'Manage automation rules', 'en', 'Manage Automation', 9),
  (19, NULL, NULL, NULL, NULL, 0, 'Truy cập tất cả các tầng', 'vi', 'Truy cập Tất cả Tầng', 10),
  (20, NULL, NULL, NULL, NULL, 0, 'Access all floors', 'en', 'Access Floor All', 10),
  (21, NULL, NULL, NULL, NULL, 0, 'Truy cập tất cả các phòng', 'vi', 'Truy cập Tất cả Phòng', 11),
  (22, NULL, NULL, NULL, NULL, 0, 'Access all rooms', 'en', 'Access Room All', 11),
  (23, NULL, NULL, NULL, NULL, 0, 'Quản lý quy tắc hệ thống', 'vi', 'Quản lý Rule', 12),
  (24, NULL, NULL, NULL, NULL, 0, 'Manage system rules', 'en', 'Manage Rule', 12);

-- ----------------------------
-- 4. Dữ liệu bảng sys_group (G_ADMIN)
-- ----------------------------
INSERT INTO
  `sys_group` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `group_code`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 0, 'G_ADMIN');

-- ----------------------------
-- 5. Dữ liệu bảng sys_group_lan
-- ----------------------------
INSERT INTO
  `sys_group_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, NULL, NULL, NULL, 2, 'Toàn quyền truy cập hệ thống', 'vi', 'Quản trị viên', 1),
  (2, NULL, NULL, NULL, NULL, 0, 'Full system access', 'en', 'Administrator', 1);

-- ----------------------------
-- 6. Dữ liệu bảng sys_role (Gán full quyền cho G_ADMIN)
-- ----------------------------
INSERT INTO
  `sys_role` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_id`, `group_id`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 0, 1, 1),
  (2, NOW(), 'system', NOW(), 'system', 0, 2, 1),
  (3, NOW(), 'system', NOW(), 'system', 0, 3, 1),
  (4, NOW(), 'system', NOW(), 'system', 0, 4, 1),
  (5, NOW(), 'system', NOW(), 'system', 0, 5, 1),
  (6, NOW(), 'system', NOW(), 'system', 0, 6, 1),
  (7, NOW(), 'system', NOW(), 'system', 0, 7, 1),
  (8, NOW(), 'system', NOW(), 'system', 0, 8, 1),
  (9, NOW(), 'system', NOW(), 'system', 0, 9, 1),
  (10, NOW(), 'system', NOW(), 'system', 0, 10, 1),
  (11, NOW(), 'system', NOW(), 'system', 0, 11, 1),
  (12, NOW(), 'system', NOW(), 'system', 0, 12, 1);

-- ----------------------------
-- 7. Dữ liệu bảng client (Tài khoản admin - username: admin, password: 123456789)
-- ----------------------------
INSERT INTO
  `client` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `avatar_url`, `client_type`, `ip_address`, `last_login_at`, `mac_address`, `password_hash`, `username`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 1, NULL, 'USER', NULL, NULL, NULL, '$2a$10$bDa1sDKylxJojO359TgMDuLOD.iaSQgT8ZThL3xtmjY7.3zK9QpKe', 'admin');

-- ----------------------------
-- 8. Dữ liệu bảng client_group
-- ----------------------------
INSERT INTO
  `client_group` (`client_id`, `group_id`)
VALUES
  (1, 1);

SET
  FOREIGN_KEY_CHECKS = 1;