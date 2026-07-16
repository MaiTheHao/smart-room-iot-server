package com.iviet.ivshs.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.SensorMetadataDao;
import com.iviet.ivshs.dto.SensorMetadataDto;
import com.iviet.ivshs.service.SensorMetadataService;
import com.iviet.ivshs.service.strategy.SensorMetadataServiceStrategy;
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
            return strategies.get(category).getSensorByRoomId(roomId);
        }

        return strategies.values().stream()
            .flatMap(strategy -> strategy.getSensorByRoomId(roomId).stream())
            .collect(Collectors.toList());
    }

    @Override
    public List<SensorMetadataDto> getAll(DeviceCategory category) {
        if (category != null) {
            validateCategory(category);
            return strategies.get(category).getAllSensor();
        }

        return strategies.values().stream()
            .flatMap(strategy -> strategy.getAllSensor().stream())
            .collect(Collectors.toList());
    }

    @Override
    public Long getCountByRoomId(Long roomId) {
        return sensorMetadataDao.countByRoomId(roomId);
    }

    @Override
    public SensorMetadataDto getSensorById(Long id, DeviceCategory category) {
        if (category == null) {
            throw new BadRequestException("Category is required");
        }
        validateCategory(category);
        return strategies.get(category).getSensorById(id);
    }

    @Override
    public SensorMetadataDto getSensorByNaturalId(String naturalId, DeviceCategory category) {
        if (category == null) {
            throw new BadRequestException("Category is required");
        }
        validateCategory(category);
        return strategies.get(category).getSensorByNaturalId(naturalId);
    }

    private void validateCategory(DeviceCategory category) {
        if (category != null && !strategies.containsKey(category)) {
            throw new BadRequestException("Invalid sensor category: " + category);
        }
    }
}
