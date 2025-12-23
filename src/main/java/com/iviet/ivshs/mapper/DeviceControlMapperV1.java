package com.iviet.ivshs.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.iviet.ivshs.dto.CreateDeviceControlDtoV1;
import com.iviet.ivshs.dto.DeviceControlDtoV1;
import com.iviet.ivshs.dto.UpdateDeviceControlDtoV1;
import com.iviet.ivshs.entities.DeviceControlV1;
import com.iviet.ivshs.annotation.IgnoreAuditFields;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeviceControlMapperV1 {

    @Mapping(target = "clientId", source = "entity.client.id")
    @Mapping(target = "roomId", source = "entity.room.id")
    DeviceControlDtoV1 toDto(DeviceControlV1 entity);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "light", ignore = true)
    @Mapping(target = "temperature", ignore = true)
    @Mapping(target = "powerConsumption", ignore = true)
    DeviceControlV1 toEntity(CreateDeviceControlDtoV1 dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "light", ignore = true)
    @Mapping(target = "temperature", ignore = true)
    @Mapping(target = "powerConsumption", ignore = true)
    void updateEntityFromDto(UpdateDeviceControlDtoV1 dto, @MappingTarget DeviceControlV1 entity);

    List<DeviceControlDtoV1> toListDto(List<DeviceControlV1> entities);
}
