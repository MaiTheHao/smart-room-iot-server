# DTO Projection Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor all DTO projection fields from hardcoded JPQL strings in DAO into static methods `jpqlProjection(...)` in DTO classes, eliminating duplication and centralizing projection definitions.

**Architecture:** Each DTO gets a `public static String jpqlProjection(...)` method with explicit alias parameters. Each DAO replaces hardcoded projection fields with a call to `XxxDto.jpqlProjection(alias1, alias2, ...)`. DTO_CLASS constants remain unchanged but are standardized to `private static final`.

**Tech Stack:** Java 21, Spring Boot, JPA/Hibernate, Lombok

## Global Constraints
- All DTO_CLASS constants must use `XxxDto.class.getName()` — no hardcoded strings
- All new static methods must use `String.formatted()` (Java 13+)
- Method parameter names must follow convention: `{entity}Alias`, `{entity}LangAlias`
- Must not change DTO constructors or business logic
- `mvn compile` must pass after each task

---

### Task 1: LightDto + LightDao

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/dto/LightDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/LightDao.java`

**Interfaces:**
- Produces: `LightDto.jpqlProjection(String lightAlias, String lightLangAlias)` → String

- [ ] **Step 1: Add static method to LightDto**

Add after the record declaration (before closing brace), after the `from` method:

```java
public static String jpqlProjection(String lightAlias, String lightLangAlias) {
    return "%s.id, %s.naturalId, %s.name, %s.description, %s.isActive, %s.power, %s.specificType, %s.level, %s.room.id, %s.hardwareConfig.id"
        .formatted(
            lightAlias, lightAlias, lightLangAlias, lightLangAlias,
            lightAlias, lightAlias, lightAlias, lightAlias,
            lightAlias, lightAlias
        );
}
```

- [ ] **Step 2: Refactor LightDao — replace all 7 hardcoded projections**

In LightDao.java, find all 7 methods (`findByNaturalId`, `findById`, `findAll(int, int, String)`, `findAll(String)`, `findAllByRoomId(Long, int, int, String)`, `findAllByRoomId(Long, String)`, `findByRoomAndNaturalId`) and replace the projection part.

Each method currently looks like:
```java
SELECT new %s(l.id, l.naturalId, ll.name, ll.description, l.isActive, l.power, l.specificType, l.level, l.room.id, l.hardwareConfig.id)
```

Change to:
```java
SELECT new %s(%s) FROM Light l ...
    .formatted(DTO_CLASS, LightDto.jpqlProjection("l", "ll"));
```

- [ ] **Step 3: Compile and verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dto/LightDto.java src/main/java/com/iviet/ivshs/dao/LightDao.java
git commit -m "refactor: extract LightDto projection to static method"
```

---

### Task 2: FanDto + FanDao

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/dto/FanDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/FanDao.java`

**Interfaces:**
- Produces: `FanDto.jpqlProjection(String fanAlias, String fanLangAlias)` → String

- [ ] **Step 1: Add static method to FanDto**

```java
public static String jpqlProjection(String fanAlias, String fanLangAlias) {
    return "%s.id, %s.naturalId, %s.name, %s.description, %s.isActive, %s.room.id, %s.power, %s.specificType, %s.duration, %s.speed, %s.mode, %s.light, %s.swing, %s.hardwareConfig.id"
        .formatted(
            fanAlias, fanAlias, fanLangAlias, fanLangAlias,
            fanAlias, fanAlias, fanAlias, fanAlias,
            fanAlias, fanAlias, fanAlias, fanAlias,
            fanAlias, fanAlias
        );
}
```

- [ ] **Step 2: Update FanDao — replace all 7 hardcoded projections**

Replace `SELECT new %s(f.id, f.naturalId, tl.name, tl.description, ...)` with `FanDto.jpqlProjection("f", "tl")` in all 7 methods.

- [ ] **Step 3: Compile and verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dto/FanDto.java src/main/java/com/iviet/ivshs/dao/FanDao.java
git commit -m "refactor: extract FanDto projection to static method"
```

---

