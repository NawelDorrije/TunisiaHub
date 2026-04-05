package org.example.backend_tunisiahub.Entities.Restaurant;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class CuisineConverter implements AttributeConverter<Cuisine, String> {

    @Override
    public String convertToDatabaseColumn(Cuisine attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public Cuisine convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String raw = dbData.trim();

        // Backward compatibility for ordinal values that may already exist in DB.
        if (raw.matches("\\d+")) {
            int ordinal = Integer.parseInt(raw);
            Cuisine[] values = Cuisine.values();
            return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : null;
        }

        String normalized = raw.toUpperCase().replace('-', '_').replace(' ', '_');
        try {
            return Cuisine.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            // Keep reads resilient if legacy/free-text cuisine values exist.
            return null;
        }
    }
}
