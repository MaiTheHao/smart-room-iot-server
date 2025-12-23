package com.iviet.ivshs.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.iviet.ivshs.dto.PowerConsumptionDtoV1;
import com.iviet.ivshs.dto.CreatePowerConsumptionDtoV1;
import com.iviet.ivshs.dto.UpdatePowerConsumptionDtoV1;
import com.iviet.ivshs.entities.PowerConsumptionV1;
import com.iviet.ivshs.entities.PowerConsumptionLanV1;
import com.iviet.ivshs.annotation.IgnoreAuditFields;

@Mapper(componentModel = "spring")
public interface PowerConsumptionMapperV1 {

	@Mapping(target = "id", source = "entity.id")
	@Mapping(target = "name", source = "sensorLan.name")
	@Mapping(target = "description", source = "sensorLan.description")
	@Mapping(target = "isActive", source = "entity.isActive")
	@Mapping(target = "currentWatt", source = "entity.currentWatt")
	@Mapping(target = "currentWattHour", source = "entity.currentWattHour")
	@Mapping(target = "roomId", source = "entity.room.id")
	PowerConsumptionDtoV1 toDto(PowerConsumptionV1 entity, PowerConsumptionLanV1 sensorLan);

	@IgnoreAuditFields
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "sensorLans", ignore = true)
	@Mapping(target = "consumptionValues", ignore = true)
	@Mapping(target = "deviceControl", ignore = true)
	@Mapping(target = "room", ignore = true)
	PowerConsumptionV1 toEntity(PowerConsumptionDtoV1 dto);

	@IgnoreAuditFields
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "sensorLans", ignore = true)
	@Mapping(target = "currentWatt", ignore = true)
	@Mapping(target = "currentWattHour", ignore = true)
	@Mapping(target = "consumptionValues", ignore = true)
	@Mapping(target = "deviceControl", ignore = true)
	@Mapping(target = "room", ignore = true)
	PowerConsumptionV1 fromCreateDto(CreatePowerConsumptionDtoV1 dto);

	@IgnoreAuditFields
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "sensorLans", ignore = true)
	@Mapping(target = "currentWatt", ignore = true)
	@Mapping(target = "currentWattHour", ignore = true)
	@Mapping(target = "consumptionValues", ignore = true)
	@Mapping(target = "deviceControl", ignore = true)
	@Mapping(target = "room", ignore = true)
	PowerConsumptionV1 fromUpdateDto(UpdatePowerConsumptionDtoV1 dto);
}