### Task 3: AirConditionDto + AirConditionDao

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/dto/AirConditionDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/AirConditionDao.java`

**Interfaces:**
- Produces: `AirConditionDto.jpqlProjection(String acAlias, String acLangAlias)` → String

- [ ] **Step 1: Add static method to AirConditionDto**

```java
public static String jpqlProjection(String acAlias, String acLangAlias) {
    return "%s.id, %s.naturalId, %s.name, %s.description, %s.isActive, %s.room.id, %s.power, %s.specificType, %s.duration, %s.temperature, %s.mode, %s.fanSpeed, %s.swing, %s.hardwareConfig.id"
        .formatted(
            acAlias, acAlias, acLangAlias, acLangAlias,
            acAlias, acAlias, acAlias, acAlias,
            acAlias, acAlias, acAlias, acAlias,
            acAlias, acAlias
        );
}
```

- [ ] **Step 2: Update AirConditionDao — replace all 7 hardcoded projections**

Replace `SELECT new %s(ac.id, ac.naturalId, tl.name, tl.description, ...)` with `AirConditionDto.jpqlProjection("ac", "tl")` in all 7 methods.

- [ ] **Step 3: Compile and verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dto/AirConditionDto.java src/main/java/com/iviet/ivshs/dao/AirConditionDao.java
git commit -m "refactor: extract AirConditionDto projection to static method"
```

---

### Task 4: TemperatureDto + TemperatureDao

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/dto/TemperatureDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/TemperatureDao.java`

**Interfaces:**
- Produces: `TemperatureDto.jpqlProjection(String tempAlias, String tempLangAlias)` → String

- [ ] **Step 1: Add static method to TemperatureDto**

```java
public static String jpqlProjection(String tempAlias, String tempLangAlias) {
    return "%s.id, %s.name, %s.description, %s.isActive, %s.currentValue, %s.naturalId, %s.room.id, %s.hardwareConfig.id"
        .formatted(
            tempAlias, tempLangAlias, tempLangAlias,
            tempAlias, tempAlias, tempAlias,
            tempAlias, tempAlias
        );
}
```

- [ ] **Step 2: Update TemperatureDao — replace all 7 hardcoded projections**

Replace `SELECT new %s(t.id, tl.name, tl.description, ...)` with `TemperatureDto.jpqlProjection("t", "tl")` in all 7 methods.

- [ ] **Step 3: Compile and verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dto/TemperatureDto.java src/main/java/com/iviet/ivshs/dao/TemperatureDao.java
git commit -m "refactor: extract TemperatureDto projection to static method"
```

---

### Task 5: PowerConsumptionDto + PowerConsumptionDao

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/dto/PowerConsumptionDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/PowerConsumptionDao.java`

**Interfaces:**
- Produces: `PowerConsumptionDto.jpqlProjection(String pcAlias, String pcLangAlias)` → String

- [ ] **Step 1: Add static method to PowerConsumptionDto**

```java
public static String jpqlProjection(String pcAlias, String pcLangAlias) {
    return "%s.id, %s.name, %s.description, %s.isActive, %s.currentWatt, %s.naturalId, %s.room.id, %s.hardwareConfig.id"
        .formatted(
            pcAlias, pcLangAlias, pcLangAlias,
            pcAlias, pcAlias, pcAlias,
            pcAlias, pcAlias
        );
}
```

- [ ] **Step 2: Update PowerConsumptionDao — replace all 7 hardcoded projections**

Replace `SELECT new %s(pc.id, pcl.name, pcl.description, ...)` with `PowerConsumptionDto.jpqlProjection("pc", "pcl")` in all 7 methods.

- [ ] **Step 3: Compile and verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dto/PowerConsumptionDto.java src/main/java/com/iviet/ivshs/dao/PowerConsumptionDao.java
git commit -m "refactor: extract PowerConsumptionDto projection to static method"
```

---

