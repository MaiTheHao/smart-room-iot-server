# CO2 & Lux Sensor Integration — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Tích hợp `SENSOR_CO2` và `SENSOR_LUX` vào hệ thống Smart Room IoT, bao gồm đầy đủ entity, DAO, setup strategy, metric service, và DB migration, theo pattern của `HumiditySensor`.

**Architecture:** Mỗi sensor đi theo vertical slice độc lập (entity → DAO → setup strategy → service). `TelemetryServiceImpl` và `DeviceSetupOrchestrator` tự auto-register strategy mới qua Spring DI — không cần sửa. Sealed interface `SensorSpecificData` phải sửa thủ công.

**Tech Stack:** Java 21, Spring Boot, Jakarta Persistence (JPA), Hibernate, Lombok, Quartz, MySQL

## Global Constraints
- Package root: `com.iviet.ivshs`
- Tất cả entity dùng `@Immutable` cho Metric class (như `HumidityMetric`)
- Metric DAO dùng JDBC batch insert (không dùng JPA save)
- Telemetry JSON field: `"co2"` và `"lux"` (lowercase, không có prefix)
- `targetCategory` string: `"SENSOR_CO2"` và `"SENSOR_LUX"` (khớp với enum name)
- Compile check sau mỗi task: `cd smartroom_server && mvn compile -q`

---

## Task 1: Cập nhật Enums

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/shared/enumeration/DeviceCategory.java`
- Modify: `src/main/java/com/iviet/ivshs/shared/enumeration/MetricDomain.java`
- Modify: `src/main/java/com/iviet/ivshs/shared/enumeration/SensorCategory.java`

**Interfaces:**
- Produces: `DeviceCategory.SENSOR_CO2`, `DeviceCategory.SENSOR_LUX`, `MetricDomain.CO2`, `MetricDomain.LUX` — dùng ở mọi task sau.

---

- [ ] **Step 1: Sửa `DeviceCategory.java`**

```java
package com.iviet.ivshs.shared.enumeration;

public enum DeviceCategory {
    LIGHT,
    AIR_CONDITION,
    TEMPERATURE,
    POWER_CONSUMPTION,
    FAN,
    HUMIDITY,
    SENSOR_CO2,
    SENSOR_LUX
}
```

- [ ] **Step 2: Sửa `MetricDomain.java`**

```java
package com.iviet.ivshs.shared.enumeration;

public enum MetricDomain {
    ENERGY,
    HEALTH,
    DEVICE_STATUS,
    TEMPERATURE,
    HUMIDITY,
    CO2,
    LUX
}
```

- [ ] **Step 3: Sửa `SensorCategory.java`**

```java
package com.iviet.ivshs.shared.enumeration;

/**
 * Future refactor: Separate sensor and device types into distinct enums instead
 * of mixing them in 'DeviceCategory'.
 */
public enum SensorCategory {
  TEMPERATURE,
  POWER_CONSUMPTION,
  HUMIDITY,
  SENSOR_CO2,
  SENSOR_LUX
}
```

- [ ] **Step 4: Compile check**

```bash
cd /home/maithehao/Workspace/projects/smart-room-iot/smartroom_server && mvn compile -q
```
Expected: BUILD SUCCESS (không có error)

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/iviet/ivshs/shared/enumeration/DeviceCategory.java \
        src/main/java/com/iviet/ivshs/shared/enumeration/MetricDomain.java \
        src/main/java/com/iviet/ivshs/shared/enumeration/SensorCategory.java
git commit -m "feat(enums): add SENSOR_CO2, SENSOR_LUX to DeviceCategory, MetricDomain, SensorCategory"
```

---

## Task 2: DB Migration

**Files:**
- Create: `infra/database/migrations/002_Add_CO2_Lux_Sensors.sql`

**Interfaces:**
- Produces: 6 tables — `co2_sensor`, `co2_sensor_lan`, `co2_metrics`, `lux_sensor`, `lux_sensor_lan`, `lux_metrics`

---

- [ ] **Step 1: Tạo file migration**

