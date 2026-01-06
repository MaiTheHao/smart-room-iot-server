package com.iviet.ivshs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO đại diện cho payload của JWT token.
 * Chứa các thông tin cơ bản về người dùng và quyền hạn được gán.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtPayload {
    /** ID duy nhất của người dùng trong hệ thống */
    private Long userId;
    
    /** Tên đăng nhập của người dùng */
    private String username;
    
    /** Địa chỉ email của người dùng */
    private String email;
    
    /** Danh sách các vai trò/quyền hạn được gán cho người dùng */
    private List<String> roles;
}
