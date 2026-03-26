package com.airnest.backend.listing.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ListingStatusConverter implements AttributeConverter<ListingStatus, String> {

    @Override
    public String convertToDatabaseColumn(ListingStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ListingStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ListingStatus.fromValue(dbData);
    }
}
