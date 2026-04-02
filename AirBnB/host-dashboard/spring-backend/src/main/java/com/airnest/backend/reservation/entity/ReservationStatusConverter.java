package com.airnest.backend.reservation.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ReservationStatusConverter implements AttributeConverter<ReservationStatus, String> {

    @Override
    public String convertToDatabaseColumn(ReservationStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ReservationStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ReservationStatus.fromValue(dbData);
    }
}
