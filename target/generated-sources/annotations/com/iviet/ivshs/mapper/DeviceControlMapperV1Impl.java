package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateDeviceControlDtoV1;
import com.iviet.ivshs.dto.DeviceControlDtoV1;
import com.iviet.ivshs.dto.UpdateDeviceControlDtoV1;
import com.iviet.ivshs.entities.ClientV1;
import com.iviet.ivshs.entities.DeviceControlV1;
import com.iviet.ivshs.entities.RoomV1;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-23T10:55:35+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class DeviceControlMapperV1Impl implements DeviceControlMapperV1 {

    @Override
    public DeviceControlDtoV1 toDto(DeviceControlV1 entity) {
        if ( entity == null ) {
            return null;
        }

        DeviceControlDtoV1.DeviceControlDtoV1Builder deviceControlDtoV1 = DeviceControlDtoV1.builder();

        deviceControlDtoV1.clientId( entityClientId( entity ) );
        deviceControlDtoV1.roomId( entityRoomId( entity ) );
        deviceControlDtoV1.id( entity.getId() );
        deviceControlDtoV1.deviceControlType( entity.getDeviceControlType() );
        deviceControlDtoV1.gpioPin( entity.getGpioPin() );
        deviceControlDtoV1.bleMacAddress( entity.getBleMacAddress() );
        deviceControlDtoV1.apiEndpoint( entity.getApiEndpoint() );

        return deviceControlDtoV1.build();
    }

    @Override
    public DeviceControlV1 toEntity(CreateDeviceControlDtoV1 dto) {
        if ( dto == null ) {
            return null;
        }

        DeviceControlV1 deviceControlV1 = new DeviceControlV1();

        deviceControlV1.setDeviceControlType( dto.getDeviceControlType() );
        deviceControlV1.setGpioPin( dto.getGpioPin() );
        deviceControlV1.setBleMacAddress( dto.getBleMacAddress() );
        deviceControlV1.setApiEndpoint( dto.getApiEndpoint() );

        return deviceControlV1;
    }

    @Override
    public void updateEntityFromDto(UpdateDeviceControlDtoV1 dto, DeviceControlV1 entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getDeviceControlType() != null ) {
            entity.setDeviceControlType( dto.getDeviceControlType() );
        }
        if ( dto.getGpioPin() != null ) {
            entity.setGpioPin( dto.getGpioPin() );
        }
        if ( dto.getBleMacAddress() != null ) {
            entity.setBleMacAddress( dto.getBleMacAddress() );
        }
        if ( dto.getApiEndpoint() != null ) {
            entity.setApiEndpoint( dto.getApiEndpoint() );
        }
    }

    @Override
    public List<DeviceControlDtoV1> toListDto(List<DeviceControlV1> entities) {
        if ( entities == null ) {
            return null;
        }

        List<DeviceControlDtoV1> list = new ArrayList<DeviceControlDtoV1>( entities.size() );
        for ( DeviceControlV1 deviceControlV1 : entities ) {
            list.add( toDto( deviceControlV1 ) );
        }

        return list;
    }

    private Long entityClientId(DeviceControlV1 deviceControlV1) {
        if ( deviceControlV1 == null ) {
            return null;
        }
        ClientV1 client = deviceControlV1.getClient();
        if ( client == null ) {
            return null;
        }
        Long id = client.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long entityRoomId(DeviceControlV1 deviceControlV1) {
        if ( deviceControlV1 == null ) {
            return null;
        }
        RoomV1 room = deviceControlV1.getRoom();
        if ( room == null ) {
            return null;
        }
        Long id = room.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
