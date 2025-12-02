package multitier.trans.dto;

import jakarta.validation.constraints.NotNull;
import multitier.trans.model.enums.VehicleClass;

// Map "create route" request from the frontend to an object

public class CreateRouteRequest {

    @NotNull(message = "Origin station ID cannot be null")
    private Long originStationId;

    @NotNull(message = "Destination station ID cannot be null")
    private Long destinationStationId;

    @NotNull(message = "Vehicle class must be specified")
    private VehicleClass vehicleClass;

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

    public VehicleClass getVehicleClass() {
        return vehicleClass;
    }

    public void setVehicleClass(VehicleClass vehicleClass) {
        this.vehicleClass = vehicleClass;
    }

    // Vehicle capacity is taken from the enum
    public int getVehicleCapacity() {
        return vehicleClass != null ? vehicleClass.getSeatCapacity() : 0;
        // if vehicle class is not set defaults to 0 to avoid NullPointerException
    }
}