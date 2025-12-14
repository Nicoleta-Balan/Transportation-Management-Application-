package multitier.trans.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "Request to create a new station")
@Data  // Lombok: Generates getters, setters, toString, equals, hashCode
@EqualsAndHashCode(callSuper = false)  // DTOs compare by field values, not superclass
public class CreateStationRequest extends StationRequestBase {

    @Schema(description = "Station name (must be unique)", example = "Central Station", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 100)
    @NotNull(message = "Station name cannot be null")
    @Size(min = 2, max = 100, message = "Station name must be between 2 and 100 characters")
    private String name;
}

