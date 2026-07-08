package com.iviet.ivshs.service.control.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iviet.ivshs.dao.DeviceMetadataDao;
import com.iviet.ivshs.service.control.DeviceMetadataService;
import com.iviet.ivshs.service.control.DeviceMetadataServiceStrategy;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.shared.exception.BadRequestException;

@Service
@Transactional(readOnly = true)
public class DeviceMetadataServiceImpl implements DeviceMetadataService {

    private final DeviceMetadataDao deviceMetadataDao;
    private final Map<DeviceCategory, DeviceMetadataServiceStrategy> strategies;

    public DeviceMetadataServiceImpl(DeviceMetadataDao deviceMetadataDao, List<DeviceMetadataServiceStrategy> strategyList) {
        this.deviceMetadataDao = deviceMetadataDao;
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(DeviceMetadataServiceStrategy::getSupportedCategory, strategy -> strategy));
    }

    @Override
    public List<Object> getAllByRoomId(Long roomId, DeviceCategory category) {
        if (category != null) {
            validateCategory(category);
            return new ArrayList<>(strategies.get(category).getDeviceByRoomId(roomId));
        }

        return strategies.values().stream()
            .flatMap(strategy -> strategy.getDeviceByRoomId(roomId).stream())
            .collect(Collectors.toList());
    }

    @Override
    public List<Object> getAll(DeviceCategory category) {
        if (category != null) {
            validateCategory(category);
            return new ArrayList<>(strategies.get(category).getDeviceAll());
        }

        return strategies.values().stream()
            .flatMap(strategy -> strategy.getDeviceAll().stream())
            .collect(Collectors.toList());
    }

    @Override
    public Long getCountByRoomId(Long roomId) {
        return deviceMetadataDao.countByRoomId(roomId);
    }

    private void validateCategory(DeviceCategory category) {
        if (!strategies.containsKey(category)) {
            throw new BadRequestException("Invalid device category: " + category);
        }
    }
}
