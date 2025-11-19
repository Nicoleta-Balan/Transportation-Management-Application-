package multitier.trans.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import multitier.trans.model.enums.StationStatus;

/**
 * Converts StationStatus enum to/from String for database storage.
 * Uses @Convert annotation on entity fields to apply this conversion automatically.
 */
@Converter(autoApply = false)
public class StationStatusConverter implements AttributeConverter<StationStatus, String> {

    @Override
    public String convertToDatabaseColumn(StationStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    @Override
    public StationStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return StationStatus.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // Handle legacy data or invalid values gracefully
            // Could log a warning here
            return null;
        }
    }
}

