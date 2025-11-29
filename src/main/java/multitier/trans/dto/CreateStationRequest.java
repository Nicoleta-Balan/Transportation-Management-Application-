package multitier.trans.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import multitier.trans.model.enums.StationStatus;

public class CreateStationRequest {

    @NotNull(message = "Station name cannot be null")
    @Size(min = 2, max = 100, message = "Station name must be between 2 and 100 characters")
    private String name;

    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;

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

    public StationStatus getStatus() {
        return status;
    }

    public void setStatus(StationStatus status) {
        this.status = status;
    }
}

