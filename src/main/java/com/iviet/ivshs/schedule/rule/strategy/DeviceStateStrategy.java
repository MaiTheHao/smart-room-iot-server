package com.iviet.ivshs.schedule.rule.strategy;

import com.iviet.ivshs.enumeration.DeviceCategory;

public interface DeviceStateStrategy {

  /**
   * Kiểm tra xem strategy này có hỗ trợ DeviceCategory tương ứng không
   */
  boolean supports(DeviceCategory category);

  /**
   * Lấy trạng thái của thiết bị
   */
  Object fetchState(Long deviceId, String property);
}
