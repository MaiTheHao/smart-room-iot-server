package com.iviet.ivshs.dto.control;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceControlRequestDto {
    private Object data;
    private String specificType;
    private Integer duration;
}
