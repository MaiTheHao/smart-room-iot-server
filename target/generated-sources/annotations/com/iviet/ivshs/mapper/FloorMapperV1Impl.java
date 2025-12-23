package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateFloorDtoV1;
import com.iviet.ivshs.dto.FloorDtoV1;
import com.iviet.ivshs.entities.FloorLanV1;
import com.iviet.ivshs.entities.FloorV1;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-23T10:55:35+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class FloorMapperV1Impl implements FloorMapperV1 {

    @Override
    public FloorDtoV1 toDto(FloorV1 entity, FloorLanV1 floorLan) {
        if ( entity == null && floorLan == null ) {
            return null;
        }

        FloorDtoV1.FloorDtoV1Builder floorDtoV1 = FloorDtoV1.builder();

        if ( entity != null ) {
            floorDtoV1.id( entity.getId() );
            floorDtoV1.code( entity.getCode() );
            floorDtoV1.level( entity.getLevel() );
        }
        if ( floorLan != null ) {
            floorDtoV1.name( floorLan.getName() );
            floorDtoV1.description( floorLan.getDescription() );
        }

        return floorDtoV1.build();
    }

    @Override
    public FloorV1 toEntity(FloorDtoV1 dto) {
        if ( dto == null ) {
            return null;
        }

        FloorV1 floorV1 = new FloorV1();

        floorV1.setLevel( dto.level() );
        floorV1.setCode( dto.code() );

        return floorV1;
    }

    @Override
    public FloorV1 fromCreateDto(CreateFloorDtoV1 dto) {
        if ( dto == null ) {
            return null;
        }

        FloorV1 floorV1 = new FloorV1();

        floorV1.setLevel( dto.level() );
        floorV1.setCode( dto.code() );

        return floorV1;
    }
}
