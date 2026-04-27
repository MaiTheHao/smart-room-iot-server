package com.iviet.ivshs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlDeviceResult {
    private int successCount;
    private int totalCount;
    private List<Detail> details = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private String parameter;
        private boolean success;
        private String message;
    }

    public void addDetail(String parameter, boolean success, String message) {
        if (details == null) {
            details = new ArrayList<>();
        }
        details.add(new Detail(parameter, success, message));
        totalCount++;
        if (success) {
            successCount++;
        }
    }
    
    public static ControlDeviceResult single(String parameter, boolean success, String message) {
        ControlDeviceResult result = new ControlDeviceResult();
        result.addDetail(parameter, success, message);
        return result;
    }
}
