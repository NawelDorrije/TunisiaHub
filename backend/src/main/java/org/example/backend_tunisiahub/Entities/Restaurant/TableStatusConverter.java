package org.example.backend_tunisiahub.Entities.Restaurant;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TableStatusConverter implements AttributeConverter<TableStatus, String> {

    @Override
    public String convertToDatabaseColumn(TableStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public TableStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return TableStatus.AVAILABLE;
        }

        String raw = dbData.trim();
        if (raw.matches("\\d+")) {
            int ordinal = Integer.parseInt(raw);
            TableStatus[] values = TableStatus.values();
            if (ordinal >= 0 && ordinal < values.length) {
                return values[ordinal];
            }
            return TableStatus.AVAILABLE;
        }

        try {
            return TableStatus.valueOf(raw.toUpperCase().replace('-', '_').replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            return TableStatus.AVAILABLE;
        }
    }
}
