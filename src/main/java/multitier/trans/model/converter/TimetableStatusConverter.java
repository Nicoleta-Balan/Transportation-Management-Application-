package multitier.trans.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import multitier.trans.model.enums.TimetableStatus;

/**
 * Converts TimetableStatus enum to/from String for database storage.
 * Uses @Convert annotation on entity fields to apply this conversion automatically.
 */
@Converter(autoApply = false)
public class TimetableStatusConverter implements AttributeConverter<TimetableStatus, String> {

    @Override
    public String convertToDatabaseColumn(TimetableStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    @Override
    public TimetableStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return TimetableStatus.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // Handle legacy data or invalid values gracefully
            // Could log a warning here
            return null;
        }
    }
}

