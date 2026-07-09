# Design Spec: Documentation Update — README.md & SYSTEM.md

**Status:** Draft  
**Date:** 2026-07-09  
**Author:** Plan Writer Agent  
**Priority:** High  

---

## Context

The project's primary documentation files (`README.md` and `SYSTEM.md`) have diverged significantly from the actual codebase. An extensive codebase analysis revealed widespread inaccuracies in tech stack declarations, missing architecture modules, incorrect endpoint paths, stale terminology, and incomplete entity listings. These inaccuracies pose a risk to new contributors, system integrators, and automated tooling that relies on these documents for ground truth.

### Key Findings (Codebase Analysis)

#### Frontend Tech Stack Errors

| Documented (DOC)                | Actual Codebase              |
|----------------------------------|------------------------------|
| AdminLTE 3.2                     | AdminLTE 4.0.0              |
| Bootstrap 4.6                    | Bootstrap 5.3.2             |
| jQuery 3.7                       | **Not used** (vanilla JS + ES modules) |
| Chart.js 4.4                     | ApexCharts                  |
| Datatables 1.13                  | Tabulator                   |
| *(not listed)*                   | Lucide Icons                |
| *(not listed)*                   | Flatpickr                   |
| *(not listed)*                   | OverlayScrollbars           |

#### Backend Tech Stack — Missing Entries

The following backend libraries are present in the codebase but absent from both documents:

- Firebase Admin SDK 9.9.0 (FCM push notifications)
- Bucket4j 8.10.1 (rate limiting)
- Apache HttpClient5 5.2.3
- Caffeine Cache 3.1.8
- Hibernate Validator 8.0.1
- Jackson 2.18.2
- Log4j 2.25.4 + SLF4J 2.0.16
- AspectJ 1.9.21

#### Architecture — Missing Modules

The following architectural modules exist in the codebase but are documented nowhere:

| Module                       | Package Path                                            |
|------------------------------|---------------------------------------------------------|
| Gateway Adapter Pattern      | `integration/gateway/` (ESP32 + Raspberry Pi adapters)  |
| Alert System                 | `service/alert/` (AlertConfig → AlertInstance → AlertInstanceLog + FCM) |
| Automation Engine            | `scheduler/dynamic/automation/` (Automation + AutomationAction + strategies) |
| Energy Metric                | `scheduler/system/metric/` (Scheduled collection & daily reset) |
| Notification                 | `service/notification/` (Email/FCM/SMS strategies)      |
| Rate Limiting                | `shared/filter/RateLimitingFilter` (Bucket4j)           |
| Request Tracing              | `shared/filter/` + `shared/logging/` (Trace filter + MDC logging) |
| Device Setup                 | `dao/setup/` (Orchestrator + Strategy pattern)          |
| Sensor/Device Metadata       | `service/control/`                                      |
| Dynamic Job Scheduler        | `scheduler/dynamic/`                                    |
| Token Service                | `service/token/` (JWT provider)                         |
| Properties Binding Classes   | `core/properties/`                                      |

#### README.md Structural Issues

- Section numbering jumps from **2** to **4** (Section 3 is missing entirely).
- No Architecture Overview section exists.
- Tech stack table is incomplete and contains obsolete entries.

#### SYSTEM.md Structural Issues

- Duplicate **"2.1"** section number.
- Uses **"Rule V2"** terminology; codebase uses simple **"Rule"** naming.
- Auth endpoint documented as `POST /get/v1/auth/signin`; actual is `POST /api/v1/auth/signin`.
- ERD file link (`infra/erd.dbml`) points to a non-existent file.
- Entity table names are wrong: `rule_v2` → `rule`, `device_control` → `device_metadata`.
- Missing 15+ entity classes.
- Missing 20+ new enum classes.
- Missing entire **Base entity hierarchy** (abstract base classes for ID, timestamps, etc.).

---

## Decision

We will rewrite **both** `README.md` and `SYSTEM.md` from scratch rather than attempt surgical patches. The divergence is too extensive for incremental fixes, and a full rewrite ensures internal consistency and a single source of truth.

### Why Not Patch

1. **Structural rot** — Missing sections, duplicate numbering, and incorrect section hierarchy require reorganising the entire document body.
2. **Terminology drift** — "Rule V2" vs "Rule" and incorrect endpoint paths infect multiple locations in `SYSTEM.md`; patching risks leaving remnants.
3. **Scope of additions** — 12+ missing architecture modules, 15+ missing entities, and 20+ missing enums require inserting content throughout both documents. A rewrite is more reliable than a sequence of edits.
4. **Tech stack table** — Every row changed; easier to rebuild the table than to fix individual cells.

### Renaming / Deprecation

- The old files will be **renamed** with a `.deprecated` suffix rather than deleted, preserving git history and providing a rollback reference.
- The new files will occupy the original paths (`README.md`, `SYSTEM.md`).

