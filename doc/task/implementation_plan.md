# Alert System Integration — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Tích hợp đầy đủ hệ thống cảnh báo (Alert System) vào Smart Room IoT Server — quản lý vòng đời cảnh báo, phân quyền RBAC (Admin/Maintenance/User), gửi thông báo đa kênh bằng Strategy Pattern, và tích hợp vào Quartz Rule Engine.

**Architecture:** `RuleProcessor` gọi trực tiếp `AlertService` ngay sau khi đánh giá điều kiện Rule. `AlertService` điều phối toàn bộ vòng đời (trigger → cooldown → save → dispatch → resolve). `NotificationService` là facade theo Strategy Pattern — mỗi kênh (FCM/Email/SMS) là một `@Component` riêng, không cần sửa `NotificationServiceImpl` khi thêm kênh mới (OCP).

**Tech Stack:** Java 21, Spring Boot 3, Spring Security (JWT + `CustomUserDetails`), JPA/Hibernate, Quartz Scheduler, Firebase Admin SDK (`FcmDispatchService` đã có sẵn), MySQL 8 (JSON column via `JsonNodeConverter`), Lombok.

---

## File Structure — Bản đồ tất cả file sẽ tạo/sửa

```
smartroom_server/src/main/java/com/iviet/ivshs/
│
├── shared/enumeration/
│   ├── [MODIFY] SysGroupEnum.java          — thêm G_MAINTENANCE
│   ├── [MODIFY] SysFunctionEnum.java       — thêm F_ACCESS_ALERT_*
│   ├── [NEW]    Severity.java
│   └── [NEW]    AlertStatus.java
│
├── entities/
│   ├── [NEW]    RuleActionAlert.java        — config alert 1:1 với Rule
│   ├── [NEW]    AlertInstance.java          — bản ghi alert được kích hoạt
│   └── [MODIFY] Rule.java                  — thêm alertConfig @OneToOne
│
├── dao/
│   ├── [NEW]    RuleActionAlertDao.java
│   └── [NEW]    AlertInstanceDao.java       — queries RBAC đầy đủ
│
├── dto/alert/
│   ├── [NEW]    AlertResponseDto.java       — response DTO (Java record)
│   └── [NEW]    AlertFilterDto.java         — filter params (Java record)
│
├── service/notification/
│   ├── [NEW]    NotificationService.java            — interface facade
│   ├── channel/
│   │   └── [NEW] NotificationChannel.java           — enum type-safe channel
│   ├── request/
│   │   └── [NEW] NotificationRequest.java           — Value Object (Builder Pattern)
│   ├── strategy/
│   │   ├── [NEW]    NotificationStrategy.java       — interface: getChannel() → enum
│   │   ├── [NEW]    NotificationStrategyRegistry.java — Registry Pattern: O(1) lookup, fail-fast
│   │   └── impl/
│   │       ├── [NEW] FcmNotificationStrategy.java
│   │       ├── [NEW] EmailNotificationStrategy.java
│   │       └── [NEW] SmsNotificationStrategy.java
│   └── impl/
│       └── [NEW] NotificationServiceImpl.java
│
├── service/alert/
│   ├── [NEW]    AlertService.java           — interface
│   └── impl/
│       └── [NEW] AlertServiceImpl.java      — toàn bộ business logic
│
├── controller/api/v1/
│   └── [NEW]    AlertController.java
│
└── scheduler/dynamic/rule/
    └── [MODIFY] RuleProcessor.java          — inject AlertService, gọi trigger/resolve

infra/database/migrations/ (hoặc resources/db/migration/)
└── [NEW]    002__Alert_System_Tables.sql
```

---

## Task 1: Database Migration

**Files:**
- Create: `src/main/resources/db/migration/002__Alert_System_Tables.sql`
  *(Kiểm tra đường dẫn migration thực tế của project — có thể là `infra/` hoặc `resources/`)*

- [ ] **Step 1: Tạo file SQL migration**

```sql
-- =========================================================
-- Migration 002: Alert System Tables
-- Adds 3 new tables: rule_action_alert, alert_instance, alert_recipient
-- =========================================================

-- Table: rule_action_alert
-- Alert configuration linked 1:1 to a Rule.
-- If this row exists for a rule_id, alert firing is enabled for that rule.
CREATE TABLE `rule_action_alert` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `created_at`       datetime(6)  DEFAULT NULL,
  `created_by`       varchar(256) DEFAULT NULL,
  `updated_at`       datetime(6)  DEFAULT NULL,
  `updated_by`       varchar(256) DEFAULT NULL,
  `v`                bigint       NOT NULL DEFAULT 0,
  `rule_id`          bigint       NOT NULL,
  `alert_name`       varchar(256) NOT NULL,
  `severity`         varchar(50)  NOT NULL    COMMENT 'Enum: INFO | WARNING | CRITICAL',
  `recipient_groups` json         DEFAULT NULL COMMENT 'e.g. ["G_ADMIN","G_MAINTENANCE"]',
  `channels`         json         DEFAULT NULL COMMENT 'e.g. ["PUSH","EMAIL","SMS"]',
  `message_template` text         NOT NULL,
  `cooldown_minutes` int          NOT NULL DEFAULT 0,
  `auto_resolve`     tinyint(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_rule_action_alert_rule_id` (`rule_id`),
  CONSTRAINT `fk_rule_action_alert_rule`
    FOREIGN KEY (`rule_id`) REFERENCES `rule` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Table: alert_instance
