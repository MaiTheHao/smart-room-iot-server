package com.iviet.ivshs.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.iviet.ivshs.dto.CreateDeviceControlDto;
import com.iviet.ivshs.dto.DeviceControlDto;
import com.iviet.ivshs.dto.UpdateDeviceControlDto;
import com.iviet.ivshs.entities.DeviceControl;
import com.iviet.ivshs.annotation.IgnoreAuditFields;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeviceControlMapperV1 {

    @Mapping(target = "clientId", source = "entity.client.id")
    @Mapping(target = "roomId", source = "entity.room.id")
    DeviceControlDto toDto(DeviceControl entity);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "light", ignore = true)
    @Mapping(target = "temperature", ignore = true)
    @Mapping(target = "powerConsumption", ignore = true)
    DeviceControl toEntity(CreateDeviceControlDto dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "light", ignore = true)
    @Mapping(target = "temperature", ignore = true)
    @Mapping(target = "powerConsumption", ignore = true)
    void updateEntityFromDto(UpdateDeviceControlDto dto, @MappingTarget DeviceControl entity);

    List<DeviceControlDto> toListDto(List<DeviceControl> entities);
}