---

## Consequences

### Positive

- **Single source of truth** restored — documentation matches codebase at the commit level.
- **Onboarding velocity** improves — new developers see accurate tech stack and module layout.
- **Tooling reliability** — automated parsers consuming these docs (e.g., for dependency graphs, ERD generation) will produce correct output.
- **Auditability** — deprecated originals preserved for diff comparison.

### Negative

- **Effort cost** — full rewrite of two documents (~500–800 lines each).
- **Review burden** — the diff will show a complete file replacement, making line-by-line review harder. Mitigation: commit the rename first, then the new content, so the rename is a trivial no-content-change commit.
- **Potential new errors** — rewriting introduces risk of fresh mistakes. Mitigation: cross-reference against the codebase analysis checklist during review.

### Risk

- **Low** — documentation changes carry no runtime risk. The main risk is inaccuracy in the new content, addressed by the review checklist.

---

## Detailed Design

### Document Structure

#### README.md — Proposed Outline

```
# SmartRoom Server

> One-paragraph project elevator pitch (unchanged in spirit, updated for accuracy).

---

## 1. Project Overview

- Brief description of the smart-room IoT system
- What problem it solves (monitoring, control, automation, alerting)
- Target audience / use cases

## 2. Core Tech Stack

| Category          | Technology                | Version    |
|-------------------|---------------------------|------------|
| Backend Framework | Spring Boot               | 3.x        |
| Language          | Java                      | 17+        |
| Build Tool        | Maven                     | ...        |
| Database          | MySQL (via JPA/Hibernate) | ...        |
| ORM               | Hibernate                 | 6.x        |
| Migrations        | Flyway                    | ...        |
| Cache             | Caffeine                  | 3.1.8      |
| Validation        | Hibernate Validator       | 8.0.1      |
| JSON              | Jackson                   | 2.18.2     |
| HTTP Client       | Apache HttpClient5        | 5.2.3      |
| Rate Limiting     | Bucket4j                  | 8.10.1     |
| Push Notifications| Firebase Admin SDK        | 9.9.0      |
| Logging           | Log4j / SLF4J             | 2.25.4 / 2.0.16 |
| AOP               | AspectJ                   | 1.9.21     |
| **Frontend**      | **Technology**            | **Version**|
| UI Framework      | AdminLTE                  | 4.0.0      |
| CSS Framework     | Bootstrap                 | 5.3.2      |
| JavaScript        | Vanilla JS + ES Modules   | (no jQuery)|
| Charts            | ApexCharts                | latest     |
| Tables            | Tabulator                 | latest     |
| Icons             | Lucide Icons              | latest     |
| Date Picker       | Flatpickr                 | latest     |
| Scrollbars        | OverlayScrollbars         | latest     |

## 3. Architecture Overview (NEW)

- **Monolith with modular packages** — layered architecture (controller → service → repository → entity)
- **Gateway Dual-Adapter Pattern** — ESP32 adapter + Raspberry Pi adapter under `integration/gateway/`
- **Scheduler Subsystem** — dynamic job scheduling, automation engine, energy metric collection
- **Alert & Notification** — rule-based alert config → alert instance → FCM push
- Diagram reference: `docs/diagrams/architecture-overview.png` (to be created)

## 4. Core Capabilities

### 4.1 Device Management
### 4.2 Real-Time Monitoring
### 4.3 Device Control (Strategy Pattern)
### 4.4 Security & RBAC
### 4.5 Alert System
### 4.6 Automation Engine
### 4.7 Energy Metrics
### 4.8 Notification (Email / FCM / SMS)

## 5. Related Documents

- [SYSTEM.md](SYSTEM.md) — Full system design
- [API docs](docs/api/) — OpenAPI / Swagger
- [ERD](docs/database/erd.md)
```

#### SYSTEM.md — Proposed Outline