### Task 6: RoomDto + RoomDeviceCountDto + RoomDao

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/dto/RoomDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dto/RoomDeviceCountDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/RoomDao.java`

**Interfaces:**
- Produces: `RoomDto.jpqlProjection(String roomAlias, String roomLangAlias)` → String
- Produces: `RoomDeviceCountDto.jpqlProjection(String roomAlias)` → String

- [ ] **Step 1: Add static method to RoomDto**

```java
public static String jpqlProjection(String roomAlias, String roomLangAlias) {
    return "%s.id, %s.code, %s.name, %s.description, %s.floor.id, %s.version"
        .formatted(roomAlias, roomAlias, roomLangAlias, roomLangAlias, roomAlias, roomAlias);
}
```

- [ ] **Step 2: Add static method to RoomDeviceCountDto**

```java
public static String jpqlProjection(String roomAlias) {
    return "%s.id, (SELECT COUNT(l) FROM Light l WHERE l.room.id = %s.id), (SELECT COUNT(ac) FROM AirCondition ac WHERE ac.room.id = %s.id), (SELECT COUNT(f) FROM Fan f WHERE f.room.id = %s.id)"
        .formatted(roomAlias, roomAlias, roomAlias, roomAlias);
}
```

- [ ] **Step 3: Update RoomDao — replace all 6 RoomDto projections + 1 RoomDeviceCountDto projection**

Replace `SELECT new %s(r.id, r.code, rlan.name, rlan.description, ...)` with `RoomDto.jpqlProjection("r", "rlan")` in all 6 methods (`findByCode`, `findById`, `findAllByFloorId` × 2, `findAll` × 2).
Replace `SELECT new %s(r.id, ...)` with `RoomDeviceCountDto.jpqlProjection("r")` in `getDeviceCountsByRoomIds`.

- [ ] **Step 4: Compile and verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dto/RoomDto.java src/main/java/com/iviet/ivshs/dto/RoomDeviceCountDto.java src/main/java/com/iviet/ivshs/dao/RoomDao.java
git commit -m "refactor: extract RoomDto and RoomDeviceCountDto projection to static methods"
```

---

### Task 7: SysFunctionDto + SysFunctionWithGroupStatusDto + GroupPermissionMapping + SysFunctionDao

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/dto/SysFunctionDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dto/SysFunctionWithGroupStatusDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dto/GroupPermissionMapping.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/SysFunctionDao.java`

**Interfaces:**
- Produces: `SysFunctionDto.jpqlProjection(String funcAlias, String funcLangAlias)` → String
- Produces: `SysFunctionWithGroupStatusDto.jpqlProjection(String funcAlias, String funcLangAlias, String roleAlias)` → String
- Produces: `GroupPermissionMapping.jpqlProjection(String groupAlias, String funcAlias)` → String

- [ ] **Step 1: Add static method to SysFunctionDto**

```java
public static String jpqlProjection(String funcAlias, String funcLangAlias) {
    return "%s.id, %s.functionCode, %s.name, %s.description"
        .formatted(funcAlias, funcAlias, funcLangAlias, funcLangAlias);
}
```

- [ ] **Step 2: Add static method to SysFunctionWithGroupStatusDto**

```java
public static String jpqlProjection(String funcAlias, String funcLangAlias, String roleAlias) {
    return "%s.id, %s.functionCode, %s.name, %s.description, CASE WHEN %s.id IS NOT NULL THEN true ELSE false END, %s.id"
        .formatted(funcAlias, funcAlias, funcLangAlias, funcLangAlias, roleAlias, roleAlias);
}
```

- [ ] **Step 3: Add static method to GroupPermissionMapping**

```java
public static String jpqlProjection(String groupAlias, String funcAlias) {
    return "%s.id, %s.functionCode"
        .formatted(groupAlias, funcAlias);
}
```

- [ ] **Step 4: Update SysFunctionDao — replace all 13 hardcoded projections**

Replace all hardcoded projections with calls to respective static methods:
- 10 SysFunctionDto methods → `SysFunctionDto.jpqlProjection("f", "flan")`
- 2 SysFunctionWithGroupStatusDto methods → `SysFunctionWithGroupStatusDto.jpqlProjection("f", "flan", "r")`
- 1 GroupPermissionMapping method → `GroupPermissionMapping.jpqlProjection("g", "f")`

- [ ] **Step 5: Compile and verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dto/SysFunctionDto.java src/main/java/com/iviet/ivshs/dto/SysFunctionWithGroupStatusDto.java src/main/java/com/iviet/ivshs/dto/GroupPermissionMapping.java src/main/java/com/iviet/ivshs/dao/SysFunctionDao.java
git commit -m "refactor: extract SysFunctionDto, SysFunctionWithGroupStatusDto, GroupPermissionMapping projection to static methods"
```

---