```sql
-- Migration: Add CO2 Sensor and Lux Sensor Tables
USE smart_room_iot;

-- =====================
-- CO2 SENSOR
-- =====================

CREATE TABLE `co2_sensor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `is_active` BOOLEAN NOT NULL,
  `natural_id` varchar(256) NOT NULL,
  `specific_type` varchar(256) DEFAULT NULL,
  `current_co2` double DEFAULT NULL,
  `hardware_config_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_co2_sensor_natural_id` (`natural_id`),
  UNIQUE KEY `idx_co2_sensor_hardware_config_id` (`hardware_config_id`),
  KEY `idx_co2_sensor_room_id` (`room_id`),
  CONSTRAINT `fk_co2_sensor_hardware_config` FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config` (`id`),
  CONSTRAINT `fk_co2_sensor_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `co2_sensor_lan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `description` text,
  `lang_code` varchar(10) NOT NULL,
  `name` varchar(256) NOT NULL,
  `owner_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_co2_sensor_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_co2_sensor_lan_co2_sensor` FOREIGN KEY (`owner_id`) REFERENCES `co2_sensor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `co2_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_category` varchar(50) NOT NULL,
  `target_id` bigint NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `unix_minute` bigint NOT NULL,
  `co2` double NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_co2_metrics_target` (`target_category`, `target_id`, `timestamp`),
  KEY `idx_co2_metrics_timestamp` (`timestamp`),
  KEY `idx_co2m_unix_minute` (`unix_minute`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================
-- LUX SENSOR
-- =====================

CREATE TABLE `lux_sensor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `is_active` BOOLEAN NOT NULL,
  `natural_id` varchar(256) NOT NULL,
  `specific_type` varchar(256) DEFAULT NULL,
  `current_lux` double DEFAULT NULL,
  `hardware_config_id` bigint DEFAULT NULL,
  `room_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_lux_sensor_natural_id` (`natural_id`),
  UNIQUE KEY `idx_lux_sensor_hardware_config_id` (`hardware_config_id`),
  KEY `idx_lux_sensor_room_id` (`room_id`),
  CONSTRAINT `fk_lux_sensor_hardware_config` FOREIGN KEY (`hardware_config_id`) REFERENCES `hardware_config` (`id`),
  CONSTRAINT `fk_lux_sensor_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lux_sensor_lan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(256) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(256) DEFAULT NULL,
  `v` bigint NOT NULL,
  `description` text,
  `lang_code` varchar(10) NOT NULL,
  `name` varchar(256) NOT NULL,
  `owner_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_lux_sensor_lan_owner_id_lang_code` (`owner_id`, `lang_code`),
  CONSTRAINT `fk_lux_sensor_lan_lux_sensor` FOREIGN KEY (`owner_id`) REFERENCES `lux_sensor` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lux_metrics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `target_category` varchar(50) NOT NULL,
  `target_id` bigint NOT NULL,
  `timestamp` datetime(6) NOT NULL,
  `unix_minute` bigint NOT NULL,
  `lux` double NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lux_metrics_target` (`target_category`, `target_id`, `timestamp`),
  KEY `idx_lux_metrics_timestamp` (`timestamp`),
  KEY `idx_luxm_unix_minute` (`unix_minute`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 2: Commit**

```bash
git add infra/database/migrations/002_Add_CO2_Lux_Sensors.sql
git commit -m "db: add co2_sensor, lux_sensor tables with metrics and lan tables"
```

---

## Task 3: Entities — Sensor + Lan + Metric

**Files:**
- Create: `src/main/java/com/iviet/ivshs/entities/Co2Sensor.java`
- Create: `src/main/java/com/iviet/ivshs/entities/Co2SensorLan.java`
- Create: `src/main/java/com/iviet/ivshs/entities/LuxSensor.java`
- Create: `src/main/java/com/iviet/ivshs/entities/LuxSensorLan.java`
- Create: `src/main/java/com/iviet/ivshs/entities/Co2Metric.java`
- Create: `src/main/java/com/iviet/ivshs/entities/LuxMetric.java`

**Interfaces:**
- Consumes: `DeviceCategory.SENSOR_CO2`, `DeviceCategory.SENSOR_LUX` (Task 1)
- Produces:
  - `Co2Sensor` — class có field `Double currentCO2`, method `getCategory() → SENSOR_CO2`
  - `Co2SensorLan` — translation entity cho `Co2Sensor`
  - `LuxSensor` — class có field `Double currentLux`, method `getCategory() → SENSOR_LUX`
  - `LuxSensorLan` — translation entity cho `LuxSensor`
  - `Co2Metric` — metric entity, field `Double co2`, table `co2_metrics`
  - `LuxMetric` — metric entity, field `Double lux`, table `lux_metrics`

---

- [ ] **Step 1: Tạo `Co2Sensor.java`**

```java
package com.iviet.ivshs.entities;

import com.iviet.ivshs.dto.Co2SensorData;
import com.iviet.ivshs.entities.base.BaseIoTSensor;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "co2_sensor", indexes = {
        @Index(name = "idx_co2_sensor_room_id", columnList = "room_id", unique = false),
        @Index(name = "idx_co2_sensor_natural_id", columnList = "natural_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class Co2Sensor extends BaseIoTSensor<Co2SensorLan> {

    private static final long serialVersionUID = 1L;

    @Column(name = "current_co2")
    private Double currentCO2;

    @Override
    public Co2SensorData extractBusinessData() {
        return new Co2SensorData(this.currentCO2);
    }

    @Override
    public DeviceCategory getCategory() {
        return DeviceCategory.SENSOR_CO2;
    }
}
```

- [ ] **Step 2: Tạo `Co2SensorLan.java`**

```java
package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseTranslation;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "co2_sensor_lan", indexes = {
        @Index(name = "idx_co2_sensor_lan_owner_id_lang_code", columnList = "owner_id, lang_code", unique = true)
})
public class Co2SensorLan extends BaseTranslation<Co2Sensor> {
}
```

- [ ] **Step 3: Tạo `LuxSensor.java`**

```java
package com.iviet.ivshs.entities;

import com.iviet.ivshs.dto.LuxSensorData;
import com.iviet.ivshs.entities.base.BaseIoTSensor;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lux_sensor", indexes = {
        @Index(name = "idx_lux_sensor_room_id", columnList = "room_id", unique = false),
        @Index(name = "idx_lux_sensor_natural_id", columnList = "natural_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class LuxSensor extends BaseIoTSensor<LuxSensorLan> {

    private static final long serialVersionUID = 1L;

    @Column(name = "current_lux")
    private Double currentLux;

    @Override
    public LuxSensorData extractBusinessData() {
        return new LuxSensorData(this.currentLux);
    }

    @Override
    public DeviceCategory getCategory() {
        return DeviceCategory.SENSOR_LUX;
    }
}
```

- [ ] **Step 4: Tạo `LuxSensorLan.java`**

```java
package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseTranslation;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "lux_sensor_lan", indexes = {
        @Index(name = "idx_lux_sensor_lan_owner_id_lang_code", columnList = "owner_id, lang_code", unique = true)
})
public class LuxSensorLan extends BaseTranslation<LuxSensor> {
}
```

- [ ] **Step 5: Tạo `Co2Metric.java`**

```java
package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseMetricData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "co2_metrics", indexes = {
        @Index(name = "idx_co2_metrics_target", columnList = "target_category, target_id, timestamp"),
        @Index(name = "idx_co2_metrics_timestamp", columnList = "timestamp"),
        @Index(name = "idx_co2m_unix_minute", columnList = "unix_minute")
})
@Immutable
@Getter
@Setter
@NoArgsConstructor
public class Co2Metric extends BaseMetricData {

    @Column(name = "co2", nullable = false)
    private Double co2;

    @Override
    public void setTargetCategory(String targetCategory) {
        if (targetCategory == null || targetCategory.isBlank())
            throw new IllegalArgumentException("Target category cannot be null or blank");
        if (!"SENSOR_CO2".equals(targetCategory)) {
            throw new IllegalArgumentException("Invalid target category: " + targetCategory);
        }
        this.targetCategory = targetCategory;
    }
}
```

- [ ] **Step 6: Tạo `LuxMetric.java`**

```java
package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseMetricData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "lux_metrics", indexes = {
        @Index(name = "idx_lux_metrics_target", columnList = "target_category, target_id, timestamp"),
        @Index(name = "idx_lux_metrics_timestamp", columnList = "timestamp"),
        @Index(name = "idx_luxm_unix_minute", columnList = "unix_minute")
})
@Immutable
@Getter
@Setter
@NoArgsConstructor
public class LuxMetric extends BaseMetricData {

    @Column(name = "lux", nullable = false)
    private Double lux;

    @Override
    public void setTargetCategory(String targetCategory) {
        if (targetCategory == null || targetCategory.isBlank())
            throw new IllegalArgumentException("Target category cannot be null or blank");
        if (!"SENSOR_LUX".equals(targetCategory)) {
            throw new IllegalArgumentException("Invalid target category: " + targetCategory);
        }
        this.targetCategory = targetCategory;
    }
}
```

- [ ] **Step 7: Compile check**

```bash
cd /home/maithehao/Workspace/projects/smart-room-iot/smartroom_server && mvn compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/iviet/ivshs/entities/Co2Sensor.java \
        src/main/java/com/iviet/ivshs/entities/Co2SensorLan.java \
        src/main/java/com/iviet/ivshs/entities/LuxSensor.java \
        src/main/java/com/iviet/ivshs/entities/LuxSensorLan.java \
        src/main/java/com/iviet/ivshs/entities/Co2Metric.java \
        src/main/java/com/iviet/ivshs/entities/LuxMetric.java
git commit -m "feat(entities): add Co2Sensor, LuxSensor entities with Lan and Metric classes"
```

---

## Task 4: DTOs

**Files:**
- Create: `src/main/java/com/iviet/ivshs/dto/Co2SensorData.java`
- Create: `src/main/java/com/iviet/ivshs/dto/LuxSensorData.java`
- Create: `src/main/java/com/iviet/ivshs/dto/Co2MetricDto.java`
- Create: `src/main/java/com/iviet/ivshs/dto/LuxMetricDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dto/SensorSpecificData.java`

**Interfaces:**
- Consumes: `Co2Metric` (Task 3), `LuxMetric` (Task 3)
- Produces:
  - `Co2SensorData(Double currentCO2)` — record, implements `SensorSpecificData`
  - `LuxSensorData(Double currentLux)` — record, implements `SensorSpecificData`
  - `Co2MetricDto` — `timestamp`, `co2`, static `fromEntity(Co2Metric)`
  - `LuxMetricDto` — `timestamp`, `lux`, static `fromEntity(LuxMetric)`

---

- [ ] **Step 1: Tạo `Co2SensorData.java`**

```java
package com.iviet.ivshs.dto;

public record Co2SensorData(Double currentCO2) implements SensorSpecificData {
}
```

- [ ] **Step 2: Tạo `LuxSensorData.java`**

```java
package com.iviet.ivshs.dto;

public record LuxSensorData(Double currentLux) implements SensorSpecificData {
}
```

- [ ] **Step 3: Tạo `Co2MetricDto.java`**

```java
package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iviet.ivshs.entities.Co2Metric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Getter
@Setter
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Co2MetricDto {

    private Instant timestamp;
    private Double co2;

    public static Co2MetricDto fromEntity(Co2Metric entity) {
        return Co2MetricDto.builder()
                .timestamp(entity.getTimestamp())
                .co2(entity.getCo2())
                .build();
    }
}
```

- [ ] **Step 4: Tạo `LuxMetricDto.java`**

```java
package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iviet.ivshs.entities.LuxMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Getter
@Setter
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LuxMetricDto {

    private Instant timestamp;
    private Double lux;

    public static LuxMetricDto fromEntity(LuxMetric entity) {
        return LuxMetricDto.builder()
                .timestamp(entity.getTimestamp())
                .lux(entity.getLux())
                .build();
    }
}
```

- [ ] **Step 5: Sửa `SensorSpecificData.java`**

Thêm 2 subtype mới vào sealed interface:

```java
package com.iviet.ivshs.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(TemperatureSensorData.class),
    @JsonSubTypes.Type(PowerConsumptionSensorData.class),
    @JsonSubTypes.Type(HumiditySensorData.class),
    @JsonSubTypes.Type(Co2SensorData.class),
    @JsonSubTypes.Type(LuxSensorData.class)
})
public sealed interface SensorSpecificData
    permits TemperatureSensorData, PowerConsumptionSensorData, HumiditySensorData,
            Co2SensorData, LuxSensorData {
}
```

- [ ] **Step 6: Compile check**

```bash
cd /home/maithehao/Workspace/projects/smart-room-iot/smartroom_server && mvn compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dto/Co2SensorData.java \
        src/main/java/com/iviet/ivshs/dto/LuxSensorData.java \
        src/main/java/com/iviet/ivshs/dto/Co2MetricDto.java \
        src/main/java/com/iviet/ivshs/dto/LuxMetricDto.java \
        src/main/java/com/iviet/ivshs/dto/SensorSpecificData.java
git commit -m "feat(dto): add Co2SensorData, LuxSensorData records and Co2MetricDto, LuxMetricDto"
```

---

## Task 5: DAOs

**Files:**
- Create: `src/main/java/com/iviet/ivshs/dao/Co2SensorDao.java`
- Create: `src/main/java/com/iviet/ivshs/dao/LuxSensorDao.java`
- Create: `src/main/java/com/iviet/ivshs/dao/Co2MetricDao.java`
- Create: `src/main/java/com/iviet/ivshs/dao/LuxMetricDao.java`

**Interfaces:**
- Consumes: `Co2Sensor`, `LuxSensor`, `Co2Metric`, `LuxMetric`, `Co2MetricDto`, `LuxMetricDto` (Tasks 3, 4)
- Produces:
  - `Co2SensorDao.findByNaturalId(String)` → `Optional<Co2Sensor>`
  - `Co2SensorDao.findByNaturalId(String, String)` → `Optional<Co2Sensor>`
  - `Co2SensorDao.findByRoomAndNaturalId(Long, String, String)` → `Optional<Co2Sensor>`
  - `Co2MetricDao.save(List<Co2Metric>)` → `List<Co2Metric>`
  - `Co2MetricDao.findHistory(Long, Instant, Instant)` → `List<Co2Metric>`
  - `Co2MetricDao.findHistory(Long, Instant, Instant, int)` → `List<Co2MetricDto>`
  - `Co2MetricDao.findLatest(Long)` → `Optional<Co2Metric>`
  - (tương tự cho Lux)

---

- [ ] **Step 1: Tạo `Co2SensorDao.java`**

```java
package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseIoTSensorDao;
import com.iviet.ivshs.entities.Co2Sensor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class Co2SensorDao extends BaseIoTSensorDao<Co2Sensor> {

    public Co2SensorDao() {
        super(Co2Sensor.class);
    }

    @Override
    public Optional<Co2Sensor> findByNaturalId(String naturalId) {
        return findOne(root ->
            entityManager.getCriteriaBuilder().equal(root.get("naturalId"), naturalId)
        );
    }

    @Override
    public Optional<Co2Sensor> findByNaturalId(String naturalId, String langCode) {
        String jpql = """
                SELECT cs
                FROM Co2Sensor cs
                LEFT JOIN cs.translations tl ON tl.langCode = :langCode
                WHERE cs.naturalId = :naturalId
                """;
        return entityManager.createQuery(jpql, Co2Sensor.class)
                .setParameter("naturalId", naturalId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<Co2Sensor> findByRoomAndNaturalId(Long roomId, String naturalId, String langCode) {
        String jpql = """
                SELECT cs
                FROM Co2Sensor cs
                LEFT JOIN cs.translations tl ON tl.langCode = :langCode
                WHERE cs.room.id = :roomId AND cs.naturalId = :naturalId
                """;
        return entityManager.createQuery(jpql, Co2Sensor.class)
                .setParameter("roomId", roomId)
                .setParameter("naturalId", naturalId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
```

- [ ] **Step 2: Tạo `LuxSensorDao.java`**

```java
package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseIoTSensorDao;
import com.iviet.ivshs.entities.LuxSensor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class LuxSensorDao extends BaseIoTSensorDao<LuxSensor> {

    public LuxSensorDao() {
        super(LuxSensor.class);
    }

    @Override
    public Optional<LuxSensor> findByNaturalId(String naturalId) {
        return findOne(root ->
            entityManager.getCriteriaBuilder().equal(root.get("naturalId"), naturalId)
        );
    }

    @Override
    public Optional<LuxSensor> findByNaturalId(String naturalId, String langCode) {
        String jpql = """
                SELECT ls
                FROM LuxSensor ls
                LEFT JOIN ls.translations tl ON tl.langCode = :langCode
                WHERE ls.naturalId = :naturalId
                """;
        return entityManager.createQuery(jpql, LuxSensor.class)
                .setParameter("naturalId", naturalId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Optional<LuxSensor> findByRoomAndNaturalId(Long roomId, String naturalId, String langCode) {
        String jpql = """
                SELECT ls
                FROM LuxSensor ls
                LEFT JOIN ls.translations tl ON tl.langCode = :langCode
                WHERE ls.room.id = :roomId AND ls.naturalId = :naturalId
                """;
        return entityManager.createQuery(jpql, LuxSensor.class)
                .setParameter("roomId", roomId)
                .setParameter("naturalId", naturalId)
                .setParameter("langCode", langCode)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
```

- [ ] **Step 3: Tạo `Co2MetricDao.java`**

```java
package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseEntityDao;
import com.iviet.ivshs.dto.Co2MetricDto;
import com.iviet.ivshs.entities.Co2Metric;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class Co2MetricDao extends BaseEntityDao<Co2Metric> {

    public Co2MetricDao() {
        super(Co2Metric.class);
    }

    @Override
    @Transactional
    public List<Co2Metric> save(List<Co2Metric> entities) {
        String sql = """
                INSERT INTO co2_metrics
                (target_category, target_id, timestamp, unix_minute, co2)
                VALUES (?, ?, ?, ?, ?)
                """;
        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                    Co2Metric e = entities.get(i);
                    ps.setString(1, e.getTargetCategory());
                    ps.setLong(2, e.getTargetId());
                    ps.setObject(3, e.getTimestamp());
                    ps.setObject(4, e.getUnixMinute());
                    ps.setDouble(5, e.getCo2());
                }

                @Override
                public int getBatchSize() {
                    return entities.size();
                }
            });
            return entities;
        } catch (Exception e) {
            throw new RuntimeException("Failed to batch insert Co2Metric entities", e);
        }
    }

    public List<Co2Metric> findHistory(Long targetId, Instant from, Instant to) {
        String jpql = """
                SELECT cm
                FROM Co2Metric cm
                WHERE cm.targetId = :targetId
                  AND cm.timestamp BETWEEN :from AND :to
                ORDER BY cm.timestamp ASC
                """;
        return entityManager.createQuery(jpql, Co2Metric.class)
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    public List<Co2MetricDto> findHistory(Long targetId, Instant from, Instant to, int divisor) {
        String jpql = """
                SELECT
                    (cm.unixMinute - MOD(cm.unixMinute, :divisor)) * 60L,
                    AVG(cm.co2)
                FROM Co2Metric cm
                WHERE cm.targetId = :targetId
                    AND cm.timestamp BETWEEN :from AND :to
                GROUP BY (cm.unixMinute - MOD(cm.unixMinute, :divisor)) * 60L
                ORDER BY (cm.unixMinute - MOD(cm.unixMinute, :divisor)) * 60L ASC
                """;

        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("divisor", divisor)
                .getResultList();

        return results.stream()
                .map(row -> Co2MetricDto.builder()
                        .timestamp(Instant.ofEpochSecond(((Number) row[0]).longValue()))
                        .co2(row[1] != null ? ((Number) row[1]).doubleValue() : null)
                        .build())
                .toList();
    }

    public Optional<Co2Metric> findLatest(Long targetId) {
        String jpql = """
                SELECT cm
                FROM Co2Metric cm
                WHERE cm.targetId = :targetId
                ORDER BY cm.timestamp DESC
                """;
        return entityManager.createQuery(jpql, Co2Metric.class)
                .setParameter("targetId", targetId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
```

- [ ] **Step 4: Tạo `LuxMetricDao.java`**

```java
package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseEntityDao;
import com.iviet.ivshs.dto.LuxMetricDto;
import com.iviet.ivshs.entities.LuxMetric;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class LuxMetricDao extends BaseEntityDao<LuxMetric> {

    public LuxMetricDao() {
        super(LuxMetric.class);
    }

    @Override
    @Transactional
    public List<LuxMetric> save(List<LuxMetric> entities) {
        String sql = """
                INSERT INTO lux_metrics
                (target_category, target_id, timestamp, unix_minute, lux)
                VALUES (?, ?, ?, ?, ?)
                """;
        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                    LuxMetric e = entities.get(i);
                    ps.setString(1, e.getTargetCategory());
                    ps.setLong(2, e.getTargetId());
                    ps.setObject(3, e.getTimestamp());
                    ps.setObject(4, e.getUnixMinute());
                    ps.setDouble(5, e.getLux());
                }

                @Override
                public int getBatchSize() {
                    return entities.size();
                }
            });
            return entities;
        } catch (Exception e) {
            throw new RuntimeException("Failed to batch insert LuxMetric entities", e);
        }
    }

    public List<LuxMetric> findHistory(Long targetId, Instant from, Instant to) {
        String jpql = """
                SELECT lm
                FROM LuxMetric lm
                WHERE lm.targetId = :targetId
                  AND lm.timestamp BETWEEN :from AND :to
                ORDER BY lm.timestamp ASC
                """;
        return entityManager.createQuery(jpql, LuxMetric.class)
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    public List<LuxMetricDto> findHistory(Long targetId, Instant from, Instant to, int divisor) {
        String jpql = """
                SELECT
                    (lm.unixMinute - MOD(lm.unixMinute, :divisor)) * 60L,
                    AVG(lm.lux)
                FROM LuxMetric lm
                WHERE lm.targetId = :targetId
                    AND lm.timestamp BETWEEN :from AND :to
                GROUP BY (lm.unixMinute - MOD(lm.unixMinute, :divisor)) * 60L
                ORDER BY (lm.unixMinute - MOD(lm.unixMinute, :divisor)) * 60L ASC
                """;

        List<Object[]> results = entityManager.createQuery(jpql, Object[].class)
                .setParameter("targetId", targetId)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("divisor", divisor)
                .getResultList();

        return results.stream()
                .map(row -> LuxMetricDto.builder()
                        .timestamp(Instant.ofEpochSecond(((Number) row[0]).longValue()))
                        .lux(row[1] != null ? ((Number) row[1]).doubleValue() : null)
                        .build())
                .toList();
    }

    public Optional<LuxMetric> findLatest(Long targetId) {
        String jpql = """
                SELECT lm
                FROM LuxMetric lm
                WHERE lm.targetId = :targetId
                ORDER BY lm.timestamp DESC
                """;
        return entityManager.createQuery(jpql, LuxMetric.class)
                .setParameter("targetId", targetId)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }
}
```

- [ ] **Step 5: Compile check**

```bash
cd /home/maithehao/Workspace/projects/smart-room-iot/smartroom_server && mvn compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dao/Co2SensorDao.java \
        src/main/java/com/iviet/ivshs/dao/LuxSensorDao.java \
        src/main/java/com/iviet/ivshs/dao/Co2MetricDao.java \
        src/main/java/com/iviet/ivshs/dao/LuxMetricDao.java
git commit -m "feat(dao): add Co2SensorDao, LuxSensorDao, Co2MetricDao, LuxMetricDao"
```

---

## Task 6: Setup Strategies

**Files:**
- Create: `src/main/java/com/iviet/ivshs/dao/setup/Co2SetupStrategy.java`
- Create: `src/main/java/com/iviet/ivshs/dao/setup/LuxSetupStrategy.java`

**Interfaces:**
- Consumes: `Co2SensorDao`, `LuxSensorDao`, `Co2Sensor`, `LuxSensor`, `Co2SensorLan`, `LuxSensorLan` (Tasks 3, 5)
- Consumes: `DeviceCategory.SENSOR_CO2`, `DeviceCategory.SENSOR_LUX` (Task 1)
- Produces: `Co2SetupStrategy` và `LuxSetupStrategy` — auto-registered vào `DeviceSetupOrchestrator` qua Spring `@Component`

---

- [ ] **Step 1: Tạo `Co2SetupStrategy.java`**

```java
package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dao.Co2SensorDao;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.entities.Co2Sensor;
import com.iviet.ivshs.entities.Co2SensorLan;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Co2SetupStrategy extends AbstractDeviceSetupStrategy {

    private final Co2SensorDao co2SensorDao;

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.SENSOR_CO2;
    }

    @Override
    public void persist(
            SetupRequest.BodyData.DeviceConfig device,
            Room room,
            HardwareConfig hardwareConfig) {
        Co2Sensor co2Sensor = new Co2Sensor();
        setupBaseIoTProperties(co2Sensor, device, room, hardwareConfig);
        entityManager.persist(co2Sensor);
        entityManager.flush();
        attachTranslations(co2Sensor, device.getTranslations(), Co2SensorLan::new);
        log.debug("Create: Device created: {}", device.getNaturalId());
    }

    @Override
    public void rollback(Long deviceId) {
        try {
            co2SensorDao.deleteById(deviceId);
            log.debug("Rollback: Rolled back: {}", deviceId);
        } catch (Exception e) {
            log.error("Rollback: Failed for {}: {}", deviceId, e.getMessage(), e);
        }
    }
}
```

- [ ] **Step 2: Tạo `LuxSetupStrategy.java`**

```java
package com.iviet.ivshs.dao.setup;

import com.iviet.ivshs.dao.LuxSensorDao;
import com.iviet.ivshs.dto.SetupRequest;
import com.iviet.ivshs.entities.HardwareConfig;
import com.iviet.ivshs.entities.Room;
import com.iviet.ivshs.entities.LuxSensor;
import com.iviet.ivshs.entities.LuxSensorLan;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LuxSetupStrategy extends AbstractDeviceSetupStrategy {

    private final LuxSensorDao luxSensorDao;

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.SENSOR_LUX;
    }

    @Override
    public void persist(
            SetupRequest.BodyData.DeviceConfig device,
            Room room,
            HardwareConfig hardwareConfig) {
        LuxSensor luxSensor = new LuxSensor();
        setupBaseIoTProperties(luxSensor, device, room, hardwareConfig);
        entityManager.persist(luxSensor);
        entityManager.flush();
        attachTranslations(luxSensor, device.getTranslations(), LuxSensorLan::new);
        log.debug("Create: Device created: {}", device.getNaturalId());
    }

    @Override
    public void rollback(Long deviceId) {
        try {
            luxSensorDao.deleteById(deviceId);
            log.debug("Rollback: Rolled back: {}", deviceId);
        } catch (Exception e) {
            log.error("Rollback: Failed for {}: {}", deviceId, e.getMessage(), e);
        }
    }
}
```

- [ ] **Step 3: Compile check**

```bash
cd /home/maithehao/Workspace/projects/smart-room-iot/smartroom_server && mvn compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dao/setup/Co2SetupStrategy.java \
        src/main/java/com/iviet/ivshs/dao/setup/LuxSetupStrategy.java
git commit -m "feat(setup): add Co2SetupStrategy and LuxSetupStrategy"
```

---

## Task 7: Service Interfaces

**Files:**
- Create: `src/main/java/com/iviet/ivshs/service/Co2MetricService.java`
- Create: `src/main/java/com/iviet/ivshs/service/LuxMetricService.java`

**Interfaces:**
- Produces: `Co2MetricService` và `LuxMetricService` — marker interfaces, dùng bởi Task 8

---

- [ ] **Step 1: Tạo `Co2MetricService.java`**

```java
package com.iviet.ivshs.service;

import com.iviet.ivshs.service.strategy.MetricServiceStrategy;
import com.iviet.ivshs.service.strategy.SensorTelemetryServiceStrategy;
import com.iviet.ivshs.service.strategy.TelemetryCRUDServiceStrategy;

/**
 * Service interface for handling CO2 Metrics, extending the platform's strategies.
 */
public interface Co2MetricService extends TelemetryCRUDServiceStrategy, MetricServiceStrategy, SensorTelemetryServiceStrategy {
}
```

- [ ] **Step 2: Tạo `LuxMetricService.java`**

```java
package com.iviet.ivshs.service;

import com.iviet.ivshs.service.strategy.MetricServiceStrategy;
import com.iviet.ivshs.service.strategy.SensorTelemetryServiceStrategy;
import com.iviet.ivshs.service.strategy.TelemetryCRUDServiceStrategy;

/**
 * Service interface for handling Lux Metrics, extending the platform's strategies.
 */
public interface LuxMetricService extends TelemetryCRUDServiceStrategy, MetricServiceStrategy, SensorTelemetryServiceStrategy {
}
```

- [ ] **Step 3: Compile check**

```bash
cd /home/maithehao/Workspace/projects/smart-room-iot/smartroom_server && mvn compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/iviet/ivshs/service/Co2MetricService.java \
        src/main/java/com/iviet/ivshs/service/LuxMetricService.java
git commit -m "feat(service): add Co2MetricService and LuxMetricService interfaces"
```

---

## Task 8: Service Implementations

**Files:**
- Create: `src/main/java/com/iviet/ivshs/service/impl/Co2MetricServiceImpl.java`
- Create: `src/main/java/com/iviet/ivshs/service/impl/LuxMetricServiceImpl.java`

**Interfaces:**
- Consumes: `Co2MetricService`, `LuxMetricService` (Task 7)
- Consumes: `Co2SensorDao`, `LuxSensorDao`, `Co2MetricDao`, `LuxMetricDao` (Task 5)
- Consumes: `Co2Metric`, `LuxMetric`, `Co2MetricDto`, `LuxMetricDto` (Tasks 3, 4)
- Consumes: `DeviceCategory.SENSOR_CO2`, `DeviceCategory.SENSOR_LUX`, `MetricDomain.CO2`, `MetricDomain.LUX` (Task 1)
- Produces: Spring `@Service` beans, auto-registered vào `TelemetryServiceImpl.strategyMap` theo key `SENSOR_CO2` / `SENSOR_LUX`

---

- [ ] **Step 1: Tạo `Co2MetricServiceImpl.java`**

```java
package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.Co2SensorDao;
import com.iviet.ivshs.dao.Co2MetricDao;
import com.iviet.ivshs.dto.Co2MetricDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.entities.Co2Metric;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.MetricDomain;
import com.iviet.ivshs.shared.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.service.Co2MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Co2MetricServiceImpl implements Co2MetricService {

    private final Co2SensorDao co2SensorDao;
    private final Co2MetricDao co2MetricDao;

    // ========== TelemetryCRUDServiceStrategy ==========

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.SENSOR_CO2;
    }

    @Override
    @Transactional
    public void create(TelemetryResponseDto.DeviceDto data) {
        JsonNode co2Node = data.getData().get("co2");
        if (co2Node == null)
            return;

        Double co2Value;
        if (co2Node.isNumber()) {
            co2Value = co2Node.asDouble();
        } else if (co2Node.isTextual()) {
            try {
                co2Value = Double.parseDouble(co2Node.asText());
            } catch (NumberFormatException e) {
                log.error("Failed to parse co2 value '{}' for sensor {}: {}",
                    co2Node.asText(), data.getNaturalId(), e.getMessage());
                return;
            }
        } else {
            return;
        }

        var sensor = co2SensorDao.findByNaturalId(data.getNaturalId())
            .orElseThrow(() -> new NotFoundException("CO2 sensor not found with natural ID: " + data.getNaturalId()));

        sensor.setCurrentCO2(co2Value);
        co2SensorDao.save(sensor);

        Co2Metric metric = new Co2Metric();
        metric.setTargetCategory("SENSOR_CO2");
        metric.setTargetId(sensor.getId());
        metric.setTimestamp(Instant.now());
        metric.setCo2(co2Value);

        co2MetricDao.save(Collections.singletonList(metric));
        log.info("Successfully saved CO2 metric {} ppm for sensor {}", co2Value, sensor.getNaturalId());
    }

    // ========== MetricServiceStrategy ==========

    @Override
    public MetricDomain getSupportedDomain() {
        return MetricDomain.CO2;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getLatest(String category, Long targetId) {
        return co2MetricDao.findLatest(targetId)
                .map(Co2MetricDto::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(String category, Long targetId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        int divisor = TelemetryTimeGroup.getDivisorForRange(limitedFrom, to);
        return co2MetricDao.findHistory(targetId, limitedFrom, to, divisor);
    }

    // ========== SensorTelemetryServiceStrategy ==========

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(Long sensorId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        return co2MetricDao.findHistory(sensorId, limitedFrom, to)
                .stream()
                .map(Co2MetricDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistoryByNaturalId(String naturalId, Instant from, Instant to) {
        var sensor = co2SensorDao.findByNaturalId(naturalId)
            .orElseThrow(() -> new NotFoundException("CO2 sensor not found: " + naturalId));
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        return co2MetricDao.findHistory(sensor.getId(), limitedFrom, to)
                .stream()
                .map(Co2MetricDto::fromEntity)
                .toList();
    }
}
```

- [ ] **Step 2: Tạo `LuxMetricServiceImpl.java`**

```java
package com.iviet.ivshs.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.LuxSensorDao;
import com.iviet.ivshs.dao.LuxMetricDao;
import com.iviet.ivshs.dto.LuxMetricDto;
import com.iviet.ivshs.dto.TelemetryResponseDto;
import com.iviet.ivshs.entities.LuxMetric;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.enumeration.MetricDomain;
import com.iviet.ivshs.shared.enumeration.TelemetryTimeGroup;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.service.LuxMetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LuxMetricServiceImpl implements LuxMetricService {

    private final LuxSensorDao luxSensorDao;
    private final LuxMetricDao luxMetricDao;

    // ========== TelemetryCRUDServiceStrategy ==========

    @Override
    public DeviceCategory getSupportedCategory() {
        return DeviceCategory.SENSOR_LUX;
    }

    @Override
    @Transactional
    public void create(TelemetryResponseDto.DeviceDto data) {
        JsonNode luxNode = data.getData().get("lux");
        if (luxNode == null)
            return;

        Double luxValue;
        if (luxNode.isNumber()) {
            luxValue = luxNode.asDouble();
        } else if (luxNode.isTextual()) {
            try {
                luxValue = Double.parseDouble(luxNode.asText());
            } catch (NumberFormatException e) {
                log.error("Failed to parse lux value '{}' for sensor {}: {}",
                    luxNode.asText(), data.getNaturalId(), e.getMessage());
                return;
            }
        } else {
            return;
        }

        var sensor = luxSensorDao.findByNaturalId(data.getNaturalId())
            .orElseThrow(() -> new NotFoundException("Lux sensor not found with natural ID: " + data.getNaturalId()));

        sensor.setCurrentLux(luxValue);
        luxSensorDao.save(sensor);

        LuxMetric metric = new LuxMetric();
        metric.setTargetCategory("SENSOR_LUX");
        metric.setTargetId(sensor.getId());
        metric.setTimestamp(Instant.now());
        metric.setLux(luxValue);

        luxMetricDao.save(Collections.singletonList(metric));
        log.info("Successfully saved Lux metric {} lux for sensor {}", luxValue, sensor.getNaturalId());
    }

    // ========== MetricServiceStrategy ==========

    @Override
    public MetricDomain getSupportedDomain() {
        return MetricDomain.LUX;
    }

    @Override
    @Transactional(readOnly = true)
    public Object getLatest(String category, Long targetId) {
        return luxMetricDao.findLatest(targetId)
                .map(LuxMetricDto::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(String category, Long targetId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        int divisor = TelemetryTimeGroup.getDivisorForRange(limitedFrom, to);
        return luxMetricDao.findHistory(targetId, limitedFrom, to, divisor);
    }

    // ========== SensorTelemetryServiceStrategy ==========

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistory(Long sensorId, Instant from, Instant to) {
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        return luxMetricDao.findHistory(sensorId, limitedFrom, to)
                .stream()
                .map(LuxMetricDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<?> getHistoryByNaturalId(String naturalId, Instant from, Instant to) {
        var sensor = luxSensorDao.findByNaturalId(naturalId)
            .orElseThrow(() -> new NotFoundException("Lux sensor not found: " + naturalId));
        Instant limitedFrom = TelemetryTimeGroup.limitRange(from, to);
        return luxMetricDao.findHistory(sensor.getId(), limitedFrom, to)
                .stream()
                .map(LuxMetricDto::fromEntity)
                .toList();
    }
}
```

- [ ] **Step 3: Compile check — final**

```bash
cd /home/maithehao/Workspace/projects/smart-room-iot/smartroom_server && mvn compile -q
```
Expected: BUILD SUCCESS (toàn bộ 25 files đã đúng)

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/iviet/ivshs/service/impl/Co2MetricServiceImpl.java \
        src/main/java/com/iviet/ivshs/service/impl/LuxMetricServiceImpl.java
git commit -m "feat(service): implement Co2MetricServiceImpl and LuxMetricServiceImpl"
```

---

## Checklist tổng kết

| Task | Files | Status |
|------|-------|--------|
| T1: Enums | DeviceCategory, MetricDomain, SensorCategory | `[ ]` |
| T2: DB Migration | 002_Add_CO2_Lux_Sensors.sql | `[ ]` |
| T3: Entities | Co2Sensor, Co2SensorLan, LuxSensor, LuxSensorLan, Co2Metric, LuxMetric | `[ ]` |
| T4: DTOs | Co2SensorData, LuxSensorData, Co2MetricDto, LuxMetricDto, SensorSpecificData | `[ ]` |
| T5: DAOs | Co2SensorDao, LuxSensorDao, Co2MetricDao, LuxMetricDao | `[ ]` |
| T6: Setup Strategies | Co2SetupStrategy, LuxSetupStrategy | `[ ]` |
| T7: Service Interfaces | Co2MetricService, LuxMetricService | `[ ]` |
| T8: Service Impls | Co2MetricServiceImpl, LuxMetricServiceImpl | `[ ]` |