```
# SmartRoom System Design

## 1. Overview

- System diagram (updated to show all modules)
- High-level data flow

## 2. Backend Architecture

### 2.1 Technologies (complete table — frontend + backend corrected)

### 2.2 Package List (~30 packages)

| Package | Responsibility |
|---------|----------------|
| `controller/` | REST + View controllers |
| `service/` | Business logic |
| `service/alert/` | Alert config → instance → log |
| `service/notification/` | Email / FCM / SMS strategies |
| `service/token/` | JWT token service |
| `service/control/` | Sensor/device metadata |
| `repository/` | JPA repositories |
| `entity/` | JPA entities + base hierarchy |
| `dao/` | Data access objects |
| `dao/setup/` | Device setup orchestrator + strategy |
| `integration/gateway/` | ESP32 + Raspberry Pi adapters |
| `scheduler/dynamic/` | Dynamic job scheduling |
| `scheduler/dynamic/automation/` | Automation engine |
| `scheduler/system/metric/` | Energy metric collection |
| `shared/filter/` | Rate-limiting, trace, auth filters |
| `shared/logging/` | MDC logging |
| `shared/util/` | Shared utilities |
| `core/properties/` | Properties binding classes |
| `config/` | Spring configuration |

### 2.3 Core Architecture Flow
### 2.4 System Configuration

## 3. Frontend Architecture

### 3.1 Technologies (corrected — no jQuery, ApexCharts, Tabulator, etc.)
### 3.2 SSR + CSR (updated)

## 4. Business Flows

### 4.1 Auth & RBAC (fix endpoint: `POST /api/v1/auth/signin`)
### 4.2 Standard API Flow
### 4.3 API vs View Controller
### 4.4 Telemetry Collection (updated gateway flow)
### 4.5 Device Control — Strategy Pattern
### 4.6 Rule Engine (`rule_v2` → `rule`, terminology corrected)
### 4.7 Alert System (NEW)
### 4.8 Automation Engine (NEW)
### 4.9 Notification (NEW)
### 4.10 Energy Metric (NEW)
### 4.11 Gateway Integration (NEW)

## 5. Database Structure

### 5.1 Complete Entity List

All entities with correct table names:
- `rule` (was `rule_v2`)
- `device_metadata` (was `device_control`)
- All base entity classes (abstract base with ID, createdAt, updatedAt)
- 15+ previously missing entities (AlertConfig, AlertInstance, AlertInstanceLog, Automation, AutomationAction, EnergyMetric, NotificationLog, etc.)

### 5.2 Business Grouping

| Group              | Entities                                              |
|--------------------|-------------------------------------------------------|
| Core               | ...                                                   |
| Device             | ...                                                   |
| Alert              | AlertConfig, AlertInstance, AlertInstanceLog (NEW)    |
| Automation         | Automation, AutomationAction (NEW)                    |
| Energy             | EnergyMetric (NEW)                                    |
| Notification       | NotificationLog, NotificationChannel (NEW)            |

### 5.3 Enum Classes (complete list, 20+ previously missing)
### 5.4 Entity Relationship Diagram (update or add note that `infra/erd.dbml` is deprecated)
```

### File Migration Plan

1. **Rename existing files**  
   `README.md` → `README.md.deprecated`  
   `SYSTEM.md` → `SYSTEM.md.deprecated`

2. **Write new `README.md`** per the outline above, with corrected tech stack, new Section 3 (Architecture Overview), expanded Core Capabilities, and updated Related Documents links.

3. **Write new `SYSTEM.md`** per the outline above, with corrected sections, fixed terminology, complete entity/enum list, added business flows for all missing modules, and corrected endpoint paths.

### Review Checklist (pre-commit gate)

| # | Check | Owner |
|---|-------|-------|
| 1 | Every tech stack entry matches actual dependency declarations (`pom.xml` / `package.json`) | Reviewer |
| 2 | No occurrence of "jQuery", "Chart.js", "Datatables", "AdminLTE 3", "Bootstrap 4" | Reviewer |
| 3 | Endpoint `POST /api/v1/auth/signin` (not `/get/v1/auth/signin`) | Reviewer |
| 4 | No occurrences of `rule_v2` or `device_control` table names | Reviewer |
| 5 | All 12+ missing architecture modules present in package list | Reviewer |
| 6 | All 15+ missing entities present in entity list | Reviewer |
| 7 | All 20+ missing enum classes present | Reviewer |
| 8 | Base entity hierarchy documented | Reviewer |
| 9 | ERD reference updated or deprecated note added | Reviewer |
| 10 | Section numbering is sequential with no duplicates | Reviewer |

---

## Open Questions

1. **Architecture diagram** — Should a new architecture diagram be created and referenced, or should we only update the text?  
   *Proposed answer:* Reference a diagram file path but leave the actual diagram creation to a separate task.

2. **ERD replacement** — `infra/erd.dbml` is referenced but does not exist. Should we generate a new ERD from the entity classes, or replace the reference with a link to the JPA entity source directory?  
   *Proposed answer:* Replace with a link to `src/main/java/.../entity/` and note that the ERD is auto-generated from entities.

3. **Changelog** — Should a `CHANGELOG.md` entry be added for this documentation update?  
   *Proposed answer:* Yes — add a documentation section to the changelog.

---

## Appendices

### A. Analysis Sources

- `pom.xml` — backend dependency declarations
- `package.json` — frontend dependency declarations
- Source tree traversal under `src/main/java/` for module discovery
- Source tree traversal under `src/main/resources/static/` for frontend asset discovery
- Entity class inspection via `@Entity` annotation scan

### B. Affected Files

| File | Action |
|------|--------|
| `README.md` | Rename → `README.md.deprecated`; create new |
| `SYSTEM.md` | Rename → `SYSTEM.md.deprecated`; create new |
| `CHANGELOG.md` | Append entry (if exists) |
