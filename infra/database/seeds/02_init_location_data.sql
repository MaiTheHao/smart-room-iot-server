/**
 * MỤC ĐÍCH & MỤC TIÊU CỦA FILE SEED:
 * 1) Khởi tạo cấu trúc hạ tầng vật lý (Physical Location) cơ bản cho hệ thống.
 * 2) Thiết lập các đơn vị không gian mẫu (Tầng, Phòng) và hỗ trợ đa ngôn ngữ tương ứng.
 * 3) Mở rộng hệ thống phân quyền (Functions/Roles) để kiểm soát truy cập cho các vị trí cụ thể.
 *
 * CHI TIẾT CÁC THÀNH PHẦN:
 * - Floor & Floor Lan: Khởi tạo dữ liệu tầng mẫu (Tầng Trệt - F00).
 * - Room & Room Lan: Khởi tạo dữ liệu phòng mẫu (Phòng Máy Chủ - R-Main-Server) thuộc tầng tương ứng.
 * - Sys Functions: Bổ sung các mã chức năng đặc thù để phân quyền truy cập theo từng tầng/phòng (F_ACCESS_FLOOR_*, F_ACCESS_ROOM_*).
 * - Sys Roles: Gán các quyền truy cập địa điểm mới khởi tạo cho nhóm quản trị (G_ADMIN).
 */
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
  (2, NOW(), 'system', NOW(), 'system', 0, 'F01', 1);

-- ----------------------------
-- 2. Dữ liệu bảng floor_lan (Đa ngôn ngữ cho Tầng)
-- ----------------------------
INSERT INTO
  `floor_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, NULL, NULL, NULL, 0, 'Khu vực kỹ thuật và sảnh chính', 'vi', 'Tầng Trệt', 1),
  (2, NULL, NULL, NULL, NULL, 0, 'Technical area and main lobby', 'en', 'Ground Floor', 1),
  (3, NULL, NULL, NULL, NULL, 0, 'Khu vực sinh hoạt và phòng ngủ', 'vi', 'Lầu 1', 2),
  (4, NULL, NULL, NULL, NULL, 0, 'Living area and bedrooms', 'en', '1st Floor', 2);

-- ----------------------------
-- 3. Dữ liệu bảng room (Phòng)
-- ----------------------------
INSERT INTO
  `room` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `code`, `floor_id`)
VALUES
  (1, NOW(), 'system', NOW(), 'system', 0, 'R-Main-Server', 1),
  (2, NOW(), 'system', NOW(), 'system', 0, 'R-Living-Room', 1),
  (3, NOW(), 'system', NOW(), 'system', 0, 'R-Kitchen', 1),
  (4, NOW(), 'system', NOW(), 'system', 0, 'R-Bedroom', 2);

-- ----------------------------
-- 4. Dữ liệu bảng room_lan (Đa ngôn ngữ cho Phòng)
-- ----------------------------
INSERT INTO
  `room_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (1, NULL, NULL, NULL, NULL, 0, 'Nơi đặt hệ thống quản trị', 'vi', 'Phòng Máy Chủ', 1),
  (2, NULL, NULL, NULL, NULL, 0, 'Central Server Room', 'en', 'Server Room', 1),
  (3, NULL, NULL, NULL, NULL, 0, 'Phòng tiếp khách và giải trí', 'vi', 'Phòng Khách', 2),
  (4, NULL, NULL, NULL, NULL, 0, 'Guest reception and entertainment', 'en', 'Living Room', 2),
  (5, NULL, NULL, NULL, NULL, 0, 'Khu vực nấu ăn và dùng bữa', 'vi', 'Nhà Bếp', 3),
  (6, NULL, NULL, NULL, NULL, 0, 'Cooking and dining area', 'en', 'Kitchen', 3),
  (7, NULL, NULL, NULL, NULL, 0, 'Không gian nghỉ ngơi riêng tư', 'vi', 'Phòng Ngủ', 4),
  (8, NULL, NULL, NULL, NULL, 0, 'Private rest space', 'en', 'Bedroom', 4);

-- ----------------------------
-- 5. Dữ liệu bổ sung bảng sys_function (Các function đặc thù/mock)
-- ----------------------------
INSERT INTO
  `sys_function` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_code`)
VALUES
  (100, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_FLOOR_F00'),
  (101, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_FLOOR_F01'),
  (200, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ROOM_R-Main-Server'),
  (201, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ROOM_R-Living-Room'),
  (202, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ROOM_R-Kitchen'),
  (203, NOW(), 'system', NOW(), 'system', 0, 'F_ACCESS_ROOM_R-Bedroom');

-- ----------------------------
-- 6. Dữ liệu bổ sung bảng sys_function_lan
-- ----------------------------
INSERT INTO
  `sys_function_lan` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `description`, `lang_code`, `name`, `owner_id`)
VALUES
  (25, NULL, NULL, NULL, NULL, 0, 'Quyền xem dữ liệu Tầng Trệt', 'vi', 'Truy cập Tầng Trệt', 100),
  (26, NULL, NULL, NULL, NULL, 0, 'Permission to view Ground Floor', 'en', 'Access Ground Floor', 100),
  (27, NULL, NULL, NULL, NULL, 0, 'Quyền xem dữ liệu Lầu 1', 'vi', 'Truy cập Lầu 1', 101),
  (28, NULL, NULL, NULL, NULL, 0, 'Permission to view 1st Floor', 'en', 'Access 1st Floor', 101),
  (29, NULL, NULL, NULL, NULL, 0, 'Quyền hạn cao cấp điều khiển Server', 'vi', 'Truy Cập Server', 200),
  (30, NULL, NULL, NULL, NULL, 0, 'Control devices in Server Room', 'en', 'Server Access', 200),
  (31, NULL, NULL, NULL, NULL, 0, 'Quyền điều khiển thiết bị Phòng Khách', 'vi', 'Truy Cập Phòng Khách', 201),
  (32, NULL, NULL, NULL, NULL, 0, 'Control devices in Living Room', 'en', 'Living Room Access', 201),
  (33, NULL, NULL, NULL, NULL, 0, 'Quyền điều khiển thiết bị Nhà Bếp', 'vi', 'Truy Cập Nhà Bếp', 202),
  (34, NULL, NULL, NULL, NULL, 0, 'Control devices in Kitchen', 'en', 'Kitchen Access', 202),
  (35, NULL, NULL, NULL, NULL, 0, 'Quyền điều khiển thiết bị Phòng Ngủ', 'vi', 'Truy Cập Phòng Ngủ', 203),
  (36, NULL, NULL, NULL, NULL, 0, 'Control devices in Bedroom', 'en', 'Bedroom Access', 203);

-- ----------------------------
-- 7. Dữ liệu bổ sung bảng sys_role
-- ----------------------------
INSERT INTO
  `sys_role` (`id`, `created_at`, `created_by`, `updated_at`, `updated_by`, `v`, `function_id`, `group_id`)
VALUES
  (13, NOW(), 'system', NOW(), 'system', 0, 100, 1),
  (14, NOW(), 'system', NOW(), 'system', 0, 101, 1),
  (15, NOW(), 'system', NOW(), 'system', 0, 200, 1),
  (16, NOW(), 'system', NOW(), 'system', 0, 201, 1),
  (17, NOW(), 'system', NOW(), 'system', 0, 202, 1),
  (18, NOW(), 'system', NOW(), 'system', 0, 203, 1);

SET
  FOREIGN_KEY_CHECKS = 1;