### Task 8: SysGroupDto + SysGroupWithClientStatusDto + ClientDto + SysGroupDao

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/dto/SysGroupDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dto/SysGroupWithClientStatusDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dto/ClientDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/SysGroupDao.java`

**Interfaces:**
- Produces: `SysGroupDto.jpqlProjection(String groupAlias, String groupLangAlias)` → String
- Produces: `SysGroupWithClientStatusDto.jpqlProjection(String groupAlias, String groupLangAlias, String clientAlias)` → String
- Produces: `ClientDto.jpqlProjection(String clientAlias)` → String

- [ ] **Step 1: Add static method to SysGroupDto**

```java
public static String jpqlProjection(String groupAlias, String groupLangAlias) {
    return "%s.id, %s.groupCode, %s.name, %s.description"
        .formatted(groupAlias, groupAlias, groupLangAlias, groupLangAlias);
}
```

- [ ] **Step 2: Add static method to SysGroupWithClientStatusDto**

```java
public static String jpqlProjection(String groupAlias, String groupLangAlias, String clientAlias) {
    return "%s.id, %s.groupCode, %s.name, %s.description, CASE WHEN %s.id IS NOT NULL THEN true ELSE false END"
        .formatted(groupAlias, groupAlias, groupLangAlias, groupLangAlias, clientAlias);
}
```

- [ ] **Step 3: Add static method to ClientDto**

```java
public static String jpqlProjection(String clientAlias) {
    return "%s.id, %s.username, %s.clientType, %s.ipAddress, %s.macAddress, %s.avatarUrl, %s.lastLoginAt, %s.gatewayPassword"
        .formatted(clientAlias, clientAlias, clientAlias, clientAlias, clientAlias, clientAlias, clientAlias, clientAlias);
}
```

- [ ] **Step 4: Update SysGroupDao — replace all 8 hardcoded projections**

Replace all hardcoded projections with calls to respective static methods.

- [ ] **Step 5: Compile and verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dto/SysGroupDto.java src/main/java/com/iviet/ivshs/dto/SysGroupWithClientStatusDto.java src/main/java/com/iviet/ivshs/dto/ClientDto.java src/main/java/com/iviet/ivshs/dao/SysGroupDao.java
git commit -m "refactor: extract SysGroupDto, SysGroupWithClientStatusDto, ClientDto projection to static methods"
```

---

### Task 9: FloorDto + FloorDao

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/dto/FloorDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/FloorDao.java`

**Interfaces:**
- Produces: `FloorDto.jpqlProjection(String floorAlias, String floorLangAlias)` → String

- [ ] **Step 1: Add static method to FloorDto**

```java
public static String jpqlProjection(String floorAlias, String floorLangAlias) {
    return "%s.id, %s.name, %s.code, %s.description, %s.level, %s.version"
        .formatted(floorAlias, floorLangAlias, floorAlias, floorLangAlias, floorAlias, floorAlias);
}
```

- [ ] **Step 2: Update FloorDao — replace all 4 hardcoded projections**

Replace `SELECT new %s(f.id, flan.name, f.code, flan.description, ...)` with `FloorDto.jpqlProjection("f", "flan")` in all 4 methods.

- [ ] **Step 3: Compile and verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dto/FloorDto.java src/main/java/com/iviet/ivshs/dao/FloorDao.java
git commit -m "refactor: extract FloorDto projection to static method"
```

---

### Task 10: AverageTemperatureValueDto + TemperatureValueDao

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/dto/AverageTemperatureValueDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/TemperatureValueDao.java`

**Interfaces:**
- Produces: `AverageTemperatureValueDto.jpqlProjection(String tvAlias)` → String

- [ ] **Step 1: Add static method to AverageTemperatureValueDto**

```java
public static String jpqlProjection(String tvAlias) {
    return "(%s.unixMinute - MOD(%s.unixMinute, :divisor)) * 60L, AVG(%s.tempC)"
        .formatted(tvAlias, tvAlias, tvAlias);
}
```

- [ ] **Step 2: Update TemperatureValueDao — replace all 4 hardcoded projections**

Replace `SELECT new %s((tv.unixMinute - MOD(tv.unixMinute, :divisor)) * 60L, AVG(tv.tempC))` with `AverageTemperatureValueDto.jpqlProjection("tv")` in all 4 methods.

Also add a `DTO_CLASS` constant:
```java
private static final String DTO_CLASS = AverageTemperatureValueDto.class.getName();
```
And replace the inline `.formatted(AverageTemperatureValueDto.class.getName())` with `.formatted(DTO_CLASS)`.

- [ ] **Step 3: Compile and verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dto/AverageTemperatureValueDto.java src/main/java/com/iviet/ivshs/dao/TemperatureValueDao.java
git commit -m "refactor: extract AverageTemperatureValueDto projection to static method"
```

