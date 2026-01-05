package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;

/**
 * Mapper cho SysRole - chủ yếu để convert Entity với Translation
 * SysRole không có DTO riêng vì nó là bảng trung gian
 * 
 * Việc tạo Role mới sẽ được xử lý trực tiếp trong Service layer
 * vì cần set Group và Function entities
 */
@Mapper(componentModel = "spring")
public interface SysRoleMapperV1 {
    // Mapper này được tạo để maintain consistency với các mapper khác
    // Các operations cho SysRole sẽ được xử lý trong Service/DAO layer
}
