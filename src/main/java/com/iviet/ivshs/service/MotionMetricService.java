package com.iviet.ivshs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.entities.MotionDetector;
import com.iviet.ivshs.service.strategy.SensorMetadataServiceStrategy;

public interface MotionMetricService extends SensorMetadataServiceStrategy<MotionDetector> {
    void processMotionData(String naturalId, JsonNode data);
}
