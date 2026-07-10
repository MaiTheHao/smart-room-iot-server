# Device Status Backup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Capture periodic snapshots of active device states from the database and save them as a time-series status history in the database.

**Architecture:** A Quartz scheduler job triggers a service to query all active devices (Lights, Fans, ACs, sensors), serialize their current status to JSON, and save them in batch to a new table `device_status_metrics`.

**Tech Stack:** Java, Spring Boot, Spring Data JPA, Hibernate, Quartz Scheduler, Jackson, MySQL/MariaDB.

## Global Constraints
* JSON column must use the `TEXT` database type for cross-db compatibility.
* Use `jdbcTemplate.batchUpdate` for efficient batch insertion of metrics.
* Avoid any HTTP gateway calls during status backup; rely entirely on the database state.

---

### Task 1: Database Migration

**Files:**
* Create: `infra/database/migrations/007__Create_Device_Status_Metrics_Table.sql`

**Interfaces:**
* Produces: `device_status_metrics` table in the database schema.

- [ ] **Step 1: Write migration SQL**
  Create the migration file `infra/database/migrations/007__Create_Device_Status_Metrics_Table.sql` with the following content:
  ```sql
  -- =============================================================================
  -- Migration: Create Device Status Metrics Table
  -- Date       : 2026-07-10
  -- Description: Create the `device_status_metrics` table to store status snapshots.
  -- =============================================================================

  CREATE TABLE `device_status_metrics` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `target_category` varchar(50) NOT NULL,
    `target_id` bigint NOT NULL,
    `timestamp` datetime(6) NOT NULL,
    `status_data` text DEFAULT NULL,
    `unix_minute` bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_device_status_metrics_target` (`target_category`, `target_id`, `timestamp`),
    KEY `idx_device_status_metrics_timestamp` (`timestamp`),
    KEY `idx_dsm_unix_minute` (`unix_minute`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
  ```

- [ ] **Step 2: Commit migration file**
  ```bash
  git add infra/database/migrations/007__Create_Device_Status_Metrics_Table.sql
  git commit -m "migration: create device_status_metrics table"
  ```

---

### Task 2: Database Entity

**Files:**
* Create: `src/main/java/com/iviet/ivshs/entities/DeviceStatusMetric.java`

**Interfaces:**
* Produces: `DeviceStatusMetric` JPA entity class.

- [ ] **Step 1: Create DeviceStatusMetric entity class**
  Create `src/main/java/com/iviet/ivshs/entities/DeviceStatusMetric.java` with the following content:
  ```java
  package com.iviet.ivshs.entities;

  import com.fasterxml.jackson.databind.JsonNode;
  import com.iviet.ivshs.entities.base.BaseMetricData;
  import com.iviet.ivshs.entities.converter.JsonNodeConverter;
  import com.iviet.ivshs.shared.enumeration.DeviceCategory;
  import jakarta.persistence.Column;
  import jakarta.persistence.Convert;
  import jakarta.persistence.Entity;
  import jakarta.persistence.Index;
  import jakarta.persistence.Table;
  import lombok.Getter;
  import lombok.NoArgsConstructor;
  import lombok.Setter;
  import org.hibernate.annotations.Immutable;

  @Entity
  @Table(name = "device_status_metrics", indexes = {
          @Index(name = "idx_device_status_metrics_target", columnList = "target_category, target_id, timestamp"),
          @Index(name = "idx_device_status_metrics_timestamp", columnList = "timestamp"),
          @Index(name = "idx_dsm_unix_minute", columnList = "unix_minute")
  })
  @Immutable
  @Getter
  @Setter
  @NoArgsConstructor
  public class DeviceStatusMetric extends BaseMetricData {

      @Column(name = "status_data", columnDefinition = "TEXT")
      @Convert(converter = JsonNodeConverter.class)
      private JsonNode statusData;

      @Override
      public void setTargetCategory(String targetCategory) {
          if (targetCategory == null || targetCategory.isBlank())
              throw new IllegalArgumentException("Target category cannot be null or blank");
          try {
              DeviceCategory.valueOf(targetCategory);
          } catch (IllegalArgumentException e) {
              throw new IllegalArgumentException("Invalid target category: " + targetCategory);
          }
          this.targetCategory = targetCategory;
      }
  }
  ```

- [ ] **Step 2: Commit entity class**
  ```bash
  git add src/main/java/com/iviet/ivshs/entities/DeviceStatusMetric.java
  git commit -m "feat: add DeviceStatusMetric JPA entity"
  ```

---

### Task 3: Shared DAO Extension

**Files:**
* Modify: `src/main/java/com/iviet/ivshs/dao/base/BaseIoTEntityDao.java`

**Interfaces:**
* Produces: `public List<T> findAllActive()` helper method on `BaseIoTEntityDao`.

- [ ] **Step 1: Add findAllActive helper method to BaseIoTEntityDao**
  Add the following method to `src/main/java/com/iviet/ivshs/dao/base/BaseIoTEntityDao.java`:
  ```java
    public List<T> findAllActive() {
      return findAll(
          root -> entityManager.getCriteriaBuilder().isTrue(root.get("isActive")),
          (root, cq) -> {
            root.fetch("room", jakarta.persistence.criteria.JoinType.LEFT);
          });
    }
  ```

- [ ] **Step 2: Commit changes to BaseIoTEntityDao**
  ```bash
  git add src/main/java/com/iviet/ivshs/dao/base/BaseIoTEntityDao.java
  git commit -m "feat: add findAllActive method to BaseIoTEntityDao"
  ```

---

### Task 4: Data Access Layer (DAO & DTO)

**Files:**
* Create: `src/main/java/com/iviet/ivshs/dao/DeviceStatusMetricDao.java`
* Create: `src/main/java/com/iviet/ivshs/dto/DeviceStatusMetricDto.java`

**Interfaces:**
* Consumes: `DeviceStatusMetric` entity.
* Produces: `DeviceStatusMetricDao` and `DeviceStatusMetricDto` classes.

- [ ] **Step 1: Create DeviceStatusMetricDao class**
  Create `src/main/java/com/iviet/ivshs/dao/DeviceStatusMetricDao.java` with the following content:
  ```java
  package com.iviet.ivshs.dao;

  import com.iviet.ivshs.dao.base.BaseEntityDao;
  import com.iviet.ivshs.entities.DeviceStatusMetric;
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
  public class DeviceStatusMetricDao extends BaseEntityDao<DeviceStatusMetric> {

      public DeviceStatusMetricDao() {
          super(DeviceStatusMetric.class);
      }

      @Override
      @Transactional
      public List<DeviceStatusMetric> save(List<DeviceStatusMetric> entities) {
          String sql = """
                  INSERT INTO device_status_metrics
                  (target_category, target_id, timestamp, unix_minute, status_data)
                  VALUES (?, ?, ?, ?, ?)
                  """;
          try {
              jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                  @Override
                  public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                      DeviceStatusMetric e = entities.get(i);
                      ps.setString(1, e.getTargetCategory());
                      ps.setLong(2, e.getTargetId());
                      ps.setObject(3, e.getTimestamp());
                      ps.setObject(4, e.getUnixMinute());
                      ps.setString(5, e.getStatusData() != null ? e.getStatusData().toString() : null);
                  }

                  @Override
                  public int getBatchSize() {
                      return entities.size();
                  }
              });
              return entities;
          } catch (Exception e) {
              throw new RuntimeException("Failed to batch insert DeviceStatusMetric entities", e);
          }
      }

      public List<DeviceStatusMetric> findHistory(String category, Long targetId, Instant from, Instant to) {
          String jpql = """
                  SELECT dsm
                  FROM DeviceStatusMetric dsm
                  WHERE dsm.targetCategory = :category
                    AND dsm.targetId = :targetId
                    AND dsm.timestamp BETWEEN :from AND :to
                  ORDER BY dsm.timestamp ASC
                  """;
          return entityManager.createQuery(jpql, DeviceStatusMetric.class)
                  .setParameter("category", category)
                  .setParameter("targetId", targetId)
                  .setParameter("from", from)
                  .setParameter("to", to)
                  .getResultList();
      }

      public Optional<DeviceStatusMetric> findLatest(String category, Long targetId) {
          String jpql = """
                  SELECT dsm
                  FROM DeviceStatusMetric dsm
                  WHERE dsm.targetCategory = :category
                    AND dsm.targetId = :targetId
                  ORDER BY dsm.timestamp DESC
                  """;
          return entityManager.createQuery(jpql, DeviceStatusMetric.class)
                  .setParameter("category", category)
                  .setParameter("targetId", targetId)
                  .setMaxResults(1)
                  .getResultStream()
                  .findFirst();
      }
  }
  ```

- [ ] **Step 2: Create DeviceStatusMetricDto class**
  Create `src/main/java/com/iviet/ivshs/dto/DeviceStatusMetricDto.java` with the following content:
  ```java
  package com.iviet.ivshs.dto;

  import com.fasterxml.jackson.databind.JsonNode;
  import lombok.Builder;
  import lombok.Value;
  import lombok.extern.jackson.Jacksonized;
  import java.time.Instant;

  @Value
  @Builder
  @Jacksonized
  public class DeviceStatusMetricDto {
      Instant timestamp;
      String targetCategory;
      Long targetId;
      JsonNode statusData;
  }
  ```

- [ ] **Step 3: Commit DAO and DTO classes**
  ```bash
  git add src/main/java/com/iviet/ivshs/dao/DeviceStatusMetricDao.java src/main/java/com/iviet/ivshs/dto/DeviceStatusMetricDto.java
  git commit -m "feat: add DeviceStatusMetricDao and DeviceStatusMetricDto"
  ```

---

### Task 5: Service Layer

**Files:**
* Create: `src/main/java/com/iviet/ivshs/service/DeviceStatusMetricService.java`
* Create: `src/main/java/com/iviet/ivshs/service/impl/DeviceStatusMetricServiceImpl.java`

**Interfaces:**
* Consumes: `LightDao`, `FanDao`, `AirConditionDao`, `PowerConsumptionDao`, `TemperatureDao`, `DeviceStatusMetricDao`
* Produces: `DeviceStatusMetricService` interface and its implementation. Registers `MetricDomain.STATUS` dynamically.

- [ ] **Step 1: Create DeviceStatusMetricService interface**
  Create `src/main/java/com/iviet/ivshs/service/DeviceStatusMetricService.java` with the following content:
  ```java
  package com.iviet.ivshs.service;

  import com.iviet.ivshs.service.strategy.MetricServiceStrategy;

  public interface DeviceStatusMetricService extends MetricServiceStrategy {
      void backupDeviceStatuses();
  }
  ```

- [ ] **Step 2: Create DeviceStatusMetricServiceImpl class**
  Create `src/main/java/com/iviet/ivshs/service/impl/DeviceStatusMetricServiceImpl.java` with the following content:
  ```java
  package com.iviet.ivshs.service.impl;

  import com.fasterxml.jackson.databind.ObjectMapper;
  import com.fasterxml.jackson.databind.node.ObjectNode;
  import com.iviet.ivshs.dao.*;
  import com.iviet.ivshs.dto.DeviceStatusMetricDto;
  import com.iviet.ivshs.entities.*;
  import com.iviet.ivshs.service.DeviceStatusMetricService;
  import com.iviet.ivshs.shared.enumeration.DeviceCategory;
  import com.iviet.ivshs.shared.enumeration.MetricDomain;
  import lombok.RequiredArgsConstructor;
  import lombok.extern.slf4j.Slf4j;
  import org.springframework.stereotype.Service;
  import org.springframework.transaction.annotation.Transactional;

  import java.time.Instant;
  import java.util.ArrayList;
  import java.util.List;

  @Slf4j
  @Service
  @RequiredArgsConstructor
  public class DeviceStatusMetricServiceImpl implements DeviceStatusMetricService {

      private final LightDao lightDao;
      private final FanDao fanDao;
      private final AirConditionDao airConditionDao;
      private final PowerConsumptionDao powerConsumptionDao;
      private final TemperatureDao temperatureDao;
      private final DeviceStatusMetricDao deviceStatusMetricDao;
      private final ObjectMapper objectMapper;

      @Override
      public MetricDomain getSupportedDomain() {
          return MetricDomain.STATUS;
      }

      @Override
      @Transactional(readOnly = true)
      public Object getLatest(String category, Long targetId) {
          return deviceStatusMetricDao.findLatest(category, targetId)
                  .map(dsm -> DeviceStatusMetricDto.builder()
                          .timestamp(dsm.getTimestamp())
                          .targetCategory(dsm.getTargetCategory())
                          .targetId(dsm.getTargetId())
                          .statusData(dsm.getStatusData())
                          .build())
                  .orElse(null);
      }

      @Override
      @Transactional(readOnly = true)
      public List<DeviceStatusMetricDto> getHistory(String category, Long targetId, Instant from, Instant to) {
          return deviceStatusMetricDao.findHistory(category, targetId, from, to).stream()
                  .map(dsm -> DeviceStatusMetricDto.builder()
                          .timestamp(dsm.getTimestamp())
                          .targetCategory(dsm.getTargetCategory())
                          .targetId(dsm.getTargetId())
                          .statusData(dsm.getStatusData())
                          .build())
                  .toList();
      }

      @Override
      @Transactional
      public void backupDeviceStatuses() {
          log.info("Starting local database device status backup");
          long start = System.currentTimeMillis();
          
          List<DeviceStatusMetric> metricsToSave = new ArrayList<>();
          Instant now = Instant.now();

          // 1. Process active Lights
          lightDao.findAllActive().forEach(light -> {
              ObjectNode data = objectMapper.createObjectNode();
              if (light.getPower() != null) data.put("power", light.getPower().name());
              if (light.getLevel() != null) data.put("level", light.getLevel());
              metricsToSave.add(createMetricEntity(DeviceCategory.LIGHT, light.getId(), now, data));
          });

          // 2. Process active Fans
          fanDao.findAllActive().forEach(fan -> {
              ObjectNode data = objectMapper.createObjectNode();
              if (fan.getPower() != null) data.put("power", fan.getPower().name());
              if (fan.getSpeed() != null) data.put("speed", fan.getSpeed());
              if (fan.getDuration() != null) data.put("duration", fan.getDuration());
              if (fan.getMode() != null) data.put("mode", fan.getMode().name());
              if (fan.getSwing() != null) data.put("swing", fan.getSwing().name());
              if (fan.getLight() != null) data.put("light", fan.getLight().name());
              metricsToSave.add(createMetricEntity(DeviceCategory.FAN, fan.getId(), now, data));
          });

          // 3. Process active ACs
          airConditionDao.findAllActive().forEach(ac -> {
              ObjectNode data = objectMapper.createObjectNode();
              if (ac.getPower() != null) data.put("power", ac.getPower().name());
              if (ac.getTemperature() != null) data.put("temperature", ac.getTemperature());
              if (ac.getMode() != null) data.put("mode", ac.getMode().name());
              if (ac.getFanSpeed() != null) data.put("fanSpeed", ac.getFanSpeed());
              if (ac.getSwing() != null) data.put("swing", ac.getSwing().name());
              if (ac.getDuration() != null) data.put("duration", ac.getDuration());
              metricsToSave.add(createMetricEntity(DeviceCategory.AIR_CONDITION, ac.getId(), now, data));
          });

          // 4. Process active Power Consumption sensors
          powerConsumptionDao.findAllActive().forEach(pc -> {
              ObjectNode data = objectMapper.createObjectNode();
              if (pc.getCurrentWatt() != null) data.put("currentWatt", pc.getCurrentWatt());
              metricsToSave.add(createMetricEntity(DeviceCategory.POWER_CONSUMPTION, pc.getId(), now, data));
          });

          // 5. Process active Temperature sensors
          temperatureDao.findAllActive().forEach(temp -> {
              ObjectNode data = objectMapper.createObjectNode();
              if (temp.getCurrentValue() != null) data.put("currentValue", temp.getCurrentValue());
              metricsToSave.add(createMetricEntity(DeviceCategory.TEMPERATURE, temp.getId(), now, data));
          });

          if (!metricsToSave.isEmpty()) {
              deviceStatusMetricDao.save(metricsToSave);
              log.info("Successfully backed up {} device statuses in {}ms", metricsToSave.size(), System.currentTimeMillis() - start);
          } else {
              log.warn("No active devices found to backup status");
          }
      }

      private DeviceStatusMetric createMetricEntity(DeviceCategory category, Long id, Instant timestamp, ObjectNode data) {
          DeviceStatusMetric metric = new DeviceStatusMetric();
          metric.setTargetCategory(category.name());
          metric.setTargetId(id);
          metric.setTimestamp(timestamp);
          metric.setStatusData(data);
          return metric;
      }
  }
  ```

- [ ] **Step 3: Commit service classes**
  ```bash
  git add src/main/java/com/iviet/ivshs/service/DeviceStatusMetricService.java src/main/java/com/iviet/ivshs/service/impl/DeviceStatusMetricServiceImpl.java
  git commit -m "feat: add DeviceStatusMetricService and implementation"
  ```

---

### Task 6: Scheduler Job Integration

**Files:**
* Modify: `src/main/java/com/iviet/ivshs/scheduler/system/metric/status/DeviceStatusMetricJob.java`

**Interfaces:**
* Consumes: `DeviceStatusMetricService`.

- [ ] **Step 1: Update DeviceStatusMetricJob execution logic**
  Replace the contents of `src/main/java/com/iviet/ivshs/scheduler/system/metric/status/DeviceStatusMetricJob.java` with the following implementation:
  ```java
  package com.iviet.ivshs.scheduler.system.metric.status;

  import lombok.extern.slf4j.Slf4j;
  import org.quartz.DisallowConcurrentExecution;
  import org.quartz.Job;
  import org.quartz.JobExecutionContext;
  import org.quartz.JobExecutionException;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.stereotype.Component;
  import com.iviet.ivshs.service.DeviceStatusMetricService;

  @Slf4j
  @Component
  @DisallowConcurrentExecution
  public class DeviceStatusMetricJob implements Job {

      public static final String JOB_NAME = "DEVICE_STATUS_METRIC_JOB";
      public static final String JOB_GROUP = "STATUS_METRIC_SYSTEM";

      @Autowired
      private DeviceStatusMetricService deviceStatusMetricService;

      @Override
      public void execute(JobExecutionContext context) throws JobExecutionException {
          try {
              deviceStatusMetricService.backupDeviceStatuses();
          } catch (Exception e) {
              log.error("Exec: Device status backup failed: {}", e.getMessage(), e);
          }
      }
  }
  ```

- [ ] **Step 2: Commit scheduler changes**
  ```bash
  git add src/main/java/com/iviet/ivshs/scheduler/system/metric/status/DeviceStatusMetricJob.java
  git commit -m "feat: integrate DeviceStatusMetricService in DeviceStatusMetricJob"
  ```
