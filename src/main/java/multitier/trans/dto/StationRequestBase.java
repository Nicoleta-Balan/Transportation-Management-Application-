package multitier.trans.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import multitier.trans.model.enums.StationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data  // Lombok: Generates getters, setters, toString, equals, hashCode for standard fields
public abstract class StationRequestBase {

    @Schema(description = "Optional station description", example = "Main transportation hub", maxLength = 255)
    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;

    @Schema(description = "Station address (must be unique)", example = "123 Main Street, City", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 500)
    @NotNull(message = "Address cannot be null")
    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address;

    @Schema(description = "Latitude coordinate (-90 to 90)", example = "51.505", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "-90", maximum = "90")
    @NotNull(message = "Latitude cannot be null")
    private Double latitude;

    @Schema(description = "Longitude coordinate (-180 to 180)", example = "-0.09", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "-180", maximum = "180")
    @NotNull(message = "Longitude cannot be null")
    private Double longitude;

    @Schema(description = "Station status", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"ACTIVE", "INACTIVE", "MAINTENANCE"})
    @NotNull(message = "Status cannot be null")
    private StationStatus status;
}

