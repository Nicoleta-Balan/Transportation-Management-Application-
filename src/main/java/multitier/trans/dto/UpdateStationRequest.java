package multitier.trans.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "Request to update an existing station. Only description, address, location, and status can be changed.")
@Data  // Lombok: Generates getters, setters, toString, equals, hashCode
@EqualsAndHashCode(callSuper = false)  // DTOs compare by field values, not superclass
public class UpdateStationRequest extends StationRequestBase {
    // All fields inherited from StationRequestBase
}
