package com.iviet.ivshs.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.iviet.ivshs.dto.CreateLanguageDto;
import com.iviet.ivshs.dto.LanguageDto;
import com.iviet.ivshs.dto.UpdateLanguageDto;
import com.iviet.ivshs.entities.Language;
import com.iviet.ivshs.annotation.IgnoreAuditFields;

@Mapper(componentModel = "spring")
public interface LanguageMapperV1 {
    
    LanguageDto toDto(Language entity);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    Language toEntity(LanguageDto dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    Language fromCreateDto(CreateLanguageDto dto);

    @IgnoreAuditFields
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(@MappingTarget Language entity, UpdateLanguageDto dto);
}
