package multitier.trans.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import multitier.trans.model.enums.PolicyStatus;

/**
 * Converts PolicyStatus enum to/from String for database storage.
 * Uses @Convert annotation on entity fields to apply this conversion automatically.
 */
@Converter(autoApply = false)
public class PolicyStatusConverter implements AttributeConverter<PolicyStatus, String> {

    @Override
    public String convertToDatabaseColumn(PolicyStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    @Override
    public PolicyStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return PolicyStatus.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // Handle legacy data or invalid values gracefully
            // Could log a warning here
            return null;
        }
    }
}

