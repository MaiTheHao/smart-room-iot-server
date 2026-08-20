package com.iviet.ivshs.service;

import com.iviet.ivshs.entities.MotionDetector;
import com.iviet.ivshs.service.strategy.EventTelemetryStrategy;
import com.iviet.ivshs.service.strategy.SensorMetadataServiceStrategy;

public interface MotionMetricService extends EventTelemetryStrategy, SensorMetadataServiceStrategy<MotionDetector> {
}
