package com.iviet.ivshs.service.control.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.SensorMetadataDao;
import com.iviet.ivshs.dto.sensor.SensorMetadataDto;
import com.iviet.ivshs.service.control.SensorMetadataService;
import com.iviet.ivshs.service.control.SensorMetadataServiceStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;

@Service
@Transactional(readOnly = true)
public class SensorMetadataServiceImpl implements SensorMetadataService {

    private final SensorMetadataDao sensorMetadataDao;
    private final Map<DeviceCategory, SensorMetadataServiceStrategy> strategies;

    public SensorMetadataServiceImpl(SensorMetadataDao sensorMetadataDao, List<SensorMetadataServiceStrategy> strategyList) {
        this.sensorMetadataDao = sensorMetadataDao;
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(SensorMetadataServiceStrategy::getSupportedCategory, strategy -> strategy));
    }

    @Override
    public List<SensorMetadataDto> getAllByRoomId(Long roomId, DeviceCategory category) {
        if (category != null) {
            validateCategory(category);
            return strategies.get(category).getMetadataByRoomId(roomId);
        }

        return strategies.values().stream()
            .flatMap(strategy -> strategy.getMetadataByRoomId(roomId).stream())
            .collect(Collectors.toList());
    }

    @Override
    public List<SensorMetadataDto> getAll(DeviceCategory category) {
        if (category != null) {
            validateCategory(category);
            return strategies.get(category).getMetadataAll();
        }

        return strategies.values().stream()
            .flatMap(strategy -> strategy.getMetadataAll().stream())
            .collect(Collectors.toList());
    }

    @Override
    public Long getCountByRoomId(Long roomId) {
        return sensorMetadataDao.countByRoomId(roomId);
    }

    private void validateCategory(DeviceCategory category) {
        if (category != null && !strategies.containsKey(category)) {
            throw new BadRequestException("Invalid sensor category: " + category);
        }
    }
}
