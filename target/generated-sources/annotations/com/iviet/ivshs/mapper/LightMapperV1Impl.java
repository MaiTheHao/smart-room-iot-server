package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateLightDtoV1;
import com.iviet.ivshs.dto.LightDtoV1;
import com.iviet.ivshs.entities.LightLanV1;
import com.iviet.ivshs.entities.LightV1;
import com.iviet.ivshs.entities.RoomV1;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-23T18:46:42+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251001-1143, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class LightMapperV1Impl implements LightMapperV1 {

    @Override
    public LightDtoV1 toDto(LightV1 entity, LightLanV1 lightLan) {
        if ( entity == null && lightLan == null ) {
            return null;
        }

        LightDtoV1.LightDtoV1Builder lightDtoV1 = LightDtoV1.builder();

        if ( entity != null ) {
            lightDtoV1.id( entity.getId() );
            lightDtoV1.naturalId( entity.getNaturalId() );
            lightDtoV1.isActive( entity.getIsActive() );
            lightDtoV1.level( entity.getLevel() );
            lightDtoV1.roomId( entityRoomId( entity ) );
        }
        if ( lightLan != null ) {
            lightDtoV1.name( lightLan.getName() );
            lightDtoV1.description( lightLan.getDescription() );
        }

        return lightDtoV1.build();
    }

    @Override
    public LightV1 toEntity(LightDtoV1 dto) {
        if ( dto == null ) {
            return null;
        }

        LightV1 lightV1 = new LightV1();

        lightV1.setNaturalId( dto.naturalId() );
        lightV1.setIsActive( dto.isActive() );
        lightV1.setLevel( dto.level() );

        return lightV1;
    }

    @Override
    public LightV1 fromCreateDto(CreateLightDtoV1 dto) {
        if ( dto == null ) {
            return null;
        }

        LightV1 lightV1 = new LightV1();

        lightV1.setNaturalId( dto.naturalId() );
        lightV1.setIsActive( dto.isActive() );
        lightV1.setLevel( dto.level() );

        return lightV1;
    }

    private Long entityRoomId(LightV1 lightV1) {
        if ( lightV1 == null ) {
            return null;
        }
        RoomV1 room = lightV1.getRoom();
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
