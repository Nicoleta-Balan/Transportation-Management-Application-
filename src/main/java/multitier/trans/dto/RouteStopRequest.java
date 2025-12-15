package multitier.trans.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data  // Lombok: Generates getters, setters, toString, equals, hashCode
public class RouteStopRequest {

    @NotNull(message = "Station ID cannot be null")
    private Long stationId;

    @NotNull(message = "Sequence order cannot be null")
    @Min(value = 0, message = "Sequence order must be >= 0")
    private Integer sequenceOrder;

    @NotNull(message = "Distance from previous cannot be null")
    @Min(value = 0, message = "Distance from previous must be >= 0")
    private Double distanceFromPrevious; // km from previous stop

    @NotNull(message = "Duration from previous cannot be null")
    @Min(value = 0, message = "Duration from previous must be >= 0")
    private Integer durationMinutesFromPrevious; // minutes from previous stop
}

