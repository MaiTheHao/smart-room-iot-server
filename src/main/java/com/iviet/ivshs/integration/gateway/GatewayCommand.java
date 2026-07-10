package com.iviet.ivshs.integration.gateway;

import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.DeviceSpecificType;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record GatewayCommand(
    String naturalId,
    DeviceCategory category,
    DeviceSpecificType specificType,
    Integer duration,
    Map<String, Object> params,
    Map<String, Object> metadata) {
  public GatewayCommand {
    params = params != null ? Collections.unmodifiableMap(params) : Map.of();
    metadata = metadata != null ? Collections.unmodifiableMap(metadata) : Map.of();
  }

  public static GatewayCommand of(
      String naturalId,
      DeviceCategory category,
      DeviceSpecificType specificType,
      String param,
      Object value) {
    return new GatewayCommand(
        naturalId, category, specificType, null, mapOf(param, value), Map.of());
  }

  public static GatewayCommand of(
      String naturalId,
      DeviceCategory category,
      DeviceSpecificType specificType,
      Integer duration,
      String param,
      Object value) {
    return new GatewayCommand(
        naturalId, category, specificType, duration, mapOf(param, value), Map.of());
  }

  public Object param(String key) {
    return params.get(key);
  }

  public Long metaTargetId() {
    Object v = metadata.get("targetId");
    return v instanceof Number n ? n.longValue() : null;
  }

  public String metaMetricCategory() {
    Object v = metadata.get("metricCategory");
    return v != null ? v.toString() : null;
  }

  public String metaGatewayPath() {
    Object v = metadata.get("gatewayPath");
    return v != null ? v.toString() : null;
  }

  public static Builder forDevice(String naturalId, DeviceCategory category) {
    return new Builder(naturalId, category);
  }

  private static Map<String, Object> mapOf(String key, Object value) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put(key, value);
    return m;
  }

  public static class Builder {
    private final String naturalId;
    private final DeviceCategory category;
    private DeviceSpecificType specificType;
    private Integer duration;
    private final Map<String, Object> params = new LinkedHashMap<>();
    private final Map<String, Object> metadata = new LinkedHashMap<>();

    Builder(String naturalId, DeviceCategory category) {
      this.naturalId = naturalId;
      this.category = category;
    }

    public Builder specificType(DeviceSpecificType t) {
      this.specificType = t;
      return this;
    }

    public Builder duration(Integer d) {
      this.duration = d;
      return this;
    }

    public Builder param(String key, Object value) {
      if (value != null) params.put(key, value);
      return this;
    }

    public Builder targetId(Long id) {
      if (id != null) metadata.put("targetId", id);
      return this;
    }

    public Builder metricCategory(String cat) {
      if (cat != null) metadata.put("metricCategory", cat);
      return this;
    }

    public Builder gatewayPath(String path) {
      if (path != null) metadata.put("gatewayPath", path);
      return this;
    }

    public Builder meta(String key, Object value) {
      if (key != null && value != null) metadata.put(key, value);
      return this;
    }

    public GatewayCommand build() {
      return new GatewayCommand(naturalId, category, specificType, duration, params, metadata);
    }
  }
}
