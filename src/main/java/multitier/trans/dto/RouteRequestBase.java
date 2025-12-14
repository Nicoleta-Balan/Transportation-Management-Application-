package multitier.trans.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import multitier.trans.model.enums.VehicleClass;
import lombok.Data;

import java.util.List;

@Data  // Lombok: Generates getters, setters, toString, equals, hashCode for standard fields
public abstract class RouteRequestBase {

    // New: List of route stops (ordered stations)
    @NotNull(message = "Route stops cannot be null")
    @Size(min = 2, message = "Route must have at least 2 stations")
    @Valid
    private List<RouteStopRequest> stops;

    @NotNull(message = "Vehicle class must be specified")
    private VehicleClass vehicleClass;

    @NotNull(message = "Distance cannot be null")
    @Min(value = 1, message = "Distance must be at least 1 km")
    @Max(value = 999, message = "Distance cannot exceed 999 km")
    private Double distance;

    @NotNull(message = "Duration cannot be null")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 2880, message = "Duration cannot exceed 48 hours (2880 minutes)")
    private Integer durationMinutes;

    @Size(max = 50, message = "Description must not exceed 50 characters")
    private String description;

    // Vehicle capacity is taken from the enum (custom getter - Lombok won't override existing methods)
    public int getVehicleCapacity() {
        return vehicleClass != null ? vehicleClass.getSeatCapacity() : 0;
    }
}

