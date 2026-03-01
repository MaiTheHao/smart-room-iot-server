package com.iviet.ivshs.entities.converter;

import com.iviet.ivshs.enumeration.ConditionOperator;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ConditionOperatorConverter implements AttributeConverter<ConditionOperator, String> {

    @Override
    public String convertToDatabaseColumn(ConditionOperator attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getSymbol();
    }

    @Override
    public ConditionOperator convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ConditionOperator.fromSymbol(dbData);
    }
}
