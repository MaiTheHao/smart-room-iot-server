# Humidity Metric Service Refactor Design Spec

## 1. Goal Description
Refactor the [HumidityMetricServiceImpl.java](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/java/com/iviet/ivshs/service/impl/HumidityMetricServiceImpl.java) class to implement a dedicated [HumidityMetricService.java](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/java/com/iviet/ivshs/service/HumidityMetricService.java) interface instead of implementing strategy interfaces inline.

## 2. Proposed Changes

### [NEW] [HumidityMetricService.java](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/java/com/iviet/ivshs/service/HumidityMetricService.java)
Create the `HumidityMetricService` interface in the `com.iviet.ivshs.service` package:
```java
package com.iviet.ivshs.service;

import com.iviet.ivshs.service.strategy.MetricServiceStrategy;
import com.iviet.ivshs.service.strategy.SensorTelemetryServiceStrategy;
import com.iviet.ivshs.service.strategy.TelemetryCRUDServiceStrategy;

/**
 * Service interface for handling Humidity Metrics, extending the platform's strategies.
 */
public interface HumidityMetricService extends TelemetryCRUDServiceStrategy, MetricServiceStrategy, SensorTelemetryServiceStrategy {
}
```

### [MODIFY] [HumidityMetricServiceImpl.java](file:///home/maithehao/Workspace/projects/smart-room-iot/smartroom_server/src/main/java/com/iviet/ivshs/service/impl/HumidityMetricServiceImpl.java)
Update class declaration to implement `HumidityMetricService` interface:
```java
public class HumidityMetricServiceImpl implements HumidityMetricService {
```

## 3. Verification Plan

### Automated Tests
- Build the project using Maven:
  ```bash
  mvn clean compile
  ```
- Run tests (if any) to verify there are no compilation errors or Spring injection issues.
