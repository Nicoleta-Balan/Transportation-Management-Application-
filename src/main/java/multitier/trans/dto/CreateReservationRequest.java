package multitier.trans.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import multitier.trans.model.enums.PassengerCategory;
import multitier.trans.model.enums.VehicleClass;
import lombok.Data;
import java.time.LocalDateTime;

@Data  // Lombok: Generates getters, setters, toString, equals, hashCode
public class CreateReservationRequest {

    @NotNull(message = "Route ID cannot be null")
    private Long routeId;

    @NotBlank(message = "Passenger name cannot be blank")
    private String passengerName;

    @Min(value = 1, message = "Must book at least 1 seat")
    private int seatCount;

    @NotNull(message = "Departure time cannot be null")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time cannot be null")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Passenger category must be specified")
    private PassengerCategory passengerCategory;

    @NotNull(message = "Vehicle class must be specified")
    private VehicleClass vehicleClass;
}