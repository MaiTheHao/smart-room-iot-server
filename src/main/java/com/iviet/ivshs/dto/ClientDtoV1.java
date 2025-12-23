package com.iviet.ivshs.dto;

import com.iviet.ivshs.enumeration.ClientTypeV1;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDtoV1 {
    
    private Long id;
    private String username;
    private ClientTypeV1 clientType;
    private String ipAddress;
    private String macAddress;
    private String avatarUrl;
    private Date lastLoginAt;
}