-- Each triggered alert event. Lifecycle: ACTIVE → ACKNOWLEDGED → RESOLVED.
CREATE TABLE `alert_instance` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `created_at`       datetime(6)  DEFAULT NULL,
  `created_by`       varchar(256) DEFAULT NULL,
  `updated_at`       datetime(6)  DEFAULT NULL,
  `updated_by`       varchar(256) DEFAULT NULL,
  `v`                bigint       NOT NULL DEFAULT 0,
  `rule_id`          bigint       NOT NULL,
  `title`            varchar(256) NOT NULL,
  `body`             text         NOT NULL,
  `severity`         varchar(50)  NOT NULL    COMMENT 'Enum: INFO | WARNING | CRITICAL',
  `status`           varchar(50)  NOT NULL    COMMENT 'Enum: ACTIVE | ACKNOWLEDGED | RESOLVED',
  `triggered_at`     datetime(6)  NOT NULL,
  `acknowledged_at`  datetime(6)  DEFAULT NULL,
  `acknowledged_by`  bigint       DEFAULT NULL COMMENT 'FK → client.id: user who acknowledged',
  `resolved_at`      datetime(6)  DEFAULT NULL,
  `resolved_by`      bigint       DEFAULT NULL COMMENT 'FK → client.id: user who resolved. NULL = auto-resolved by system',
  PRIMARY KEY (`id`),
  KEY `idx_alert_instance_rule_id`       (`rule_id`),
  KEY `idx_alert_instance_status`        (`status`),
  KEY `idx_alert_instance_status_time`   (`status`, `triggered_at`),
  CONSTRAINT `fk_alert_instance_rule`
    FOREIGN KEY (`rule_id`)        REFERENCES `rule`   (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_alert_instance_ack_by`
    FOREIGN KEY (`acknowledged_by`) REFERENCES `client` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_alert_instance_res_by`
    FOREIGN KEY (`resolved_by`)    REFERENCES `client` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Table: alert_recipient
-- Mapping which clients received notification for each alert.
-- Used by RBAC: G_USER queries join this table to return "My Alerts".
CREATE TABLE `alert_recipient` (
  `alert_id`  bigint NOT NULL,
  `client_id` bigint NOT NULL,
  PRIMARY KEY (`alert_id`, `client_id`),
  KEY `idx_alert_recipient_client_id` (`client_id`),
  CONSTRAINT `fk_alert_recipient_alert`
    FOREIGN KEY (`alert_id`)  REFERENCES `alert_instance` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_alert_recipient_client`
    FOREIGN KEY (`client_id`) REFERENCES `client`         (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 2: Chạy migration và verify**
```bash
# Chạy file SQL (thay thế user/db-name tương ứng)
mysql -u <user> -p smart_room_iot < src/main/resources/db/migration/002__Alert_System_Tables.sql
```
Expected: Lệnh thành công, không có ERROR. Verify:
```bash
mysql -u <user> -p smart_room_iot -e "SHOW TABLES LIKE 'alert%';"
# Expected output: alert_instance, alert_recipient, rule_action_alert
```

---

## Task 2: Enums & Constants

**Files:**
- Create: `src/main/java/com/iviet/ivshs/shared/enumeration/Severity.java`
- Create: `src/main/java/com/iviet/ivshs/shared/enumeration/AlertStatus.java`
- Modify: `src/main/java/com/iviet/ivshs/shared/enumeration/SysGroupEnum.java` — thêm `G_MAINTENANCE`
- Modify: `src/main/java/com/iviet/ivshs/shared/enumeration/SysFunctionEnum.java` — thêm `F_ACCESS_ALERT_*`

- [ ] **Step 1: Tạo `Severity.java`**

```java
package com.iviet.ivshs.shared.enumeration;

/**
 * Mức độ nghiêm trọng của cảnh báo.
 * Được lưu dạng STRING trong cột VARCHAR(50) của DB.
 */
public enum Severity {
    INFO,
    WARNING,
    CRITICAL
}
```

- [ ] **Step 2: Tạo `AlertStatus.java`**

```java
package com.iviet.ivshs.shared.enumeration;

/**
 * Trạng thái vòng đời của một AlertInstance.
 * ACTIVE      → Cảnh báo vừa được kích hoạt, chưa được xử lý.
 * ACKNOWLEDGED→ Đã có người xác nhận đã biết về cảnh báo này.
 * RESOLVED    → Đã được giải quyết (thủ công hoặc tự động).
 * Được lưu dạng STRING trong cột VARCHAR(50) của DB.
 */
public enum AlertStatus {
    ACTIVE,
    ACKNOWLEDGED,
    RESOLVED
}
```

- [ ] **Step 3: Cập nhật `SysGroupEnum.java` — thêm `G_MAINTENANCE`**

File hiện tại ở `src/main/java/com/iviet/ivshs/shared/enumeration/SysGroupEnum.java`.
Thêm entry `G_MAINTENANCE` vào giữa `G_MANAGER` và `G_HARDWARE_GATEWAY`:

```java
package com.iviet.ivshs.shared.enumeration;

public enum SysGroupEnum {
    G_ADMIN("G_ADMIN"),
    G_USER("G_USER"),
    G_MANAGER("G_MANAGER"),
    G_MAINTENANCE("G_MAINTENANCE"),       // NEW: nhóm kỹ thuật viên bảo trì
    G_HARDWARE_GATEWAY("G_HARDWARE_GATEWAY");

    private String code;

    SysGroupEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return this.getCode();
    }
}
```

- [ ] **Step 4: Cập nhật `SysFunctionEnum.java` — thêm 3 function code Alert**

File hiện tại ở `src/main/java/com/iviet/ivshs/shared/enumeration/SysFunctionEnum.java`.
Thêm section mới sau `F_ACCESS_ROOM_ALL`:

```java
package com.iviet.ivshs.shared.enumeration;

public enum SysFunctionEnum {
    // ========== MANAGEMENT FUNCTIONS ==========
    F_MANAGE_CLIENT("F_MANAGE_CLIENT"),
    F_MANAGE_FLOOR("F_MANAGE_FLOOR"),
    F_MANAGE_ROOM("F_MANAGE_ROOM"),
    F_MANAGE_DEVICE("F_MANAGE_DEVICE"),
    F_MANAGE_ALL("F_MANAGE_ALL"),
    F_MANAGE_SOME("F_MANAGE_SOME"),
    F_MANAGE_FUNCTION("F_MANAGE_FUNCTION"),
    F_MANAGE_GROUP("F_MANAGE_GROUP"),
    F_MANAGE_ROLE("F_MANAGE_ROLE"),
    F_MANAGE_AUTOMATION("F_MANAGE_AUTOMATION"),
    F_MANAGE_RULE("F_MANAGE_RULE"),

    // ========== ACCESS FUNCTIONS ==========
    F_ACCESS_FLOOR_ALL("F_ACCESS_FLOOR_ALL"),
    F_ACCESS_ROOM_ALL("F_ACCESS_ROOM_ALL"),

    // ========== ALERT ACCESS FUNCTIONS ==========
    F_ACCESS_ALERT_ALL("F_ACCESS_ALERT_ALL"),       // G_ADMIN: xem toàn bộ alerts
    F_ACCESS_ALERT_GROUP("F_ACCESS_ALERT_GROUP"),   // G_MAINTENANCE: xem alerts của group
    F_ACCESS_ALERT_OWN("F_ACCESS_ALERT_OWN");       // G_USER: chỉ xem "My Alerts"

    private String code;

    SysFunctionEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return this.getCode();
    }
}
```

---

## Task 3: JPA Entities

**Files:**
- Create: `src/main/java/com/iviet/ivshs/entities/RuleActionAlert.java`
- Create: `src/main/java/com/iviet/ivshs/entities/AlertInstance.java`
- Modify: `src/main/java/com/iviet/ivshs/entities/Rule.java`

- [ ] **Step 1: Tạo entity `RuleActionAlert.java`**

> **Lưu ý:** `JsonNodeConverter` trong project đã được đánh dấu `@Converter(autoApply = true)` — các field `JsonNode` được convert tự động, KHÔNG cần thêm `@Convert`.

```java
package com.iviet.ivshs.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.base.BaseAuditEntity;
import com.iviet.ivshs.shared.enumeration.Severity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Cấu hình alert cho một Rule cụ thể (quan hệ 1:1 với Rule).
 * Nếu bản ghi này tồn tại cho một rule_id, hệ thống sẽ bật cơ chế alert khi Rule khớp.
 * Khi Rule bị xóa → bản ghi này bị xóa theo (ON DELETE CASCADE từ DB + CascadeType.ALL từ Rule.java).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rule_action_alert", indexes = {
    @Index(name = "idx_rule_action_alert_rule_id", columnList = "rule_id", unique = true)
})
public class RuleActionAlert extends BaseAuditEntity {

    /**
     * Rule mà config này thuộc về.
     * Unique = true đảm bảo 1 Rule chỉ có đúng 1 alert config.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false, unique = true)
    private Rule rule;

    /** Tên hiển thị của cảnh báo — dùng làm title trong push notification. */
    @Column(name = "alert_name", nullable = false, length = 256)
    private String alertName;

    /** Mức độ nghiêm trọng. Lưu dạng STRING ("INFO", "WARNING", "CRITICAL"). */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 50)
    private Severity severity;

    /**
     * Mảng JSON các group code nhận alert.
     * Ví dụ: ["G_ADMIN", "G_MAINTENANCE"]
     * JsonNodeConverter @autoApply = true — không cần @Convert thủ công.
     */
    @Column(name = "recipient_groups", columnDefinition = "json")
    private JsonNode recipientGroups;

    /**
     * Mảng JSON các kênh gửi thông báo.
     * Ví dụ: ["PUSH", "EMAIL"]
     * JsonNodeConverter @autoApply = true — không cần @Convert thủ công.
     */
    @Column(name = "channels", columnDefinition = "json")
    private JsonNode channels;

    /**
     * Template nội dung thông báo.
     * Ví dụ: "Nhiệt độ phòng 101 đạt 42°C, vượt ngưỡng cho phép 35°C."
     */
    @Column(name = "message_template", nullable = false, columnDefinition = "TEXT")
    private String messageTemplate;

    /**
     * Thời gian tối thiểu (phút) giữa 2 lần kích hoạt alert liên tiếp cùng Rule.
     * 0 = không có cooldown (kích hoạt mỗi lần Rule match).
     */
    @Column(name = "cooldown_minutes", nullable = false)
    private Integer cooldownMinutes = 0;

    /**
     * Nếu true: khi điều kiện Rule không còn thỏa mãn, hệ thống tự động
     * chuyển alert đang ACTIVE/ACKNOWLEDGED sang trạng thái RESOLVED.
     */
    @Column(name = "auto_resolve", nullable = false)
    private Boolean autoResolve = false;
}
```

- [ ] **Step 2: Tạo entity `AlertInstance.java`**

```java
package com.iviet.ivshs.entities;

