package multitier.trans.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import multitier.trans.model.enums.StationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to create a new station")
public class CreateStationRequest {

    @Schema(description = "Station name (must be unique)", example = "Central Station", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 100)
    @NotNull(message = "Station name cannot be null")
    @Size(min = 2, max = 100, message = "Station name must be between 2 and 100 characters")
    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public StationStatus getStatus() {
        return status;
    }

    public void setStatus(StationStatus status) {
        this.status = status;
    }
}

