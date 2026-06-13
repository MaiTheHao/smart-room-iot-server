package com.iviet.ivshs.service.control.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.iviet.ivshs.dto.fan.FanDto;
import com.iviet.ivshs.dto.aircondition.AirConditionDto;
import com.iviet.ivshs.dto.light.LightDto;
import com.iviet.ivshs.shared.enumeration.DeviceCategory;
import com.iviet.ivshs.service.control.DeviceMetadataService;
import com.iviet.ivshs.service.light.LightService;
import com.iviet.ivshs.service.aircondition.AirConditionService;
import com.iviet.ivshs.service.fan.FanService;
import com.iviet.ivshs.dao.DeviceMetadataDao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceMetadataServiceImpl implements DeviceMetadataService {

    private final DeviceMetadataDao deviceMetadataDao;
    private final LightService lightService;
    private final FanService fanService;
    private final AirConditionService airConditionService;

    @Override
    public List<Object> getAllByRoomId(Long roomId, DeviceCategory category) {
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();

        CompletableFuture<List<LightDto>> lightFuture = (category == null || category == DeviceCategory.LIGHT) ? CompletableFuture.supplyAsync(() -> {
            LocaleContextHolder.setLocaleContext(localeContext);
            try {
                return lightService.getAllByRoomId(roomId);
            } finally {
                LocaleContextHolder.resetLocaleContext();
            }
        }) : CompletableFuture.completedFuture(Collections.emptyList());

        CompletableFuture<List<FanDto>> fanFuture = (category == null || category == DeviceCategory.FAN) ? CompletableFuture.supplyAsync(() -> {
            LocaleContextHolder.setLocaleContext(localeContext);
            try {
                return fanService.getAllByRoomId(roomId);
            } finally {
                LocaleContextHolder.resetLocaleContext();
            }
        }) : CompletableFuture.completedFuture(Collections.emptyList());

        CompletableFuture<List<AirConditionDto>> acFuture = (category == null || category == DeviceCategory.AIR_CONDITION) ? CompletableFuture.supplyAsync(() -> {
            LocaleContextHolder.setLocaleContext(localeContext);
            try {
                return airConditionService.getAllByRoomId(roomId);
            } finally {
                LocaleContextHolder.resetLocaleContext();
            }
        }) : CompletableFuture.completedFuture(Collections.emptyList());

        return CompletableFuture.allOf(lightFuture, fanFuture, acFuture)
                .thenApply(v -> {
                    List<Object> all = new ArrayList<>();
                    all.addAll(lightFuture.join());
                    all.addAll(fanFuture.join());
                    all.addAll(acFuture.join());
                    return all;
                })
                .join();
    }

    @Override
    public List<Object> getAll(DeviceCategory category) {
        LocaleContext localeContext = LocaleContextHolder.getLocaleContext();

        CompletableFuture<List<LightDto>> lightFuture = (category == null || category == DeviceCategory.LIGHT) ? CompletableFuture.supplyAsync(() -> {
            LocaleContextHolder.setLocaleContext(localeContext);
            try {
                return lightService.getAll();
            } finally {
                LocaleContextHolder.resetLocaleContext();
            }
        }) : CompletableFuture.completedFuture(Collections.emptyList());

        CompletableFuture<List<FanDto>> fanFuture = (category == null || category == DeviceCategory.FAN) ? CompletableFuture.supplyAsync(() -> {
            LocaleContextHolder.setLocaleContext(localeContext);
            try {
                return fanService.getAll();
            } finally {
                LocaleContextHolder.resetLocaleContext();
            }
        }) : CompletableFuture.completedFuture(Collections.emptyList());

        CompletableFuture<List<AirConditionDto>> acFuture = (category == null || category == DeviceCategory.AIR_CONDITION) ? CompletableFuture.supplyAsync(() -> {
            LocaleContextHolder.setLocaleContext(localeContext);
            try {
                return airConditionService.getAll();
            } finally {
                LocaleContextHolder.resetLocaleContext();
            }
        }) : CompletableFuture.completedFuture(Collections.emptyList());

        return CompletableFuture.allOf(lightFuture, fanFuture, acFuture)
                .thenApply(v -> {
                    List<Object> all = new ArrayList<>();
                    all.addAll(lightFuture.join());
                    all.addAll(fanFuture.join());
                    all.addAll(acFuture.join());
                    return all;
                })
                .join();
    }

    @Override
    public Long getCountByRoomId(Long roomId) {
        return deviceMetadataDao.countByRoomId(roomId);
    }

}
