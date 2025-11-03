package multitier.trans.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO (Data Transfer Object) for capturing the "create route" request from the frontend.
 */

public class CreateRouteRequest {

    @NotNull(message = "Origin station ID cannot be null")
    private Long originStationId;

    @NotNull(message = "Destination station ID cannot be null")
    private Long destinationStationId;

    @Min(value = 1, message = "Vehicle capacity must be at least 1")
    private int vehicleCapacity;

    // --- Getters and Setters (Essential for JSON mapping) ---

    public Long getOriginStationId() {
        return originStationId;
    }

    public void setOriginStationId(Long originStationId) {
        this.originStationId = originStationId;
    }

    public Long getDestinationStationId() {
        return destinationStationId;
    }

    public void setDestinationStationId(Long destinationStationId) {
        this.destinationStationId = destinationStationId;
    }

    public int getVehicleCapacity() {
        return vehicleCapacity;
    }

    public void setVehicleCapacity(int vehicleCapacity) {
        this.vehicleCapacity = vehicleCapacity;
    }
}