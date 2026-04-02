package com.airnest.backend.inbox.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class InboxThreadStatusConverter implements AttributeConverter<InboxThreadStatus, String> {

    @Override
    public String convertToDatabaseColumn(InboxThreadStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public InboxThreadStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : InboxThreadStatus.fromValue(dbData);
    }
}
