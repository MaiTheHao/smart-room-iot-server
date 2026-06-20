Kế hoạch triển khai (Implementation Plan) dưới đây được điều chỉnh để hỗ trợ mô hình **1 Rule có nhiều cấu hình Alert độc lập (1-N)**. Mỗi cấu hình Alert (`RuleActionAlert`) sẽ quản lý riêng biệt chu kỳ gửi thông báo, nội dung, mức độ ưu tiên, danh sách người nhận và thời gian cooldown.

---

## Task 1: Cập nhật Database Schema (Migration)

Thay đổi cơ sở dữ liệu dựa trên file `003__Alert_System_Tables.sql`.

* Xóa bỏ ràng buộc `UNIQUE` ở bảng cấu hình.
* Chuyển khóa ngoại của `alert_instance` trỏ về `alert_config_id` thay vì `rule_id`.



**File:** `src/main/resources/db/migration/004__Refactor_Alert_One_To_Many.sql`

```sql
-- 1. Bảng cấu hình RuleActionAlert: Cho phép nhiều config trên 1 rule
CREATE TABLE `rule_action_alert` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `created_at`       datetime(6)  DEFAULT NULL,
  `created_by`       varchar(256) DEFAULT NULL,
  `updated_at`       datetime(6)  DEFAULT NULL,
  `updated_by`       varchar(256) DEFAULT NULL,
  `v`                bigint       NOT NULL DEFAULT 0,
  `rule_id`          bigint       NOT NULL, -- BỎ UNIQUE KEY tại đây
  `alert_name`       varchar(256) NOT NULL,
  `severity`         varchar(50)  NOT NULL,
  `recipient_groups` json         DEFAULT NULL,
  `channels`         json         DEFAULT NULL,
  `message_template` text         NOT NULL,
  `cooldown_minutes` int          NOT NULL DEFAULT 0,
  `auto_resolve`     tinyint(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_rule_action_alert_rule_id` (`rule_id`),
  CONSTRAINT `fk_rule_action_alert_rule` FOREIGN KEY (`rule_id`) REFERENCES `rule` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng sự kiện AlertInstance: Trỏ thẳng về config sinh ra nó
CREATE TABLE `alert_instance` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `created_at`       datetime(6)  DEFAULT NULL,
  `created_by`       varchar(256) DEFAULT NULL,
  `updated_at`       datetime(6)  DEFAULT NULL,
  `updated_by`       varchar(256) DEFAULT NULL,
  `v`                bigint       NOT NULL DEFAULT 0,
  `alert_config_id`  bigint       NOT NULL, -- THAY ĐỔI: Dùng alert_config_id thay vì rule_id
  `title`            varchar(256) NOT NULL,
  `body`             text         NOT NULL,
  `severity`         varchar(50)  NOT NULL,
  `status`           varchar(50)  NOT NULL,
  `triggered_at`     datetime(6)  NOT NULL,
  `acknowledged_at`  datetime(6)  DEFAULT NULL,
  `acknowledged_by`  bigint       DEFAULT NULL,
  `resolved_at`      datetime(6)  DEFAULT NULL,
  `resolved_by`      bigint       DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_alert_instance_config_id` (`alert_config_id`),
  KEY `idx_alert_instance_status` (`status`),
  CONSTRAINT `fk_alert_instance_config` FOREIGN KEY (`alert_config_id`) REFERENCES `rule_action_alert` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_alert_instance_ack_by` FOREIGN KEY (`acknowledged_by`) REFERENCES `client` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_alert_instance_res_by` FOREIGN KEY (`resolved_by`) REFERENCES `client` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng alert_recipient (Giữ nguyên cấu trúc)

```

---

## Task 2: Cập nhật Entity Layer (JPA Mapping)

* [ ] **Step 1: Cập nhật `Rule.java**`
Chuyển thuộc tính cấu hình từ 1 Object sang 1 List (Mảng).

```java
    // Xóa: private RuleActionAlert alertConfig;
    
    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RuleActionAlert> alerts = new ArrayList<>();

    public void addAlertConfig(RuleActionAlert alert) {
        this.alerts.add(alert);
        alert.setRule(this);
    }

    public void removeAlertConfig(RuleActionAlert alert) {
        this.alerts.remove(alert);
        alert.setRule(null);
    }

```

* [ ] **Step 2: Cập nhật `RuleActionAlert.java**`
Sử dụng `@ManyToOne` để tạo liên kết về `Rule`.

```java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

```

* [ ] **Step 3: Cập nhật `AlertInstance.java**`
Trỏ tới `RuleActionAlert` thay vì trỏ tới `Rule`.



```java
    // Xóa: private Rule rule;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_config_id", nullable = false)
    private RuleActionAlert alertConfig;

```

---

## Task 3: Cập nhật DAO Layer

* [ ] **Step 1: `RuleActionAlertDao.java**`
Sửa hàm trả về một danh sách thay vì 1 đối tượng duy nhất.

```java
    public List<RuleActionAlert> findAllByRuleId(Long ruleId) {
        String jpql = "SELECT raa FROM RuleActionAlert raa WHERE raa.rule.id = :ruleId";
        return entityManager.createQuery(jpql, RuleActionAlert.class)
                .setParameter("ruleId", ruleId)
                .getResultList();
    }

```

