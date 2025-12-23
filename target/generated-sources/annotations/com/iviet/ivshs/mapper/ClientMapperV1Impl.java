package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.ClientDtoV1;
import com.iviet.ivshs.dto.CreateClientDtoV1;
import com.iviet.ivshs.dto.UpdateClientDtoV1;
import com.iviet.ivshs.entities.ClientV1;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-23T10:55:35+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class ClientMapperV1Impl implements ClientMapperV1 {

    @Override
    public ClientDtoV1 toDto(ClientV1 entity) {
        if ( entity == null ) {
            return null;
        }

        ClientDtoV1.ClientDtoV1Builder clientDtoV1 = ClientDtoV1.builder();

        clientDtoV1.id( entity.getId() );
        clientDtoV1.username( entity.getUsername() );
        clientDtoV1.clientType( entity.getClientType() );
        clientDtoV1.ipAddress( entity.getIpAddress() );
        clientDtoV1.macAddress( entity.getMacAddress() );
        clientDtoV1.avatarUrl( entity.getAvatarUrl() );
        clientDtoV1.lastLoginAt( entity.getLastLoginAt() );

        return clientDtoV1.build();
    }

    @Override
    public ClientV1 toEntity(CreateClientDtoV1 dto) {
        if ( dto == null ) {
            return null;
        }

        ClientV1 clientV1 = new ClientV1();

        clientV1.setUsername( dto.getUsername() );
        clientV1.setClientType( dto.getClientType() );
        clientV1.setIpAddress( dto.getIpAddress() );
        clientV1.setMacAddress( dto.getMacAddress() );
        clientV1.setAvatarUrl( dto.getAvatarUrl() );

        return clientV1;
    }

    @Override
    public void updateEntityFromDto(UpdateClientDtoV1 dto, ClientV1 entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getUsername() != null ) {
            entity.setUsername( dto.getUsername() );
        }
        if ( dto.getClientType() != null ) {
            entity.setClientType( dto.getClientType() );
        }
        if ( dto.getIpAddress() != null ) {
            entity.setIpAddress( dto.getIpAddress() );
        }
        if ( dto.getMacAddress() != null ) {
            entity.setMacAddress( dto.getMacAddress() );
        }
        if ( dto.getAvatarUrl() != null ) {
            entity.setAvatarUrl( dto.getAvatarUrl() );
        }
    }
}
