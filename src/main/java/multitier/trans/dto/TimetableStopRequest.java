package multitier.trans.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data  // Lombok: Generates getters, setters, toString, equals, hashCode
public class TimetableStopRequest {

    @NotNull(message = "Station ID cannot be null")
    private Long stationId;

    @NotNull(message = "Sequence order cannot be null")
    private Integer sequenceOrder;

    @NotNull(message = "Arrival time cannot be null")
    private LocalDateTime arrivalTime;

    private LocalDateTime departureTime; // Nullable for end station
}