* [ ] **Step 2: `AlertInstanceDao.java**`
Điều chỉnh logic query cooldown và fetch dựa trên `alert_config_id`.

```java
    // Dùng alertConfigId thay vì ruleId cho Cooldown check
    public Optional<AlertInstance> findLatestOpenByConfigId(Long configId) {
        String jpql = """
            SELECT ai FROM AlertInstance ai
            WHERE ai.alertConfig.id = :configId
              AND ai.status IN (:statuses)
            ORDER BY ai.triggeredAt DESC
            """;
        return entityManager.createQuery(jpql, AlertInstance.class)
                .setParameter("configId", configId)
                .setParameter("statuses", List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED))
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    // Phục vụ Auto-resolve
    public List<AlertInstance> findAllOpenByConfigId(Long configId) {
        String jpql = """
            SELECT DISTINCT ai FROM AlertInstance ai
            LEFT JOIN FETCH ai.recipients
            WHERE ai.alertConfig.id = :configId
              AND ai.status IN (:statuses)
            """;
        // ... set parameters and return getResultList()
    }

```

---

## Task 4: Cập nhật `AlertService` và Logic cốt lõi

* [ ] **Step 1: Cập nhật Interface `AlertService.java**`
Nhận tham số là ID của từng cấu hình cảnh báo, thêm hàm dọn dẹp theo rule.

```java
public interface AlertService {
    void triggerAlert(Long alertConfigId);
    void resolveAlertIfNeeded(Long alertConfigId);
    void deleteAlertsByRuleId(Long ruleId); // Hàm dọn dẹp chủ động
    // ... getAlerts, acknowledge, resolve giữ nguyên
}

```

* [ ] **Step 2: Triển khai trong `AlertServiceImpl.java**`

```java
    @Override
    @Transactional
    public void triggerAlert(Long alertConfigId) {
        RuleActionAlert config = ruleActionAlertDao.findById(alertConfigId)
                .orElseThrow(() -> new NotFoundException("Config not found"));

        // Kiểm tra cooldown dựa trên ID của config cụ thể này
        if (config.getCooldownMinutes() > 0) {
            Optional<AlertInstance> latestOpen = alertInstanceDao.findLatestOpenByConfigId(alertConfigId);
            // ... logic cooldown
        }

        // Tạo Instance
        AlertInstance alert = AlertInstance.builder()
                .alertConfig(config)
                .title(config.getAlertName())
                // ...
                .build();
                
        // ... Gửi NotificationService giống cũ
    }

    @Override
    @Transactional
    public void deleteAlertsByRuleId(Long ruleId) {
        List<RuleActionAlert> configs = ruleActionAlertDao.findAllByRuleId(ruleId);
        for (RuleActionAlert config : configs) {
            ruleActionAlertDao.delete(config);
        }
        log.info("[Alert] Cleaned up {} alert configs for rule {}", configs.size(), ruleId);
    }

```

---

## Task 5: Tích hợp duyệt mảng vào Quartz Rule Engine

* [ ] **Step 1: Cập nhật `RuleProcessor.java**`
Khi chạy rule, duyệt qua danh sách các alert configs để xử lý đồng thời.

```java
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void process(Rule rule) {
        if (!Boolean.TRUE.equals(rule.getIsActive())) return;

        boolean isMatched = evaluateConditions(rule);

        // Duyệt toàn bộ mảng Alert Configs
        List<RuleActionAlert> alertConfigs = rule.getAlerts();
        
        if (isMatched) {
            executeActions(rule);
            
            if (alertConfigs != null) {
                for (RuleActionAlert config : alertConfigs) {
                    try {
                        alertService.triggerAlert(config.getId());
                    } catch (Exception e) {
                        log.error("Failed to trigger alert {}", config.getId(), e);
                    }
                }
            }
        } else {
            if (alertConfigs != null) {
                for (RuleActionAlert config : alertConfigs) {
                    try {
                        alertService.resolveAlertIfNeeded(config.getId());
                    } catch (Exception e) {
                        log.error("Failed to resolve alert {}", config.getId(), e);
                    }
                }
            }
        }
    }

```

---

## Task 6: Cập nhật luồng xóa Rule ở Layer Service

* [ ] **Step 1: Cập nhật logic xóa Rule (`RuleServiceImpl.java`)**
Khi có yêu cầu xóa Rule (từ Admin), hệ thống sẽ dọn dẹp các Alert một cách tường minh tại Service thay vì hoàn toàn phó mặc cho DB Cascade.

```java
    @Override
    @Transactional
    public void deleteRule(Long ruleId) {
        Rule rule = ruleDao.findById(ruleId)
                .orElseThrow(() -> new NotFoundException("Rule not found: " + ruleId));
                
        // Dọn dẹp tầng Service (xóa các bảng liên quan)
        alertService.deleteAlertsByRuleId(ruleId);
        
        ruleDao.delete(rule);
        log.info("Rule {} and associated alerts deleted.", ruleId);
    }

```