import com.iviet.ivshs.entities.base.BaseAuditEntity;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Đại diện cho một sự kiện alert được kích hoạt.
 * Mỗi lần Rule khớp điều kiện (và không trong cooldown), một AlertInstance được tạo mới.
 * Vòng đời: ACTIVE → ACKNOWLEDGED → RESOLVED
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alert_instance", indexes = {
    @Index(name = "idx_alert_instance_rule_id",     columnList = "rule_id"),
    @Index(name = "idx_alert_instance_status",      columnList = "status"),
    @Index(name = "idx_alert_instance_status_time", columnList = "status, triggered_at")
})
public class AlertInstance extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

    /** Title push notification. */
    @Column(name = "title", nullable = false, length = 256)
    private String title;

    /** Body push notification / nội dung mô tả cảnh báo. */
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 50)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private AlertStatus status;

    /** Thời điểm UTC khi alert được kích hoạt lần đầu. */
    @Column(name = "triggered_at", nullable = false)
    private Instant triggeredAt;

    /** Thời điểm user xác nhận. Null nếu chưa được xác nhận. */
    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    /** Client đã xác nhận alert. Null nếu chưa xác nhận. FK → client.id */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acknowledged_by")
    private Client acknowledgedBy;

    /** Thời điểm alert được giải quyết. Null nếu chưa resolved. */
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    /**
     * Client đã resolve. Null = hệ thống tự động resolve (auto_resolve = true).
     * Nếu có giá trị = user thủ công resolve qua API.
     * FK → client.id (ON DELETE SET NULL)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private Client resolvedBy;

    /**
     * Tập hợp tất cả Client nhận được notification của alert này.
     * Được dùng bởi RBAC: G_USER query join bảng alert_recipient để lấy "My Alerts".
     * Được populate lúc tạo alert từ RuleActionAlert.recipientGroups.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "alert_recipient",
        joinColumns = @JoinColumn(name = "alert_id"),
        inverseJoinColumns = @JoinColumn(name = "client_id")
    )
    @Builder.Default
    private Set<Client> recipients = new HashSet<>();
}
```

- [ ] **Step 3: Cập nhật `Rule.java` — thêm relation `alertConfig`**

File tại `src/main/java/com/iviet/ivshs/entities/Rule.java`.
Thêm vào phần import và thêm field + method vào cuối class (KHÔNG sửa các field hiện có):

```java
// Thêm vào phần import (nếu chưa có):
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToOne;

// Thêm field vào class Rule (đặt sau field `actions` hoặc cuối class trước dấu `}`):

    /**
     * Cấu hình alert tùy chọn cho Rule này.
     * Nếu tồn tại: hệ thống tạo AlertInstance khi tất cả conditions được thỏa mãn.
     * CascadeType.ALL + orphanRemoval = true: xóa Rule → config này tự động bị xóa.
     */
    @OneToOne(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RuleActionAlert alertConfig;

    /** Setter tùy chỉnh đảm bảo tính nhất quán hai chiều của quan hệ. */
    public void setAlertConfig(RuleActionAlert alertConfig) {
        this.alertConfig = alertConfig;
        if (alertConfig != null) {
            alertConfig.setRule(this);
        }
    }

    public boolean hasAlertConfig() {
        return this.alertConfig != null;
    }
```

---

## Task 4: DAO Layer

**Files:**
- Create: `src/main/java/com/iviet/ivshs/dao/RuleActionAlertDao.java`
- Create: `src/main/java/com/iviet/ivshs/dao/AlertInstanceDao.java`

> **Pattern:** Kế thừa `BaseAuditEntityDao<T>` giống `ClientDao`, `RuleDao`, v.v. Dùng JPQL thuần, không dùng Spring Data JPA Repository.

- [ ] **Step 1: Tạo `RuleActionAlertDao.java`**

```java
package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
import com.iviet.ivshs.entities.RuleActionAlert;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class RuleActionAlertDao extends BaseAuditEntityDao<RuleActionAlert> {

    public RuleActionAlertDao() {
        super(RuleActionAlert.class);
    }

    /**
     * Tìm cấu hình alert cho một Rule ID cụ thể.
     * Trả về Optional.empty() nếu Rule đó chưa có alert config.
     */
    public Optional<RuleActionAlert> findByRuleId(Long ruleId) {
        String jpql = "SELECT raa FROM RuleActionAlert raa WHERE raa.rule.id = :ruleId";
        return entityManager.createQuery(jpql, RuleActionAlert.class)
                .setParameter("ruleId", ruleId)
                .getResultStream()
                .findFirst();
    }

    public boolean existsByRuleId(Long ruleId) {
        String jpql = "SELECT COUNT(raa) FROM RuleActionAlert raa WHERE raa.rule.id = :ruleId";
        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("ruleId", ruleId)
                .getSingleResult();
        return count > 0;
    }
}
```

- [ ] **Step 2: Tạo `AlertInstanceDao.java`**

```java
package com.iviet.ivshs.dao;

import com.iviet.ivshs.dao.base.BaseAuditEntityDao;
import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AlertInstanceDao extends BaseAuditEntityDao<AlertInstance> {

    public AlertInstanceDao() {
        super(AlertInstance.class);
    }

    /**
     * Tìm alert đang mở (ACTIVE hoặc ACKNOWLEDGED) gần nhất của một Rule.
     * Dùng để kiểm tra logic cooldown trước khi tạo alert mới.
     */
    public Optional<AlertInstance> findLatestOpenByRuleId(Long ruleId) {
        String jpql = """
            SELECT ai FROM AlertInstance ai
            WHERE ai.rule.id = :ruleId
              AND ai.status IN (:statuses)
            ORDER BY ai.triggeredAt DESC
            """;
        return entityManager.createQuery(jpql, AlertInstance.class)
                .setParameter("ruleId", ruleId)
                .setParameter("statuses", List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED))
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    /**
     * Tìm tất cả alert đang mở của một Rule (kèm recipients đã FETCH).
     * Dùng cho logic auto-resolve khi điều kiện không còn thỏa mãn.
     */
    public List<AlertInstance> findAllOpenByRuleId(Long ruleId) {
        String jpql = """
            SELECT DISTINCT ai FROM AlertInstance ai
            LEFT JOIN FETCH ai.recipients
            WHERE ai.rule.id = :ruleId
              AND ai.status IN (:statuses)
            """;
        return entityManager.createQuery(jpql, AlertInstance.class)
                .setParameter("ruleId", ruleId)
                .setParameter("statuses", List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED))
                .getResultList();
    }

    /**
     * Tìm một AlertInstance kèm recipients được FETCH sẵn (tránh N+1).
     * Dùng trước khi gọi NotificationService hoặc kiểm tra quyền G_USER.
     */
    public Optional<AlertInstance> findByIdWithRecipients(Long id) {
        String jpql = """
            SELECT ai FROM AlertInstance ai
            LEFT JOIN FETCH ai.recipients
            WHERE ai.id = :id
            """;
        return entityManager.createQuery(jpql, AlertInstance.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    // ========== G_ADMIN: Xem toàn bộ alerts ==========

    public List<AlertInstance> findAll(AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder(
            "SELECT ai FROM AlertInstance ai WHERE 1=1"
        );
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");
        jpql.append(" ORDER BY ai.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstance.class);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countAll(AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(ai) FROM AlertInstance ai WHERE 1=1");
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }

    // ========== G_MAINTENANCE: Xem alerts của group ==========
    // Dùng JSON_CONTAINS của MySQL 8 qua JPQL để kiểm tra groupCode trong recipient_groups

    public List<AlertInstance> findAllByGroupCode(
            String groupCode, AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder("""
            SELECT ai FROM AlertInstance ai
            JOIN ai.rule r
            JOIN RuleActionAlert raa ON raa.rule.id = r.id
            WHERE JSON_CONTAINS(raa.recipientGroups, :groupCodeJson) = 1
            """);
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");
        jpql.append(" ORDER BY ai.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstance.class)
                .setParameter("groupCodeJson", "\"" + groupCode + "\"");
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countByGroupCode(String groupCode, AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("""
            SELECT COUNT(ai) FROM AlertInstance ai
            JOIN ai.rule r
            JOIN RuleActionAlert raa ON raa.rule.id = r.id
            WHERE JSON_CONTAINS(raa.recipientGroups, :groupCodeJson) = 1
            """);
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class)
                .setParameter("groupCodeJson", "\"" + groupCode + "\"");
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }

    // ========== G_USER: "My Alerts" — chỉ xem alert mà mình là recipient ==========

    public List<AlertInstance> findAllByRecipientClientId(
            Long clientId, AlertStatus status, Severity severity, int page, int size) {
        StringBuilder jpql = new StringBuilder("""
            SELECT ai FROM AlertInstance ai
            JOIN ai.recipients r
            WHERE r.id = :clientId
            """);
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");
        jpql.append(" ORDER BY ai.triggeredAt DESC");

        var q = entityManager.createQuery(jpql.toString(), AlertInstance.class)
                .setParameter("clientId", clientId);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.setFirstResult(page * size).setMaxResults(size).getResultList();
    }

    public long countByRecipientClientId(Long clientId, AlertStatus status, Severity severity) {
        StringBuilder jpql = new StringBuilder("""
            SELECT COUNT(ai) FROM AlertInstance ai
            JOIN ai.recipients r
            WHERE r.id = :clientId
            """);
        if (status   != null) jpql.append(" AND ai.status = :status");
        if (severity != null) jpql.append(" AND ai.severity = :severity");

        var q = entityManager.createQuery(jpql.toString(), Long.class)
                .setParameter("clientId", clientId);
        if (status   != null) q.setParameter("status",   status);
        if (severity != null) q.setParameter("severity", severity);
        return q.getSingleResult();
    }
}
```

---

## Task 5: DTO Layer

**Files:**
- Create: `src/main/java/com/iviet/ivshs/dto/alert/AlertResponseDto.java`
- Create: `src/main/java/com/iviet/ivshs/dto/alert/AlertFilterDto.java`

> **Pattern:** Dùng Java `record` (như `PaginatedResponse`) cho DTOs. Response DTO chứa static factory `from()`.

- [ ] **Step 1: Tạo `AlertResponseDto.java`**

```java
package com.iviet.ivshs.dto.alert;

import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;

import java.time.Instant;

/**
 * Response DTO cho một AlertInstance.
 * Được trả về bởi GET /api/v1/alerts và GET /api/v1/alerts/{id}.
 * Dùng Java record (immutable, tự có constructor, equals, hashCode, toString).
 */
public record AlertResponseDto(
        Long id,
        Long ruleId,
        String ruleName,
        String title,
        String body,
        Severity severity,
        AlertStatus status,
        Instant triggeredAt,
        Instant acknowledgedAt,
        Long acknowledgedById,
        String acknowledgedByUsername,
        Instant resolvedAt,
        Long resolvedById,
        String resolvedByUsername
) {
    /**
     * Static factory từ entity AlertInstance.
     * QUAN TRỌNG: Các relation rule, acknowledgedBy, resolvedBy phải được
     * load trong transaction trước khi gọi hàm này để tránh LazyInitializationException.
     */
    public static AlertResponseDto from(AlertInstance alert) {
        return new AlertResponseDto(
                alert.getId(),
                alert.getRule().getId(),
                alert.getRule().getName(),
                alert.getTitle(),
                alert.getBody(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getTriggeredAt(),
                alert.getAcknowledgedAt(),
                alert.getAcknowledgedBy() != null ? alert.getAcknowledgedBy().getId()       : null,
                alert.getAcknowledgedBy() != null ? alert.getAcknowledgedBy().getUsername() : null,
                alert.getResolvedAt(),
                alert.getResolvedBy() != null ? alert.getResolvedBy().getId()       : null,
                alert.getResolvedBy() != null ? alert.getResolvedBy().getUsername() : null
        );
    }
}
```

- [ ] **Step 2: Tạo `AlertFilterDto.java`**

```java
package com.iviet.ivshs.dto.alert;

import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;

/**
 * Filter params cho GET /api/v1/alerts.
 * Tất cả fields đều optional.
 * Compact constructor tự validate và sanitize giá trị page/size.
 */
public record AlertFilterDto(
        AlertStatus status,
        Severity severity,
        int page,
        int size
) {
    public AlertFilterDto {
        if (page < 0)   page = 0;
        if (size <= 0)  size = 10;
        if (size > 100) size = 100;
    }
}
```

---

## Task 6: Notification Strategy Pattern

**Files:**
- Create: `src/main/java/com/iviet/ivshs/service/notification/NotificationService.java`
- Create: `src/main/java/com/iviet/ivshs/service/notification/strategy/NotificationStrategy.java`
- Create: `src/main/java/com/iviet/ivshs/service/notification/strategy/impl/FcmNotificationStrategy.java`
- Create: `src/main/java/com/iviet/ivshs/service/notification/strategy/impl/EmailNotificationStrategy.java`
- Create: `src/main/java/com/iviet/ivshs/service/notification/strategy/impl/SmsNotificationStrategy.java`
- Create: `src/main/java/com/iviet/ivshs/service/notification/impl/NotificationServiceImpl.java`

- [ ] **Step 1: Tạo interface `NotificationService.java`**

```java
package com.iviet.ivshs.service.notification;

import com.iviet.ivshs.entities.Client;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Facade interface cho việc gửi thông báo qua nhiều kênh.
 * AlertService (và các service khác) CHỈ phụ thuộc vào interface này, không biết cụ thể FCM/Email/SMS.
 * Implementation cụ thể là NotificationServiceImpl — tự động tìm các NotificationStrategy bean phù hợp.
 */
public interface NotificationService {

    /**
     * Gửi thông báo tới tập hợp recipients qua tất cả các channels được chỉ định.
     *
     * @param recipients Tập Client cần nhận thông báo. ClientDevices phải được load trong transaction.
     * @param channels   Danh sách kênh: "PUSH", "EMAIL", "SMS" (không phân biệt hoa thường).
     * @param title      Tiêu đề hiển thị trên thiết bị.
     * @param body       Nội dung hiển thị trên thiết bị.
     * @param data       Map key-value payload cho logic mobile app (tất cả values phải là String per FCM v1 spec).
     */
    void sendNotification(
            Set<Client> recipients,
            List<String> channels,
            String title,
            String body,
            Map<String, String> data
    );
}
```

- [ ] **Step 2: Tạo interface `NotificationStrategy.java`**

```java
package com.iviet.ivshs.service.notification.strategy;

import com.iviet.ivshs.entities.Client;

import java.util.Map;
import java.util.Set;

/**
 * Strategy interface cho một kênh gửi thông báo cụ thể.
 *
 * Để thêm kênh mới (vd: Zalo OA): tạo @Component mới implement interface này.
 * NotificationServiceImpl KHÔNG cần sửa đổi (Open/Closed Principle).
 *
 * Spring tự động collect tất cả @Component của interface này vào List<NotificationStrategy>
 * trong NotificationServiceImpl qua Dependency Injection.
 */
public interface NotificationStrategy {

    /**
     * @param channel Tên kênh (vd: "PUSH", "FCM", "EMAIL", "SMS"). Case-insensitive.
     * @return true nếu strategy này xử lý kênh đã cho.
     */
    boolean supports(String channel);

    /**
     * Gửi thông báo qua kênh cụ thể của strategy này.
     *
     * @param recipients Tập Client nhận thông báo.
     *                   - FcmStrategy: đọc client.clientDevices để lấy FCM tokens.
     *                   - EmailStrategy: đọc client.username (email).
     *                   - SmsStrategy: đọc thông tin điện thoại (nếu có).
     * @param title      Tiêu đề thông báo.
     * @param body       Nội dung thông báo.
     * @param data       Extra data payload (FCM/PUSH dùng, Email/SMS thường bỏ qua).
     */
    void send(Set<Client> recipients, String title, String body, Map<String, String> data);
}
```

- [ ] **Step 3: Tạo `FcmNotificationStrategy.java`**

> **Lưu ý quan trọng:** `Client.clientDevices` là `FetchType.LAZY`. `AlertServiceImpl` phải **load lại** recipients trong transaction hiện tại (bằng `ClientDao.findById`) trước khi truyền vào đây, tránh `LazyInitializationException`. Chi tiết xem Task 7 Step 2.

```java
package com.iviet.ivshs.service.notification.strategy.impl;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.ClientDevice;
import com.iviet.ivshs.service.notification.FcmDispatchService;
import com.iviet.ivshs.service.notification.strategy.NotificationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FCM Push Notification Strategy.
 * Gom tất cả FCM tokens từ clientDevices của recipients rồi gửi multicast qua FcmDispatchService.
 * FcmDispatchService.sendToMultipleDevices() đã là @Async — không block thread hiện tại.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FcmNotificationStrategy implements NotificationStrategy {

    private final FcmDispatchService fcmDispatchService;

    @Override
    public boolean supports(String channel) {
        return "PUSH".equalsIgnoreCase(channel) || "FCM".equalsIgnoreCase(channel);
    }

    @Override
    public void send(Set<Client> recipients, String title, String body, Map<String, String> data) {
        List<String> fcmTokens = recipients.stream()
                .flatMap(client -> client.getClientDevices().stream())
                .map(ClientDevice::getFcmToken)
                .filter(token -> token != null && !token.isBlank())
                .distinct()
                .collect(Collectors.toList());

        if (fcmTokens.isEmpty()) {
            log.debug("[FCM] No FCM tokens found for {} recipients — skipping", recipients.size());
            return;
        }

        log.info("[FCM] Dispatching push notification to {} tokens from {} recipients",
                fcmTokens.size(), recipients.size());
        // FcmDispatchService là @Async — không block thread này
        fcmDispatchService.sendToMultipleDevices(fcmTokens, title, body, data);
    }
}
```

- [ ] **Step 4: Tạo `EmailNotificationStrategy.java`** (placeholder)

```java
package com.iviet.ivshs.service.notification.strategy.impl;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.service.notification.strategy.NotificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Email Notification Strategy — Placeholder.
 * Tích hợp JavaMailSender sẽ được hoàn thiện trong sprint tiếp theo.
 * Log để xác nhận luồng chạy đúng mà không throw exception.
 * Kênh PUSH vẫn hoạt động bình thường dù EMAIL chưa implement.
 */
@Slf4j
@Component
public class EmailNotificationStrategy implements NotificationStrategy {

    @Override
    public boolean supports(String channel) {
        return "EMAIL".equalsIgnoreCase(channel);
    }

    @Override
    public void send(Set<Client> recipients, String title, String body, Map<String, String> data) {
        log.info("[EMAIL][PLACEHOLDER] Would send email '{}' to {} recipients. JavaMailSender not yet integrated.",
                title, recipients.size());
        // TODO Sprint N+1: inject JavaMailSender, build MimeMessage, send per recipient email.
    }
}
```

- [ ] **Step 5: Tạo `SmsNotificationStrategy.java`** (placeholder)

```java
package com.iviet.ivshs.service.notification.strategy.impl;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.service.notification.strategy.NotificationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * SMS Notification Strategy — Placeholder.
 * Tích hợp SMS Gateway (Twilio, VietGuys...) trong sprint tiếp theo.
 */
@Slf4j
@Component
public class SmsNotificationStrategy implements NotificationStrategy {

    @Override
    public boolean supports(String channel) {
        return "SMS".equalsIgnoreCase(channel);
    }

    @Override
    public void send(Set<Client> recipients, String title, String body, Map<String, String> data) {
        log.info("[SMS][PLACEHOLDER] Would send SMS '{}' to {} recipients. SMS Gateway not yet integrated.",
                body, recipients.size());
        // TODO Sprint N+1: inject SmsGatewayClient, send body per recipient phone number.
    }
}
```

- [ ] **Step 6: Tạo `NotificationServiceImpl.java`**

```java
package com.iviet.ivshs.service.notification.impl;

import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.service.notification.NotificationService;
import com.iviet.ivshs.service.notification.strategy.NotificationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central dispatcher cho tất cả kênh thông báo.
 * Spring auto-inject TẤT CẢ @Component implements NotificationStrategy vào list strategies.
 *
 * Thêm kênh mới = chỉ cần tạo @Component NotificationStrategy mới.
 * Class này KHÔNG BAO GIỜ cần sửa (Open/Closed Principle).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    /** Auto-injected bởi Spring: tất cả bean implements NotificationStrategy trong context. */
    private final List<NotificationStrategy> strategies;

    @Override
    public void sendNotification(
            Set<Client> recipients,
            List<String> channels,
            String title,
            String body,
            Map<String, String> data
    ) {
        if (recipients == null || recipients.isEmpty()) {
            log.debug("[Notification] No recipients — skipping all channels");
            return;
        }
        if (channels == null || channels.isEmpty()) {
            log.debug("[Notification] No channels configured — skipping");
            return;
        }

        for (String channel : channels) {
            strategies.stream()
                    .filter(s -> s.supports(channel))
                    .findFirst()
                    .ifPresentOrElse(
                        strategy -> {
                            log.debug("[Notification] Dispatching via '{}' to {} recipients",
                                    channel, recipients.size());
                            strategy.send(recipients, title, body, data);
                        },
                        () -> log.warn("[Notification] No strategy for channel '{}'. Skipping.", channel)
                    );
        }
    }
}
```

---

## Task 7: AlertService

**Files:**
- Create: `src/main/java/com/iviet/ivshs/service/alert/AlertService.java`
- Create: `src/main/java/com/iviet/ivshs/service/alert/impl/AlertServiceImpl.java`

- [ ] **Step 1: Tạo interface `AlertService.java`**

```java
package com.iviet.ivshs.service.alert;

import com.iviet.ivshs.dto.alert.AlertFilterDto;
import com.iviet.ivshs.dto.alert.AlertResponseDto;
import com.iviet.ivshs.dto.common.PaginatedResponse;

/**
 * Service quản lý toàn bộ vòng đời Alert:
 * - Kích hoạt alert khi Rule conditions được thỏa mãn (gọi từ RuleProcessor)
 * - Tự động resolve alert khi conditions không còn thỏa mãn
 * - RBAC-aware query cho REST API
 * - Thủ công acknowledge/resolve qua REST API
 */
public interface AlertService {

    /**
     * Gọi bởi RuleProcessor khi TẤT CẢ conditions của Rule được thỏa mãn.
     * Tạo AlertInstance mới nếu không trong cooldown, resolve recipients, dispatch notification.
     * @param ruleId ID của Rule vừa match.
     */
    void triggerAlert(Long ruleId);

    /**
     * Gọi bởi RuleProcessor khi conditions KHÔNG CÒN thỏa mãn.
     * Nếu auto_resolve = true: cập nhật tất cả ACTIVE/ACKNOWLEDGED alerts của Rule này sang RESOLVED.
     * @param ruleId ID của Rule không match.
     */
    void resolveAlertIfNeeded(Long ruleId);

    /**
     * Lấy danh sách alerts phân trang, filter theo RBAC của user hiện tại.
     * G_ADMIN → xem tất cả | G_MAINTENANCE → xem group | G_USER → "My Alerts"
     */
    PaginatedResponse<AlertResponseDto> getAlerts(AlertFilterDto filter);

    /**
     * Lấy chi tiết 1 alert theo ID. Throw ForbiddenException nếu không có quyền.
     */
    AlertResponseDto getAlertById(Long alertId);

    /**
     * Đánh dấu ACKNOWLEDGED bởi user hiện tại. No-op nếu đã ACKNOWLEDGED/RESOLVED.
     */
    AlertResponseDto acknowledge(Long alertId);

    /**
     * Đánh dấu RESOLVED bởi user hiện tại. Dispatch ALERT_RESOLVED notification.
     * No-op nếu đã RESOLVED.
     */
    AlertResponseDto resolve(Long alertId);
}
```

- [ ] **Step 2: Tạo `AlertServiceImpl.java`**

> **Lưu ý thiết kế:**
> - `triggerAlert` và `resolveAlertIfNeeded` chạy trong Quartz thread. `RuleProcessor.process()` dùng `Propagation.NOT_SUPPORTED` → KHÔNG có transaction. Hai method này cần `@Transactional` riêng.
> - `getAlerts`, `getAlertById`, `acknowledge`, `resolve` chạy trong HTTP request thread bình thường với `@Transactional`.
> - `loadRecipientsWithDevices()`: load lại Client entities trong transaction hiện tại để `clientDevices` (LAZY) không gây `LazyInitializationException` trong `FcmNotificationStrategy`.
> - `SecurityContextUtil.getCustomUserDetails()` lấy `CustomUserDetails` đã được inject bởi JWT filter — chứa `List<String> groups` là danh sách groupCode.

```java
package com.iviet.ivshs.service.alert.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.dao.AlertInstanceDao;
import com.iviet.ivshs.dao.ClientDao;
import com.iviet.ivshs.dao.RuleActionAlertDao;
import com.iviet.ivshs.dao.SysGroupDao;
import com.iviet.ivshs.dto.alert.AlertFilterDto;
import com.iviet.ivshs.dto.alert.AlertResponseDto;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.entities.AlertInstance;
import com.iviet.ivshs.entities.Client;
import com.iviet.ivshs.entities.RuleActionAlert;
import com.iviet.ivshs.service.alert.AlertService;
import com.iviet.ivshs.service.notification.NotificationService;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.SysGroupEnum;
import com.iviet.ivshs.shared.exception.ForbiddenException;
import com.iviet.ivshs.shared.exception.NotFoundException;
import com.iviet.ivshs.shared.util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private final AlertInstanceDao    alertInstanceDao;
    private final RuleActionAlertDao  ruleActionAlertDao;
    private final ClientDao           clientDao;
    private final SysGroupDao         sysGroupDao;
    private final NotificationService notificationService;

    // =====================================================================
    // Rule Engine Integration
    // =====================================================================

    @Override
    @Transactional
    public void triggerAlert(Long ruleId) {
        // 1. Tìm config alert. Nếu Rule không có config → bỏ qua.
        Optional<RuleActionAlert> configOpt = ruleActionAlertDao.findByRuleId(ruleId);
        if (configOpt.isEmpty()) {
            log.debug("[Alert] Rule {} has no alert config — skipping trigger", ruleId);
            return;
        }
        RuleActionAlert config = configOpt.get();

        // 2. Kiểm tra cooldown: nếu còn alert đang mở trong cửa sổ cooldown → bỏ qua.
        if (config.getCooldownMinutes() > 0) {
            Optional<AlertInstance> latestOpen = alertInstanceDao.findLatestOpenByRuleId(ruleId);
            if (latestOpen.isPresent()) {
                Instant cooldownEnd = latestOpen.get().getTriggeredAt()
                        .plusSeconds(config.getCooldownMinutes() * 60L);
                if (Instant.now().isBefore(cooldownEnd)) {
                    log.info("[Alert] Rule {} is in cooldown until {} — skipping trigger", ruleId, cooldownEnd);
                    return;
                }
            }
        }

        // 3. Resolve danh sách recipients từ group codes trong config.
        Set<Client> recipients = resolveRecipients(config.getRecipientGroups());

        // 4. Tạo và lưu AlertInstance (status = ACTIVE).
        AlertInstance alert = AlertInstance.builder()
                .rule(config.getRule())
                .title(config.getAlertName())
                .body(config.getMessageTemplate())
                .severity(config.getSeverity())
                .status(AlertStatus.ACTIVE)
                .triggeredAt(Instant.now())
                .recipients(new HashSet<>(recipients))
                .build();
        alertInstanceDao.save(alert);
        alertInstanceDao.flush(); // Flush để có ID trước khi dùng trong FCM data

        log.info("[Alert] AlertInstance created: id={}, ruleId={}, severity={}, recipients={}",
                alert.getId(), ruleId, config.getSeverity(), recipients.size());

        // 5. Build FCM data payload (tất cả values phải là String theo FCM v1 spec).
        Map<String, String> data = buildFcmData("ALERT_TRIGGERED", alert, "ACTIVE");

        // 6. Parse danh sách channels từ JSON config.
        List<String> channels = parseJsonStringArray(config.getChannels());

        // 7. Load lại recipients với clientDevices (LAZY) trong transaction hiện tại.
        Set<Client> recipientsWithDevices = loadRecipientsWithDevices(recipients);

        // 8. Dispatch qua NotificationService (Strategy Pattern: FCM/Email/SMS).
        notificationService.sendNotification(
                recipientsWithDevices,
                channels,
                config.getAlertName(),
                config.getMessageTemplate(),
                data
        );
    }

    @Override
    @Transactional
    public void resolveAlertIfNeeded(Long ruleId) {
        // 1. Kiểm tra config có auto_resolve = true không.
        Optional<RuleActionAlert> configOpt = ruleActionAlertDao.findByRuleId(ruleId);
        if (configOpt.isEmpty() || Boolean.FALSE.equals(configOpt.get().getAutoResolve())) {
            return;
        }
        RuleActionAlert config = configOpt.get();

        // 2. Tìm tất cả alert đang mở (đã FETCH recipients).
        List<AlertInstance> openAlerts = alertInstanceDao.findAllOpenByRuleId(ruleId);
        if (openAlerts.isEmpty()) return;

        List<String> channels = parseJsonStringArray(config.getChannels());
        Instant now = Instant.now();

        for (AlertInstance alert : openAlerts) {
            // 3. Cập nhật trạng thái RESOLVED (resolved_by = null = auto-resolved bởi hệ thống).
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(now);
            alert.setResolvedBy(null);
            alertInstanceDao.update(alert);

            log.info("[Alert] Auto-resolved AlertInstance id={} for Rule {}", alert.getId(), ruleId);

            // 4. Gửi recovery notification.
            Map<String, String> data = buildFcmData("ALERT_RESOLVED", alert, "RESOLVED");
            String resolvedTitle = "Cảnh báo đã phục hồi: " + config.getAlertName();
            String resolvedBody  = "Tình trạng đã trở lại bình thường.";

            Set<Client> recipientsWithDevices = loadRecipientsWithDevices(alert.getRecipients());
            notificationService.sendNotification(
                    recipientsWithDevices, channels, resolvedTitle, resolvedBody, data
            );
        }
    }

    // =====================================================================
    // REST API Methods
    // =====================================================================

    @Override
    public PaginatedResponse<AlertResponseDto> getAlerts(AlertFilterDto filter) {
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        List<String> userGroups = SecurityContextUtil.getCustomUserDetails().getGroups();

        List<AlertInstance> alerts;
        long total;

        if (userGroups.contains(SysGroupEnum.G_ADMIN.getCode())) {
            // Admin: xem toàn bộ alerts trong hệ thống
            alerts = alertInstanceDao.findAll(filter.status(), filter.severity(), filter.page(), filter.size());
            total  = alertInstanceDao.countAll(filter.status(), filter.severity());

        } else if (userGroups.contains(SysGroupEnum.G_MAINTENANCE.getCode())) {
            // Maintenance: xem alerts được gửi tới group G_MAINTENANCE
            String groupCode = SysGroupEnum.G_MAINTENANCE.getCode();
            alerts = alertInstanceDao.findAllByGroupCode(groupCode, filter.status(), filter.severity(), filter.page(), filter.size());
            total  = alertInstanceDao.countByGroupCode(groupCode, filter.status(), filter.severity());

        } else {
            // End user: chỉ xem "My Alerts" — những alert mà mình là recipient
            alerts = alertInstanceDao.findAllByRecipientClientId(currentClientId, filter.status(), filter.severity(), filter.page(), filter.size());
            total  = alertInstanceDao.countByRecipientClientId(currentClientId, filter.status(), filter.severity());
        }

        List<AlertResponseDto> dtos = alerts.stream()
                .map(AlertResponseDto::from)
                .collect(Collectors.toList());
        return new PaginatedResponse<>(dtos, filter.page(), filter.size(), total);
    }

    @Override
    public AlertResponseDto getAlertById(Long alertId) {
        AlertInstance alert = alertInstanceDao.findByIdWithRecipients(alertId)
                .orElseThrow(() -> new NotFoundException("Alert not found: " + alertId));
        checkReadAccess(alert);
        return AlertResponseDto.from(alert);
    }

    @Override
    @Transactional
    public AlertResponseDto acknowledge(Long alertId) {
        AlertInstance alert = alertInstanceDao.findByIdWithRecipients(alertId)
                .orElseThrow(() -> new NotFoundException("Alert not found: " + alertId));
        checkReadAccess(alert);

        if (alert.getStatus() == AlertStatus.ACTIVE) {
            Long currentClientId = SecurityContextUtil.getCurrentClientId();
            Client currentClient = clientDao.findById(currentClientId)
                    .orElseThrow(() -> new NotFoundException("Client not found: " + currentClientId));
            alert.setStatus(AlertStatus.ACKNOWLEDGED);
            alert.setAcknowledgedAt(Instant.now());
            alert.setAcknowledgedBy(currentClient);
            alertInstanceDao.update(alert);
            log.info("[Alert] Alert {} acknowledged by client {}", alertId, currentClientId);
        }
        return AlertResponseDto.from(alert);
    }

    @Override
    @Transactional
    public AlertResponseDto resolve(Long alertId) {
        AlertInstance alert = alertInstanceDao.findByIdWithRecipients(alertId)
                .orElseThrow(() -> new NotFoundException("Alert not found: " + alertId));
        checkReadAccess(alert);

        if (alert.getStatus() != AlertStatus.RESOLVED) {
            Long currentClientId = SecurityContextUtil.getCurrentClientId();
            Client currentClient = clientDao.findById(currentClientId)
                    .orElseThrow(() -> new NotFoundException("Client not found: " + currentClientId));
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(Instant.now());
            alert.setResolvedBy(currentClient);
            alertInstanceDao.update(alert);
            log.info("[Alert] Alert {} manually resolved by client {}", alertId, currentClientId);

            // Dispatch ALERT_RESOLVED push notification
            ruleActionAlertDao.findByRuleId(alert.getRule().getId()).ifPresent(config -> {
                List<String> channels = parseJsonStringArray(config.getChannels());
                Map<String, String> data = buildFcmData("ALERT_RESOLVED", alert, "RESOLVED");
                String resolvedTitle = "Cảnh báo đã giải quyết: " + config.getAlertName();
                String resolvedBody  = "Sự cố đã được xử lý bởi " + currentClient.getUsername() + ".";
                Set<Client> recipientsWithDevices = loadRecipientsWithDevices(alert.getRecipients());
                notificationService.sendNotification(
                        recipientsWithDevices, channels, resolvedTitle, resolvedBody, data
                );
            });
        }
        return AlertResponseDto.from(alert);
    }

    // =====================================================================
    // Private Helpers
    // =====================================================================

    /**
     * Đọc mảng JSON group codes và trả về tất cả Client thuộc các groups đó.
     * Input: JsonNode đại diện cho ["G_ADMIN", "G_MAINTENANCE"]
     */
    private Set<Client> resolveRecipients(JsonNode recipientGroups) {
        Set<Client> recipients = new HashSet<>();
        if (recipientGroups == null || !recipientGroups.isArray()) {
            return recipients;
        }
        StreamSupport.stream(recipientGroups.spliterator(), false)
                .map(JsonNode::asText)
                .filter(code -> !code.isBlank())
                .forEach(groupCode ->
                    sysGroupDao.findEntityByCode(groupCode).ifPresent(group -> {
                        List<Client> groupClients = sysGroupDao.findClientEntitiesByGroupId(group.getId());
                        recipients.addAll(groupClients);
                    })
                );
        log.debug("[Alert] Resolved {} unique recipients from groups {}", recipients.size(), recipientGroups);
        return recipients;
    }

    /**
     * Load lại tập Client với clientDevices được FETCH trong transaction hiện tại.
     * Cần thiết vì Client.clientDevices = FetchType.LAZY —
     * nếu không reload, FcmNotificationStrategy sẽ gặp LazyInitializationException.
     */
    private Set<Client> loadRecipientsWithDevices(Set<Client> recipients) {
        if (recipients == null || recipients.isEmpty()) return Set.of();
        return recipients.stream()
                .map(c -> clientDao.findById(c.getId()).orElse(null))
                .filter(c -> c != null)
                .collect(Collectors.toSet());
    }

    /**
     * Build FCM v1 data map. TẤT CẢ values phải là String theo FCM v1 API spec.
     * Deep link format: smartroom://alert/{id}
     */
    private Map<String, String> buildFcmData(String type, AlertInstance alert, String statusStr) {
        Map<String, String> data = new HashMap<>();
        data.put("type",      type);
        data.put("entityId",  String.valueOf(alert.getId()));
        data.put("severity",  alert.getSeverity().name());
        data.put("status",    statusStr);
        data.put("deepLink",  "smartroom://alert/" + alert.getId());
        data.put("timestamp", String.valueOf(Instant.now().toEpochMilli()));
        return data;
    }

    /**
     * Parse JsonNode mảng strings thành List<String>.
     * Trả về List.of() nếu node là null hoặc không phải array.
     */
    private List<String> parseJsonStringArray(JsonNode jsonNode) {
        if (jsonNode == null || !jsonNode.isArray()) return List.of();
        List<String> result = new ArrayList<>();
        jsonNode.forEach(node -> {
            String text = node.asText();
            if (!text.isBlank()) result.add(text);
        });
        return result;
    }

    /**
     * Kiểm tra quyền đọc alert của user hiện tại:
     * G_ADMIN    → luôn pass
     * G_MAINTENANCE → pass nếu G_MAINTENANCE trong recipient_groups config của Rule
     * G_USER     → pass chỉ khi clientId có trong alert.recipients (alert_recipient table)
     */
    private void checkReadAccess(AlertInstance alert) {
        List<String> userGroups = SecurityContextUtil.getCustomUserDetails().getGroups();

        if (userGroups.contains(SysGroupEnum.G_ADMIN.getCode())) return;

        if (userGroups.contains(SysGroupEnum.G_MAINTENANCE.getCode())) {
            Optional<RuleActionAlert> configOpt = ruleActionAlertDao.findByRuleId(alert.getRule().getId());
            if (configOpt.isPresent()) {
                JsonNode groups = configOpt.get().getRecipientGroups();
                if (groups != null && groups.isArray()) {
                    boolean hasAccess = StreamSupport.stream(groups.spliterator(), false)
                            .anyMatch(n -> SysGroupEnum.G_MAINTENANCE.getCode().equals(n.asText()));
                    if (hasAccess) return;
                }
            }
        }

        // G_USER: kiểm tra danh sách recipient
        Long currentClientId = SecurityContextUtil.getCurrentClientId();
        boolean isRecipient = alert.getRecipients().stream()
                .anyMatch(c -> currentClientId.equals(c.getId()));
        if (isRecipient) return;

        throw new ForbiddenException("You do not have access to alert " + alert.getId());
    }
}
```

---

## Task 8: Rule Engine Integration

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/scheduler/dynamic/rule/RuleProcessor.java`

- [ ] **Step 1: Thêm field `AlertService` vào `RuleProcessor`**

Thêm import và field (Lombok `@RequiredArgsConstructor` tự inject):
```java
// Thêm import (đặt cùng block import hiện có):
import com.iviet.ivshs.service.alert.AlertService;

// Thêm field vào class RuleProcessor (sau field `private final RuleDao ruleDao;`):
    private final AlertService alertService;
```

- [ ] **Step 2: Cập nhật method `process()` trong `RuleProcessor`**

Thay thế method `process()` hiện tại (dòng 65-85) bằng:
```java
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void process(Rule rule) {
        if (!Boolean.TRUE.equals(rule.getIsActive()))
            return;

        List<RuleCondition> conditions = rule.getConditions();
        if (conditions == null || conditions.isEmpty()) {
            log.warn("Rule {} has no conditions", rule.getId());
            return;
        }

        log.info("Evaluating Rule {} with {} conditions", rule.getId(), conditions.size());

        EvaluationContext initCtx = new EvaluationContext();
        boolean isMatched = conditions.stream()
                .sorted(Comparator.comparingInt(RuleCondition::getSortOrder))
                .reduce(initCtx, this::accumulateResult, (a, b) -> a)
                .isFinalResult();

        if (isMatched) {
            // Thực thi device control actions (hành vi hiện tại — giữ nguyên)
            executeActions(rule);

            // Kích hoạt alert nếu Rule này có alert config
            try {
                alertService.triggerAlert(rule.getId());
            } catch (Exception e) {
                // Alert failure KHÔNG được abort việc thực thi device action
                log.error("[Alert] Failed to trigger alert for rule {}: {}", rule.getId(), e.getMessage(), e);
            }

        } else {
            // Tự động resolve alert nếu conditions không còn thỏa mãn
            try {
                alertService.resolveAlertIfNeeded(rule.getId());
            } catch (Exception e) {
                log.error("[Alert] Failed to auto-resolve alert for rule {}: {}", rule.getId(), e.getMessage(), e);
            }
        }
    }
```

---

## Task 9: REST API Controller

**Files:**
- Create: `src/main/java/com/iviet/ivshs/controller/api/v1/AlertController.java`

- [ ] **Step 1: Tạo `AlertController.java`**

> **Pattern:** Bám sát hoàn toàn `RuleController.java` — `@RestController`, `@RequestMapping("/v1/alerts")`, `ResponseEntity<ApiResponse<T>>`, `@RequiredArgsConstructor`.

```java
package com.iviet.ivshs.controller.api.v1;

import com.iviet.ivshs.dto.alert.AlertFilterDto;
import com.iviet.ivshs.dto.alert.AlertResponseDto;
import com.iviet.ivshs.dto.common.ApiResponse;
import com.iviet.ivshs.dto.common.PaginatedResponse;
import com.iviet.ivshs.service.alert.AlertService;
import com.iviet.ivshs.shared.enumeration.AlertStatus;
import com.iviet.ivshs.shared.enumeration.Severity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller cho Alert Management.
 * Tất cả endpoint RBAC được xử lý trong AlertService dựa trên group của user hiện tại.
 * Route: /api/v1/alerts (prefix /api được thêm bởi Spring Security filter hoặc global prefix config)
 */
@Slf4j
@RestController("alertController")
@RequiredArgsConstructor
@RequestMapping("/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    /**
     * GET /api/v1/alerts
     * Lấy danh sách alerts với filter và phân trang.
     * RBAC: Admin → tất cả | Maintenance → của group | User → "My Alerts"
     *
     * @param status   Optional: ACTIVE | ACKNOWLEDGED | RESOLVED
     * @param severity Optional: INFO | WARNING | CRITICAL
     * @param page     Trang (0-based). Default: 0.
     * @param size     Kích thước trang. Default: 10. Max: 100.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<AlertResponseDto>>> getAlerts(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(name = "page", defaultValue = "0")  int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        AlertFilterDto filter = new AlertFilterDto(status, severity, page, size);
        return ResponseEntity.ok(ApiResponse.ok(alertService.getAlerts(filter)));
    }

    /**
     * GET /api/v1/alerts/{id}
     * Chi tiết 1 alert. Throw 403 nếu user không có quyền xem.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertResponseDto>> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.getAlertById(id)));
    }

    /**
     * POST /api/v1/alerts/{id}/acknowledge
     * Xác nhận đã biết về alert. No-op nếu đã ACKNOWLEDGED hoặc RESOLVED.
     */
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<AlertResponseDto>> acknowledgeAlert(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.acknowledge(id)));
    }

    /**
     * POST /api/v1/alerts/{id}/resolve
     * Giải quyết alert thủ công. Dispatch ALERT_RESOLVED notification tới recipients.
     * No-op nếu đã RESOLVED.
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<AlertResponseDto>> resolveAlert(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.resolve(id)));
    }
}
```

---

## Self-Review Checklist

### 1. Spec Coverage

| Yêu cầu | Task |
|---|---|
| 3 bảng DB: `rule_action_alert`, `alert_instance`, `alert_recipient` | Task 1 |
| Enum `Severity`, `AlertStatus` | Task 2 |
| `SysGroupEnum.G_MAINTENANCE` | Task 2 |
| `SysFunctionEnum.F_ACCESS_ALERT_*` | Task 2 |
| Entity `RuleActionAlert` + `AlertInstance` | Task 3 |
| `Rule.java` thêm `alertConfig` @OneToOne | Task 3 |
| DAO layer đầy đủ cho Admin/Maintenance/User query | Task 4 |
| DTO `AlertResponseDto` + `AlertFilterDto` | Task 5 |
| `NotificationService` interface (facade) | Task 6 |
| Strategy Pattern: FCM / Email / SMS | Task 6 |
| `FcmNotificationStrategy` dùng `FcmDispatchService` đã có sẵn | Task 6 |
| `AlertService` interface + implementation | Task 7 |
| Cooldown logic | Task 7 |
| Auto-resolve logic | Task 7 |
| RBAC: Admin/Maintenance/User | Task 7 |
| FCM v1 payload: type, entityId, severity, status, deepLink, timestamp | Task 7 |
| Deep link `smartroom://alert/{id}` | Task 7 |
| Tích hợp vào `RuleProcessor.process()` | Task 8 |
| REST Controller 4 endpoints | Task 9 |

### 2. Type Consistency

- `AlertService.triggerAlert(Long)` → gọi trong `RuleProcessor.process()` ✅
- `AlertService.resolveAlertIfNeeded(Long)` → gọi trong `RuleProcessor.process()` ✅
- `AlertResponseDto.from(AlertInstance)` → dùng trong `AlertServiceImpl` và `AlertController` ✅
- `AlertFilterDto(status, severity, page, size)` → tạo trong `AlertController`, pass vào `AlertService.getAlerts()` ✅
- `NotificationService.sendNotification(Set<Client>, List<String>, String, String, Map<String,String>)` → gọi trong `AlertServiceImpl` ✅
- `NotificationStrategy.supports(String)` + `send(Set<Client>, String, String, Map<String,String>)` → implement đầy đủ trong cả 3 strategies ✅
- `SysGroupEnum.G_MAINTENANCE.getCode()` → dùng trong `AlertServiceImpl.getAlerts()` và `checkReadAccess()` ✅
- `SecurityContextUtil.getCustomUserDetails().getGroups()` → trả về `List<String>` chứa groupCode ✅
- `PaginatedResponse<T>(content, page, size, totalElements)` → đúng constructor 4-arg ✅
- `ApiResponse.ok(data)` → đúng static factory ✅

### 3. Verified Against Codebase

| Điểm kiểm tra | Kết quả |
|---|---|
| `BaseAuditEntity` kế thừa: `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `version` | ✅ Verified |
| `JsonNodeConverter @autoApply=true` → không cần `@Convert` thủ công | ✅ Verified |
| `Client.clientDevices` = `FetchType.LAZY` → cần `loadRecipientsWithDevices()` | ✅ Verified |
| `FcmDispatchService.sendToMultipleDevices()` là `@Async` | ✅ Verified |
| `CustomUserDetails.getGroups()` trả về `List<String>` groupCodes | ✅ Verified |
| `SysGroupDao.findEntityByCode()` + `findClientEntitiesByGroupId()` tồn tại | ✅ Verified |
| `ClientDao.findById()` tồn tại trong `BaseEntityDao` | ✅ Verified |
| `RuleProcessor` dùng `@RequiredArgsConstructor` → thêm `AlertService` field là đủ | ✅ Verified |
| Controller pattern: `@RestController("beanName")` + `@RequestMapping("/v1/...")` | ✅ Verified |
| `ApiResponse.ok()` + `PaginatedResponse(content, page, size, total)` | ✅ Verified |

---

## Execution Options

**Plan complete. Hai lựa chọn thực thi:**

**1. Inline Execution** — Thực thi ngay trong session này, task-by-task với checkpoint review.
Dùng skill: `executing-plans`

**2. Subagent-Driven** *(recommended)* — Mỗi task một subagent riêng, review giữa các tasks, iteration nhanh hơn.
Dùng skill: `subagent-driven-development`