---

### Task 11: EnergyMetricDto + EnergyMetricDao

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/dto/EnergyMetricDto.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/EnergyMetricDao.java`

**Interfaces:**
- Produces: `EnergyMetricDto.jpqlProjection(String emAlias)` → String

- [ ] **Step 1: Add static method to EnergyMetricDto**

```java
public static String jpqlProjection(String emAlias) {
    return "%s.timestamp, %s.voltage, %s.current, %s.power, %s.energy, %s.frequency, %s.powerFactor"
        .formatted(emAlias, emAlias, emAlias, emAlias, emAlias, emAlias, emAlias);
}
```

- [ ] **Step 2: Update EnergyMetricDao — replace the 1 hardcoded projection**

In `findLatest`, replace `SELECT new %s(em.timestamp, em.voltage, em.current, em.power, em.energy, em.frequency, em.powerFactor)` with:
```java
SELECT new %s(%s) ...
    .formatted(DTO_CLASS, EnergyMetricDto.jpqlProjection("em"));
```

- [ ] **Step 3: Compile and verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dto/EnergyMetricDto.java src/main/java/com/iviet/ivshs/dao/EnergyMetricDao.java
git commit -m "refactor: extract EnergyMetricDto projection to static method"
```

---

### Task 12: Final Review — Standardize DTO_CLASS constants

**Files:**
- Modify: `src/main/java/com/iviet/ivshs/dao/FloorDao.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/RoomDao.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/SysFunctionDao.java`
- Modify: `src/main/java/com/iviet/ivshs/dao/SysGroupDao.java`

- [ ] **Step 1: FloorDao — add class-level DTO_CLASS and remove locals**

Add `private static final String DTO_CLASS = FloorDto.class.getName();` at class level. Remove all local `String dtoClassPath = ...` declarations.

- [ ] **Step 2: RoomDao — add class-level DTO_CLASS and remove locals**

Add `private static final String DTO_CLASS = RoomDto.class.getName();` at class level. Remove all local `String dtoPath = ...` declarations.

- [ ] **Step 3: SysFunctionDao — add class-level DTO_CLASS and remove locals**

Add `private static final String DTO_CLASS = SysFunctionDto.class.getName();` at class level. Remove all local `String dtoClassPath = ...` declarations.

- [ ] **Step 4: SysGroupDao — convert instance fields to static**

Change `private final String GROUP_DTO = ...` to `private static final String GROUP_DTO = ...`.
Change `private final String FUNC_DTO = ...` to `private static final String FUNC_DTO = ...`.
Change `private final String CLIENT_DTO = ...` to `private static final String CLIENT_DTO = ...`.

- [ ] **Step 5: Compile and verify**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/iviet/ivshs/dao/FloorDao.java src/main/java/com/iviet/ivshs/dao/RoomDao.java src/main/java/com/iviet/ivshs/dao/SysFunctionDao.java src/main/java/com/iviet/ivshs/dao/SysGroupDao.java
git commit -m "refactor: standardize DTO_CLASS constants to private static final"
```

---

### Task 13: Final Verification

- [ ] **Step 1: Global compile check**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: Verify no hardcoded JPQL projections remain**

```bash
rg "SELECT new %s\(" src/main/java/com/iviet/ivshs/dao/ | grep -v "jpqlProjection" | grep -v "//.*SELECT" || echo "No remaining hardcoded projections found"
```

Expected: No remaining hardcoded projections.

- [ ] **Step 3: Verify all static methods exist**

```bash
rg "public static String jpqlProjection" src/main/java/com/iviet/ivshs/dto/
```

Expected: All 16 DTOs have the method.

- [ ] **Step 4: Final commit (if any fixes needed)**

```bash
git add -A
git commit -m "chore: final review cleanup for DTO projection refactor"
```
