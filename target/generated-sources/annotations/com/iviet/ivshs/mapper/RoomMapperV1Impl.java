package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateRoomDtoV1;
import com.iviet.ivshs.dto.RoomDtoV1;
import com.iviet.ivshs.entities.FloorV1;
import com.iviet.ivshs.entities.RoomLanV1;
import com.iviet.ivshs.entities.RoomV1;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-23T10:55:35+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class RoomMapperV1Impl implements RoomMapperV1 {

    @Override
    public RoomDtoV1 toDto(RoomV1 entity, RoomLanV1 roomLan) {
        if ( entity == null && roomLan == null ) {
            return null;
        }

        RoomDtoV1.RoomDtoV1Builder roomDtoV1 = RoomDtoV1.builder();

        if ( entity != null ) {
            roomDtoV1.id( entity.getId() );
            roomDtoV1.code( entity.getCode() );
            roomDtoV1.floorId( entityFloorId( entity ) );
        }
        if ( roomLan != null ) {
            roomDtoV1.name( roomLan.getName() );
            roomDtoV1.description( roomLan.getDescription() );
        }

        return roomDtoV1.build();
    }

    @Override
    public RoomV1 toEntity(RoomDtoV1 dto) {
        if ( dto == null ) {
            return null;
        }

        RoomV1 roomV1 = new RoomV1();

        roomV1.setCode( dto.code() );

        return roomV1;
    }

    @Override
    public RoomV1 fromCreateDto(CreateRoomDtoV1 dto) {
        if ( dto == null ) {
            return null;
        }

        RoomV1 roomV1 = new RoomV1();

        roomV1.setCode( dto.code() );

        return roomV1;
    }

    private Long entityFloorId(RoomV1 roomV1) {
        if ( roomV1 == null ) {
            return null;
        }
        FloorV1 floor = roomV1.getFloor();
        if ( floor == null ) {
            return null;
        }
        Long id = floor.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
