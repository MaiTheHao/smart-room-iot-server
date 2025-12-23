package com.iviet.ivshs.mapper;

import com.iviet.ivshs.dto.CreateLanguageDtoV1;
import com.iviet.ivshs.dto.LanguageDtoV1;
import com.iviet.ivshs.dto.UpdateLanguageDtoV1;
import com.iviet.ivshs.entities.LanguageV1;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-23T10:55:35+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class LanguageMapperV1Impl implements LanguageMapperV1 {

    @Override
    public LanguageDtoV1 toDto(LanguageV1 entity) {
        if ( entity == null ) {
            return null;
        }

        LanguageDtoV1.LanguageDtoV1Builder languageDtoV1 = LanguageDtoV1.builder();

        languageDtoV1.id( entity.getId() );
        languageDtoV1.code( entity.getCode() );
        languageDtoV1.name( entity.getName() );
        languageDtoV1.description( entity.getDescription() );

        return languageDtoV1.build();
    }

    @Override
    public LanguageV1 toEntity(LanguageDtoV1 dto) {
        if ( dto == null ) {
            return null;
        }

        LanguageV1 languageV1 = new LanguageV1();

        languageV1.setName( dto.name() );
        languageV1.setDescription( dto.description() );
        languageV1.setCode( dto.code() );

        return languageV1;
    }

    @Override
    public LanguageV1 fromCreateDto(CreateLanguageDtoV1 dto) {
        if ( dto == null ) {
            return null;
        }

        LanguageV1 languageV1 = new LanguageV1();

        languageV1.setName( dto.name() );
        languageV1.setDescription( dto.description() );
        languageV1.setCode( dto.code() );

        return languageV1;
    }

    @Override
    public void updateFromDto(LanguageV1 entity, UpdateLanguageDtoV1 dto) {
        if ( dto == null ) {
            return;
        }

        if ( dto.name() != null ) {
            entity.setName( dto.name() );
        }
        if ( dto.description() != null ) {
            entity.setDescription( dto.description() );
        }
        if ( dto.code() != null ) {
            entity.setCode( dto.code() );
        }
    }
}
