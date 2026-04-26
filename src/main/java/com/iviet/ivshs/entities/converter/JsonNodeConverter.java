package com.iviet.ivshs.entities.converter;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.iviet.ivshs.util.JsonUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Component
@Converter(autoApply = true)
@RequiredArgsConstructor
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {
  
  @Override
  public String convertToDatabaseColumn(JsonNode attribute) {
    return JsonUtil.stringify(attribute);
  }

  @Override
  public JsonNode convertToEntityAttribute(String dbData) {
    return JsonUtil.parse(dbData);
  }
  
}
