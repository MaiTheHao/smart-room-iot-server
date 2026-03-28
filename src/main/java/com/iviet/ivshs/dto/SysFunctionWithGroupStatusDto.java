package com.iviet.ivshs.dto;

import lombok.Builder;

/**
 * DTO để hiển thị Function với trạng thái đã được assign vào Group hay chưa
 * Dùng cho UI khi chọn Functions để add/remove khỏi Group
 */
@Builder
public record SysFunctionWithGroupStatusDto(
    Long id,
    String functionCode,
    String name,
    String description,
    Boolean isAssignedToGroup,  // true nếu function đã được assign vào group
    Long roleId                  // ID của SysRole nếu đã được assign (null nếu chưa)
) {
}
