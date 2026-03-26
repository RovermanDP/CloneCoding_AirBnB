package com.airnest.backend.inbox.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MessageSenderConverter implements AttributeConverter<MessageSender, String> {

    @Override
    public String convertToDatabaseColumn(MessageSender attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public MessageSender convertToEntityAttribute(String dbData) {
        return dbData == null ? null : MessageSender.fromValue(dbData);
    }
}
